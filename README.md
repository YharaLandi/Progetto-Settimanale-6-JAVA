# ChatAI — Progetto Settimanale S6/L5

Chat in tempo reale tra utenti con suggerimenti AI e report statistico via email.

## Funzionalità

- **Chat real-time** tra utenti autenticati tramite WebSocket (STOMP)
- **Suggerimento AI** — chiede a un modello LLM (via OpenRouter) di proporre il prossimo messaggio (non salvato nel DB)
- **Statistiche personali** — messaggi inviati, ricevuti, chat aperte
- **Email statistiche** — report in formato HTML via Thymeleaf

## Stack

| Layer     | Tecnologia                           |
|-----------|--------------------------------------|
| Backend   | Spring Boot 4.1.1, Java 25           |
| WebSocket | STOMP + SimpMessagingTemplate        |
| Auth      | Spring Security + JWT (BCrypt)       |
| AI        | LLM via OpenRouter (RestClient)      |
| Database  | PostgreSQL + Spring Data JPA         |
| Email     | Spring Mail + Thymeleaf              |
| Frontend  | React 19, Vite, @stomp/stompjs       |

## Prerequisiti

- Java 25
- Maven 3.9+
- Node 18+ / npm 9+
- PostgreSQL in esecuzione su `localhost:5432`

## Setup

### 1. Database

```sql
CREATE DATABASE chatai;
```

### 2. Backend

Copia il template e riempi i valori reali:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Valori da sostituire nel file copiato:
- `spring.datasource.password` — password di PostgreSQL
- `spring.mail.username` / `spring.mail.password` — credenziali Mailtrap (sandbox SMTP)
- `app.llm.api-key` — API key da [openrouter.ai/settings/keys](https://openrouter.ai/settings/keys)
- `jwt.secret` — stringa Base64 di almeno 32 byte (puoi generarla con `openssl rand -base64 32`)

Il database deve chiamarsi `PROGETTO-SETTIMANALE-JAVA6` (o aggiorna l'URL nel file):

```sql
CREATE DATABASE "PROGETTO-SETTIMANALE-JAVA6";
```

Avvio:

```bash
./mvnw spring-boot:run
```

### 3. Frontend

```bash
cd FE
npm install
npm run dev
```

Apri `http://localhost:5173`

## Struttura progetto

```
Progetto-Settimanale-6/
├── src/                          # Spring Boot backend
│   └── main/java/org/example/progettosettimanale6/
│       ├── model/                # Utente, Conversazione, Messaggio
│       ├── repository/           # JpaRepository
│       ├── service/              # ChatService, LlmService, StatisticheMailService
│       ├── web/                  # AuthController, ChatController, ...
│       └── config/               # StompConfig, CorsConfig, LlmConfig
├── FE/                           # React frontend
│   └── src/
│       ├── api/                  # http.js, stomp.js
│       ├── pages/                # LoginPage, ChatPage
│       └── components/           # Sidebar, FinestraChat, InputMessaggio, ModalStatistiche
└── pom.xml
```

## API principali

| Metodo | Endpoint                  | Descrizione                             |
|--------|---------------------------|-----------------------------------------|
| Metodo | Endpoint                          | Descrizione                             |
|--------|-----------------------------------|-----------------------------------------|
| POST   | `/api/auth/registra`              | Registrazione nuovo utente              |
| POST   | `/api/auth/login`                 | Login → token                           |
| POST   | `/api/auth/logout`                | Logout (204)                            |
| GET    | `/api/auth/utenti?q=`             | Ricerca utenti (autocomplete)           |
| WS     | `/ws` (STOMP)                     | Connessione WebSocket                   |
| MSG    | `/app/chat.invia`                 | Invia messaggio                         |
| POST   | `/api/chat/nuova`                 | Avvia conversazione con un utente       |
| GET    | `/api/conversazioni`              | Lista conversazioni con non letti       |
| GET    | `/api/chat/{id}/messaggi`         | Storico messaggi paginato               |
| POST   | `/api/chat/{id}/segna-letti`      | Segna messaggi come letti               |
| POST   | `/api/suggerimento`               | Chiede all'AI un suggerimento           |
| GET    | `/api/statistiche`                | Statistiche personali                   |
| POST   | `/api/statistiche/invia-email`    | Invia statistiche via email (204)       |

## Note di sicurezza

- La chiave OpenRouter va **solo** in `application.properties`, mai in git (già in `.gitignore`)
- Il token di sessione viaggia nell'header `Authorization` e nel frame STOMP CONNECT
- Nessun dato AI viene persistito nel database
