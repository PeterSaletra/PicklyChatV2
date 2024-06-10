package com.app.chatapp.auth;

import javax.crypto.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

public class ChatServer implements Runnable {
    private final ArrayList<ConnectionHandler> activeUserHandlers;
    private final ArrayList<String> activeUsersName;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private boolean done;
    private SecretKey sessionKey = null;
    private final int port;
    private final DataBase dataBase;
    private final Logger logger;

    public ChatServer() throws Exception {
        this(9999);
    }

    public ChatServer(int port) throws Exception {
        this.activeUserHandlers = new ArrayList<>();
        this.activeUsersName = new ArrayList<>();
        this.done = false;
        this.port = port;
        this.dataBase = new DataBase();
        this.logger = new Logger("Server");
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            pool = Executors.newCachedThreadPool();

            logger.echo("Server started");

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            sessionKey = keyGen.generateKey();

            while (!done) {
                if (serverSocket.isClosed()) {
                    break;
                }

                try {
                    Socket client = serverSocket.accept();
                    ConnectionHandler handler = new ConnectionHandler(client);

                    activeUserHandlers.add(handler);
                    FutureTask<Integer> future = new FutureTask<>(handler){
                        @Override
                        protected void done() {
                            try {
                                int i = get();

                                if(i==0){
                                    logger.echo("Handler stopped working successfully");
                                }else if(i == -1){
                                    logger.err("Handler had some problem", "Login or register flag was null");
                                }else if(i == -2){
                                    logger.err("Handler had some problem", "User data was null");
                                }else if(i == -3){
                                    logger.err("Handler had some problem", "User data was to long or wrong");
                                }else if(i == -4){
                                    logger.err("Handler had some problem", "Unknown login or register flag");
                                }else if(i == 5){
                                    logger.err("Handler had some problem", "Message was null");
                                }else if(i == -6){
                                    logger.err("Handler had some problem", "Error with output/input stream");
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    pool.submit(future);
                } catch (Exception e) {
                    logger.err("Error while running server", e.getMessage());
                    done = false;
                }
            }
        } catch (IOException e) {
            logger.err("Unknown error occurred: ", e.getMessage());
            try {
                serverSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            dataBase.shutdown();
        } catch (NoSuchAlgorithmException e) {
            logger.err("Error with generating key: ", e.getMessage());
            try {
                serverSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            dataBase.shutdown();
        }
    }

        class ConnectionHandler implements Callable<Integer> {
            private final Socket client;
            private BufferedReader in;
            private PrintWriter out;
            private String nickname;
            private boolean logged;
            private Cipher encryptAES;
            private Cipher encryptRSA;
            private Cipher decryptAES;

            public ConnectionHandler(Socket client) {
                this.client = client;
                try {
                    this.encryptAES = Cipher.getInstance("AES");
                    this.decryptAES = Cipher.getInstance("AES");
                    this.encryptRSA = Cipher.getInstance("RSA");

                    encryptAES.init(Cipher.ENCRYPT_MODE, sessionKey);
                    decryptAES.init(Cipher.DECRYPT_MODE, sessionKey);
                } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                    logger.err("Error occured: ", e.getMessage());
                }
            }

            @Override
            public Integer call() {
                try {
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                    String userPublicKey = in.readLine();
                    logger.echo("Recived public key");
                    sendSessionKey(userPublicKey);

                    while (!logged) {
                        String loginOrRegisterEnc = in.readLine();
                        if (loginOrRegisterEnc == null) {
                            sendMessage("Error: 400 Null or empty login or register");
                            shutdown();
                            return -1;
                        }

                        String userDataEnc = in.readLine();
                        if (userDataEnc == null) {
                            sendMessage("Error: 400 Null or empty user data");
                            shutdown();
                            return -2;
                        }

                        String loginOrRegister = decryptMessage(loginOrRegisterEnc);
                        String userData = decryptMessage(userDataEnc.strip());
                        String[] userDataArray = userData.split(" ");
                        if (userDataArray.length < 2) {
                            sendMessage("Error: 400");
                            shutdown();
                            return -3;
                        }
                        nickname = userDataArray[0];
                        logger.echo("User: " + nickname + " connected to server");
                        String password = userDataArray[userDataArray.length - 1];


                        switch (loginOrRegister) {
                            case "LOGIN":
                                logged = login(password);
                                break;
                            case "REG/F":
                                String path = receiveUserPicture();
                                logged = register(password, path);
                                break;
                            case "REG/N":
                                logged = register(password, "");
                                break;
                            default:
                                logger.err("User: " + nickname + " sent unknown command", "Error: 404 Unknown Command");
                                sendMessage("Error: 404 Unknown Command");
                                shutdown();
                                return -4;
                        }
                    }

                    sendUpdateActiveUsers();
                    sendBroadcast("USR", nickname, nickname);

                    String message;
                    while ((message = in.readLine()) != null) {
                        message = decryptMessage(message);
                        if (message.equals("QUIT")) {
                            shutdown();
                            return 0;
                        } else if (message.startsWith("SEND_FILE")) {
                            String[] parts = message.split(" ", 2);
                            sendFile();
                        } else {
                            List<String> splitedMessage = new ArrayList<>(Arrays.asList(message.split(" ")));
                            String sender = splitedMessage.getFirst();
                            splitedMessage.removeFirst();
                            String reciver = splitedMessage.getFirst();
                            splitedMessage.removeFirst();
                            sendToOtherUser(sender, reciver, String.join(" ", splitedMessage));
                        }
                    }
                    shutdown();
                    return -5;
                } catch (Exception e) {
                    logger.err("Error occured: ", e.getMessage());
                    return -6;
                }

            }

            private String encryptMessage(String message) {
                String newMessage = "";
                try {
                    byte[] messageInBytes = message.getBytes(StandardCharsets.UTF_8);
                    byte[] encryptedBytes = encryptAES.doFinal(messageInBytes);
                    newMessage = Base64.getEncoder().encodeToString(encryptedBytes);
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    logger.err("Error occurred while encrypting message", e.getMessage());
                }
                return newMessage;
            }

            private String decryptMessage(String message) {
                String newMessage = "";
                try {
                    byte[] messageInBytes = Base64.getDecoder().decode(message);
                    byte[] encryptedBytes = decryptAES.doFinal(messageInBytes);
                    newMessage = new String(encryptedBytes, StandardCharsets.UTF_8);
                } catch (IllegalBlockSizeException | BadPaddingException e) {
                    logger.err("Error occurred while encrypting message", e.getMessage());
                }
                return newMessage;
            }

            private void sendSessionKey(String userPublicKey) {
                try {
                    byte[] userPublicKeyBytes = Base64.getDecoder().decode(userPublicKey);
                    X509EncodedKeySpec spec = new X509EncodedKeySpec(userPublicKeyBytes);
                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
                    encryptRSA.init(Cipher.ENCRYPT_MODE, publicKey);
                    byte[] encryptedSessionKey = encryptRSA.doFinal(Base64.getDecoder().decode(Base64.getEncoder().encodeToString(sessionKey.getEncoded())));
                    out.println(Base64.getEncoder().encodeToString(encryptedSessionKey));
                } catch (Exception e) {
                    logger.err("Error occurred while sending session key: ", e.getMessage());
                }
            }

            private void sendMessage(String message) {
                String encryptedMessage = encryptMessage(message);
                out.println(encryptedMessage);
            }

            private void sendUpdateActiveUsers() {
                String activeClientUpdate = "ACTIVE: " + String.join(" ", activeUsersName);
                sendMessage(activeClientUpdate);
            }

            private String receiveUserPicture() {
                try {
                    String fileName = nickname + ".jpg";
                    int fileSize = Integer.parseInt(decryptMessage(in.readLine()));

                    File file = new File(fileName);

                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    int totalBytesRead = 0;

                    sendMessage("BEGIN TRANSFER");

                    while (totalBytesRead < fileSize) {
                        bytesRead = client.getInputStream().read(buffer, 0, Math.min(buffer.length, fileSize - totalBytesRead));
                        totalBytesRead += bytesRead;
                        fos.write(buffer, 0, bytesRead);
                        fos.flush();
                    }

                    fos.close();
                    logger.echo("User: " + nickname + " image receive");
                    return fileName;
                } catch (IOException e) {
                    logger.err("Error occurred while receiving file", e.getMessage());
                    return "";
                }
            }

            private void sendFile() {
                try {

                    File file = new File(nickname + ".jpg");
                    if (!file.exists()){
                        file = new File("avatar.jpg");
                    }


                    sendMessage(String.valueOf(file.length()));
                    if (!file.exists()) {
                        sendMessage("Error: 404 File not found");
                        return;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    sendMessage("FILE " + file.length());

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        byte[] encryptedBuffer = encryptAES.doFinal(buffer, 0, bytesRead);
                        out.println(Base64.getEncoder().encodeToString(encryptedBuffer));
                    }

                    fis.close();
                    logger.echo("Sent file to user: " + nickname);
                } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
                    logger.err("Error occurred while sending file: ", e.getMessage());
                }
            }

            private boolean login(String password) {
                if (!activeUsersName.contains(nickname)) {
                    if (dataBase.doesUsernameExist(nickname)) {
                        if (dataBase.getUserPassword(nickname).equals(password)) {
                            logger.echo("User: " + nickname + " successfully logged in");
                            sendMessage("OK: 200");
                            activeUsersName.add(nickname);
                            return true;
                        } else {
                            logger.err("Error occurred", "Wrong password by user: " + nickname);
                            sendMessage("Error: 401");
                        }
                    } else {
                        sendMessage("Error: 404");
                    }
                } else {
                    logger.err("Error occurred", "User is logged in: " + nickname);
                    sendMessage("Error: 405");
                }
                return false;
            }

            private boolean register(String password, String path) {
                if (!dataBase.doesUsernameExist(nickname)) {
                    if (path.isEmpty()) {
                        if (dataBase.insertNewUser(nickname, password)) {
                            logger.echo("User: " + nickname + " successfully registered");
                            sendMessage("OK: 201");
                            activeUsersName.add(nickname);
                            return true;
                        } else {
                            sendMessage("Error: 400");
                            return false;
                        }
                    } else {
                        if (dataBase.insertNewUser(nickname, password, path)) {
                            logger.echo("User: " + nickname + " successfully registered");
                            sendMessage("OK: 201");
                            activeUsersName.add(nickname);
                            return true;
                        } else {
                            sendMessage("Error: 400");
                            return false;
                        }
                    }
                }
                sendMessage("Error: 404");
                return false;
            }

            private void shutdown() {
                try {
                    activeUsersName.remove(nickname);
                    logger.echo("User: " + nickname + " logged out");

                    activeUserHandlers.remove(this);
                    sendBroadcast("QUIT", nickname, nickname);

                    in.close();
                    out.close();
                    if (!client.isClosed()) {
                        client.close();
                    }

                } catch (IOException e) {
                    logger.err("Error occurred while closing handler", e.getMessage());
                }
            }
        }

        private void sendBroadcast (String prefix, String nickname, String message){
            for (ConnectionHandler handler : activeUserHandlers) {
                if (!handler.nickname.equals(nickname)) {
                    handler.sendMessage(prefix + " " + message);
                }
            }
        }

        private void sendToOtherUser (String sender, String reciver, String message){
            for (ConnectionHandler handler : activeUserHandlers) {
                if (handler.nickname.equals(reciver)) {
                    handler.sendMessage(sender + " " + message);
                    break;
                }
            }
        }

        public static void main (String[] args){
            try {
                ChatServer server = new ChatServer(9999);
                Thread serverThread = new Thread(server);
                serverThread.start();
                //test
            } catch (Exception e) {
                System.err.println("Bład w trakcie uruchamiania servera: " + e.getMessage());
            }
        }
}
