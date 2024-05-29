package com.app.chatapp.auth;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ChatServer implements Runnable{
    private ArrayList<ConnetionHandler> activeUserHandlers;
    private ArrayList<Future<Integer>> activeUserThreads;
    private ArrayList<String> activeUsersName;
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private boolean done;
    private final int port;
    private final DataBase dataBase;

    public ChatServer() throws Exception {
        this.activeUserHandlers = new ArrayList<>();
        this.activeUserThreads = new ArrayList<>();
        this.activeUsersName = new ArrayList<>();
        this.done = false;
        this.port = 9999;
        this.dataBase = new DataBase();
    }

    public ChatServer(int port) throws Exception {
        this.activeUserHandlers = new ArrayList<>();
        this.activeUserThreads = new ArrayList<>();
        this.activeUsersName = new ArrayList<>();
        this.done = false;
        this.port = port;
        this.dataBase = new DataBase();
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            pool = Executors.newCachedThreadPool();

            System.out.println("Server started ....");

            while (!done){
                if (serverSocket.isClosed()){
                    break;
                }

                try{
                    Socket client = serverSocket.accept();
                    ConnetionHandler handler = new ConnetionHandler(client);
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


    private void sendBrodcast(String nickname, String message){
        for(ConnetionHandler handler: activeUserHandlers){
            if(!handler.nickname.equals(nickname)){
                handler.sendMessage(message);
             }
        }
    }

    class ConnetionHandler implements Callable<Integer>{
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        private boolean logged;

        public ConnetionHandler(Socket client){
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
                    String password = userDataArray[userDataArray.length-1];


                    switch (loginOrRegister){
                        case "LOGIN":
                            logged = login(password);
                            break;
                        case "REG/F":
                            String path = reciveUserPicture();
                            logged = register(password, path);
                            break;
                        case "REG/N":
                            logged = register(password, "");
                            break;
                        default:
                            sendMessage("Error: 404 Unknown Command");
                            break;
                    }

                    String message;
                    while((message = in.readLine()) != null){
                        if(message.equals("QUIT")){
                            shutdown();
                            return 0;
                        }else {
                            sendBrodcast(nickname + ": " + message, nickname);
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
            sendBrodcast(activeClientUpdate, nickname);
        }

        private String reciveUserPicture(){
            String path = nickname + ".jpg";
            try(ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            FileOutputStream outputStream = new FileOutputStream(path)){
                int nRead;
                byte[] data = new byte[1024];
                while((nRead = client.getInputStream().read(data)) != -1){
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] imageData = buffer.toByteArray();

                outputStream.write(imageData);
                System.out.println("Dodano zdjęcie użytkownika: " + nickname);
                return path;
            }catch (IOException e){
                System.err.println("Problem w pobieraniu zdjęcia: " + e.getMessage());
                sendMessage("Error: 500");
                return null;
            }
        }

        private boolean login(String password){
            if(dataBase.doesUsernameExist(nickname)) {
                if(dataBase.getUserPassword(nickname).equals(password)){
                    sendMessage("OK: 200");
                    activeUsersName.add(nickname);
                    return true;
                } else{
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
                        sendMessage("OK: 201");
                        activeUsersName.add(nickname);
                        return true;
                    }else{
                        sendMessage("Error: 400");
                        return false;
                    }
                }else{
                    if(dataBase.insertNewUser(nickname, password, path)){
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
                sendBrodcast(nickname + " left chat", nickname);
                sendUpdateActiveUsers();

                activeUserHandlers.remove(this);

                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }

            } catch (IOException e) {
                // ignore
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
