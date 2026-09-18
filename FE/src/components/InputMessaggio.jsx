import { useState } from 'react';
import { post } from '../api/http';
import { scrive } from '../api/stomp';
import './InputMessaggio.css';

export default function InputMessaggio({ destinatario, idConversazione, onInvia, disabilitato }) {
  const [testo, setTesto] = useState('');
  const [suggerito, setSuggerito] = useState('');
  const [caricando, setCaricando] = useState(false);

  function handleChange(e) {
    setTesto(e.target.value);
    if (destinatario) scrive(destinatario);
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      invia();
    }
  }

  function invia() {
    const t = testo.trim();
    if (!t || disabilitato) return;
    onInvia(t);
    setTesto('');
    setSuggerito('');
  }

  async function chiediAI() {
    if (!idConversazione) return;
    setCaricando(true);
    try {
      const { suggerimento } = await post('/api/suggerisci', { idConversazione });
      setSuggerito(suggerimento);
      setTesto(suggerimento);
    } catch {
      setSuggerito('');
    } finally {
      setCaricando(false);
    }
  }

  return (
    <div className="input-area">
      {suggerito && (
        <div className="ai-chip">
          <span>✨ AI:</span> {suggerito}
          <button className="chip-dismiss" onClick={() => { setSuggerito(''); setTesto(''); }}>✕</button>
        </div>
      )}
      <div className="input-row">
        <button
          className="btn-ai"
          title="Chiedi all'IA un suggerimento"
          onClick={chiediAI}
          disabled={!idConversazione || caricando}
        >
          {caricando ? '⏳' : '✨'}
        </button>
        <textarea
          rows={1}
          placeholder="Scrivi un messaggio…"
          value={testo}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          disabled={disabilitato}
        />
        <button
          className="btn-send"
          onClick={invia}
          disabled={!testo.trim() || disabilitato}
        >
          ➤
        </button>
      </div>
    </div>
  );
}
