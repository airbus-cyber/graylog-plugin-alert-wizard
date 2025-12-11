// @ts-check
import { test, expect } from '@playwright/test';
import crypto from 'node:crypto';
import { login_steps, fill_field_condition, open_alert_page_and_filter } from './test-utils.js';

test('Wizard Configuration Summary should work', async ({ page }) => {
    await page.goto('system/configurations/Plugins/com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig');

    await login_steps(page);

    await page.waitForTimeout(200);

    await expect(page.getByRole('heading', { name: 'Wizard Alert Configuration' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Edit configuration' })).toBeVisible();
});

test('Edit Wizard Configuration priority should work', async ({ page }) => {
    await page.goto('system/configurations/Plugins/com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig');

    await login_steps(page);

    await page.waitForTimeout(200);

    await page.getByRole('button', { name: 'Edit configuration' }).click();
    await page.getByText('arrow_drop_down').first().click();
    await page.getByTestId('react-select-list').getByText('High').click();
    await page.locator('button').filter({ hasText: 'Save' }).click();

    await expect(page.getByText('Priority:High')).toBeVisible();
});

test('Edit Wizard Configuration Aggregation Time Range should work', async ({ page }) => {
    await page.goto('system/configurations/Plugins/com.airbus_cyber_security.graylog.wizard.config.rest.AlertWizardConfig');

    await login_steps(page);

    await page.waitForTimeout(200);

    await page.getByRole('button', { name: 'Edit configuration' }).click();
    await page.getByLabel('Aggregation Time Range (').fill('30');
    await page.locator('button').filter({ hasText: 'Save' }).click();

    await expect(page.getByText('Aggregation time range:30')).toBeVisible();
});