package pages;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page Object детальной страницы товара.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class ProductDetailPage {

    private final Page page;

    public ProductDetailPage(Page page) {
        this.page = page;
    }

    @Step("Проверить, что открылась страница товара")
    public InventoryPage assertOnDetailPage() {
        assertTrue(page.url().contains("inventory-item.html?id="));
        assertThat(page.locator(".inventory_details_name")).isVisible();
        return new InventoryPage(page);
    }
}
