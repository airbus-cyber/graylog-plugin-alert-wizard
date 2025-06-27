// @ts-check
import { test, expect } from '@playwright/test';
import crypto from 'node:crypto';
import { login_steps, fill_field_condition, open_alert_page_and_filter } from './test-utils.js';

test('disable/enable rule without stream should work', async ({ page }) => {
    await page.goto('wizard/AlertRules');

    await login_steps(page);

    // Create Rule without stream
    const title = `AAA-${crypto.randomUUID()}`;
    await page.getByRole('link', { name: 'Create' }).click();
    await page.locator('#title').fill(title);
    await page.getByRole('button', { name: 'Save' }).click();

    // Filter Rule
    await open_alert_page_and_filter(page, title);

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
    await page.goto('wizard/AlertRules');

    await login_steps(page);

    // Create Rule with 2 streams
    const title = `AAA-${crypto.randomUUID()}`;
    await page.getByRole('link', { name: 'Create' }).click();
    await page.getByRole('button', { name: 'OR' }).click();
    await page.locator('#title').fill(title);

    await fill_field_condition(page, 'message', 'matches exactly', 'abc');

    await page.getByRole('button', { name: 'add_circle' }).nth(1).click();
    await page.waitForTimeout(200);
    await page.locator('#field-input').nth(1).fill('message');
    await page.waitForTimeout(200);
    await page.getByText('arrow_drop_down').nth(5).click();
    await page.getByRole('option', { name: 'matches exactly' }).click();
    await page.locator('#value').nth(1).fill('cba');
    await page.waitForTimeout(200);

    await page.getByRole('button', { name: 'Save' }).click();

    // Disable stream#2
    await page.goto(`streams?page=1&query=${title}%232`);
    await page.waitForTimeout(200);
    page.on('dialog', dialog => dialog.accept());
    await page.getByLabel('Pause stream').click();
    await page.waitForTimeout(200);

    // Check Rule is disabled
    await open_alert_page_and_filter(page, title);

    await expect(page.getByText('Disabled')).toBeVisible();
    await expect(page.getByText('Disabled')).toHaveCSS('background-color', 'rgb(255, 165, 0)');
});