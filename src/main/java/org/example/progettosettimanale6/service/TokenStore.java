package org.example.progettosettimanale6.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenStore {

    private static final SecureRandom RNG = new SecureRandom();

    private final Map<String, String> utentePerToken = new ConcurrentHashMap<>();

    public String emetti(String utente) {
        byte[] grezzo = new byte[24];
        RNG.nextBytes(grezzo);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(grezzo);
        utentePerToken.put(token, utente);
        return token;
    }

    public Optional<String> utenteDi(String token) {
        return Optional.ofNullable(utentePerToken.get(token));
    }

    public void revoca(String token) {
        utentePerToken.remove(token);
    }
}
