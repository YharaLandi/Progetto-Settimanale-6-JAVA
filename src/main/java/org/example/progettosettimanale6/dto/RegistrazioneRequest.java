package org.example.progettosettimanale6.dto;

public record RegistrazioneRequest(String username, String password, String nomeCompleto, String email) {

    public RegistrazioneRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username: non puo' essere vuoto");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password: non puo' essere vuota");
        }
        if (nomeCompleto == null || nomeCompleto.isBlank()) {
            throw new IllegalArgumentException("nomeCompleto: non puo' essere vuoto");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("email: serve un indirizzo valido");
        }
    }
}
