package pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page Object страницы подтверждения завершения оформления заказа.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class CheckoutCompletePage {

    private final Page page;

    public CheckoutCompletePage(Page page) {
        this.page = page;
    }

    @Step("Нажать Cancel")
    public CheckoutCompletePage clickCancel() {
        page.locator("[data-test=\"cancel\"]").click();
        return new CheckoutCompletePage(page);
    }

    @Step("Проверить URL страницы Complete")
    public void assertUrl() {
        assertTrue(page.url().contains("checkout-complete.html"));
    }
    @Step("Проверить URL карточки товара")
    public void assertUrls() {
        assertTrue(page.url().contains("/inventory-item.html?id="));
    }
    @Step("Нажать Generate PDF order")
    public CheckoutCompletePage clickGeneratePDF() {
        page.locator("[data-test=\"generate-pdf-order\"]").click();
        return new CheckoutCompletePage(page);}
    @Step("Проверить URL страницы каталога")
    public void assertUrl2() {
        assertTrue(page.url().contains("/inventory.html"));
    }

    @Step("Нажать Back Home")
    public CheckoutCompletePage clickBackHome() {
        page.locator("[data-test=\"back-to-products\"]").click();
        return new CheckoutCompletePage(page);}


}
