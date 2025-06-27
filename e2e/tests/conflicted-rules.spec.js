// @ts-check
import { test, expect } from '@playwright/test';
import crypto from 'node:crypto';
import { login_steps, fill_field_condition, open_alert_page_and_filter } from './test-utils.js';

test('remove event definition should conflict rule', async ({ page }) => {
    await page.goto('wizard/AlertRules');

    await login_steps(page);

    // Fill Title
    const title = `CONF-${crypto.randomUUID()}`;
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

    // Delete EventDefinition
    await page.goto('alerts/definitions');
    await page.getByPlaceholder('Search for event definitions').fill(title);
    await page.waitForTimeout(500);
    await page.getByLabel("More actions").first().click();
    await page.getByRole('menuitem', { name: 'Delete' }).click();
    await page.getByLabel('Confirm').click();
    await page.waitForTimeout(200);

    // Check if rule is corrupted
    await open_alert_page_and_filter(page, title);

    await expect(page.getByText('Corrupted')).toBeVisible();
});
