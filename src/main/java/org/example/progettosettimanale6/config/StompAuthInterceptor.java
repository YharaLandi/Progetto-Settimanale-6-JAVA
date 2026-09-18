package org.example.progettosettimanale6.config;

import org.example.progettosettimanale6.service.TokenStore;
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

    private final TokenStore token;

    public StompAuthInterceptor(TokenStore token) {
        this.token = token;
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

        String valore = intestazione.substring(PREFISSO.length());
        token.utenteDi(valore).ifPresentOrElse(
                utente -> {
                    accessor.setUser(() -> utente);
                    log.info("CONNECT accettato   utente={}", utente);
                },
                () -> log.warn("CONNECT con token sconosciuto: sessione anonima"));

        return message;
    }
}
