package org.example.progettosettimanale6.service;

import org.example.progettosettimanale6.dto.LlmCorpo;
import org.example.progettosettimanale6.dto.LlmRisposta;
import org.example.progettosettimanale6.model.Conversazione;
import org.example.progettosettimanale6.model.Messaggio;
import org.example.progettosettimanale6.repository.ConversazioneRepository;
import org.example.progettosettimanale6.repository.MessaggioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private static final String ISTRUZIONI = """
            Sei un assistente per una chat in italiano.
            Analizza la conversazione e suggerisci un breve messaggio (massimo 2 frasi)
            che l'utente potrebbe inviare per continuare la conversazione.
            Rispondi SOLO con il testo del messaggio suggerito, senza spiegazioni o prefissi.
            """;

    private final RestClient client;
    private final ConversazioneRepository conversazioneRepo;
    private final MessaggioRepository messaggioRepo;
    private final String modello;
    private final int maxTokens;
    private final boolean ragionamento;

    public LlmService(RestClient llmClient,
                      ConversazioneRepository conversazioneRepo,
                      MessaggioRepository messaggioRepo,
                      @Value("${app.llm.model}") String modello,
                      @Value("${app.llm.max-tokens}") int maxTokens,
                      @Value("${app.llm.reasoning:false}") boolean ragionamento) {
        this.client = llmClient;
        this.conversazioneRepo = conversazioneRepo;
        this.messaggioRepo = messaggioRepo;
        this.modello = modello;
        this.maxTokens = maxTokens;
        this.ragionamento = ragionamento;
    }

    @Transactional(readOnly = true)
    public String suggerisci(UUID idConversazione, String utente) {
        Conversazione conv = conversazioneRepo.findById(idConversazione)
                .orElseThrow(() -> new IllegalArgumentException("conversazione non trovata"));
        if (!conv.partecipa(utente)) {
            throw new IllegalArgumentException("non sei partecipante di questa conversazione");
        }

        List<Messaggio> ultimi = new ArrayList<>(
                messaggioRepo.ultimi(idConversazione, Limit.of(10)));
        ultimi = ultimi.reversed();

        String storico = ultimi.stream()
                .map(m -> m.getMittente().getUsername() + ": " + m.getTesto())
                .collect(Collectors.joining("\n"));

        String prompt = "Conversazione recente:\n\n" + storico
                + "\n\nSuggerisci il prossimo messaggio che \"" + utente + "\" potrebbe inviare.";

        LlmCorpo corpo = LlmCorpo.di(modello, maxTokens, ragionamento, ISTRUZIONI, prompt);

        long inizio = System.nanoTime();
        LlmRisposta risposta = chiama(corpo);
        long millis = (System.nanoTime() - inizio) / 1_000_000;

        log.info("suggerimento conv={} utente={} modello={} in={} out={} ragionamento={}car stop={} durata={}ms",
                idConversazione, utente, risposta.model(),
                risposta.tokenIngresso(), risposta.tokenUscita(),
                risposta.lunghezzaRagionamento(), risposta.motivoArresto(), millis);

        String testo = risposta.primoTesto();
        if (testo.isBlank()) {
            log.warn("risposta senza testo: stop={} - se stop=length alzare app.llm.max-tokens",
                    risposta.motivoArresto());
        }
        return testo;
    }

    private LlmRisposta chiama(LlmCorpo corpo) {
        try {
            return client.post()
                    .uri("/chat/completions")
                    .body(corpo)
                    .retrieve()
                    .onStatus(stato -> stato.value() == 429, (req, resp) -> {
                        String riprova = resp.getHeaders().getFirst("retry-after");
                        log.warn("429 dal servizio esterno, retry-after={}", riprova);
                        throw new LimiteRaggiuntoException(riprova);
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                        log.error("4xx dal servizio esterno: {} - corpo: {}",
                                resp.getStatusCode(), corpoDi(resp));
                        throw new RichiestaEsternaNonValidaException(
                                "il servizio ha rifiutato la richiesta: " + resp.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                        log.error("5xx dal servizio esterno: {} - corpo: {}",
                                resp.getStatusCode(), corpoDi(resp));
                        throw new ServizioNonDisponibileException(
                                "il servizio non ha risposto correttamente: " + resp.getStatusCode());
                    })
                    .body(LlmRisposta.class);
        } catch (ResourceAccessException ex) {
            log.error("il servizio esterno non e' raggiungibile: {}", ex.getMessage());
            throw new ServizioNonDisponibileException("il servizio esterno non e' raggiungibile");
        }
    }

    private String corpoDi(org.springframework.http.client.ClientHttpResponse risposta) {
        try (var flusso = risposta.getBody()) {
            return new String(flusso.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "(corpo non leggibile: " + ex.getMessage() + ")";
        }
    }
}
