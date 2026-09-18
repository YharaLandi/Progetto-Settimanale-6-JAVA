package org.example.progettosettimanale6.repository;

import org.example.progettosettimanale6.model.Messaggio;
import org.example.progettosettimanale6.model.StatoMessaggio;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface MessaggioRepository extends JpaRepository<Messaggio, UUID> {

    @Query("select m from Messaggio m where m.conversazione.id = :conversazione order by m.istante desc")
    List<Messaggio> ultimi(UUID conversazione, Limit limite);

    @Query("select count(m) from Messaggio m where m.conversazione.id = :conversazione and m.destinatario.username = :utente and m.stato <> 'LETTO'")
    long nonLetti(UUID conversazione, String utente);

    @Query("select m.id from Messaggio m where m.conversazione.id = :conversazione and m.destinatario.username = :utente and m.stato <> 'LETTO'")
    List<UUID> idNonLetti(UUID conversazione, String utente);

    @Modifying
    @Query("update Messaggio m set m.stato = :stato where m.conversazione.id = :conversazione and m.destinatario.username = :utente and m.stato <> 'LETTO'")
    void segnaLetti(UUID conversazione, String utente, StatoMessaggio stato);

    @Modifying
    @Query("update Messaggio m set m.stato = 'CONSEGNATO', m.deliveredAt = :ora where m.conversazione.id = :conversazione and m.destinatario.username = :utente and m.stato = 'SPEDITO'")
    void segnaConsegnati(UUID conversazione, String utente, Instant ora);

    @Query("select count(m) from Messaggio m where m.mittente.username = :utente")
    long contaInviati(String utente);

    @Query("select count(m) from Messaggio m where m.destinatario.username = :utente")
    long contaRicevuti(String utente);
}
