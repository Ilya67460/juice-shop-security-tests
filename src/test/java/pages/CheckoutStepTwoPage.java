package pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page Object страницы второго шага оформления заказа.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class CheckoutStepTwoPage {

    private final Page page;

    public CheckoutStepTwoPage(Page page) {
        this.page = page;
    }

    @Step("Нажать Continue")
    public CheckoutStepTwoPage clickContinue() {
        page.locator("[data-test=\"continue\"]").click();
        return new CheckoutStepTwoPage(page);
    }
    @Step("Нажать Cancel")
    public CheckoutStepTwoPage clickCancel() {
        page.locator("[data-test=\"cancel\"]").click();
        return new CheckoutStepTwoPage(page);
    }

    @Step("Проверить URL = checkout-step-two.html")
    public CheckoutStepTwoPage assertUrl() {
        assertTrue(page.url().contains("/checkout-step-two.html"));
        return this;
    }
    @Step("Проверить URL = /cart.html")
    public CheckoutStepTwoPage assertUrl1() {
        assertTrue(page.url().contains("/cart.html"));
        return this;
    }
    @Step("Проверить URL = checkout-step-one.html")
    public CheckoutStepTwoPage assertUrl2() {
        assertTrue(page.url().contains("/checkout-step-one.html"));
        return this;
    }
    @Step("Проверить, что список товаров отображается")
    public CheckoutStepTwoPage assertItemsVisible() {
        assertThat(page.locator(".cart_item").first()).isVisible();
        return this;
    }


    @Step("Проверить, что inventory_item_price отображается")
    public CheckoutStepTwoPage assertInventoryItemPriceVisible() {
        assertThat(page.locator(".summary_subtotal_label")).isVisible();
        return this;
    }

    @Step("Проверить, что Item Total отображается")
    public CheckoutStepTwoPage assertSubtotalVisible() {
        assertThat(page.locator(".summary_subtotal_label")).isVisible();
        return this;
    }

    @Step("Проверить, что Tax отображается")
    public CheckoutStepTwoPage assertTaxVisible() {
        assertThat(page.locator(".summary_tax_label")).isVisible();
        return this;
    }

    @Step("Проверить, что Total отображается")
    public CheckoutStepTwoPage assertTotalVisible() {
        assertThat(page.locator(".summary_total_label")).isVisible();
        return this;
    }

    @Step("Нажать на название товара")
    public CheckoutCompletePage clickLincs() {
        page.locator(".inventory_item_name").click();
        return new CheckoutCompletePage(page);
    }

    @Step("Нажать Finish")
    public CheckoutCompletePage clickFinish() {
        page.locator("[data-test='finish']").click();
        return new CheckoutCompletePage(page);
    }



}
