package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.options.WaitForSelectorState;
import config.TestConfig;
import io.qameta.allure.Step;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class XssPage {
    private Page page;
    private boolean dialogAppeared = false;

    public XssPage(Page page) {
        this.page = page;
        // Регистрируем обработчик ЗАРАНЕЕ
        page.onDialog(dialog -> {
            System.out.println(">>> XSS СРАБОТАЛ! Alert: " + dialog.message());
            dialogAppeared = true;
            dialog.accept();
        });
    }

    @Step("Выполнить Reflected XSS в поиске с payload: {payload}")
    public XssPage attemptSearchXss(String payload) {
        page.navigate(TestConfig.getBaseUrl() + "/#/search");

        // 1. Кликаем по иконке поиска, чтобы развернуть поле
        Locator searchIcon = page.locator("app-navbar mat-icon:text('search')");
        searchIcon.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
        searchIcon.click();

        // 2. Ждём, пока поле станет видимым (после анимации разворота)
        Locator searchInput = page.locator(".mat-search_field input");
        searchInput.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));

        // 3. Вводим payload
        searchInput.fill(payload);
        searchInput.press("Enter");
        page.waitForTimeout(2000);
        return this;
    }

    @Step("Проверить, что XSS-скрипт исполнился")
    public void assertXssTriggered() {
        if (!dialogAppeared) {
            throw new AssertionError("XSS не сработал: диалоговое окно не появилось");
        }
        System.out.println(">>> Reflected XSS подтверждён");
    }
    @Step("Выполнить DOM XSS через URL с payload: {payload}")
    public XssPage attemptDomXss(String payload) {
        // Кодируем payload для URL
        String encodedPayload = URLEncoder.encode(payload, StandardCharsets.UTF_8);

        // Переходим по URL с параметром q
        page.navigate(TestConfig.getBaseUrl() + "/#/search?q=" + encodedPayload);

        // Ждем, чтобы Angular обработал параметр и выполнил скрипт
        page.waitForTimeout(2000);
        return this;
    }
    @Step("Оставить отзыв с XSS payload: {payload}")
    public XssPage leaveReviewWithPayload(String payload) {
        // 1. Открываем товар (Apple Juice)
        page.navigate(TestConfig.getBaseUrl() + "/#/search?q=carrot");
        page.locator("mat-card.ribbon-card").first().click();
        page.waitForTimeout(1000);

        // 2. Форма отзыва уже открыта — сразу заполняем textarea
        Locator textarea = page.locator("textarea[aria-label='Text field to review a product']");
        textarea.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));
        textarea.fill(payload);

        // 3. Нажимаем Submit
        page.locator("#submitButton").click();
        page.waitForTimeout(2000);

        System.out.println(">>> Отзыв с payload отправлен: " + payload);
        return this;
    }

    @Step("Проверить, что XSS в отзыве не исполняется (защита работает)")
    public void assertXssBlocked() {
        // После сохранения отзыва и перезагрузки — проверяем, что диалог НЕ появился
        page.reload();
        page.waitForTimeout(2000);
        if (dialogAppeared) {
            throw new AssertionError("XSS сработал! Защита не работает.");
        }
        System.out.println(">>> Stored XSS заблокирован (защита работает)");
    }

}