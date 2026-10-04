# Free-tier demo deployment

This deployment is intentionally separate from `greenstonepainting.co.nz`. It packages the Vite frontend and Spring Boot API into one Render web service and uses a Neon PostgreSQL database. No DNS changes are required.

## Free-tier behaviour

- Render assigns a temporary `https://...onrender.com` URL and may put the service to sleep after it is idle.
- The first request after sleep can take about a minute.
- The free Render filesystem is ephemeral. Uploaded enquiry, project, article, service, and job images can disappear after a sleep, restart, or redeploy. The database records remain in Neon, but the corresponding uploaded files will no longer be available.
- Images and the roof video committed under `frontend/public` are built into the container and remain available.
- Automated notifications and email delivery are disabled for the demo. Public enquiries are still completed and retained in the admin inbox. Sending a quote email requires SMTP settings; see **Optional email testing**.
- Do not collect real customer data in this demonstration environment.

## 1. Prepare the repository

Run the checks locally and push the deployment commit to GitHub:

```bash
cd frontend
npm ci
npm run lint
npm run build

cd ../backend
./gradlew test
```

The repository root contains the `Dockerfile` and `render.yaml` used by Render.

## 2. Create the free Neon database

1. Create a Neon account and a new PostgreSQL project.
2. Use Neon's direct connection hostname. This demo runs one application instance with a three-connection pool, so it does not need the pooled endpoint.
3. Keep the database name, username, password, and hostname available for Render.
4. Convert the Neon connection details into the values below. Do not commit them.

Example only:

```text
DB_URL=jdbc:postgresql://ep-example.ap-southeast-1.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=the-password-from-neon
```

Flyway creates and seeds the database automatically when the application first starts.

## 3. Create the Render service

1. Sign in to Render and choose **New → Blueprint**.
2. Connect the GitHub repository.
3. Select the repository's `render.yaml`.
4. Keep the service on the **Free** plan.
5. Supply the requested secret variables:

| Variable | Value |
| --- | --- |
| `DB_URL` | Neon JDBC URL described above |
| `DB_USERNAME` | Neon database user |
| `DB_PASSWORD` | Neon database password |
| `ADMIN_EMAIL` | Demo administrator email |
| `ADMIN_PASSWORD` | A new password of at least 12 characters, used only for this demo |
| `GOOGLE_PLACES_API_KEY` | A new, restricted server key; do not reuse the key shared in chat |
| `PUBLIC_BASE_URL` | The final Render URL, for example `https://greenstone-painting-demo.onrender.com` |
| `FRONTEND_PUBLIC_BASE_URL` | The same final Render URL |

The database connection pool is limited to three connections for the free database. Secure cookies are enabled and notification schedules are disabled by `render.yaml`.

If Render changes the service name because it is already taken, update both public URL variables after the first deployment and deploy again.

## 4. Verify the demo

Use the Render URL to check:

1. `/api/health` returns `{"status":"ok"}`.
2. The home page loads its images and roof video.
3. Services, projects, blog, contact, privacy, and article URLs open directly and after refresh.
4. `/admin/` accepts the demo administrator credentials.
5. Admin colours can be added, reordered, edited, and removed, and the public colour studio updates.
6. `/api/reviews` returns the current Google rating and review count.
7. A public enquiry completes successfully and is visible in the admin inbox without sending an email.

## Optional email testing

Render free web services block outbound SMTP ports 25, 465, and 587. Brevo supports port 2525 and can be configured with:

```text
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=2525
MAIL_USERNAME=the-SMTP-login-shown-by-Brevo
MAIL_PASSWORD=a-Brevo-SMTP-key
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
MAIL_DELIVERY_ENABLED=true
QUOTE_NOTIFICATION_TO=demo-recipient@example.com
QUOTE_NOTIFICATION_FROM=verified-sender@example.com
```

Keep `NOTIFICATION_AUTOMATION_ENABLED=false` until direct enquiry and quote emails have been tested. The From address must be a sender verified in Brevo. If another provider only offers the blocked SMTP ports, an HTTPS email API integration is required instead.

## Removing the demo

Delete the Render web service and the Neon project. Since the real domain is never connected, removing the demo has no effect on the existing public website.
