package org.example.progettosettimanale6.service;

import org.example.progettosettimanale6.model.Utente;
import org.example.progettosettimanale6.repository.UtenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UtenteService {

    private final UtenteRepository repository;

    public UtenteService(UtenteRepository repository) {
        this.repository = repository;
    }

    public Utente save(Utente utente) {
        return repository.save(utente);
    }

    public List<Utente> findAll() {
        return repository.findAll();
    }

    public Optional<Utente> findById(UUID id) {
        return repository.findById(id);
    }

    public Optional<Utente> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public List<String> cerca(String q, String escludiUsername) {
        return repository.findTop10ByUsernameContainingIgnoreCaseAndUsernameNot(q, escludiUsername)
                .stream().map(Utente::getUsername).toList();
    }
}
