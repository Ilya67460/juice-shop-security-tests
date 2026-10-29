package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page Object страницы корзины.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class CartPage {

    private final Page page;

    public CartPage(Page page) {
        this.page = page;
    }

    @Step("Проверить URL = /#/basket")
    public CartPage assertUrl() {
        assertTrue(page.url().contains("/#/basket"));
        return this;
    }
    @Step("Проверить, что в корзине {expectedCount} товар(а/ов)")
    public CartPage assertBasketCount(int expectedCount) {
        Locator badge = page.locator("app-navbar .warn-notification");
        if (expectedCount == 0) {
            // assertThat сам будет ждать, пока бейдж скроется или исчезнет
            assertThat(badge).isHidden();
            return this;
        }

        // assertThat будет ретраить, пока текст не станет ожидаемым
        assertThat(badge).hasText(String.valueOf(expectedCount));
        return this;
    }

    @Step("Удалить товар с индексом {index} из корзины")
    public CartPage removeFromBasketByIndex(int index) {
        // 1. Ждём загрузки карточек
        Locator items = page.locator("mat-card");
        items.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));

        // 2. Ждём загрузки кнопок удаления (по всей странице)
        Locator removeButtons = page.locator("button:has(svg[data-icon='trash-alt'])");
        removeButtons.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE).setTimeout(10_000));

        int totalItems = items.count();
        int totalButtons = removeButtons.count();
        System.out.println(">>> Карточек: " + totalItems + ", кнопок удаления: " + totalButtons);

        if (index < 0 || index >= totalButtons) {
            throw new AssertionError(
                    "Индекс " + index + " вне диапазона. Кнопок удаления: " + totalButtons);
        }

        // 3. Кликаем по кнопке с тем же индексом
        removeButtons.nth(index).click();

        System.out.println(">>> Удалён товар #" + index);
        return this;
    }

    @Step("Нажать кнопку Checkout")
    public CheckoutAddressSelectPage clickCheckout() {
        page.locator("#checkoutButton").click();
        return new CheckoutAddressSelectPage(page);
    }
    @Step("Нажать кнопку Continue Shopping")
    public CheckoutAddressSelectPage clickContinueShopping() {
        page.locator("[data-test=\"continue-shopping\"]").click();
        return new CheckoutAddressSelectPage(page);
    }



}
