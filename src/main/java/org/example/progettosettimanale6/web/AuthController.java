package org.example.progettosettimanale6.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.progettosettimanale6.dto.LoginRequest;
import org.example.progettosettimanale6.dto.LoginRisposta;
import org.example.progettosettimanale6.dto.RegistrazioneRequest;
import org.example.progettosettimanale6.model.Utente;
import org.example.progettosettimanale6.service.TokenStore;
import org.example.progettosettimanale6.service.UtenteNonAutenticatoException;
import org.example.progettosettimanale6.service.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtenteService utenteService;
    private final TokenStore tokenStore;

    public AuthController(UtenteService utenteService, TokenStore tokenStore) {
        this.utenteService = utenteService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/login")
    public LoginRisposta login(@RequestBody LoginRequest richiesta) {
        Utente utente = utenteService.findByUsername(richiesta.username())
                .orElseThrow(() -> new IllegalArgumentException("credenziali non valide"));
        if (!utente.getPassword().equals(richiesta.password())) {
            throw new IllegalArgumentException("credenziali non valide");
        }
        String token = tokenStore.emetti(richiesta.username());
        return new LoginRisposta(token, utente.getUsername(), utente.getNomeCompleto());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            tokenStore.revoca(h.substring(7));
        }
    }

    @PostMapping("/registra")
    @ResponseStatus(HttpStatus.CREATED)
    public Utente registra(@RequestBody RegistrazioneRequest richiesta) {
        Utente utente = Utente.builder()
                .username(richiesta.username())
                .password(richiesta.password())
                .nomeCompleto(richiesta.nomeCompleto())
                .email(richiesta.email())
                .build();
        return utenteService.save(utente);
    }

    @GetMapping("/utenti")
    public List<String> utenti(@RequestParam(required = false, defaultValue = "") String q,
                                HttpServletRequest req) {
        String me = utenteCorrente(req);
        if (q.isBlank()) return List.of();
        return utenteService.cerca(q, me);
    }

    private String utenteCorrente(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            return tokenStore.utenteDi(h.substring(7))
                    .orElseThrow(UtenteNonAutenticatoException::new);
        }
        throw new UtenteNonAutenticatoException();
    }
}
