import { useEffect, useState } from 'react';
import { get, post } from '../api/http';
import './ModalStatistiche.css';

export default function ModalStatistiche({ onChiudi }) {
  const [stats, setStats] = useState(null);
  const [invio, setInvio] = useState('idle'); // idle | loading | ok | err

  useEffect(() => {
    get('/api/statistiche').then(setStats).catch(console.error);
  }, []);

  async function inviaEmail() {
    setInvio('loading');
    try {
      await post('/api/statistiche/email', {});
      setInvio('ok');
    } catch {
      setInvio('err');
    }
  }

  return (
    <div className="modal-overlay" onClick={onChiudi}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>📊 Le tue statistiche</h2>
          <button className="modal-close" onClick={onChiudi}>✕</button>
        </div>

        {!stats ? (
          <p className="modal-carico">Caricamento…</p>
        ) : (
          <>
            <div className="stats-grid">
              <StatCard emoji="📤" label="Messaggi inviati"  valore={stats.messaggiInviati} />
              <StatCard emoji="📥" label="Messaggi ricevuti" valore={stats.messaggiRicevuti} />
              <StatCard emoji="💬" label="Chat aperte"       valore={stats.chatAperte} />
            </div>

            <button
              className="btn-email"
              onClick={inviaEmail}
              disabled={invio === 'loading' || invio === 'ok'}
            >
              {invio === 'idle'    && '✉️  Ricevi via email'}
              {invio === 'loading' && 'Invio in corso…'}
              {invio === 'ok'      && '✓ Email inviata!'}
              {invio === 'err'     && '⚠️ Errore — riprova'}
            </button>
          </>
        )}
      </div>
    </div>
  );
}

function StatCard({ emoji, label, valore }) {
  return (
    <div className="stat-card">
      <span className="stat-emoji">{emoji}</span>
      <span className="stat-valore">{valore}</span>
      <span className="stat-label">{label}</span>
    </div>
  );
}
