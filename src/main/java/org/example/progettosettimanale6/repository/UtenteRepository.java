package org.example.progettosettimanale6.repository;

import org.example.progettosettimanale6.model.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UtenteRepository extends JpaRepository<Utente, UUID> {
    Optional<Utente> findByUsername(String username);
    List<Utente> findTop10ByUsernameContainingIgnoreCaseAndUsernameNot(String q, String escludi);
}
