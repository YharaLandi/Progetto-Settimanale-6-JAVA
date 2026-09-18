import { useEffect, useRef } from 'react';
import './FinestraChat.css';

export default function FinestraChat({ messaggi, io, staScrivendo }) {
  const fondo = useRef(null);

  useEffect(() => {
    fondo.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messaggi]);

  if (!messaggi) {
    return (
      <div className="chat-vuota">
        <span>💬</span>
        <p>Seleziona una conversazione</p>
      </div>
    );
  }

  return (
    <div className="finestra-chat">
      {messaggi.map((m) => {
        const mio = m.mittente === io;
        return (
          <div key={m.id ?? m.idTemporaneo} className={`bubble-wrap ${mio ? 'mio' : 'suo'}`}>
            <div className={`bubble ${mio ? 'bubble-me' : 'bubble-in'}`}>
              {m.testo}
              <span className="bubble-ora">
                {new Date(m.istante).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })}
                {mio && <span className="stato-msg">{statoIcon(m.stato)}</span>}
              </span>
            </div>
          </div>
        );
      })}
      {staScrivendo && (
        <div className="bubble-wrap suo">
          <div className="bubble bubble-in digitando">
            <span /><span /><span />
          </div>
        </div>
      )}
      <div ref={fondo} />
    </div>
  );
}

function statoIcon(stato) {
  if (stato === 'LETTO')      return ' ✓✓';
  if (stato === 'CONSEGNATO') return ' ✓';
  return ' ·';
}
