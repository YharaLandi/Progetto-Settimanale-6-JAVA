import { useState } from 'react';
import { post } from '../api/http';
import './LoginPage.css';

export default function LoginPage({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [nomeCompleto, setNomeCompleto] = useState('');
  const [email, setEmail] = useState('');
  const [errore, setErrore] = useState('');
  const [carico, setCarico] = useState(false);
  const [modalita, setModalita] = useState('login');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!username.trim() || !password.trim()) return;
    setCarico(true);
    setErrore('');
    try {
      if (modalita === 'login') {
        const res = await post('/api/auth/login', { username, password });
        localStorage.setItem('token', res.token);
        localStorage.setItem('username', username);
        localStorage.setItem('nomeCompleto', res.nomeCompleto);
        onLogin(username, res.nomeCompleto);
      } else {
        if (!nomeCompleto.trim() || !email.trim()) return;
        await post('/api/auth/registra', { username, password, nomeCompleto, email });
        const res = await post('/api/auth/login', { username, password });
        localStorage.setItem('token', res.token);
        localStorage.setItem('username', username);
        localStorage.setItem('nomeCompleto', res.nomeCompleto);
        onLogin(username, res.nomeCompleto);
      }
    } catch {
      setErrore(modalita === 'login' ? 'Credenziali non valide.' : 'Registrazione fallita. Username o email già in uso.');
    } finally {
      setCarico(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">💬</div>
        <h1>ChatAI</h1>
        <p className="login-sub">{modalita === 'login' ? 'Accedi per iniziare a chattare' : 'Crea un nuovo account'}</p>

        <form onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoFocus
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {modalita === 'registra' && (
            <>
              <input
                type="text"
                placeholder="Nome completo"
                value={nomeCompleto}
                onChange={(e) => setNomeCompleto(e.target.value)}
              />
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </>
          )}
          {errore && <p className="login-errore">{errore}</p>}
          <button type="submit" className="btn-primary" disabled={carico}>
            {carico ? '…' : modalita === 'login' ? 'Entra' : 'Registrati'}
          </button>
        </form>

        <button
          className="btn-secondary"
          onClick={() => { setModalita(modalita === 'login' ? 'registra' : 'login'); setErrore(''); }}
        >
          {modalita === 'login' ? 'Non hai un account? Registrati' : 'Hai già un account? Accedi'}
        </button>
      </div>
    </div>
  );
}
