package org.example.progettosettimanale6.dto;

public record NuovaChatRequest(String usernameDestinatario) {

    public NuovaChatRequest {
        if (usernameDestinatario == null || usernameDestinatario.isBlank()) {
            throw new IllegalArgumentException("usernameDestinatario: obbligatorio");
        }
    }
}
