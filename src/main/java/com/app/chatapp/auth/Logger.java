package com.app.chatapp.auth;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private String server;
    private BufferedWriter bufferedWriter;

    public Logger(String server){
        try {
            this.server = server;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[yyyy-MM-dd]");
            bufferedWriter = new BufferedWriter(new FileWriter( server + simpleDateFormat.format(new Date()) + ".txt"));
        }catch (IOException e){
            System.err.println("[Logger]" + getTimestamp() + " " + e.getMessage());
        }
    }

    private String getTimestamp(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd][HH:mm:ss.SSS]");
        return dateFormat.format(new Date());
    }

    public void echo(String message){
        try {
            System.out.println("[" + server + "]" + getTimestamp() + " " + message);
            bufferedWriter.write("[" + server + "]"  + getTimestamp() + " " + message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            System.err.println("[Logger]" + getTimestamp() + " " + e.getMessage());
        }
    }

    public void err(String message, String err){
        try {
            System.err.println("[" + server + "]"  + getTimestamp() + " " + message + ": " + err);
            bufferedWriter.write("[" + server + "]"  + getTimestamp() + " " + message + ": " + err);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e){
            System.err.println("[Logger]" + getTimestamp() + " " + e.getMessage());
        }
    }
}
