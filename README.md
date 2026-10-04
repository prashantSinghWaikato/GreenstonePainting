# Greenstone Painting Digital Platform

Greenstone Painting is a full-stack customer and operations platform for a painting business in Hamilton, New Zealand. It combines a public marketing website and quotation journey with an authenticated administration portal for content, enquiries, quotes, jobs, invoices, staff notifications, and reporting.

The repository currently supports local development, automated testing, and an isolated demonstration deployment.
## Contents

- [Capabilities](#capabilities)
- [Architecture](#architecture)
- [Technology stack](#technology-stack)
- [Repository structure](#repository-structure)
- [Local development](#local-development)
- [Configuration](#configuration)
- [Testing](#testing)
- [Container build and deployment](#container-build-and-deployment)
- [Operations](#operations)
- [Security](#security)
- [License](#license)

## Capabilities

### Public website

- Responsive service, project, company, service-area, blog, contact, and privacy pages.
- Database-managed projects, services, articles, and Resene colour-palette entries.
- Live Google Places rating summary with a direct link to the business listing.
- Structured quotation request with validation and a customer reference number.
- Up to four project-photo attachments, limited to 5 MB each.
- Secure customer pages for downloading, accepting, or declining sent quotes.

### Administration portal

- Session-based staff authentication with administrator and owner permissions.
- Enquiry assignment, priority, follow-up dates, workflow history, and notification status.
- Quote drafts, revisions, PDF previews, email delivery, and expiring customer response links.
- Job scheduling, crew assignment, status changes, private notes, progress photos, and activity history.
- Six-point completion checklist and customer walkthrough sign-off.
- Invoice PDFs, due dates, payment status, payment references, and operational reports.
- Management of projects, services, articles, and public colour-palette entries.
- Per-user controls for assignment alerts, reminders, and weekday digests.

Payment collection is recorded manually. No payment gateway or bank feed is connected.

## Architecture

```text
Browser
  ├─ Public React application
  └─ Administration React application
                │
                ▼
Spring Boot REST application
  ├─ Spring Security and HTTP sessions
  ├─ Validation and business services
  ├─ PDF generation and notifications
  ├─ Google Places integration
  └─ Spring Data JPA
                │
                ▼
PostgreSQL and Flyway migrations
```

The production-style Docker image builds the Vite frontend and packages the generated assets inside the Spring Boot application. This provides one origin for the website, API, and authenticated session. Local development runs Vite and Spring Boot separately, with explicit CORS configuration.

## Technology stack

| Area | Technology |
| --- | --- |
| Frontend | React 19, TypeScript, Vite 8 |
| Backend | Java 17, Spring Boot 4, Spring Security, Spring Data JPA |
| Database | PostgreSQL 17, Flyway |
| Documents | Apache PDFBox |
| Testing | JUnit, Spring Boot Test, H2, Playwright |
| Local infrastructure | Docker Compose, PostgreSQL, Mailpit |
| Deployment | Multi-stage Docker build; Render and Neon demo configuration included |

## Repository structure

```text
.
├── backend/                 Spring Boot application, tests, and Flyway migrations
├── frontend/                React applications and Playwright test
├── compose.yaml             Local PostgreSQL and Mailpit services
├── Dockerfile               Combined production-style application image
├── render.yaml              Free-tier demonstration Blueprint
├── DEMO_DEPLOYMENT.md       Demonstration deployment instructions
└── PHOTO_IMPORT.md          Project photo import notes
```

## Local development

### Prerequisites

- Docker with Docker Compose
- Java 17
- Node.js 24 and npm

### 1. Start PostgreSQL and Mailpit

From the repository root:

```bash
docker compose up -d
```

PostgreSQL is available on `localhost:5433`. Mailpit captures development email on SMTP port `1025`, with its inbox at [http://localhost:8025](http://localhost:8025).

### 2. Start the backend

```bash
cd backend
ADMIN_EMAIL='admin@example.com' \
ADMIN_PASSWORD='replace-with-a-long-password1' \
ADMIN_DISPLAY_NAME='Greenstone Admin' \
GOOGLE_PLACES_API_KEY='your-restricted-server-key' \
./gradlew bootRun
```

The API starts on [http://localhost:8080](http://localhost:8080). Flyway validates and applies the database migrations automatically.

The bootstrap administrator is created only when no account with `ADMIN_EMAIL` exists. A password must contain 12–100 characters, including at least one letter and one number. Changing the environment variable later does not reset an existing account.

### 3. Start the frontend

In another terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open [http://localhost:5173](http://localhost:5173). The staff portal is available at [http://localhost:5173/admin/](http://localhost:5173/admin/).

The frontend defaults to `http://localhost:8080` for API calls. To use another backend, copy `frontend/.env.example` to `frontend/.env` and update `VITE_API_BASE_URL`.

### 4. Exercise the main workflow

1. Submit a quotation request through the public site.
2. Confirm that it appears under **Admin → Enquiries**.
3. Create a quote draft, add pricing, save it, and preview the PDF.
4. Send the quote and open the captured message in Mailpit.
5. Follow the secure customer link and accept or decline the quote.
6. For an accepted quote, create a job and progress it through scheduling and completion.
7. Complete the site checklist and walkthrough sign-off, then create an invoice.

## Configuration

Configuration is supplied through environment variables. Never commit production values, local `.env` files, database credentials, SMTP keys, or API keys.

### Database and administrator

| Variable | Default | Production guidance |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port assigned by the hosting platform. |
| `DB_URL` | Local PostgreSQL JDBC URL | Required. Use the managed PostgreSQL JDBC URL with TLS enabled. |
| `DB_USERNAME` | `greenstone` | Required. Use a dedicated least-privilege application user. |
| `DB_PASSWORD` | `greenstone` | Required. Store it in the platform secret manager. |
| `DB_MAX_POOL_SIZE` | `10` | Size for the database plan and application replica count. |
| `ADMIN_EMAIL` | Empty | Used only to create the initial administrator. Supply it together with `ADMIN_PASSWORD`. |
| `ADMIN_PASSWORD` | Empty | Initial administrator password; 12–100 characters with a letter and number. |
| `ADMIN_DISPLAY_NAME` | `Greenstone Admin` | Initial administrator display name. |

After the first administrator is created and access is verified, remove the bootstrap credentials from the runtime environment. Manage subsequent staff accounts through the owner interface.

### URLs, sessions, and uploads

| Variable | Default | Production guidance |
| --- | --- | --- |
| `PUBLIC_BASE_URL` | `http://localhost:8080` | Externally accessible backend origin. |
| `FRONTEND_PUBLIC_BASE_URL` | `http://localhost:5173` | Public frontend origin used in customer quote links. |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Exact allowed browser origin when frontend and API are hosted separately. |
| `ADMIN_SESSION_TIMEOUT` | `8h` | Shorten if required by the organisation's security policy. |
| `SESSION_COOKIE_SECURE` | `false` | Set to `true` behind production HTTPS. |
| `SESSION_COOKIE_SAME_SITE` | `lax` | Keep `lax` for a same-site deployment; reassess before separating origins. |
| `UPLOAD_DIRECTORY` | `./data/uploads` | Must be durable storage in production; container-local storage is insufficient. |

`FRONTEND_PUBLIC_BASE_URL` is security-sensitive because it is used to construct customer response links. Set it to the exact trusted public origin.

### Email and automation

| Variable | Default | Purpose |
| --- | --- | --- |
| `MAIL_HOST` | `localhost` | SMTP server hostname. |
| `MAIL_PORT` | `1025` | SMTP server port. |
| `MAIL_USERNAME` | Empty | SMTP username. |
| `MAIL_PASSWORD` | Empty | SMTP password or provider-issued SMTP key. |
| `MAIL_SMTP_AUTH` | `false` | Enable SMTP authentication. |
| `MAIL_STARTTLS` | `false` | Enable STARTTLS. |
| `MAIL_DELIVERY_ENABLED` | `true` | Master switch for real email delivery. |
| `QUOTE_NOTIFICATION_TO` | `quotes@greenstone.local` | Team recipient for new quotation requests. |
| `QUOTE_NOTIFICATION_FROM` | `no-reply@greenstone.local` | Verified sender address. |
| `NOTIFICATION_AUTOMATION_ENABLED` | `true` | Enables scheduled reminders and digests. |
| `FOLLOW_UP_NOTIFICATION_CRON` | Every minute | Cron expression for due follow-up checks. |
| `DAILY_DIGEST_CRON` | `08:00`, Monday–Friday | Cron expression evaluated in `Pacific/Auckland`. |

Verify the sender domain and configure SPF, DKIM, and DMARC before enabling production delivery. The Render demonstration uses Brevo on port `2525` because its free service blocks common SMTP ports; see [DEMO_DEPLOYMENT.md](DEMO_DEPLOYMENT.md).

### Google Places

| Variable | Default | Purpose |
| --- | --- | --- |
| `GOOGLE_PLACES_API_KEY` | Empty | Restricted server-side Places API key. |
| `GOOGLE_PLACES_PLACE_ID` | Current configured Place ID | Business record used for the public rating summary. |

The key remains on the backend and must not be added to a `VITE_` variable or frontend source. Restrict it by API and deployment environment. Confirm that the Place ID belongs to the authoritative business profile, especially if Google merges or duplicates listings.

## Testing

Run the backend test suite:

```bash
cd backend
./gradlew test
```

Run frontend static checks and the production build:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

Run the isolated full-stack browser test:

```bash
cd frontend
npx playwright install chromium
npm run test:e2e
```

The Playwright test starts Spring Boot on port `18080`, Vite on port `15173`, and a fresh H2 database. It submits a public quotation request, signs into the staff portal, and verifies that the same enquiry appears in the administration inbox. It does not modify the development PostgreSQL database or send email through Mailpit.

Use `npm run test:e2e:headed` to observe the browser journey locally.

## Container build and deployment

Build the combined application image from the repository root:

```bash
docker build -t greenstone-painting:local .
```

The multi-stage build:

1. installs frontend dependencies with `npm ci` and builds the Vite applications;
2. copies the frontend output into Spring Boot static resources;
3. builds the executable application JAR; and
4. runs it as a non-root user on a Java 17 JRE image.

The repository includes `render.yaml` for the temporary Render and Neon demonstration. Follow [DEMO_DEPLOYMENT.md](DEMO_DEPLOYMENT.md) for that environment. The demo is intentionally independent of `greenstonepainting.co.nz` and does not require a DNS change.

For production, deploy the container behind HTTPS with a managed PostgreSQL database, durable object storage, centralised secrets, log collection, alerting, and tested backups. Do not promote the free-tier demonstration service directly to production.

## Operations

### Health check

```http
GET /api/health
```

Expected response:

```json
{"status":"ok"}
```

The health endpoint confirms that the web application is responding. It is not a full dependency-readiness check and does not prove that email, Google Places, uploaded files, or every database workflow is healthy.

### Database migrations

Flyway migrations under `backend/src/main/resources/db/migration` run at startup. Hibernate uses `ddl-auto=validate`; it validates mappings but does not modify the schema independently.

Before a production deployment:

1. back up the database and verify restore access;
2. test the application and migrations against a production-like copy;
3. ensure only one controlled migration process runs during rollout; and
4. prepare an application rollback plan that is compatible with the migrated schema.

### Uploaded files

Development uploads are written to `backend/data/uploads`. The container defaults to `/tmp/greenstone-uploads`, which is ephemeral on the demonstration platform. Database records can therefore outlive their files after a restart or redeploy.

Production use requires managed object storage or a durable mounted volume, retention rules, access controls, backup, malware/file validation appropriate to the risk, and a tested deletion process. Until that work is complete, do not use the demonstration deployment to collect real customer photographs.

### Email behaviour

The enquiry is stored before its team notification is attempted. If email delivery fails, the saved request remains available in the administration inbox and the notification can be retried. Delivery failures for workflow notifications are retried up to three times and recorded in the relevant activity history.

## Security

Implemented controls include:

- BCrypt password hashing and a 12–100 character password policy.
- HTTP-only authenticated sessions with session-ID rotation at login.
- CSRF protection on administration operations.
- Role checks for administrator and owner-only actions.
- Secure-cookie configuration for HTTPS deployments.
- Backend-only API and SMTP credentials.
- Random customer quote-response tokens stored only as SHA-256 hashes.
- Ninety-day expiry for customer quote-response links.
- Validation and size limits for public uploads.

These controls are not a substitute for a production security assessment. Public quotation and customer-response routes are intentionally unauthenticated and require abuse protection at the application or edge layer. Before launch, complete threat modelling, dependency and container scanning, rate limiting, logging and alerting, access review, penetration testing, and an incident-response plan.

Rotate any credential that has appeared in chat, screenshots, terminal output, source history, or shared documentation. Never reuse demonstration credentials in production.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
