package com.app.chatapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.ObservableMap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatusStore {
    private static final String STATUS_FILE = "user_statuses.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    public static void saveStatus(String username, String status) {
        try {
            Map<String, String> statuses = loadStatuses();
            statuses.put(username, status);
            mapper.writeValue(new File(STATUS_FILE), statuses);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Map<String, String> loadStatuses() {
        try {
            File file = new File(STATUS_FILE);
            if (!file.exists()) {
                return new HashMap<>();
            }
            return mapper.readValue(file, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    public static void startPolling(ObservableMap<String, String> userStatuses) {
        scheduler.scheduleAtFixedRate(() -> {
            Map<String, String> currentStatuses = loadStatuses();
            Platform.runLater(() -> {
                for (Map.Entry<String, String> entry : currentStatuses.entrySet()) {
                    if (!Objects.equals(userStatuses.get(entry.getKey()), entry.getValue())) {
                        userStatuses.put(entry.getKey(), entry.getValue());
                    }
                }
            });
        }, 0, 1, TimeUnit.SECONDS);
    }
}