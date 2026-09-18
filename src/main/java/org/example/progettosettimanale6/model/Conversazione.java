package org.example.progettosettimanale6.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "conversazioni",
        uniqueConstraints = @UniqueConstraint(columnNames = {"uno_id", "altro_id"}))
public class Conversazione {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uno_id")
    private Utente uno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "altro_id")
    private Utente altro;

    protected Conversazione() {
    }

    private Conversazione(Utente uno, Utente altro) {
        this.uno = uno;
        this.altro = altro;
    }

    public static Conversazione fra(Utente a, Utente b) {
        return a.getUsername().compareTo(b.getUsername()) <= 0
                ? new Conversazione(a, b) : new Conversazione(b, a);
    }

    public static Utente primo(Utente a, Utente b) {
        return a.getUsername().compareTo(b.getUsername()) <= 0 ? a : b;
    }

    public static Utente secondo(Utente a, Utente b) {
        return a.getUsername().compareTo(b.getUsername()) <= 0 ? b : a;
    }

    public Utente controparteDi(String username) {
        return uno.getUsername().equals(username) ? altro : uno;
    }

    public boolean partecipa(String username) {
        return uno.getUsername().equals(username) || altro.getUsername().equals(username);
    }

    public UUID getId() { return id; }
    public Utente getUno() { return uno; }
    public Utente getAltro() { return altro; }
}
