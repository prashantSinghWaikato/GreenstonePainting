# GreenstonePainting

The goal of this project is to design and develop an enterprise-level digital platform for Greenstone Painting that improves its online presence, customer experience, and business operations.

## Run locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Start the Spring Boot API:

```bash
cd backend
./gradlew bootRun
```

Start the React frontend in another terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend uses `http://localhost:8080` as its default API URL. Copy `frontend/.env.example` to `frontend/.env` when a different API URL is required.

## Quote enquiry API

`POST /api/enquiries` validates and stores quote requests. Service offerings are created by the Flyway seed migration so frontend service slugs resolve consistently.

Each enquiry response includes a short-lived upload token. The frontend uses it with `POST /api/enquiries/{enquiryId}/attachments` to attach up to four validated project photos of no more than 5 MB each. Local uploads are stored under `backend/data/uploads`; set `UPLOAD_DIRECTORY` to use another local path.
