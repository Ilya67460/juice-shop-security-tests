package tests;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.*;
import pages.InventoryPage;
import pages.LoginPage;

/**
 * Page Object страницы оформления заказа.
 * работает с CartPage и InventoryPage
 * что упрощает поддержку тестов при изменении UI.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("JuiceShopped UI Tests")
@Feature("Корзина")
@DisplayName("Тесты корзины")
@Owner("Ilya Testinov")
@Order(4)
public class CartTest extends BaseTest {

    private InventoryPage inventoryPage;

    @BeforeEach
    void loginAndOpenCatalog() {
        openBaseUrl();
        inventoryPage = new LoginPage(page)
                .loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());
    }

    @Test
    @Order(1)
    @DisplayName("CR-01: При добавлении товара счетчик корзины увеличивается на 1")
    void cartBadgeIncrementsOnAdd() {
        inventoryPage
                .addItemToCartByIndex(0)
                .assertCartBadgeCount(1);
    }
    @Test
    @Order(2)
    @DisplayName("CR-02: В корзине отображаются все ранее добавленные товары")
    void cartContainsAddedItems() {
        inventoryPage
                .addItemToCartByIndex(0)
                .addItemToCartByIndex(1)
                .goToCart()
                .assertUrl()
                .assertBasketCount(3);

    }
    @Test
    @Order(3)
    @DisplayName("CR-03: После удаления товара из корзины количество товаров уменьшается")
    void removeItemInCart() {
        inventoryPage
                .addItemToCartByIndex(0)
                .addItemToCartByIndex(1)
                .goToCart()
                .assertUrl()
                .removeFromBasketByIndex(0)
                .assertBasketCount(2);
    }
    @Test
    @Order(4)
    @DisplayName("CR-04: Кнопка Checkout открывает страницу выбора адреса покупателя Select an address")
    void checkoutButtonNavigatesToStepOne() {
        inventoryPage
                .addItemToCartByIndex(0)
                .goToCart()
                .clickCheckout()
                .assertUrl();
    }
}
