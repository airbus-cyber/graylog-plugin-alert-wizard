// @ts-check
import { test, expect } from '@playwright/test';
import crypto from 'node:crypto';
import { login_steps, fill_field_condition, open_alert_page_and_filter } from './test-utils.js';

test('statistics rule should retain field', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'Statistics' }).click();
  await page.locator('#title').fill(title);

  await page.getByText('Select...arrow_drop_down').first().click();
  await page.getByRole('option', { name: 'standard deviation' }).click();

  await page.locator('#react-select-9-input').fill('source');
  await page.getByRole('option', { name: 'source – string' }).click();
  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await open_alert_page_and_filter(page, title);

  await expect(page.getByRole('link', { name: 'Edit' })).toHaveCount(1);
  await page.getByRole('link', { name: 'Edit' }).click();
  await expect(page.getByText('source – string')).toBeVisible();
});


test('go_on_search_page_when_click_on_search_button', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.locator('#title').fill(title);

  // Add Field Condition
  await fill_field_condition(page, 'message', 'matches exactly', 'abc');

  // Fill Search Query
  const searchQuery = 'a?c';
  await page.locator('#search_query').fill(searchQuery);
  await page.waitForTimeout(200);

  // Save
  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await open_alert_page_and_filter(page, title);
  await page.getByRole('link', { name: 'play_arrow' }).click();

  await expect(page.getByText(title)).toBeVisible();
  await expect(page.getByText(searchQuery)).toBeVisible();
});
