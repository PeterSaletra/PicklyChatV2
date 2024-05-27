package com.app.chatapp.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private int port;

    public ChatServer() {
        this.activeUserThreads = new ArrayList<>();
        this.activeUsersName = new ArrayList<>();
        this.done = false;
        this.port = 9999;
    }

    public ChatServer(int port){
        this.activeUserThreads = new ArrayList<>();
        this.activeUsersName = new ArrayList<>();
        this.done = false;
        this.port = port;
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
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;
        private boolean logged;

        public ConnetionHandler(Socket client){
            this.client = client;
        }

        @Override
        public Integer call() throws Exception {
            try{
                out = new PrintWriter(client.getOutputStream(), true);;
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String loginOrRegister = in.readLine();
                String userData = in.readLine();
                nickname = userData.split(" ")[0];
                String password = userData.split(" ")[1];
                if(loginOrRegister.equals("LOGN")){
                    logged = login(password);
                }else if(loginOrRegister.equals("REG")){
                    String path = userData.split(" ")[2];
                     logged = register(password, path);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        private void sendMessage(String message){out.println(message);}

        private boolean login(String password){
            return false;
        }

        private boolean register(String password, String path){
            return false;
        }
    }
}
