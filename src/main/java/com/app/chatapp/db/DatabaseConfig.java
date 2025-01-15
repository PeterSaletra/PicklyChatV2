package com.app.chatapp.db;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseConfig {
    private String jdbcUrl;
    private String username;
    private String password;

    public static DatabaseConfig createDefault() {
        return DatabaseConfig.builder()
                .jdbcUrl("jdbc:h2:mem:picklyDB")
                .username("sa")
                .password("sa")
                .build();
    }
}