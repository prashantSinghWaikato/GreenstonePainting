# Greenstone Painting Website

Production marketing website for Greenstone Painting Limited in Hamilton, New Zealand. The site is a static React application deployed on Netlify.

## Features

- Responsive service, project, company, service-area, article, contact, and privacy pages.
- Local project, service, article, colour-palette, and customer-review content.
- Interactive colour preview and guided project assistant.
- Quote requests handled by Netlify Forms with honeypot spam protection.
- No administration portal, database, Java backend, Google Places API, or browser-exposed mail credentials.

## Technology

- React 19
- TypeScript
- Vite 8
- Netlify static hosting and Forms

## Local development

Requirements: Node.js 24 and npm.

```bash
cd frontend
npm ci
npm run dev
```

The local Vite server displays the website, but successful quote submission requires a deployed Netlify site because form handling is provided by Netlify.

## Verification

```bash
cd frontend
npm run lint
npm run build
```

The production output is generated in `frontend/dist`.

## Netlify deployment

The root `netlify.toml` defines:

- Base directory: `frontend`
- Build command: `npm ci && npm run build`
- Publish directory: `frontend/dist`
- Node.js version: 24
- Basic production security and cache headers

After the first deploy:

1. Open **Forms** in Netlify and confirm `quote-request` was detected.
2. Add an email notification for the form. Netlify may require the recipient to be an active team member.
3. Submit a real test enquiry and verify both the Forms dashboard and email notification.
4. Add `greenstonepainting.co.nz` and `www.greenstonepainting.co.nz` as custom domains.
5. Update Cloudflare DNS only after the Netlify preview has been approved.

## Updating content

- Services, featured projects, service areas, and homepage articles: `frontend/src/data/site.ts`
- Full article content: `frontend/src/data/blog.ts`
- Project gallery: `frontend/src/data/projectPhotos.ts`
- Colour palette: `frontend/src/data/colours.ts`
- Customer reviews: `frontend/src/data/reviews.ts`

Only publish customer reviews and photographs that the business is authorised to use.

## Privacy

Quote form submissions contain customer contact and property information. They are transmitted to and stored by Netlify Forms. Access to Netlify must use strong authentication and should be limited to authorised business personnel. Configure an appropriate retention process and delete submissions when they are no longer required.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
