package org.example.progettosettimanale6.dto;

import java.util.List;
import java.util.UUID;

public record Aggiornamento(String tipo, UUID conversazione, String utente, List<UUID> messaggi) {

    public static Aggiornamento letti(UUID conversazione, String daChi, List<UUID> messaggi) {
        return new Aggiornamento("LETTI", conversazione, daChi, messaggi);
    }

    public static Aggiornamento scrive(UUID conversazione, String chi) {
        return new Aggiornamento("SCRIVE", conversazione, chi, List.of());
    }

    public static Aggiornamento presenza(String utente) {
        return new Aggiornamento("PRESENZA", null, utente, List.of());
    }
}
