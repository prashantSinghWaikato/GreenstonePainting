# Public Website Demo Deployment

This deployment runs the public Greenstone Painting website, quotation API, PostgreSQL persistence, and optional SMTP notification. It does not deploy an administration portal, accept customer file uploads, or use Google Places.

## Required Render environment variables

| Variable | Value |
| --- | --- |
| `DB_URL` | Neon pooled JDBC URL |
| `DB_USERNAME` | Neon database username |
| `DB_PASSWORD` | Neon database password |
| `QUOTE_NOTIFICATION_TO` | Address that receives completed quote requests |
| `QUOTE_NOTIFICATION_FROM` | Verified sender address |
| `MAIL_HOST` | SMTP provider hostname |
| `MAIL_PORT` | Provider port; Brevo supports `2525` |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP key or password |
| `MAIL_SMTP_AUTH` | `true` for authenticated SMTP |
| `MAIL_STARTTLS` | `true` when required by the provider |
| `MAIL_DELIVERY_ENABLED` | `true` to deliver email, otherwise `false` |

## Verification

1. Open `/api/health` and confirm `{"status":"ok"}`.
2. Open the public home page and check the service, project, article, and colour content.
3. Submit a quotation request and confirm the success reference.
4. Confirm the configured recipient receives the quotation email.
5. Confirm `/admin/`, `/quote/`, `/api/reviews`, and quotation attachment routes are not application routes.

Before using the service for real customers, configure rate limiting, monitoring, and backup/restore procedures.
