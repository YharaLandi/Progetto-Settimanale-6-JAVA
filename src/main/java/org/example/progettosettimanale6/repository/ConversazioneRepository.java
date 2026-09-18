package org.example.progettosettimanale6.repository;

import org.example.progettosettimanale6.model.Conversazione;
import org.example.progettosettimanale6.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversazioneRepository extends JpaRepository<Conversazione, UUID> {

    Optional<Conversazione> findByUnoAndAltro(Utente uno, Utente altro);

    @Query("select c from Conversazione c where c.uno.username = :username or c.altro.username = :username")
    List<Conversazione> diUtente(String username);

    @Query("select count(c) from Conversazione c where c.uno.username = :username or c.altro.username = :username")
    long contaConversazioni(String username);
}
