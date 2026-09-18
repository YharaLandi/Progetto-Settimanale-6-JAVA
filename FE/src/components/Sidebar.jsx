import { useEffect, useRef, useState } from 'react';
import { get, post } from '../api/http';
import './Sidebar.css';

export default function Sidebar({ conversazioni, selezionata, onSeleziona, utente, onStats, onLogout, onNuovaChat }) {
  const [mostraInput, setMostraInput] = useState(false);
  const [destinatario, setDestinatario] = useState('');
  const [suggerimenti, setSuggerimenti] = useState([]);
  const [errore, setErrore] = useState('');
  const timerRef = useRef(null);

  useEffect(() => {
    if (!destinatario.trim()) { setSuggerimenti([]); return; }
    clearTimeout(timerRef.current);
    timerRef.current = setTimeout(async () => {
      try {
        const lista = await get(`/api/auth/utenti?q=${encodeURIComponent(destinatario.trim())}`);
        setSuggerimenti(lista ?? []);
      } catch { setSuggerimenti([]); }
    }, 200);
    return () => clearTimeout(timerRef.current);
  }, [destinatario]);

  function chiudiForm() {
    setMostraInput(false);
    setDestinatario('');
    setSuggerimenti([]);
    setErrore('');
  }

  async function avviaChat(username) {
    const target = username ?? destinatario.trim();
    if (!target) return;
    setErrore('');
    try {
      const conv = await post('/api/chat/nuova', { usernameDestinatario: target });
      chiudiForm();
      onNuovaChat(conv);
    } catch {
      setErrore('Utente non trovato.');
      setSuggerimenti([]);
    }
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="sidebar-avatar">{utente[0]?.toUpperCase()}</div>
        <span className="sidebar-nome">{utente}</span>
        <div className="sidebar-actions">
          <button className="icon-btn" title="Nuova chat" onClick={() => { setMostraInput(v => !v); setErrore(''); setDestinatario(''); setSuggerimenti([]); }}>✏️</button>
          <button className="icon-btn" title="Statistiche" onClick={onStats}>📊</button>
          <button className="icon-btn" title="Esci" onClick={onLogout}>↩</button>
        </div>
      </div>

      {mostraInput && (
        <div className="nuova-chat-form">
          <div className="nuova-chat-row">
            <input
              autoFocus
              placeholder="Username…"
              value={destinatario}
              onChange={e => { setDestinatario(e.target.value); setErrore(''); }}
              onKeyDown={e => e.key === 'Enter' && avviaChat()}
            />
            <button className="nuova-chat-btn" onClick={() => avviaChat()}>→</button>
          </div>
          {suggerimenti.length > 0 && (
            <ul className="nuova-chat-suggerimenti">
              {suggerimenti.map(u => (
                <li key={u} onClick={() => avviaChat(u)}>{u}</li>
              ))}
            </ul>
          )}
          {errore && <span className="nuova-chat-errore">{errore}</span>}
        </div>
      )}

      <div className="sidebar-title">Conversazioni</div>

      <div className="sidebar-lista">
        {conversazioni.length === 0 && (
          <p className="sidebar-vuota">Nessuna conversazione ancora.<br/>Cerca un utente per iniziare.</p>
        )}
        {conversazioni.map((c) => (
          <button
            key={c.idConversazione}
            className={`conv-item ${selezionata?.idConversazione === c.idConversazione ? 'attiva' : ''}`}
            onClick={() => onSeleziona(c)}
          >
            <div className="conv-avatar">{c.controparte[0]?.toUpperCase()}</div>
            <div className="conv-info">
              <span className="conv-nome">{c.controparte}</span>
              <span className="conv-preview">{c.ultimoTesto ?? '…'}</span>
            </div>
            {c.nonLetti > 0 && <span className="conv-badge">{c.nonLetti}</span>}
            {c.online && <span className="conv-online" title="Online" />}
          </button>
        ))}
      </div>
    </aside>
  );
}
