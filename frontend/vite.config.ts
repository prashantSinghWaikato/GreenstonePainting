import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { fileURLToPath } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    rollupOptions: {
      input: {
        home: fileURLToPath(new URL('./index.html', import.meta.url)),
        services: fileURLToPath(new URL('./services/index.html', import.meta.url)),
        projects: fileURLToPath(new URL('./projects/index.html', import.meta.url)),
        blog: fileURLToPath(new URL('./blog/index.html', import.meta.url)),
        preparation: fileURLToPath(new URL('./how-painters-prepare-your-home-for-a-smooth-paint-job/index.html', import.meta.url)),
        compliance: fileURLToPath(new URL('./822-2/index.html', import.meta.url)),
        staining: fileURLToPath(new URL('./wood-staining-benefits-you-need-to-take-advantage-of/index.html', import.meta.url)),
      },
    },
  },
})
