import { defineConfig, devices } from '@playwright/test'

const adminEmail = 'e2e.admin@greenstone.test'
const adminPassword = 'E2eIntegration!2026'

export default defineConfig({
  testDir: './e2e',
  timeout: 45_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  workers: 1,
  reporter: [['list']],
  use: {
    baseURL: 'http://127.0.0.1:15173',
    trace: 'retain-on-failure',
    ...devices['Desktop Chrome'],
  },
  webServer: [
    {
      command: '../backend/gradlew -p ../backend e2eBootRun',
      url: 'http://127.0.0.1:18080/api/services',
      timeout: 120_000,
      reuseExistingServer: false,
      env: {
        ADMIN_EMAIL: adminEmail,
        ADMIN_PASSWORD: adminPassword,
        ADMIN_DISPLAY_NAME: 'E2E Admin',
      },
    },
    {
      command: 'npm run dev -- --host 127.0.0.1 --port 15173',
      url: 'http://127.0.0.1:15173',
      timeout: 60_000,
      reuseExistingServer: false,
      env: {
        VITE_API_BASE_URL: 'http://127.0.0.1:18080',
      },
    },
  ],
})
