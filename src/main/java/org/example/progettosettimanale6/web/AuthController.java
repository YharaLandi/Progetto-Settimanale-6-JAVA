package org.example.progettosettimanale6.web;

import org.example.progettosettimanale6.dto.LoginRequest;
import org.example.progettosettimanale6.dto.LoginRisposta;
import org.example.progettosettimanale6.dto.RegistrazioneRequest;
import org.example.progettosettimanale6.model.Utente;
import org.example.progettosettimanale6.service.AuthService;
import org.example.progettosettimanale6.service.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UtenteService utenteService;

    public AuthController(AuthService authService, UtenteService utenteService) {
        this.authService = authService;
        this.utenteService = utenteService;
    }

    @PostMapping("/registra")
    @ResponseStatus(HttpStatus.CREATED)
    public Utente registra(@RequestBody RegistrazioneRequest richiesta) {
        return authService.registra(richiesta);
    }

    @PostMapping("/login")
    public LoginRisposta login(@RequestBody LoginRequest richiesta) {
        return authService.login(richiesta);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        // JWT è stateless: il client scarta il token
    }

    @GetMapping("/utenti")
    public List<String> utenti(@RequestParam(required = false, defaultValue = "") String q,
                                Principal principal) {
        if (q.isBlank()) return List.of();
        return utenteService.cerca(q, principal.getName());
    }
}
