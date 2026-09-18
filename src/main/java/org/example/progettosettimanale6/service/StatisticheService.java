package org.example.progettosettimanale6.service;

import org.example.progettosettimanale6.dto.StatisticheRisposta;
import org.example.progettosettimanale6.repository.ConversazioneRepository;
import org.example.progettosettimanale6.repository.MessaggioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticheService {

    private final MessaggioRepository messaggi;
    private final ConversazioneRepository conversazioni;

    public StatisticheService(MessaggioRepository messaggi, ConversazioneRepository conversazioni) {
        this.messaggi = messaggi;
        this.conversazioni = conversazioni;
    }

    @Transactional(readOnly = true)
    public StatisticheRisposta statistiche(String utente) {
        return new StatisticheRisposta(
                messaggi.contaInviati(utente),
                messaggi.contaRicevuti(utente),
                conversazioni.contaConversazioni(utente));
    }
}
