package org.example.progettosettimanale6.service;

import org.example.progettosettimanale6.dto.LoginRequest;
import org.example.progettosettimanale6.dto.LoginRisposta;
import org.example.progettosettimanale6.dto.RegistrazioneRequest;
import org.example.progettosettimanale6.model.Utente;
import org.example.progettosettimanale6.repository.UtenteRepository;
import org.example.progettosettimanale6.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public Utente registra(RegistrazioneRequest richiesta) {
        if (utenteRepository.findByUsername(richiesta.username()).isPresent()) {
            throw new IllegalArgumentException("username già in uso");
        }
        Utente utente = Utente.builder()
                .username(richiesta.username())
                .password(passwordEncoder.encode(richiesta.password()))
                .nomeCompleto(richiesta.nomeCompleto())
                .email(richiesta.email())
                .build();
        return utenteRepository.save(utente);
    }

    public LoginRisposta login(LoginRequest richiesta) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(richiesta.username(), richiesta.password()));
        Utente utente = (Utente) auth.getPrincipal();
        String token = jwtUtil.generateToken(utente.getUsername());
        return new LoginRisposta(token, utente.getUsername(), utente.getNomeCompleto());
    }
}
