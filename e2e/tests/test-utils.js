

export async function login_steps(page) {
    await page.getByLabel('Username').fill('admin');
    await page.getByLabel('Password').fill('admin');
    await page.getByLabel('Sign in').click();
}

export async function open_alert_page_and_filter(page, filter) {
    await page.goto('wizard/AlertRules');
    // Wait for rules are loaded
    await page.waitForTimeout(1000);
    await page.getByPlaceholder('Search for alert rules').fill(filter);
    // Wait for filter is applied
    await page.waitForTimeout(500);
}

export async function fill_field_condition(page, input, option, value, nth= 0) {
    await page.getByRole('button', { name: 'add_circle' }).nth(nth).click();
    await page.waitForTimeout(200);
    await page.locator('#field-input').nth(nth).fill(input);
    await page.waitForTimeout(200);
    await page.getByText('arrow_drop_down').nth(nth * 3 + 2).click();
    await page.getByRole('option', { name: option }).click();
    await page.locator('#value').nth(nth).fill(value);
    await page.waitForTimeout(200);
}