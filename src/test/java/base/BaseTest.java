package base;

import com.microsoft.playwright.*;
import config.TestConfig;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import utils.ScreenshotOnFailure;

import java.nio.file.Paths;

import static utils.ScreenshotOnFailure.PAGE_HOLDER;

/**
 * BaseTest — базовый класс для всех тестов.
 * Содержит:
 * - создание и закрытие браузера (@BeforeEach / @AfterEach)
 * - автоматический скриншот при падении теста
 * Все тестовые классы наследуются от BaseTest,
 * чтобы не дублировать этот код в каждом файле.
 */
@ExtendWith(ScreenshotOnFailure.class)
public abstract class BaseTest {

    // Playwright — «фабрика» браузеров. Создаётся один раз на тест.
    private Playwright playwright;

    // Browser — сам процесс браузера (Chromium / Firefox / WebKit).
    private Browser browser;

    private BrowserContext context;   // ← добавь это поле

    // Page — одна вкладка браузера. Именно с ней работают Page Object.
    protected Page page;

    /**
     * @BeforeEach — JUnit вызывает этот метод ПЕРЕД каждым тестом.
     * Здесь мы создаём браузер и открываем новую вкладку.
     */
    @BeforeEach
    void setUp() {
        // Cоздаём экземпляр Playwright — точку входа для всех браузеров
        playwright = Playwright.create();

        // Настройки запуска браузера
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(TestConfig.isHeadless());

        // Запускаем нужный браузер в зависимости от настройки в test.properties
        browser = switch (TestConfig.getBrowser()) {
            case "firefox" -> playwright.firefox().launch(launchOptions);
            case "webkit" -> playwright.webkit().launch(launchOptions);
            default -> playwright.chromium().launch(launchOptions);
        };

        // BrowserContext — изолированная сессия браузера (свои куки и т.д.)
        // Аналог отдельного окна браузера в режиме инкогнито.
        //BrowserContext
                context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1920, 1080) // размер окна браузера
        );
        // запуск записи трассировки
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)   // скриншоты каждого шага
                .setSnapshots(true)     // DOM-снимки (можно кликать по элементам в просмотрщике)
                .setSources(true));     // исходный код — подсветка строк теста


        // Устанавливаем таймаут ожидания элементов по умолчанию (30 секунд)
        context.setDefaultTimeout(TestConfig.getTimeout());

        // Открываем новую вкладку — именно с ней будем работать в тестах
        page = context.newPage();

        // Сохраняем page в ThreadLocal, чтобы ScreenshotOnFailure мог его получить
        PAGE_HOLDER.set(page);
    }

    /**
     * @AfterEach — JUnit вызывает этот метод ПОСЛЕ каждого теста.
     * Здесь мы закрываем браузер и освобождаем ресурсы.
     */
    @AfterEach
    void tearDown() {
        // ← ВСТАВИТЬ: сохраняем трассировку перед закрытием контекста
        try {
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(Paths.get("target/traces/trace-"
                            + System.currentTimeMillis() + ".zip")));
        } catch (Exception e) {
            System.err.println("Не удалось сохранить трассировку: " + e.getMessage());
        }

        // Закрываем контекст и браузер
        page.context().close();
        browser.close();
        playwright.close();
        PAGE_HOLDER.remove();
    }

    @Step("Открыть главную страницу сайта")
    protected void openBaseUrl() {
        page.navigate(TestConfig.getBaseUrl());
    }
}
