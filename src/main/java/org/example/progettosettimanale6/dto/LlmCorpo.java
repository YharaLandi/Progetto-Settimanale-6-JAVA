package org.example.progettosettimanale6.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LlmCorpo(
        String model,
        int max_tokens,
        List<Turno> messages,
        Ragionamento reasoning) {

    public record Turno(String role, String content) {

        public static Turno sistema(String contenuto) {
            return new Turno("system", contenuto);
        }

        public static Turno utente(String contenuto) {
            return new Turno("user", contenuto);
        }
    }

    public record Ragionamento(boolean enabled) {

        private static final Ragionamento ACCESO = new Ragionamento(true);

        public static Ragionamento se(boolean attivo) {
            return attivo ? ACCESO : null;
        }
    }

    public static LlmCorpo di(String modello, int maxTokens, boolean ragionamento,
                               String istruzioni, String testoUtente) {
        return new LlmCorpo(modello, maxTokens,
                List.of(Turno.sistema(istruzioni), Turno.utente(testoUtente)),
                Ragionamento.se(ragionamento));
    }
}
