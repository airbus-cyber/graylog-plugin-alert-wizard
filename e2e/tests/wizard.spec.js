// @ts-check
import { test, expect } from '@playwright/test';
import crypto from 'node:crypto';

test('statistics rule should retain field', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await _login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'Statistics' }).click();
  await page.locator('#title').fill(title);

  // this is a test spell, should have a better way of selecting, ideally byRole (see: https://playwright.dev/docs/locators)
  await page.locator('div:nth-child(9) > .col-md-10 > div > span > div > .css-b62m3t-container > .common-select-control > .css-1wy0on6 > div > .sc-hKizoo').first().click();
  await page.getByRole('option', { name: 'standard deviation' }).click();

  await page.locator('#react-select-9-input').fill('source');
  await page.getByRole('option', { name: 'source – string' }).click();
  await page.getByRole('button', { name: 'Save' }).click();
  //await page.waitForTimeout(500);

  // Go on search page
  await _open_alert_page_and_filter(page, title);

  await expect(page.getByRole('link', { name: 'Edit' })).toHaveCount(1);
  await page.getByRole('link', { name: 'Edit' }).click();
  await expect(page.getByText('source – string')).toBeVisible();
});


test('go_on_search_page_when_click_on_search_button', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await _login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.locator('#title').fill(title);

  // Add Field Condition
  await _fill_field_condition(page, 'message', 'matches exactly', 'abc');

  // Fill Search Query
  const searchQuery = 'a?c';
  await page.locator('#search_query').fill(searchQuery);
  await page.waitForTimeout(200);

  // Save
  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await _open_alert_page_and_filter(page, title);
  await page.getByRole('link', { name: 'play_arrow' }).click();

  await expect(page.getByText(title)).toBeVisible();
  await expect(page.getByText(searchQuery)).toBeVisible();
});


test('disable/enable rule without stream should work', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await _login_steps(page);

  // Create Rule without stream
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.locator('#title').fill(title);
  await page.getByRole('button', { name: 'Save' }).click();

  // Filter Rule
  await _open_alert_page_and_filter(page, title);

  // Disable Rule
  await page.getByTitle('Select entity').click();
  await page.waitForTimeout(500);
  await page.getByRole('button', { name: 'Bulk actions arrow_drop_down' }).click();
  await page.waitForTimeout(500);
  await page.getByRole('menuitem', { name: 'Disable' }).click();
  await page.waitForTimeout(500);
  await page.getByLabel('Confirm').click();
  await page.waitForTimeout(500);

  await expect(page.getByText('Disabled')).toBeVisible();
  await expect(page.getByText('Disabled')).toHaveCSS('background-color', 'rgb(255, 165, 0)');

  // Enable Rule
  await page.getByTitle('Select entity').click();
  await page.waitForTimeout(500);
  await page.getByRole('button', { name: 'Bulk actions arrow_drop_down' }).click();
  await page.waitForTimeout(500);
  await page.getByRole('menuitem', { name: 'Enable' }).click();
  await page.waitForTimeout(500);
  await page.getByLabel('Confirm').click();
  await page.waitForTimeout(500);

  await expect(page.getByText('Enable')).toBeVisible();
});


test('disable stream#2 should disable rule', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await _login_steps(page);

  // Create Rule with 2 streams
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'OR' }).click();
  await page.locator('#title').fill(title);

  await _fill_field_condition(page, 'message', 'matches exactly', 'abc');

  await page.getByRole('button', { name: 'add_circle' }).nth(1).click();
  await page.waitForTimeout(200);
  await page.locator('#field-input').nth(1).fill('message');
  await page.waitForTimeout(200);
  await page.getByText('arrow_drop_down').nth(4).click();
  await page.getByRole('option', { name: 'matches exactly' }).click();
  await page.locator('#value').nth(1).fill('cba');
  await page.waitForTimeout(200);

  await page.getByRole('button', { name: 'Save' }).click();

  // Disable stream#2
  await page.goto(`/streams?page=1&query=${title}%232`);
  await page.waitForTimeout(200);
  page.on('dialog', dialog => dialog.accept());
  await page.getByLabel('Pause stream').click();
  await page.waitForTimeout(200);

  // Check Rule is disabled
  await _open_alert_page_and_filter(page, title);

  await expect(page.getByText('Disabled')).toBeVisible();
  await expect(page.getByText('Disabled')).toHaveCSS('background-color', 'rgb(255, 165, 0)');
});

async function _login_steps(page) {
  await page.getByLabel('Username').fill('admin');
  await page.getByLabel('Password').fill('admin');
  await page.getByLabel('Sign in').click();
}

async function _open_alert_page_and_filter(page, filter) {
  await page.goto('/wizard/AlertRules');
  // Wait for rules are loaded
  await page.waitForTimeout(1000);
  await page.getByPlaceholder('Filter alert rules').fill(filter);
  // Wait for filter is applied
  await page.waitForTimeout(500);
}

async function _fill_field_condition(page, input, option, value) {
  await page.getByRole('button', { name: 'add_circle' }).first().click();
  await page.waitForTimeout(200);
  await page.locator('#field-input').fill(input);
  await page.waitForTimeout(200);
  await page.getByText('arrow_drop_down').nth(2).click();
  await page.getByRole('option', { name: option }).click();
  await page.locator('#value').fill(value);
  await page.waitForTimeout(200);
}