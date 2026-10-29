package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import config.TestConfig;
import io.qameta.allure.Step;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page Object страницы авторизации.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class LoginPage {

    private Page page;

    private final Locator errorMessage;

    public LoginPage(Page page) {
        this.page = page;
        this.errorMessage = page.locator("app-login .error");
    }
    @Step("Нажать на кнопку Dismiss")
    public void clickDismiss(){
        page.locator("button.close-dialog").click();
    }

    @Step("Нажать на кнопку Account")
    public void clickAccount(){
        page.locator("#navbarAccount > span.mat-button-wrapper > span").click();
    }

    @Step("Нажать на Login")
    public void clickLogin(){
        page.locator("#navbarLoginButton").click();
    }
    @Step("Нажать на кнопку Log in")
    public void clickLogin1(){
        page.locator("#loginButton").click();
    }

    @Step("Нажать на Logout")
    public void clickLogout() {
        page.locator("#navbarLogoutButton").click();
    }

    @Step("Ввести логин: {username}")
    public void enterUsername(String username) {

        page.locator("#email").fill(username);
    }

    @Step("Ввести пароль")
    public void enterPassword(String password) {

        page.locator("#password").fill(password);
    }
    @Step("Дождаться страницы с товарами")
    // Ждем переход на страницу Login и ищем кнопку Login
    public void waitForLoginPage() {
        page.waitForURL("**/search**", new Page.WaitForURLOptions().setTimeout(15_000));
        //page.locator("#loginButton").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }
    @Step("Дождаться страницы логина")
    // Ждем переход на страницу Login и ищем кнопку Login
    public void waitForLoginPage1() {
        page.waitForURL("**/login**", new Page.WaitForURLOptions().setTimeout(15_000));
        page.locator("#loginButton").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }
    @Step("Дождаться страницы доски с результатами")
    // Ждем переход на страницу Login и ищем кнопку Login
    public void waitForLoginPage2() {
        page.waitForURL("**/score-board**", new Page.WaitForURLOptions().setTimeout(15_000));

    }
    /*@Step("Открыть скрытую доску с наградами по прямому URL")
    public void scoreBoard() {
        // Прямой переход по скрытому маршруту
        page.navigate(TestConfig.getBaseUrl() + "/#/score-board");
    }*/
    public void scoreBoard() {
        // 1. Имитируем предварительное исследование приложения
        //    (например, клик по логотипу или переход на главную)
        //page.locator("app-header a").first().click();
        page.locator("span")
                .filter(new Locator.FilterOptions().setHasText("OWASP Juice Shop"));
        // Небольшая пауза, чтобы действие было "замечено"
        page.waitForTimeout(1000);

        // 2. Теперь переходим по скрытому URL
        page.navigate(TestConfig.getBaseUrl() + "/#/score-board");

        // 3. Ждем загрузки страницы
        page.waitForURL("**/#/score-board**");
    }

    @Step("Проверить, что доска с наградами загрузилась")
    public void assertScoreBoardLoaded() {
        // Ждём появления элемента, который есть только на этой странице
        // Вариант 1: Проверяем заголовок (обычно это mat-card-title или h1)
        Locator title = page.locator("mat-card-title, h1").first();
        title.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Дополнительная проверка: убедимся, что в заголовке есть слово "Score Board"
        // (это не обязательно, но даёт больше уверенности)
        String titleText = title.textContent();
        if (!titleText.toLowerCase().contains("score board")) {
            // Если заголовок не совпал, возможно, селектор поймал что-то другое.
            // Можно добавить более специфичный селектор, например, для списка челленджей.
            page.locator("mat-table, .challenge-list").first()
                    .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        }
    }




    @Step("Логинимся {username}")
    public InventoryPage loginAs(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        enterUsername(username);
        enterPassword(password);
        clickLogin1();
        waitForLoginPage();
        return new InventoryPage(page);
    }
    @Step("Логинимся без дбанера {username}")
    public InventoryPage loginAs1(String username, String password) {
        clickAccount();
        clickLogin();
        enterUsername(username);
        enterPassword(password);
        clickLogin1();
        waitForLoginPage();
        return new InventoryPage(page);
    }
    @Step("Выходим из кабинета {username}")
    public InventoryPage logoutAs(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        enterUsername(username);
        enterPassword(password);
        clickLogin1();
        waitForLoginPage1();

        return new InventoryPage(page);
    }

    @Step("Попытаться войти с невалидными данными: username: {username} password:{password}")
    public LoginPage loginExpectingError(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        enterUsername(username);
        enterPassword(password);
        clickLogin1();

        return this;
    }
    @Step("Логинимся ищем доску{username}")
    public InventoryPage openScoreBoard(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        enterUsername(username);
        enterPassword(password);
        clickLogin1();
        waitForLoginPage();
        scoreBoard();
        waitForLoginPage2();

        return new InventoryPage(page);
    }
    @Step("Проверить, что отображается сообщение об ошибке: {expectedMessage}")
    public void assertErrorMessage(String expectedMessage) {
        assertThat(errorMessage).isVisible();
        assertThat(errorMessage).containsText(expectedMessage);
    }


}
