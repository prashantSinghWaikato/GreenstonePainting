# GreenstonePainting

The goal of this project is to design and develop an enterprise-level digital platform for Greenstone Painting that improves its online presence, customer experience, and business operations.

## Run locally

Start PostgreSQL and the local email inbox:

```bash
docker compose up -d
```

Start the Spring Boot API:

```bash
cd backend
ADMIN_EMAIL=office@example.com ADMIN_PASSWORD='use-at-least-12-characters' ./gradlew bootRun
```

The admin account is created on the first backend start and can be used at [http://localhost:5173/admin/](http://localhost:5173/admin/). The password is stored as a BCrypt hash and is never written to source control. If the account already exists, changing the environment variable does not silently reset its password.

Start the React frontend in another terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend uses `http://localhost:8080` as its default API URL. Copy `frontend/.env.example` to `frontend/.env` when a different API URL is required.

Development quote notifications are captured locally by Mailpit at [http://localhost:8025](http://localhost:8025). They are not delivered to a real recipient. Production email delivery requires explicit `MAIL_*`, `QUOTE_NOTIFICATION_TO`, `QUOTE_NOTIFICATION_FROM`, and `PUBLIC_BASE_URL` environment settings.

## Quote enquiry API

`POST /api/enquiries` validates and stores quote requests. Service offerings are created by the Flyway seed migration so frontend service slugs resolve consistently.

Each enquiry response includes a short-lived upload token. The frontend uses it with `POST /api/enquiries/{enquiryId}/attachments` to attach up to four validated project photos of no more than 5 MB each. Local uploads are stored under `backend/data/uploads`; set `UPLOAD_DIRECTORY` to use another local path.

After uploads finish, `POST /api/enquiries/{enquiryId}/complete` sends one idempotent team notification. Project photos are shared through separate 30-day review links rather than large email attachments.
