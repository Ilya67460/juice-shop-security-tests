package pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

import java.nio.file.Path;
import java.nio.file.Paths;

/*import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;*/
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderDetailsPage {

    private final Page page;

    public OrderDetailsPage(Page page) {
        this.page = page;
    }

    @Step("Нажать 'Generate PDF order'")
    public OrderDetailsPage clickGeneratePdf() {
        // Локатор ссылки/кнопки для генерации PDF
        // На Demowebshop это обычно ссылка с текстом "Generate PDF order"
        page.locator("a:has-text('Generate PDF order')").click();
        return this;
    }

    @Step("Проверить, что PDF-файл скачался")
    public OrderDetailsPage assertPdfDownloaded() {
        // Ждем начала скачивания, которое инициируется кликом
        Download download = page.waitForDownload(() -> {
            // Если клик уже был в clickGeneratePdf(), то здесь нужно просто дождаться
            // Если нет, можно объединить: page.waitForDownload(() -> page.locator("...").click());
        });

        // Сохраняем файл (можно во временную папку или в target)
        Path pdfPath = Paths.get("target/downloads", download.suggestedFilename());
        download.saveAs(pdfPath);

        // Проверяем, что файл существует и не пустой
        assertTrue(pdfPath.toFile().exists(), "PDF-файл должен существовать");
        assertTrue(pdfPath.toFile().length() > 0, "PDF-файл не должен быть пустым");

        return this;
    }
}
