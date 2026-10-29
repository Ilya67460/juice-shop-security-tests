package tests;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.*;
import pages.RegisterPage;

/**
 * Page Object страницы регистрации.
 * Работает с RegisterPage и InventoryPage,
 * что упрощает поддержку тестов при изменении UI.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("JuiceShopped UI Tests")
@Feature("Регистрация")
@DisplayName("Тесты регистрации")
@Owner("Ilya Testinov")
@Order(1)
public class RegisterTest extends BaseTest {
    private RegisterPage registerPage;

    @BeforeEach
    void openLogin() {
        openBaseUrl();
        registerPage = new RegisterPage(page);
    }

    @Test
    @Order(1)
    @DisplayName("RG-01: Регистрация с валидным логином и паролем")
    void successfulLogin() {
        registerPage
                .registerAs(TestConfig.getStandardUser(), TestConfig.getPassword())
                .assertUrl();
    }

    @Test
    @Order(2)
    @DisplayName("RG-02: Регистрация с не валидным Email")
    void invalidUserLogin() {
        registerPage
                .registerInvalidEmailAs(TestConfig.getInvalidUser(), TestConfig.getPassword())
                .assertErrorMessage("Email address is not valid.");
    }
    @Test
    @Order(3)
    @DisplayName("RG-03: Регистрация с пустым Email")
    void nullLogin() {
        registerPage
                .registerInvalidEmailAs(TestConfig.getNulUser(), TestConfig.getPassword())
                .assertErrorMessage("Please provide an email address.");
    }
    @Test
    @Order(4)
    @DisplayName("RG-04: Регистрация Email с помощью другого языка")
    void languageLogin() {
        registerPage
                .registerInvalidEmailAs(TestConfig.getLanguageUser(), TestConfig.getPassword())
                .assertErrorMessage("Email address is not valid.");
    }
    @Test
    @Order(5)
    @DisplayName("RG-05: Регистрация Email с существующей  учетной записи")
    void ExistingLogin() {
        registerPage
                .registerAs1(TestConfig.getStandardUser(), TestConfig.getPassword())
                .assertErrorMessage1("Email must be unique");
    }
    @Test
    @Order(6)
    @DisplayName("RG-06: Регистрация с паролем меньше 5 символов")
    void PasswordLogin() {
        registerPage
                .registerInvalidPasswordAs(TestConfig.getStandardUser(), TestConfig.getInvalidPassword4())
                .assertErrorMessage("Password must be 5-40 characters long. ");
    }
    @Test
    @Order(7)
    @DisplayName("RG-07: Регистрация с паролем только из цифр")
    void PasswordLogin1() {
        registerPage
                .registerInvalidPasswordAs(TestConfig.getStandardUser(), TestConfig.getInvalidPassword1())
                .assertErrorMessage2("Password must be 5-40 characters long. ");
    }
    @Test
    @Order(8)
    @DisplayName("RG-08: Регистрация с паролем длиннее 5 символов (цифры + заглавная буква)")
    void PasswordLogin2() {
        registerPage
                .registerInvalidPasswordAs(TestConfig.getStandardUser(), TestConfig.getInvalidPassword2())
                .assertErrorMessage2("Password must be 5-40 characters long. ");
    }
    @Test
    @Order(9)
    @DisplayName("RG-09: Регистрация с паролем длиннее 5символов (цифры + заглавная + строчная)")
    void PasswordLogin3() {
        registerPage
                .registerInvalidPasswordAs(TestConfig.getStandardUser(), TestConfig.getInvalidPassword3())
                .assertErrorMessage2("Password must be 5-40 characters long. ");
    }
    @Test
    @Order(10)
    @DisplayName("RG-10: Регистрация с паролем длиннее 40 символов (цифры + буквы + спецсимвол)")
    void PasswordLogin41() {
        registerPage
                .registerInvalidPasswordAs(TestConfig.getStandardUser(), TestConfig.getInvalidPassword41())
                .assertErrorMessage2("Password must be 5-40 characters long. ");
    }


}