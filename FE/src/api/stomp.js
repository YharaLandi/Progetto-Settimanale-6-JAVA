import { Client } from '@stomp/stompjs';

const WS_URL = 'ws://localhost:8080/ws';

let client = null;

export function connect(token, onMessage, onAggiornamento) {
  client = new Client({
    brokerURL: WS_URL,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 3000,
    onConnect: () => {
      client.subscribe('/user/queue/messaggi',    (frame) => onMessage(JSON.parse(frame.body)));
      client.subscribe('/user/queue/aggiornamenti', (frame) => onAggiornamento(JSON.parse(frame.body)));
    },
  });
  client.activate();
  return client;
}

export function disconnect() {
  client?.deactivate();
  client = null;
}

export function invia(destinatario, testo, idTemporaneo) {
  client?.publish({
    destination: '/app/invia',
    body: JSON.stringify({ destinatario, testo, idTemporaneo }),
  });
}

export function scrive(destinatario) {
  client?.publish({
    destination: '/app/scrive',
    body: JSON.stringify({ destinatario }),
  });
}

export function segnaLetti(idConversazione) {
  client?.publish({
    destination: '/app/letti',
    body: JSON.stringify({ idConversazione }),
  });
}
