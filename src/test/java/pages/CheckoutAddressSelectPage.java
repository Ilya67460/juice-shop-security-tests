package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.qameta.allure.Step;

import java.lang.foreign.AddressLayout;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Page Object страницы первого шага оформления заказа.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class CheckoutAddressSelectPage {

    private final Page page;

    public CheckoutAddressSelectPage(Page page) {
        this.page = page;
    }

    /*@Step("Нажать Submit (ожидая ошибку)")
    public CheckoutAddressSelectPage clickSubmit() {
        page.locator("#submitButton").click();
        return this;
    }*/
    @Step("Нажать Submit")
    public CheckoutAddressSelectPage clickSubmit() {
        Locator submit = page.locator("button[type='submit'], #submitButton, button:has-text('Submit')");

        // Ждём видимости
        submit.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));

        // Ждём активации (Angular уберёт disabled)
        try {
            submit.click(new Locator.ClickOptions().setTimeout(10_000));
        } catch (TimeoutError e) {
            // Диагностика: какие поля невалидны
            Object invalids = page.evaluate(
                    "() => Array.from(document.querySelectorAll('.ng-invalid'))" +
                            ".map(el => el.tagName + '#' + (el.id || el.name || el.placeholder))"
            );
            throw new AssertionError(
                    "Кнопка Submit не активировалась — форма невалидна. " +
                            "Невалидные поля: " + invalids, e);
        }
        return this;
    }


    @Step("Проверить сообщение об ошибке: {expectedError}")
    public void assertError(String expectedError) {
        assertThat(page.locator("[data-test=\"error\"]")).containsText(expectedError);
    }


    @Step("Заполнить форму: {Country} {Name} {Mobile Number} {ZIP Code} {Address} {City}")
    public CheckoutAddressSelectPage fillForm(String Country, String Name, String MobileNumber, String ZIPCode, String Address, String City) {
        // Ждём, пока форма появится (опционально, если нужно)
        page.getByPlaceholder("Please provide a country.").waitFor();

        page.getByPlaceholder("Please provide a country.").fill(Country);
        page.getByPlaceholder("Please provide a name.").fill(Name);
        page.getByPlaceholder("Please provide a mobile number.").fill(MobileNumber);
        page.getByPlaceholder("Please provide a ZIP code.").fill(ZIPCode); // проверь точку!
        page.getByPlaceholder("Please provide an address.").fill(Address);
        page.getByPlaceholder("Please provide a city.").fill(City);

        return this;
    }

    @Step("Нажать Continue")
    public CheckoutAddressSelectPage clickContinue() {
        page.locator("app-address button:has-text('Continue')").click();
        return new CheckoutAddressSelectPage(page);
    }
    @Step("Нажать Continue (переход к выбору доставки)")
    public CheckoutAddressSelectPage clickContinue1() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Proceed to delivery method selection")).click();
        return this;
    }    @Step("Нажать выбор адреса")
    public CheckoutAddressSelectPage addressSelection() {
        // Первая радио кнопка на странице
        page.locator("mat-radio-button").first().click();
        return this;
    }
    @Step("Нажать выбор адреса")
    public CheckoutAddressSelectPage addressSelection1() {
        // Первая радио кнопка на странице
        page.locator("mat-radio-button").first().click();
        return this;
    }
    @Step("Нажать Back")
    public CheckoutAddressSelectPage clickBack() {
        page.locator("mat-icon:text('navigate_before')").click();
        //page.getByRole(AriaRole.BUTTON,
                //new Page.GetByRoleOptions().setName("Back")).click();
        return this;
    }
    @Step("Нажать Back")
    public CheckoutAddressSelectPage clickBack1() {
        page.locator("mat-icon:text('navigate_before')").click();
        //page.getByRole(AriaRole.BUTTON,
                //new Page.GetByRoleOptions().setName("Back")).click();
        return this;
    }

    @Step("Проверить URL страницы /#/address/select")
    public void assertUrl() {
        page.waitForURL("**/#/address/select**",
                new Page.WaitForURLOptions().setTimeout(10_000));
    }

    @Step("Проверить URL страницы /#/delivery-method")
    public void assertUrl1() {
        page.waitForURL("**/#/delivery-method**",
                new Page.WaitForURLOptions().setTimeout(10_000));
    }
    @Step("Проверить URL страницы /#/payment/shop")
    public void assertUrl2() {
        page.waitForURL("**/#/payment/shop**",
                new Page.WaitForURLOptions().setTimeout(10_000));
    }

    @Step("Перейти в форму заполнения адреса покупателя Add New Address")
    public CheckoutAddressSelectPage addNewAddress() {
        page.locator("app-address button:has-text('Add New Address')").click();
        return new CheckoutAddressSelectPage(page);
    }
    @Step("Проверить URL страницы /#/address/create")
    public void assertUrl3() {
        assertTrue(page.url().contains("/#/address/create"));
    }



}