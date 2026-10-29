package tests;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.*;
import pages.LoginPage;

/**
 * Page Object страницы входа в личный кабинет.
 * Работает с LoginPage и InventoryPage,
 * что упрощает поддержку тестов при изменении UI.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("JuiceShopped UI Tests")
@Feature("Авторизация")
@DisplayName("Тесты авторизации")
@Owner("Ilya Testinov")
@Order(2)
public class LoginTest extends BaseTest {
    private LoginPage loginPage;

    @BeforeEach
    void openLogin() {
        openBaseUrl();
        loginPage = new LoginPage(page);
    }

    @Test
    @Order(1)
    @DisplayName("LG-01: Авторизация с валидным логином и паролем")
    void successfulLogin() {
        loginPage
                .loginAs(TestConfig.getStandardUser(), TestConfig.getPassword())
                .assertUrl1();
    }

    @Test
    @Order(2)
    @DisplayName("LG-02: Авторизация с неверным паролем")
    void wrongUserLogin() {
        loginPage
                .loginExpectingError(TestConfig.getStandardUser(), TestConfig.getInvalidPassword3())
                .assertErrorMessage("Invalid email or password.");
    }

    @Test
    @Order(3)
    @DisplayName("LG-03: Вход с несуществующим email")
    void wrongPasswordLogin() {
        loginPage
                .loginExpectingError(TestConfig.getInvalidUser(), TestConfig.getPassword())
                .assertErrorMessage("Invalid email or password.");
    }

    @Test
    @Order(4)
    @DisplayName("LG-04: Выход из системы (Logout)")
    void logoutLogin() {
        loginPage
                .logoutAs(TestConfig.getStandardUser(), TestConfig.getPassword())
                .assertUrl();
    }
    @Test
    @Order(5) // Следующий после тестов логина
    @DisplayName("LG-05: Поиск скрытой страницы Score Board")
    void findScoreBoard() {
        loginPage
                .openScoreBoard(TestConfig.getStandardUser(), TestConfig.getPassword())
                .assertUrl2();

    }
}
