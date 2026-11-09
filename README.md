# Greenstone Painting Public Website

Greenstone Painting is a public marketing and quotation website for a Hamilton painting business. It presents services, projects, company information, service areas, articles, contact details, a colour studio, and a structured quotation form.

<<<<<<< Updated upstream
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
=======
The repository contains no administration portal or Google Places integration. Approved customer reviews are stored as local frontend content.
>>>>>>> Stashed changes

## Capabilities

- Responsive public pages for services, projects, company information, service areas, articles, contact details, and privacy.
- Database-backed public services, projects, articles, and colour-palette entries.
- Structured quotation requests with validation and customer reference numbers.
- SMTP notification to the configured team address when a request is submitted.
- Locally managed customer reviews with no third-party review API or API key.

## Architecture

```text
Browser
  └─ React public website
          │
          ▼
Spring Boot REST application
  ├─ Public content endpoints
  ├─ Quotation validation and email delivery
  └─ Spring Data JPA
          │
          ▼
PostgreSQL and Flyway migrations
```

The multi-stage Docker image builds the Vite frontend, copies the generated assets into Spring Boot, and serves the website and API from one service.

## Technology stack

| Area | Technology |
| --- | --- |
| Frontend | React 19, TypeScript, Vite 8 |
| Backend | Java 17, Spring Boot 4, Spring Data JPA |
| Database | PostgreSQL 17, Flyway |
| Email | Spring Mail and SMTP |
| Testing | JUnit, Spring Boot Test, H2, ESLint |
| Local infrastructure | Docker Compose, PostgreSQL, Mailpit |

## Local development

Prerequisites: Docker, Java 17, Node.js 24, and npm.

Start PostgreSQL and Mailpit:

```bash
docker compose up -d
```

Start the backend:

```bash
cd backend
./gradlew bootRun
```

Start the frontend in another terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open <http://localhost:5173>. Mailpit is available at <http://localhost:8025>.

## Customer reviews

Customer reviews are defined in `frontend/src/data/reviews.ts`:

```ts
export const customerReviews = [
  {
    author: 'Customer name',
    rating: 5,
    text: 'Approved review text.',
    source: 'Google review',
  },
]
```

Only add reviews that the business is authorised to publish. The reviews section remains hidden while the array is empty. A normal Google search link may be shown to help visitors find the original public listing, but the application does not call the Google Places API.

## Configuration

Never commit production credentials or local `.env` files.

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | Application HTTP port. |
| `DB_URL` | Local PostgreSQL JDBC URL | PostgreSQL connection URL. |
| `DB_USERNAME` | `greenstone` | Database username. |
| `DB_PASSWORD` | `greenstone` | Database password. |
| `DB_MAX_POOL_SIZE` | `10` | Maximum database connection-pool size. |
| `FRONTEND_ORIGIN` | `http://localhost:5173` | Allowed origin when Vite and the backend run separately. |
| `CONTENT_DIRECTORY` | `./data/uploads` | Read-only location for any legacy database-backed public images. |
| `MAIL_HOST` | `localhost` | SMTP host. |
| `MAIL_PORT` | `1025` | SMTP port. |
| `MAIL_USERNAME` | Empty | SMTP username. |
| `MAIL_PASSWORD` | Empty | SMTP password or SMTP key. |
| `MAIL_SMTP_AUTH` | `false` | Enable SMTP authentication. |
| `MAIL_STARTTLS` | `false` | Enable STARTTLS. |
| `MAIL_DELIVERY_ENABLED` | `true` | Enable or disable real email delivery. |
| `QUOTE_NOTIFICATION_TO` | `quotes@greenstone.local` | Recipient for quotation requests. |
| `QUOTE_NOTIFICATION_FROM` | `no-reply@greenstone.local` | Verified sender address. |
| `VITE_API_BASE_URL` | `http://localhost:8080` | Frontend API base URL during separate local development. |

## Verification

Backend tests:

```bash
cd backend
./gradlew test
```

Frontend lint and production build:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

## Container build

```bash
docker build -t greenstone-painting:local .
docker run --rm -p 8080:8080 --env-file .env greenstone-painting:local
```

The health endpoint is `GET /api/health` and returns `{"status":"ok"}`.

## Deployment notes

The included `render.yaml` deploys the combined public website and backend. The deployment still needs PostgreSQL because public content and quotation requests are stored in the database.

Production use requires HTTPS, managed secrets, database backups with restore testing, rate limiting for the public form, monitoring, and a verified email sender domain.

The Flyway directory retains historical migrations from earlier development versions so existing databases continue to validate. The removed administration application is no longer compiled, routed, deployed, or represented by runtime Java entities.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
