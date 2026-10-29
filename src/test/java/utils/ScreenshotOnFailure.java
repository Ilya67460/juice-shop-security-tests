package utils;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.io.ByteArrayInputStream;

/**
 * JUnit 5 extension для автоматического создания скриншота
 * и прикрепления его к Allure при падении теста.
 */
public class ScreenshotOnFailure implements TestExecutionExceptionHandler {

    /**
     * Хранит текущую страницу Playwright для доступа из extension.
     */
    public static final ThreadLocal<Page> PAGE_HOLDER = new ThreadLocal<>();

    /**
     * Делает скриншот текущей страницы при ошибке теста
     * и прикрепляет его к Allure отчёту.
     */
    @Override
    public void handleTestExecutionException(
            ExtensionContext context,
            Throwable throwable
    ) throws Throwable {

        Page page = PAGE_HOLDER.get();

        if (page != null) {
            byte[] screenshot = page.screenshot(
                    new Page.ScreenshotOptions().setFullPage(true)
            );

            Allure.addAttachment(
                    "Скриншот при падении",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png"
            );
        }

        throw throwable;
    }
}
