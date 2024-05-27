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
                    String userData = in.readLine();
                    nickname = userData.split(" ")[0];
                    String password = userData.split(" ")[1];


                    switch (loginOrRegister){
                        case "LOGIN":
                            logged = login(password);
                            break;
                        case "REG/F":
                            String path = reciveUserPicture();
                            logged = register(password, path);
                        case "REG/N":
                            logged = register(password, "");
                        default:
                            sendMessage("Nieznany bład");
                    }
                    System.out.println(logged);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        private void sendMessage(String message){out.println(message);}

        private String reciveUserPicture(){
            String path = "userPictuers/" + nickname + ".jpg";
            try(ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            FileOutputStream outputStream = new FileOutputStream(path)){
                int nRead;
                byte[] data = new byte[1024];
                while((nRead = client.getInputStream().read(data, 0, data.length)) != -1){
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] imageData = buffer.toByteArray();

                outputStream.write(imageData);
                return path;
            }catch (IOException e){
                System.err.println("Problem w pobieraniu zdjęcia: " + e.getMessage());
                return null;
            }
        }

        private boolean login(String password){
            if(dataBase.doesUsernameExist(nickname)) {
                if(dataBase.getUserPassword(nickname).equals(password)){
                    sendMessage("OK");
                    return true;
                } else{
                    sendMessage("Nieprawidłowe hasło!");
                }
            }else{
                sendMessage("Użytkownik nie istnieje!");
            }
            return false;
        }

        private boolean register(String password, String path){
            if(dataBase.doesUsernameExist(nickname)){
                if(path.isEmpty()){
                    if(dataBase.insertNewUser(nickname, password)){
                        sendMessage("Udało sie utworzyć użytkownika");
                        return true;
                    }else{
                        sendMessage("Bład podczas tworzenia użytkownika");
                        return false;
                    }
                }else{
                    if(dataBase.insertNewUser(nickname, password)){
                        sendMessage("Udało sie utworzyć użytkownika");
                        return true;
                    }else{
                        sendMessage("Bład podczas tworzenia użytkownika");
                        return false;
                    }
                }
            }
            sendMessage("Użytkownik o nicku: " + nickname + " już istnieje");
            return false;
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
