package org.example.progettosettimanale6.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messaggi",
        indexes = {
                @Index(name = "ix_msg_conv_istante", columnList = "conversazione_id, istante"),
                @Index(name = "ix_msg_conv_dest_stato", columnList = "conversazione_id, destinatario_id, stato")
        })
public class Messaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversazione_id")
    private Conversazione conversazione;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mittente_id")
    private Utente mittente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id")
    private Utente destinatario;

    @Column(nullable = false, length = 2000)
    private String testo;

    @Column(nullable = false)
    private Instant istante;

    @Column
    private Instant deliveredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatoMessaggio stato;

    protected Messaggio() {
    }

    public Messaggio(Conversazione conversazione, Utente mittente, Utente destinatario, String testo) {
        this.conversazione = conversazione;
        this.mittente = mittente;
        this.destinatario = destinatario;
        this.testo = testo;
        this.istante = Instant.now();
        this.stato = StatoMessaggio.SPEDITO;
    }

    public void consegnato() {
        if (this.stato == StatoMessaggio.SPEDITO) {
            this.stato = StatoMessaggio.CONSEGNATO;
            this.deliveredAt = Instant.now();
        }
    }

    public void letto() {
        this.stato = StatoMessaggio.LETTO;
    }

    public UUID getId() { return id; }
    public Conversazione getConversazione() { return conversazione; }
    public Utente getMittente() { return mittente; }
    public Utente getDestinatario() { return destinatario; }
    public String getTesto() { return testo; }
    public Instant getIstante() { return istante; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public StatoMessaggio getStato() { return stato; }
}
