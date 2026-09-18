import { useEffect, useRef, useState } from 'react';
import { get } from '../api/http';
import { connect, disconnect, invia as stompInvia, segnaLetti } from '../api/stomp';
import FinestraChat from '../components/FinestraChat';
import InputMessaggio from '../components/InputMessaggio';
import ModalStatistiche from '../components/ModalStatistiche';
import Sidebar from '../components/Sidebar';
import './ChatPage.css';

export default function ChatPage({ io, nomeCompleto, onLogout }) {
  const [conversazioni, setConversazioni] = useState([]);
  const [selezionata, setSelezionata] = useState(null);
  const [messaggi, setMessaggi] = useState({});     // idConv → Messaggio[]
  const [staScrivendo, setStaScrivendo] = useState(false);
  const [mostraStats, setMostraStats] = useState(false);
  const timerScrittura = useRef(null);
  const selezionataRef = useRef(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    connect(token, onMessaggio, onAggiornamento);
    caricaConversazioni();
    return () => disconnect();
  }, []);

  async function caricaConversazioni() {
    try {
      const lista = await get('/api/chat/conversazioni');
      setConversazioni(lista);
    } catch { /* non autenticato */ }
  }

  async function seleziona(conv) {
    selezionataRef.current = conv;
    setSelezionata(conv);
    if (!messaggi[conv.idConversazione]) {
      try {
        const { messaggi: lista } = await get(`/api/chat/cronologia?conChi=${conv.controparte}`);
        setMessaggi((prev) => ({ ...prev, [conv.idConversazione]: lista }));
      } catch { /* ignora */ }
    }
    if (conv.nonLetti > 0) segnaLetti(conv.idConversazione);
  }

  function onMessaggio(msg) {
    const idConv = msg.idConversazione;
    setMessaggi((prev) => {
      const lista = prev[idConv] ?? [];
      const senzaTemp = msg.idTemporaneo
        ? lista.filter((m) => m.idTemporaneo !== msg.idTemporaneo)
        : lista;
      return { ...prev, [idConv]: [...senzaTemp, msg] };
    });
    caricaConversazioni();
  }

  function onAggiornamento(agg) {
    if (agg.tipo === 'SCRIVE') {
      setStaScrivendo(true);
      clearTimeout(timerScrittura.current);
      timerScrittura.current = setTimeout(() => setStaScrivendo(false), 4000);
    }
    if (agg.tipo === 'PRESENZA') {
      caricaConversazioni();
    }
    if (agg.tipo === 'LETTI') {
      const idConv = agg.conversazione;
      const idLetti = new Set(agg.messaggi);
      setMessaggi((prev) => {
        const lista = prev[idConv];
        if (!lista) return prev;
        return {
          ...prev,
          [idConv]: lista.map((m) => idLetti.has(m.id) ? { ...m, stato: 'LETTO' } : m),
        };
      });
      caricaConversazioni();
    }
  }

  function handleInvia(testo) {
    if (!selezionata) return;
    const idTemporaneo = crypto.randomUUID();
    const msg = {
      idTemporaneo,
      mittente: io,
      destinatario: selezionata.controparte,
      testo,
      istante: new Date().toISOString(),
      stato: 'INVIATO',
    };
    setMessaggi((prev) => ({
      ...prev,
      [selezionata.idConversazione]: [...(prev[selezionata.idConversazione] ?? []), msg],
    }));
    stompInvia(selezionata.controparte, testo, idTemporaneo);
  }

  const msgsAttivi = selezionata ? (messaggi[selezionata.idConversazione] ?? []) : null;

  return (
    <div className="chat-page">
      <Sidebar
        conversazioni={conversazioni}
        selezionata={selezionata}
        onSeleziona={seleziona}
        utente={nomeCompleto || io}
        onStats={() => setMostraStats(true)}
        onLogout={onLogout}
        onNuovaChat={(conv) => { caricaConversazioni(); seleziona(conv); }}
      />

      <div className="chat-main">
        {selezionata ? (
          <div className="chat-header">
            <div className="chat-header-avatar">{selezionata.controparte[0]?.toUpperCase()}</div>
            <div>
              <div className="chat-header-nome">{selezionata.controparte}</div>
              {selezionata.online && <div className="chat-header-status">Online</div>}
            </div>
          </div>
        ) : (
          <div className="chat-header chat-header-empty">
            <span>Seleziona una conversazione</span>
          </div>
        )}

        <FinestraChat messaggi={msgsAttivi} io={io} staScrivendo={staScrivendo}
          onClick={() => selezionata && segnaLetti(selezionata.idConversazione)} />

        <InputMessaggio
          destinatario={selezionata?.controparte}
          idConversazione={selezionata?.idConversazione}
          onInvia={handleInvia}
          disabilitato={!selezionata}
        />
      </div>

      {mostraStats && <ModalStatistiche onChiudi={() => setMostraStats(false)} />}
    </div>
  );
}
