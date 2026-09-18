package org.example.progettosettimanale6.service;

import org.example.progettosettimanale6.dto.Aggiornamento;
import org.example.progettosettimanale6.dto.ConversazioneRiepilogo;
import org.example.progettosettimanale6.dto.MessaggioRisposta;
import org.example.progettosettimanale6.dto.PaginaMessaggi;
import org.example.progettosettimanale6.model.Conversazione;
import org.example.progettosettimanale6.model.Messaggio;
import org.example.progettosettimanale6.model.StatoMessaggio;
import org.example.progettosettimanale6.model.Utente;
import org.example.progettosettimanale6.repository.ConversazioneRepository;
import org.example.progettosettimanale6.repository.MessaggioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Limit;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public static final String MESSAGGI = "/queue/messaggi";
    public static final String AGGIORNAMENTI = "/queue/aggiornamenti";
    private static final int PAGINA = 30;
    private static final PaginaMessaggi VUOTA = new PaginaMessaggi(null, List.of(), 0);

    private final MessaggioRepository messaggi;
    private final ConversazioneRepository conversazioni;
    private final UtenteService utenteService;
    private final SimpMessagingTemplate template;
    private final SimpUserRegistry registro;

    public ChatService(MessaggioRepository messaggi,
                       ConversazioneRepository conversazioni,
                       UtenteService utenteService,
                       SimpMessagingTemplate template,
                       SimpUserRegistry registro) {
        this.messaggi = messaggi;
        this.conversazioni = conversazioni;
        this.utenteService = utenteService;
        this.template = template;
        this.registro = registro;
    }

    public MessaggioRisposta inoltra(String mittenteUsername, String destinatarioUsername,
                                      String testo, String idTemporaneo) {
        Utente mittente = utenteService.findByUsername(mittenteUsername)
                .orElseThrow(() -> new IllegalArgumentException("utente non trovato: " + mittenteUsername));
        Utente destinatario = utenteService.findByUsername(destinatarioUsername)
                .orElseThrow(() -> new IllegalArgumentException("utente non trovato: " + destinatarioUsername));

        Conversazione conversazione = trovaOCrea(mittente, destinatario);
        Messaggio salvato = messaggi.save(new Messaggio(conversazione, mittente, destinatario, testo));

        if (registro.getUser(destinatarioUsername) != null) {
            salvato.consegnato();
            salvato = messaggi.save(salvato);
        }

        template.convertAndSendToUser(destinatarioUsername, MESSAGGI,
                MessaggioRisposta.con(salvato, mittenteUsername, destinatarioUsername, null));
        template.convertAndSendToUser(mittenteUsername, MESSAGGI,
                MessaggioRisposta.con(salvato, mittenteUsername, destinatarioUsername, idTemporaneo));

        log.info("messaggio {} conv={} {} -> {} ({})",
                salvato.getId(), conversazione.getId(), mittenteUsername, destinatarioUsername, salvato.getStato());
        return MessaggioRisposta.con(salvato, mittenteUsername, destinatarioUsername, null);
    }

    public void scrive(String chiUsername, String aChiUsername) {
        Utente chi = utenteService.findByUsername(chiUsername).orElse(null);
        Utente aChi = utenteService.findByUsername(aChiUsername).orElse(null);
        if (chi == null || aChi == null) return;
        trova(chi, aChi).ifPresent(conv ->
                template.convertAndSendToUser(aChiUsername, AGGIORNAMENTI,
                        Aggiornamento.scrive(conv.getId(), chiUsername)));
    }

    @Transactional
    public void segnaLetti(UUID idConversazione, String utenteUsername) {
        Conversazione conv = conversazioni.findById(idConversazione)
                .orElseThrow(() -> new IllegalArgumentException("conversazione: non esiste"));
        if (!conv.partecipa(utenteUsername)) {
            throw new IllegalArgumentException("conversazione: non sei un partecipante");
        }

        List<UUID> id = messaggi.idNonLetti(idConversazione, utenteUsername);
        if (id.isEmpty()) return;

        messaggi.segnaLetti(idConversazione, utenteUsername, StatoMessaggio.LETTO);

        String controparteUsername = conv.controparteDi(utenteUsername).getUsername();
        template.convertAndSendToUser(controparteUsername, AGGIORNAMENTI,
                Aggiornamento.letti(idConversazione, utenteUsername, id));
        template.convertAndSendToUser(utenteUsername, AGGIORNAMENTI,
                Aggiornamento.letti(idConversazione, utenteUsername, id));

        log.info("letti {} messaggi conv={} da {}", id.size(), idConversazione, utenteUsername);
    }

    @Transactional
    public void segnaConsegnati(UUID idConversazione, String utenteUsername) {
        Conversazione conv = conversazioni.findById(idConversazione)
                .orElseThrow(() -> new IllegalArgumentException("conversazione: non esiste"));
        if (!conv.partecipa(utenteUsername)) {
            throw new IllegalArgumentException("conversazione: non sei un partecipante");
        }
        messaggi.segnaConsegnati(idConversazione, utenteUsername, Instant.now());
        log.info("consegnati conv={} dest={}", idConversazione, utenteUsername);
    }

    @Transactional(readOnly = true)
    public PaginaMessaggi cronologia(String utenteUsername, String conChiUsername) {
        Utente utente = utenteService.findByUsername(utenteUsername).orElse(null);
        Utente conChi = utenteService.findByUsername(conChiUsername).orElse(null);
        if (utente == null || conChi == null) return VUOTA;

        Conversazione conv = trova(utente, conChi).orElse(null);
        if (conv == null) return VUOTA;

        List<MessaggioRisposta> pagina = new ArrayList<>(
                messaggi.ultimi(conv.getId(), Limit.of(PAGINA)).stream()
                        .map(m -> MessaggioRisposta.con(m, null))
                        .toList());
        pagina.sort(Comparator.comparing(MessaggioRisposta::istante));
        return new PaginaMessaggi(conv.getId(), pagina,
                messaggi.nonLetti(conv.getId(), utenteUsername));
    }

    @Transactional(readOnly = true)
    public List<ConversazioneRiepilogo> riepilogo(String utenteUsername) {
        return conversazioni.diUtente(utenteUsername).stream()
                .map(c -> {
                    List<Messaggio> ultimo = messaggi.ultimi(c.getId(), Limit.of(1));
                    String controparte = c.controparteDi(utenteUsername).getUsername();
                    return new ConversazioneRiepilogo(
                            c.getId(),
                            controparte,
                            messaggi.nonLetti(c.getId(), utenteUsername),
                            ultimo.isEmpty() ? null : ultimo.getFirst().getTesto(),
                            ultimo.isEmpty() ? null : ultimo.getFirst().getIstante(),
                            registro.getUser(controparte) != null);
                })
                .sorted(Comparator.comparing(ConversazioneRiepilogo::controparte))
                .toList();
    }

    @EventListener
    @Transactional(readOnly = true)
    public void suConnessione(SessionConnectedEvent evento) {
        if (evento.getUser() == null) return;
        String username = evento.getUser().getName();
        conversazioni.usernameContatti(username).forEach(controparte ->
                template.convertAndSendToUser(controparte, AGGIORNAMENTI, Aggiornamento.presenza(username)));
    }

    @EventListener
    @Transactional(readOnly = true)
    public void suDisconnessione(SessionDisconnectEvent evento) {
        if (evento.getUser() == null) return;
        String username = evento.getUser().getName();
        conversazioni.usernameContatti(username).forEach(controparte ->
                template.convertAndSendToUser(controparte, AGGIORNAMENTI, Aggiornamento.presenza(username)));
    }

    public Optional<Conversazione> trova(Utente a, Utente b) {
        if (a.getUsername().equals(b.getUsername())) {
            throw new IllegalArgumentException("destinatario: non puoi scrivere a te stesso");
        }
        return conversazioni.findByUnoAndAltro(
                Conversazione.primo(a, b), Conversazione.secondo(a, b));
    }

    @Transactional
    public Conversazione trovaOCrea(Utente a, Utente b) {
        return trova(a, b).orElseGet(() -> conversazioni.save(Conversazione.fra(a, b)));
    }
}
