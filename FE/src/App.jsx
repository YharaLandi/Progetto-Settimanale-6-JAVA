import { useState } from 'react';
import ChatPage from './pages/ChatPage';
import LoginPage from './pages/LoginPage';

export default function App() {
  const [utente, setUtente] = useState(() => localStorage.getItem('username'));
  const [nome, setNome] = useState(() => localStorage.getItem('nomeCompleto'));

  function handleLogin(username, nomeCompleto) {
    setUtente(username);
    setNome(nomeCompleto);
  }

  function handleLogout() {
    localStorage.clear();
    setUtente(null);
    setNome(null);
  }

  if (!utente) return <LoginPage onLogin={handleLogin} />;

  return <ChatPage io={utente} nomeCompleto={nome} onLogout={handleLogout} />;
}
