package org.example.progettosettimanale6.config;

import org.example.progettosettimanale6.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompAuthInterceptor.class);
    private static final String PREFISSO = "Bearer ";

    private final JwtUtil jwtUtil;

    public StompAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String intestazione = accessor.getFirstNativeHeader("Authorization");
        if (intestazione == null || !intestazione.startsWith(PREFISSO)) {
            log.warn("CONNECT senza Authorization valida: sessione anonima");
            return message;
        }

        String token = intestazione.substring(PREFISSO.length());
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("CONNECT con JWT non valido: sessione anonima");
            return message;
        }

        String username = jwtUtil.extractUsername(token);
        accessor.setUser(() -> username);
        log.info("CONNECT accettato   utente={}", username);

        return message;
    }
}
