// @ts-check
import { test, expect } from '@playwright/test';
import crypto from 'node:crypto';

test('statistics rule should retain field', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  const title = `AAA - ${crypto.randomUUID()}`
  await page.getByLabel('Username').fill('admin');
  await page.getByLabel('Password').fill('admin');
  await page.getByLabel('Sign in').click();
  //await page.getByLabel('Cancel').click();

  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'Statistics' }).click();
  await page.locator('#title').fill(title);

  // this is a test spell, should have a better way of selecting, ideally byRole (see: https://playwright.dev/docs/locators)
  await page.locator('div:nth-child(9) > .col-md-10 > div > span > div > .css-b62m3t-container > .common-select-control > .css-1wy0on6 > div > .sc-hKizoo').first().click();
  await page.getByRole('option', { name: 'standard deviation' }).click();

  await page.locator('#react-select-9-input').fill('source');
  await page.getByRole('option', { name: 'source – string' }).click();
  await page.getByRole('button', { name: 'Save' }).click();

  await page.getByPlaceholder('Filter alert rules').fill(title);
  await expect(page.getByRole('link', { name: 'Edit' })).toHaveCount(1);
  await page.getByRole('link', { name: 'Edit' }).click();
  await expect(page.getByText('source – string')).toBeVisible();
});
