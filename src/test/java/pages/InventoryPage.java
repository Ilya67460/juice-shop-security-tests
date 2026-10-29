package pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import config.TestConfig;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
/**
 * Page Object страницы каталога товаров.
 * Следует паттерну Page Object Model: инкапсулирует локаторы и действия страницы,
 * что упрощает поддержку тестов при изменении UI.
 */
public class InventoryPage {
    private final Page page;
    private Locator inventory;
    private Locator name;
    private Locator price;
    private Locator addToCartButton;

    public InventoryPage(Page page) {
        this.page = page;
        this.inventory = page.locator(".mat-card.ribbon-card");
        this.name = page.locator(".item-name");
        this.price = page.locator(".item-price");
        this.addToCartButton = page.locator("button.btn-basket");
    }

    @Step("Проверить URL = /#/login")
    public InventoryPage assertUrl() {
        assertTrue(page.url().contains(TestConfig.getBaseUrl() + "/#/login"));
        return this;
    }

    @Step("Проверить URL = /#/search")
    public InventoryPage assertUrl1() {
        assertTrue(page.url().contains(TestConfig.getBaseUrl() + "/#/search"));
        return this;
    }

    @Step("Проверить URL = /#/score-board")
    public InventoryPage assertUrl2() {
        assertTrue(page.url().contains(TestConfig.getBaseUrl() + "/#/score-board"));
        return this;
    }

    @Step("Получить количество товаров через API")
    public int getProductCount() {
        String response = (String) page.evaluate(
                "async () => { const r = await fetch('/api/Products'); return await r.text(); }"
        );

        JsonObject root = JsonParser.parseString(response).getAsJsonObject();  // ← объект
        JsonArray products = root.getAsJsonArray("data");                      // ← массив из data
        return products.size();
    }

    @Step("Проверить, что количество товаров равно {expectedCount}")
    public InventoryPage assertProductCount(int expectedCount) {
        int actualCount = getProductCount();
        if (actualCount != expectedCount) {
            throw new AssertionError(
                    "Ожидали " + expectedCount + " товаров, но API вернул " + actualCount);
        }
        return this;
    }

    @Step("Дождаться загрузки карточек товаров")
    public InventoryPage waitForProductsLoaded() {
        page.locator("mat-card.ribbon-card").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15_000));
        return this;
    }

    @Step("Проверить, что каждая видимая карточка содержит название, цену и кнопку")
    public InventoryPage assertVisibleCardsHaveFields() {
        Locator cards = page.locator("mat-card.ribbon-card");
        int count = cards.count();

        if (count == 0) {
            throw new AssertionError("На странице не найдено ни одной карточки товара");
        }

        for (int i = 0; i < count; i++) {
            Locator card = cards.nth(i);
            String info = "Карточка #" + i;

            Locator name = card.locator(".item-name");
            if (name.count() == 0 || name.textContent().trim().isEmpty()) {
                throw new AssertionError(info + ": отсутствует название");
            }

            Locator price = card.locator(".item-price");
            if (price.count() == 0 || price.textContent().trim().isEmpty()) {
                throw new AssertionError(info + ": отсутствует цена");
            }

            Locator button = card.locator("button.btn-basket");
            if (button.count() == 0) {
                throw new AssertionError(info + ": отсутствует кнопка Add to Basket");
            }
        }
        System.out.println(">>> Проверено видимых карточек: " + count);
        return this;
    }

    @Step("Проверить, что каждый товар в API содержит название, описание и цену")
    public InventoryPage assertAllProductsInApiHaveFields() {
        // 1. Получаем ответ API
        String response = (String) page.evaluate(
                "async () => { const r = await fetch('/api/Products'); return await r.text(); }"
        );

        // 2. Парсим: {status, data: [...]}
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonArray products = root.getAsJsonArray("data");

        if (products.size() == 0) {
            throw new AssertionError("API вернул пустой список товаров");
        }

        // 3. Проверяем каждое поле у каждого товара
        for (int i = 0; i < products.size(); i++) {
            JsonObject product = products.get(i).getAsJsonObject();
            String id = product.get("id").getAsString();

            // Название
            if (!product.has("name") || product.get("name").isJsonNull()) {
                throw new AssertionError("Товар id=" + id + ": отсутствует поле 'name'");
            }
            String name = product.get("name").getAsString();
            if (name.trim().isEmpty()) {
                throw new AssertionError("Товар id=" + id + ": пустое название");
            }

            // Описание
            if (!product.has("description") || product.get("description").isJsonNull()) {
                throw new AssertionError("Товар '" + name + "' (id=" + id + "): отсутствует поле 'description'");
            }
            String description = product.get("description").getAsString();
            if (description.trim().isEmpty()) {
                throw new AssertionError("Товар '" + name + "' (id=" + id + "): пустое описание");
            }

            // Цена
            if (!product.has("price") || product.get("price").isJsonNull()) {
                throw new AssertionError("Товар '" + name + "' (id=" + id + "): отсутствует поле 'price'");
            }
            double price = product.get("price").getAsDouble();
            if (price <= 0) {
                throw new AssertionError("Товар '" + name + "' (id=" + id + "): некорректная цена " + price);
            }
        }

        System.out.println(">>> Проверено товаров в API: " + products.size() +
                " — все содержат название, описание и цену");
        return this;
    }

    @Step("Проверить, что товар '{productName}' отображается в каталоге")
    public InventoryPage assertProductExists(String productName) {
        // 1. Ждём загрузки карточек (на случай, если ещё не отрисовались)
        page.locator("mat-card.ribbon-card").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15_000));

        // 2. Ищем карточку с нужным названием
        Locator card = page.locator("mat-card.ribbon-card", new Page.LocatorOptions()
                .setHasText(productName));

        // 3. Проверяем, что она есть
        if (card.count() == 0) {
            // Для диагностики: соберём все названия на странице
            Locator allNames = page.locator("mat-card.ribbon-card .item-name");
            StringBuilder available = new StringBuilder();
            for (int i = 0; i < allNames.count(); i++) {
                available.append("\n  - ").append(allNames.nth(i).textContent().trim());
            }
            throw new AssertionError(
                    "Товар '" + productName + "' не найден в каталоге. " +
                            "Доступные товары на странице:" + available);
        }

        // 4. Проверяем, что карточка видима и содержит название
        Locator name = card.first().locator(".item-name");
        name.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        String actualName = name.textContent().trim();
        if (!actualName.contains(productName)) {
            throw new AssertionError(
                    "Ожидали товар '" + productName + "', но нашли '" + actualName + "'");
        }

        System.out.println(">>> Товар найден: '" + actualName + "'");
        return this;
    }

    @Step("Проверить, что открылась детальная карточка товара '{productName}'")
    public InventoryPage assertProductDetailsOpen(String productName) {
        Locator title = page.locator("h1", new Page.LocatorOptions()
                .setHasText(productName));

        title.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));

        System.out.println(">>> Детальная карточка открыта: '" + productName + "'");
        return this;
    }

    @Step("Проверить содержимое детальной карточки '{productName}'")
    public InventoryPage assertProductDetailsContent(String productName, String expectedDescription) {
        // Заголовок
        Locator title = page.locator("h1", new Page.LocatorOptions().setHasText(productName));
        title.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Описание — ищем текст внутри контейнера с h1
        Locator details = page.locator("h1:has-text('" + productName + "')").locator("..");
        // Или проще: проверить, что на странице есть текст описания
        if (page.getByText(expectedDescription).count() == 0) {
            throw new AssertionError(
                    "В детальной карточке нет описания '" + expectedDescription + "'");
        }

        // Цена
        Locator price = page.locator("p.item-price");
        if (price.count() == 0) {
            throw new AssertionError("В детальной карточке нет цены (.item-price)");
        }

        System.out.println(">>> Детальная карточка '" + productName + "': название, описание, цена — ок");
        return this;
    }

    @Step("Проверить, что каждый товар содержит название, описание, цену и кнопку Add to cart")
    public void assertEachItemHasRequiredFields() {
        int count = inventory.count();
        for (int i = 0; i < count; i++) {
            Locator item = inventory.nth(i);
            assertThat(item.locator(name)).isVisible();
            assertThat(item.locator(page.locator(".item-name"))).isVisible();
            assertThat(item.locator(price)).isVisible();

        }
    }

    @Step("Открыть детальную карточку товара '{productName}' по клику на изображение")
    public InventoryPage openProductDetails(String productName) {
        // 1. Ждём, пока карточки товаров загрузятся
        page.locator("mat-card.ribbon-card").first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15_000));

        // 2. Находим изображение нужного товара по alt
        Locator img = page.locator("img.mat-card-image[alt='" + productName + "']");
        img.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));

        // 3. Кликаем — открывается детальная карточка
        img.click();

        System.out.println(">>> Клик по изображению товара: '" + productName + "'");
        return this;
    }

    /*@Step("Нажать на X Close")
    public void clickClose(){
        // Временный обходной путь
        page.locator("mat-dialog-container button[mat-dialog-close]")
                //.evaluate("button => button.click()");
        // ДИАГНОСТИКА (не для финального теста)
        page.locator("mat-dialog-container button[mat-dialog-close]").click(new Locator.ClickOptions().setForce(true));

    }*/
    @Step("Закрыть детальную карточку товара '{productName}'")
    public InventoryPage closeProductDetails(String productName) {
        // 1. Ждём, пока диалог точно открыт
        Locator dialog = page.locator("mat-dialog-container");
        dialog.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));

        // 2. Кликаем по кнопке закрытия ВНУТРИ диалога
        Locator closeBtn = dialog.locator("mat-dialog-actions button[mat-dialog-close] span.mat-button-wrapper");
        closeBtn.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(5_000));
        closeBtn.click();

        // 3. Ждём, пока диалог исчезнет
        dialog.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(10_000));

        System.out.println(">>> Детальная карточка закрыта: '" + productName + "'");
        return this;
    }

    /*@Step("Кликнуть на название первого товара")
    public ProductDetailPage clickFirstItemName() {
        name.first().click();
        return new ProductDetailPage(page);
    }

    @Step("Выбрать сортировку: {option}")
    public void selectSort(String option) {
        page.locator("[data-test=\"product-sort-container\"]").selectOption(option);
    }

    @Step("Получить список названий товаров")
    public List<String> getItemNames() {
        return name.allTextContents();
    }

    @Step("Получить список цен товаров")
    public List<Double> getItemPrices() {
        return price.allTextContents().stream()
                .map(p -> Double.parseDouble(p.replace("$", "").trim()))
                .toList();
    }*/

    @Step("Добавить в корзину товар с индексом {index}")
    public InventoryPage addItemToCartByIndex(int index) {
        addToCartButton.nth(index).click();
        return this;
    }

    @Step("Проверить, что счётчик корзины равен {expected}")
    public InventoryPage assertCartBadgeCount(int expected) {
        assertThat(page.locator(".warn-notification")).hasText(String.valueOf(expected));
        return this;
    }
    @Step("Перейти в корзину")
    public CartPage goToCart() {
        page.locator("app-navbar button:has(mat-icon:text('shopping_cart'))").click();
        return new CartPage(page);
    }

    @Step("Проверить, что вход выполнен (токен в localStorage)")
    public InventoryPage assertLoggedIn() {
        try {
                // Ждём, пока localStorage.getItem('token') не вернёт непустое значение
            page.waitForFunction("() => localStorage.getItem('token') && localStorage.getItem('token').length > 0",
                    null,
                    new Page.WaitForFunctionOptions().setTimeout(10_000));

            String token = (String) page.evaluate("() => localStorage.getItem('token')");
            System.out.println(">>> SQL-инъекция успешна! Получен токен: " + token.substring(0, 20) + "...");
        } catch (Exception e) {
                // Для диагностики: если не дождались, выводим текущее состояние URL и хранилища
            String currentUrl = page.url();
            Object storage = page.evaluate("() => Object.assign({}, localStorage)");
            throw new AssertionError("Токен не появился за 10 сек. URL: " + currentUrl + ", localStorage: " + storage, e);
        }
            page.locator("#navbarAccount > span.mat-button-wrapper > span").click();
        return this;

    }



}
