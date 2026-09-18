package org.example.progettosettimanale6.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmRisposta(
        String id,
        String model,
        List<Scelta> choices,
        Uso usage) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Scelta(int index, Messaggio message, String finish_reason) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Messaggio(String role, String content, String refusal, String reasoning) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Uso(int prompt_tokens, int completion_tokens, int total_tokens) {}

    public String primoTesto() {
        if (choices == null || choices.isEmpty()) return "";
        var msg = choices.getFirst().message();
        if (msg == null || msg.content() == null) return "";
        return msg.content();
    }

    public String motivoArresto() {
        if (choices == null || choices.isEmpty()) return null;
        return choices.getFirst().finish_reason();
    }

    public int lunghezzaRagionamento() {
        if (choices == null || choices.isEmpty()) return 0;
        var msg = choices.getFirst().message();
        if (msg == null || msg.reasoning() == null) return 0;
        return msg.reasoning().length();
    }

    public int tokenIngresso() {
        return usage == null ? 0 : usage.prompt_tokens();
    }

    public int tokenUscita() {
        return usage == null ? 0 : usage.completion_tokens();
    }
}
