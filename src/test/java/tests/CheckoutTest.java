package tests;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.*;
import pages.CheckoutAddressSelectPage;
import pages.InventoryPage;
import pages.LoginPage;
import pages.OrderDetailsPage;

import static org.junit.jupiter.api.Assertions.assertTrue;


// Page Object страницы оформления товара.
 // ,
 // что упрощает поддержку тестов при изменении UI.

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("JuiceShopped UI Tests")
@Feature("Оформление заказа")
@DisplayName("Тесты оформления заказа")
@Owner("Ilya Testinov")
@Order(5)
public class CheckoutTest extends BaseTest {

    private InventoryPage inventoryPage;

    @BeforeEach
    void loginAddItemAndGoToCheckout() {
        openBaseUrl();
        inventoryPage = new LoginPage(page)
                .loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());
    }

    private CheckoutAddressSelectPage openCheckout(){
        return inventoryPage
                .addItemToCartByIndex(0)
                .goToCart()
                .clickCheckout()
                .addNewAddress();
    }
    @Test
    @Order(1)
    @DisplayName("CHK-01: Кнопка Checkout открывает страницу выбора адреса покупателя Select an address")
    void checkoutButtonNavigatesAddressSelect() {
        inventoryPage
                .addItemToCartByIndex(0)
                .goToCart()
                .clickCheckout()
                .assertUrl();
    }
    @Test
    @Order(2)
    @DisplayName("CHK-02: Кнопка Add New Address открывает страницу заполнения адреса")
    void checkoutButtonNewAddressSelect() {
        inventoryPage
                .addItemToCartByIndex(0)
                .goToCart()
                .clickCheckout()
                .addNewAddress()
                .assertUrl3();
    }
    @Test
    @Order(3)
    @DisplayName("CHK-03: При заполнении всех полей кнопка Submit становится активной нажимая ее происходит переход на станицу выбора адреса")
    void formShowsName() {
        openCheckout()

                .fillForm("Ru", "Ivan", "1234567", "122", "cWert", "Kirov")
                .clickSubmit()
                .assertUrl();
    }
    @Test
    @Order(4)
    @DisplayName("CHK-04: на странице /#/address/select выбираем адрес после нажатия Continue переходим станицу /#/delivery-method")
    void ContinueButtonAddressSelect() {
        openCheckout()
                .fillForm("Ru", "Ivan", "1234567", "122", "cWert", "Kirov")
                .clickSubmit()
                .addressSelection()
                .clickContinue()
                .assertUrl1();
    }
    @Test
    @Order(5)
    @DisplayName("CHK-05: После нажатия кнопки Back на странице /#/delivery-method открывается предыдущая страница ")
    void clickBack() {
        openCheckout()
                .fillForm("Ru", "Ivan", "1234567", "122", "cWert", "Kirov")
                .clickSubmit()
                .addressSelection()
                .clickContinue()
                .clickBack()
                .assertUrl();

    }
    @Test
    @Order(6)
    @DisplayName("CHK-06: на странице /#/delivery-method выбираем метод доставки нажимаем Continue переход на страницу /#/payment/shop")
    void ContinueButtonSelect() {
        openCheckout()
                .fillForm("Ru", "Ivan", "1234567", "122", "cWert", "Kirov")
                .clickSubmit()
                .addressSelection()
                .clickContinue()
                .addressSelection1()
                .clickContinue1()
                .assertUrl2();

    }
    @Test
    @DisplayName("CHK-09: После нажатия кнопки Back на странице /#/payment/shop открывается предыдущая страница ")
    void clickBack1() {
        openCheckout()
                .fillForm("Ru", "Ivan", "1234567", "122", "cWert", "Kirov")
                .clickSubmit()
                .addressSelection()
                .clickContinue()
                .addressSelection1()
                .clickContinue1()
                .clickBack1()
                .assertUrl1();
    }

}
