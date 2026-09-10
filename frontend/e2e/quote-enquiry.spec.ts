import { expect, test } from '@playwright/test'

test('submits a public quote request and exposes it to authenticated staff', async ({ page }) => {
  const suffix = Date.now().toString().slice(-8)
  const firstName = 'E2E'
  const lastName = `Customer ${suffix}`
  const fullName = `${firstName} ${lastName}`
  const email = `e2e.${suffix}@example.test`

  await page.goto('/#quote')

  await page.getByLabel('First name *').fill(firstName)
  await page.getByLabel('Last name *').fill(lastName)
  await page.getByLabel('Email *').fill(email)
  await page.getByLabel('Phone *').fill('021 555 0101')
  await page.getByLabel('Property location *').fill('29 Lachlan Drive, Dinsdale')
  await page.getByLabel('Service required *').selectOption('interior-painting')
  await page.getByLabel('Project description *').fill('End-to-end test request for an interior repaint.')

  const submitButton = page.getByRole('button', { name: 'Submit Quote Request' })
  await expect(submitButton).toBeDisabled()
  await page.locator('#quote-privacy').check()
  await expect(submitButton).toBeEnabled()
  await submitButton.click()

  await expect(page.getByRole('status')).toContainText('Thanks—your request has been received.')

  await page.goto('/admin/')
  await page.getByLabel('Email address').fill('e2e.admin@greenstone.test')
  await page.getByLabel('Password').fill('E2eIntegration!2026')
  await page.getByRole('button', { name: 'Sign in securely' }).click()

  await expect(page.getByRole('heading', { name: 'Today across Greenstone.' })).toBeVisible()
  await page.getByRole('button', { name: 'Enquiries' }).click()
  await expect(page.getByRole('heading', { name: 'Quote request inbox' })).toBeVisible()

  await page.getByPlaceholder('Name, email, phone, address or reference').fill(email)
  await page.getByRole('button', { name: 'Apply filters' }).click()

  const enquiryResult = page.getByRole('button', { name: new RegExp(`^${fullName}`) }).first()
  await expect(enquiryResult).toBeVisible()
  await expect(enquiryResult).toContainText(email)
})
