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
  await page.getByRole('button', { name: 'play_arrow' }).click();

  // Wait new tab
  await page.waitForTimeout(200);
  let pages = page.context().pages();
  await expect(pages[1].getByText(title)).toBeVisible();
  await expect(pages[1].getByText(searchQuery)).toBeVisible();
});

test('open_two_tabs_when_click_on_search_button', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'OR' }).click();
  await page.locator('#title').fill(title);

  // Add 1st Field Condition
  await fill_field_condition(page, 'message', 'matches exactly', 'abc');

  // Fill 1st Search Query
  const searchQuery = 'a?c';
  await page.locator('#search_query').first().fill(searchQuery);
  await page.waitForTimeout(200);

  // Add 2nd Field Condition
  await fill_field_condition(page, 'message', 'matches exactly', 'abc', 1);

  // Fill 2nd Search Query
  const searchQuery2 = 'b?d';
  await page.locator('#search_query').nth(1).fill(searchQuery2);
  await page.waitForTimeout(200);

  // Save
  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await open_alert_page_and_filter(page, title);
  await page.getByRole('button', { name: 'play_arrow' }).click();

  // Wait new tabs
  await page.waitForTimeout(2000);
  let pages = page.context().pages();
  expect(pages.length).toBe(3);
  if (await pages[1].getByText(title + '#2').isVisible()) {
    await expect(pages[2].getByText(title)).toBeVisible();
    await expect(pages[2].getByText(searchQuery)).toBeVisible();

    await expect(pages[1].getByText(title + '#2')).toBeVisible();
    await expect(pages[1].getByText(searchQuery2)).toBeVisible();
  } else {
    await expect(pages[1].getByText(title)).toBeVisible();
    await expect(pages[1].getByText(searchQuery)).toBeVisible();

    await expect(pages[2].getByText(title + '#2')).toBeVisible();
    await expect(pages[2].getByText(searchQuery2)).toBeVisible();
  }
});


test('open_two_tabs_when_click_on_search_button_when_second_stream_condition_is_empty_#156', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'OR' }).click();
  await page.locator('#title').fill(title);

  // Add 1st Field Condition
  await fill_field_condition(page, 'message', 'matches exactly', 'abc');

  // Fill 1st Search Query
  const searchQuery = 'a?c';
  await page.locator('#search_query').first().fill(searchQuery);
  await page.waitForTimeout(200);

  // Fill 2nd Search Query
  const searchQuery2 = 'b?d';
  await page.locator('#search_query').nth(1).fill(searchQuery2);
  await page.waitForTimeout(200);

  // Save
  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await open_alert_page_and_filter(page, title);
  await page.getByRole('button', { name: 'play_arrow' }).click();

  // Wait new tabs
  await page.waitForTimeout(2000);
  let pages = page.context().pages();
  expect(pages.length).toBe(3);
  if (await pages[1].getByText(title + '#2').isVisible()) {
    await expect(pages[2].getByText(title)).toBeVisible();
    await expect(pages[2].getByText(searchQuery)).toBeVisible();

    await expect(pages[1].getByText(searchQuery2)).toBeVisible();
  } else {
    await expect(pages[1].getByText(title)).toBeVisible();
    await expect(pages[1].getByText(searchQuery)).toBeVisible();

    await expect(pages[2].getByText(searchQuery2)).toBeVisible();
  }
});

test('OR rule should contains GroupBy field', async ({ page }) => {
  await page.goto('/wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'OR' }).click();
  await page.locator('#title').fill(title);

  await expect(page.getByText('Group by Condition')).toBeVisible();
});

test('transmit query from search to new wizard alert - #142', async ({ page }) => {
  await page.goto('/search?q=source%3A+test');

  await login_steps(page);

  await page.getByLabel('Open search actions dropdown').click();
  await page.getByRole('menuitem', { name: 'Create wizard alert rule' }).click();

  await expect(page.locator('#search_query')).toHaveValue('source: test');
});
