# ChatAI — Progetto Settimanale S6/L5

Chat in tempo reale tra utenti con suggerimenti AI e report statistico via email.

## Funzionalità

- **Chat real-time** tra utenti autenticati tramite WebSocket (STOMP)
- **Suggerimento AI** — chiede a Claude di proporre il prossimo messaggio (non salvato nel DB)
- **Statistiche personali** — messaggi inviati, ricevuti, chat aperte
- **Email statistiche** — report in formato HTML via Thymeleaf

## Stack

| Layer     | Tecnologia                           |
|-----------|--------------------------------------|
| Backend   | Spring Boot 4.1.1, Java 25           |
| WebSocket | STOMP + SimpMessagingTemplate        |
| AI        | Claude via OpenRouter (RestClient)   |
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

Crea `src/main/resources/application.properties` con:

```properties
spring.application.name=Progetto-Settimanale-6

spring.datasource.url=jdbc:postgresql://localhost:5432/chatai
spring.datasource.username=postgres
spring.datasource.password=1234

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=587
spring.mail.username=<USERNAME_MAILTRAP>
spring.mail.password=<PASSWORD_MAILTRAP>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.mail.mittente=noreply@chatai.local
app.base-url=http://localhost:8080

app.llm.url=https://openrouter.ai/api/v1
app.llm.key=<API_KEY_OPENROUTER>
app.llm.model=anthropic/claude-haiku-4-5-20251001
app.llm.max-tokens=512
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
│       ├── service/              # ChatService, ClaudeService, StatisticheMailService
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
| POST   | `/api/auth/login`         | Login → token                           |
| WS     | `/ws` (STOMP)             | Connessione WebSocket                   |
| MSG    | `/app/invia`              | Invia messaggio                         |
| MSG    | `/app/scrive`             | Indicatore di scrittura                 |
| MSG    | `/app/letti`              | Segna messaggi come letti               |
| GET    | `/api/chat/conversazioni` | Lista conversazioni con non letti       |
| GET    | `/api/chat/cronologia`    | Storico messaggi con un utente          |
| POST   | `/api/suggerisci`         | Chiede all'AI un suggerimento           |
| GET    | `/api/statistiche`        | Statistiche personali                   |
| POST   | `/api/statistiche/email`  | Invia statistiche via email             |

## Note di sicurezza

- La chiave OpenRouter va **solo** in `application.properties`, mai in git (già in `.gitignore`)
- Il token di sessione viaggia nell'header `Authorization` e nel frame STOMP CONNECT
- Nessun dato AI viene persistito nel database
