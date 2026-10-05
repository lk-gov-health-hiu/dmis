import { test as setup, expect } from '@playwright/test';

setup('authenticate', async ({ page }) => {
  await page.goto('');
  await page.locator('input[id$="username"]').fill(process.env.DMIS_TEST_USER!);
  await page.locator('input[id$="password"]').fill(process.env.DMIS_TEST_PASS!);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.locator('input[id$="password"]')).toHaveCount(0);
  await page.context().storageState({ path: '.auth/user.json' });
});
