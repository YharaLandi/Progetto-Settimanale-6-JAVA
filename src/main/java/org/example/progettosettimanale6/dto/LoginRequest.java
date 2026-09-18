package org.example.progettosettimanale6.dto;

public record LoginRequest(String username, String password) {

    public LoginRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username: non puo' essere vuoto");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password: non puo' essere vuota");
        }
    }
}
