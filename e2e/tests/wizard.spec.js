// @ts-check
import { test, expect } from '@playwright/test';
import crypto from 'node:crypto';
import { login_steps, fill_field_condition, open_alert_page_and_filter } from './test-utils.js';

const rules_button = ['Count', 'Group / Distinct', 'Statistics', 'THEN', 'AND', 'OR'];

test('follow menu link should works #161', async ({ page }) => {
  await page.goto('welcome');

  await login_steps(page);

  await page.getByRole('button', { name: 'Wizard' }).click();
  await page.getByRole('menuitem', { name: 'Alert Rules' }).click();
  await page.waitForTimeout(200);

  await expect(page.getByRole('heading', { name: 'Alert Rules' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Create' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Import' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Export' })).toBeVisible();
});


test('statistics rule should retain field', async ({ page }) => {
  await page.goto('wizard/AlertRules');

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
  await page.goto('wizard/AlertRules');

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
  await page.goto('wizard/AlertRules');

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
  await page.locator('#search_query').fill(searchQuery);
  await page.waitForTimeout(200);

  // Add 2nd Field Condition
  await fill_field_condition(page, 'message', 'matches exactly', 'abc', 1);

  // Fill 2nd Search Query
  const searchQuery2 = 'b?d';
  await page.locator('#additional_search_query').fill(searchQuery2);
  await page.waitForTimeout(200);

  // Save
  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await open_alert_page_and_filter(page, title);
  await page.getByRole('button', { name: 'play_arrow' }).click();

  // Wait new tabs
  await page.waitForTimeout(2000);
  let pages = page.context().pages();
  await expect(pages.length).toBe(3);

  const url = pages[1].url();
  const searchInPageOne = url.includes("q=a%3Fc");

  if (searchInPageOne) {
    await expect(pages[1].getByText(title)).toBeVisible();
    await expect(pages[1].getByText(searchQuery)).toBeVisible();

    await expect(pages[2].getByText(title + '#2')).toBeVisible();
    await expect(pages[2].getByText(searchQuery2)).toBeVisible();
  } else {
    await expect(pages[2].getByText(title)).toBeVisible();
    await expect(pages[2].getByText(searchQuery)).toBeVisible();

    await expect(pages[1].getByText(title + '#2')).toBeVisible();
    await expect(pages[1].getByText(searchQuery2)).toBeVisible();
  }
});


test('open_two_tabs_when_click_on_search_button_when_second_stream_condition_is_empty_#156', async ({ page }) => {
  await page.goto('wizard/AlertRules');

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
  await page.locator('#search_query').fill(searchQuery);
  await page.waitForTimeout(200);

  // Fill 2nd Search Query
  const searchQuery2 = 'b?d';
  await page.locator('#additional_search_query').fill(searchQuery2);
  await page.waitForTimeout(200);

  // Save
  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await open_alert_page_and_filter(page, title);
  await page.getByRole('button', { name: 'play_arrow' }).click();

  // Wait new tabs
  await page.waitForTimeout(2000);
  let pages = page.context().pages();
  await expect(pages.length).toBe(3);

  const url = pages[1].url();
  const searchInPageOne = url.includes("q=a%3Fc");

  if (searchInPageOne) {
    await expect(pages[1].getByText(title)).toBeVisible();
    await expect(pages[1].getByText(searchQuery)).toBeVisible();

    await expect(pages[2].getByText(searchQuery2)).toBeVisible();
  } else {
    await expect(pages[2].getByText(title)).toBeVisible();
    await expect(pages[2].getByText(searchQuery)).toBeVisible();

    await expect(pages[1].getByText(searchQuery2)).toBeVisible();
  }
});

test('OR rule should contains GroupBy field', async ({ page }) => {
  await page.goto('wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.getByRole('button', { name: 'OR' }).click();
  await page.locator('#title').fill(title);

  await expect(page.getByText('Group by Condition')).toBeVisible();
});

test('transmit query from search to new wizard alert - #142', async ({ page }) => {
  await page.goto('search?q=source%3A+test');

  await login_steps(page);

  await page.getByLabel('Open search actions dropdown').click();
  await page.getByRole('menuitem', { name: 'Create wizard alert rule' }).click();

  await expect(page.locator('#search_query')).toHaveValue('source: test');
});

test('Rules filter is not case sensitive - #163', async ({ page }) => {
  await page.goto('wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  const lowerTitle = title.toLowerCase();
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

  // Check if filter works
  await open_alert_page_and_filter(page, lowerTitle);
  await expect(page.getByRole('button', { name: 'play_arrow' })).toBeVisible();
});

test('Update rules cannot change type of rule - #136', async ({ page }) => {
  await page.goto('wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', { name: 'Create' }).click();
  await page.locator('#title').fill(title);

// Add Field Condition
  await fill_field_condition(page, 'message', 'matches exactly', 'abc');

  await page.getByRole('button', { name: 'Save' }).click();

  // Go on search page
  await open_alert_page_and_filter(page, title);

  await page.getByRole('link', { name: 'Edit' }).click();

  for (const rule_name of rules_button) {
    await expect(page.locator('li').filter({ hasText:  rule_name })).toBeVisible();
    await expect(page.locator('li').filter({ hasText:  rule_name })).toContainClass('disabled');
  }
});

test('use switch button should works - #158', async ({ page }) => {
  await page.goto('wizard/AlertRules');

  await login_steps(page);

  // Fill Title
  const title = `AAA-${crypto.randomUUID()}`;
  await page.getByRole('link', {name: 'Create'}).click();
  await page.getByRole('button', {name: 'THEN'}).click();
  await page.locator('#title').fill(title);

  // Add 1st Field Condition
  await fill_field_condition(page, 'message', 'matches exactly', 'abc');

  // Change 1st must match
  await page.getByText('arrow_drop_down').nth(1).click();
  await page.getByRole('option', { name: 'at least one' }).click();
  await page.waitForTimeout(200);

  // Fill 1st Search Query
  const searchQuery = 'a?c';
  await page.locator('#search_query').fill(searchQuery);
  await page.waitForTimeout(200);

  // Change 1st count condition
  await page.getByText('arrow_drop_down').nth(3).click();
  await page.getByRole('option', { name: 'less than' }).click();
  await page.waitForTimeout(200);
  await page.locator('#threshold').first().fill('5');
  await page.waitForTimeout(200);

  // Add 2nd Field Condition
  await fill_field_condition(page, 'user', 'contains', 'def', 1);

  // Fill 2nd Search Query
  const searchQuery2 = 'b?d';
  await page.locator('#additional_search_query').fill(searchQuery2);
  await page.waitForTimeout(200);

  // Click on Switch Button
  await page.getByRole('button', { name: 'swap_vert' }).click();
  await page.waitForTimeout(200);

  // Check switch result
  await expect(page.locator('#search_query')).toHaveValue(searchQuery2);
  await expect(page.locator('#additional_search_query')).toHaveValue(searchQuery);

  await expect(page.locator('#matching_type_select').first()).toContainText('all');
  await expect(page.locator('#matching_type_select').nth(1)).toContainText('at least one');

  await expect(page.locator('#field-input').first()).toHaveValue('user');
  await expect(page.locator('#value').first()).toHaveValue('def');
  await expect(page.locator('#field-input').nth(1)).toHaveValue('message');
  await expect(page.locator('#value').nth(1)).toHaveValue('abc');

  await expect(page.locator('#threshold').first()).toHaveValue('0');
  await expect(page.locator('#threshold').nth(1)).toHaveValue('5');
});