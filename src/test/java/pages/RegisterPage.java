package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Page Object страницы авторизации.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class RegisterPage {

    private final Page page1;

    private final Locator errorMessage;
    private final Locator errorMessage1;
    private final Locator errorMessage2;

    public RegisterPage(Page page1) {
        this.page1 = page1;
        //Ошибки все остальные
        this.errorMessage = page1.locator(".mat-error");
        //ошибка при нажатии кнопки регистрация
        this.errorMessage1 = page1.locator(".error");
        // Селектор — привязан к полю пароля, без динамического ID
        this.errorMessage2 = page1.locator("mat-form-field:has(#passwordControl) em");
    }
    @Step("Нажать на кнопку Dismiss")
    public void clickDismiss(){
        page1.locator("button.close-dialog").click();
    }

    @Step("Нажать на кнопку Account")
    public void clickAccount(){
        page1.locator("#navbarAccount > span.mat-button-wrapper > span").click();
    }

    @Step("Нажать на Login")
    public void clickLogin(){
        page1.locator("#navbarLoginButton").click();
    }

    @Step("Нажать на ссылку Not yet a customer?")
    public void clickNewRegister(){
        page1.locator("#newCustomerLink > a").click();
    }

    @Step("Ввести логин: {username}")
    public void enterUsername(String username) {

        page1.locator("#emailControl").fill(username);
    }

    @Step("Ввести пароль")
    public void enterPassword(String password) {

        page1.locator("#passwordControl").fill(password);
    }

    @Step("Подтвердить пароль")
    public void repeatPasswordControl(String password) {

        page1.locator("#repeatPasswordControl").fill(password);
    }

    @Step("Нажать на выпадающее поле Secret question")
    public void clickSecretQuestion() {
        page1.locator("mat-select[name=\"securityQuestion\"]").click();
        page1.locator(".cdk-overlay-container mat-option").first()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Дождаться открытия списка опций
        page1.locator(".mat-option").first()
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    @Step("Выбрать первый секретный вопрос")
    public void selectFirstSecretQuestion() {
        page1.locator(".cdk-overlay-container mat-option").first().click();
    }

    @Step("Ввести ответ на секретный вопрос")
    public void fillSecurityAnswer(String answer) {
        page1.locator("#securityAnswerControl").fill(answer);

    }
    @Step("Нажать на кнопку Me want it!")
    public void clickMeWantIt(){
        page1.locator("body > div.cc-window.cc-floating.cc-type-info.cc-theme-classic.cc-bottom.cc-right.cc-color-override--1225450786 > div > a").click();
    }
    @Step("Закрыть приветственный баннер")
    public void dismissWelcomeBanner() {
        // Пробуем нажать Escape. Если баннера нет - просто ничего не произойдёт.
        page1.keyboard().press("Escape");

        // Небольшая пауза, чтобы Angular успел завершить анимацию закрытия
        page1.waitForTimeout(5000);
    }
    @Step("Нажать Register")
    public void clickRegister() {
        page1.locator("#registerButton").click(new Locator.ClickOptions().setTimeout(5000));
    }
    @Step("Дождаться страницы логина")
    // Ждем переход на страницу Login и ищем кнопку Login
    public void waitForLoginPage() {
        page1.waitForURL("**/login**", new Page.WaitForURLOptions().setTimeout(15_000));
        page1.locator("#loginButton").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
    }

    @Step("Регистрируемся {username}")
    public InventoryPage registerAs(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        clickNewRegister();
        enterUsername(username);
        enterPassword(password);
        repeatPasswordControl(password);
        clickSecretQuestion();
        selectFirstSecretQuestion();
        fillSecurityAnswer("Test");
        clickMeWantIt();
        dismissWelcomeBanner();
        clickRegister();
        waitForLoginPage();

        return new InventoryPage(page1);
    }
    @Step("Регистрируемся {username1}")
    public RegisterPage registerAs1(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        clickNewRegister();
        enterUsername(username);
        enterPassword(password);
        repeatPasswordControl(password);
        clickSecretQuestion();
        selectFirstSecretQuestion();
        fillSecurityAnswer("Test");
        clickMeWantIt();
        dismissWelcomeBanner();
        clickRegister();


        return this;
    }
    // Регистрация с невалидным Email
    @Step("Регистрируемся {username}")
    public RegisterPage registerInvalidEmailAs(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        clickNewRegister();
        enterUsername(username);
        enterPassword(password);
        repeatPasswordControl(password);

        return this;
    }

    @Step("Попытаться войти с невалидными данными: password:{password}")
    public RegisterPage registerInvalidPasswordAs(String username, String password) {
        clickDismiss();
        clickAccount();
        clickLogin();
        clickNewRegister();
        enterUsername(username);
        enterPassword(password);
        repeatPasswordControl(password);

        return this;
    }

    @Step("Проверить, что отображается сообщение об ошибке: {expectedMessage}")
    public void assertErrorMessage(String expectedMessage) {
        assertThat(errorMessage).isVisible();
        assertThat(errorMessage).containsText(expectedMessage);
    }
    @Step("Проверить, что отображается сообщение об ошибке: {expectedMessage1}")
    public void assertErrorMessage1(String expectedMessage1) {
        assertThat(errorMessage1).isVisible();
        assertThat(errorMessage1).containsText(expectedMessage1);
    }
    @Step("Проверить, что отображается сообщение об ошибке: {expectedMessage2}")
    public void assertErrorMessage2(String expectedMessage2) {
        errorMessage2.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));
        assertThat(errorMessage2).containsText(expectedMessage2);
    }
}
