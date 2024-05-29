package com.app.chatapp.auth;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ChatServer implements Runnable{
    private ArrayList<ConnectionHandler> activeUserHandlers;
    private ArrayList<Future<Integer>> activeUserThreads;
    private ArrayList<String> activeUsersName;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private boolean done;
    private final int port;
    private final DataBase dataBase;
    private final Logger logger;

    public ChatServer() throws Exception {
        this.activeUserHandlers = new ArrayList<>();
        this.activeUserThreads = new ArrayList<>();
        this.activeUsersName = new ArrayList<>();
        this.done = false;
        this.port = 9999;
        this.dataBase = new DataBase();
        this.logger = new Logger("Server");
    }

    public ChatServer(int port) throws Exception {
        this.activeUserHandlers = new ArrayList<>();
        this.activeUserThreads = new ArrayList<>();
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

            while (!done){
                if (serverSocket.isClosed()){
                    break;
                }

                try{
                    Socket client = serverSocket.accept();
                    ConnectionHandler handler = new ConnectionHandler(client);
                    activeUserHandlers.add(handler);
                    Future<Integer> future = pool.submit(handler);
                    activeUserThreads.add(future);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            throw  new RuntimeException(e);
        }

    }


    private void sendBroadcast(String nickname, String message){
        for(ConnectionHandler handler: activeUserHandlers){
            if(!handler.nickname.equals(nickname)){
                handler.sendMessage(message);
             }
        }
    }

    class ConnectionHandler implements Callable<Integer>{
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        private boolean logged;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public Integer call(){
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while(!logged) {
                    String loginOrRegister = in.readLine();
                    String userData = in.readLine().strip();
                    String []userDataArray = userData.split(" ");
                    if(userDataArray.length < 2){
                        sendMessage("Error: 400");
                    }
                    nickname = userDataArray[0];
                    logger.echo("User: " + nickname + " connected to server");
                    String password = userDataArray[userDataArray.length-1];


                    switch (loginOrRegister){
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
                            break;
                    }

                    String message;
                    while((message = in.readLine()) != null){
                        if(message.equals("QUIT")){
                            shutdown();
                            return 0;
                        }else {
                            sendBroadcast(nickname + ": " + message, nickname);
                        };
                    }

                }
            }catch (Exception e){
                e.printStackTrace();

            }
            return null;
        }

        private void sendMessage(String message){out.println(message);}

        private void sendUpdateActiveUsers(){
            String activeClientUpdate = "ACTIVE: " + String.join(",", activeUsersName);
            sendBroadcast(activeClientUpdate, nickname);
        }

        private String receiveUserPicture(){
            try {
                String fileName = nickname + ".jpg";
                int fileSize = Integer.parseInt(in.readLine());
                File file = new File(fileName);
                //file.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                int totalBytesRead = 0;

                while (totalBytesRead < fileSize){
                    bytesRead = client.getInputStream().read(buffer, 0, Math.min(buffer.length, fileSize-totalBytesRead));
                    totalBytesRead += bytesRead;
                    fos.write(buffer, 0 , bytesRead);
                }
                fos.close();
                logger.echo("User: " + nickname + " image receive");
                return fileName;
            }catch (IOException e){
                logger.err("Error occurred while receiving file", e.getMessage());
                return  "";
            }
        }

        private boolean login(String password){
            if(dataBase.doesUsernameExist(nickname)) {
                if(dataBase.getUserPassword(nickname).equals(password)){
                    logger.echo("User: " + nickname + " successfully logged in");
                    sendMessage("OK: 200");
                    activeUsersName.add(nickname);
                    return true;
                } else{
                    logger.err("Error occurred", "Wrong password by user: " + nickname);
                    sendMessage("Error: 401");
                }
            }else{
                sendMessage("Error: 404");
            }
            return false;
        }

        private boolean register(String password, String path){
            if(!dataBase.doesUsernameExist(nickname)){
                if(path.isEmpty()){
                    if(dataBase.insertNewUser(nickname, password)){
                        logger.echo("User: " + nickname + " successfully registered");
                        sendMessage("OK: 201");
                        activeUsersName.add(nickname);
                        return true;
                    }else{
                        sendMessage("Error: 400");
                        return false;
                    }
                }else{
                    if(dataBase.insertNewUser(nickname, password, path)){
                        logger.echo("User: " + nickname + " successfully registered");
                        sendMessage("OK: 201");
                        activeUsersName.add(nickname);
                        return true;
                    }else{
                        sendMessage("Error: 400");
                        return false;
                    }
                }
            }
            sendMessage("Error: 404");
            return false;
        }

        private void shutdown(){
            try {
                activeUsersName.remove(nickname);
                logger.echo("User: " + nickname + " logged out");
                sendBroadcast(nickname + " left chat", nickname);
                sendUpdateActiveUsers();

                activeUserHandlers.remove(this);

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


    public static void main(String[] args) {
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
