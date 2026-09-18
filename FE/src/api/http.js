const BASE = 'http://localhost:8080';

function getToken() {
  return localStorage.getItem('token') ?? '';
}

function gestisciRisposta(res) {
  if (res.status === 401) {
    localStorage.clear();
    window.location.reload();
  }
  if (!res.ok) throw new Error(res.status);
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  return res.json();
}

export async function post(path, body) {
  const res = await fetch(BASE + path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
    body: JSON.stringify(body),
  });
  return gestisciRisposta(res);
}

export async function get(path) {
  const res = await fetch(BASE + path, {
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  return gestisciRisposta(res);
}
