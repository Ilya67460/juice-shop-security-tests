package tests;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.*;
import pages.InventoryPage;
import pages.LoginPage;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Page Object работа с каталогом товаров.
 * работает с InventoryPage и ProductDetailPage,
 * что упрощает поддержку тестов при изменении UI.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Epic("JuiceShopped UI Tests")
@Feature("Каталог товаров")
@DisplayName("Тесты каталога товаров")
@Owner("Ilya Testinov")
@Order(3)
public class InventoryTest extends BaseTest {
    private InventoryPage inventoryPage;

    @BeforeEach
    void loginAndOpenCatalog() {
        openBaseUrl();
        inventoryPage = new LoginPage(page)
                .loginAs(TestConfig.getStandardUser(), TestConfig.getPassword());

    }
    @Test
    @Order(1)
    @DisplayName("INV-01: Количество товаров в каталоге")
    void checkProductCount1() {
        int actual = inventoryPage.getProductCount();
        System.out.println(">>> ФАКТИЧЕСКОЕ КОЛИЧЕСТВО: " + actual);
        assertEquals(37, actual);
    }

    @Test
    @Order(2)
    @DisplayName("INV-02: Количество товаров в каталоге соответствует 37")
    void checkProductCount3() {
        inventoryPage.assertProductCount(37);
    }

    @Test
    @Order(3)
    @DisplayName("INV-03: Каждый видимый товар содержит название, цену и кнопку Add to Basket")
    void checkVisibleProductsHaveFields() {
        inventoryPage
                .waitForProductsLoaded()          // ← ждём загрузки
                .assertVisibleCardsHaveFields();
    }
    @Test
    @Order(4)
    @DisplayName("INV-04: Каждый товар в API содержит название, описание и цену")
    void checkAllProductsInApiHaveFields() {
        inventoryPage.assertAllProductsInApiHaveFields();
    }
    @Test
    @Order(5)
    @DisplayName("INV-05: Каждый товар содержит название, описание, цену и кнопку Add to Basket")
    void eachItemHasRequiredFields() {
        inventoryPage.assertEachItemHasRequiredFields();
    }
    @Test
    @Order(6)
    @DisplayName("INV-06: Товар Apple Juice присутствует в каталоге")
    void checkAppleJuiceExists() {
        inventoryPage.assertProductExists("Apple Juice (1000ml)");
    }
    @Test
    @Order(7)
    @DisplayName("INV-07: Клик по изображению открывает детальную карточку товара")
    void openProductDetails() {
        inventoryPage
                .openProductDetails("Apple Juice (1000ml)")
                .assertProductDetailsOpen("Apple Juice (1000ml)")
                .assertProductDetailsContent("Apple Juice (1000ml)", "The all-time classic.");
    }
    @Test
    @Order(8)
    @DisplayName("INV-08: Клик по x Close закрывает детальную карточку товара")
    void closeProductDetails() {
        inventoryPage
                .openProductDetails("Apple Juice (1000ml)")
                .assertProductDetailsOpen("Apple Juice (1000ml)")
                .closeProductDetails("Apple Juice (1000ml)");

    }

    /*@Test
    @DisplayName("INV-04: При выборе сортировки A→Z товары отображаются по алфавиту")
    void sortByNameAtoZ() {
        inventoryPage.selectSort("az");
        List<String> names = inventoryPage.getItemNames();
        List<String> sorted = names.stream().sorted().toList();
        assertEquals(names, sorted, "Товары должны быть отсортированы по названию A→Z");
    }
    @Test
    @DisplayName("INV-05: При выборе сортировки Z→A товары отображаются с конца алфавита")
    void sortByNameZtoA() {
        inventoryPage.selectSort("za");
        List<String> names = inventoryPage.getItemNames();
        List<String> sorted = names.stream().sorted(Comparator.reverseOrder()).toList();
        assertEquals(names, sorted, "Товары должны быть отсортированы по названию Z→A");
    }

    @Test
    @DisplayName("INV-06: Сортировка Low→High отображает товары по возрастанию цены")
    void sortByPriceLowToHigh() {
        inventoryPage.selectSort("lohi");
        List<Double> prices = inventoryPage.getItemPrices();
        List<Double> sorted = prices.stream().sorted().toList();
        assertEquals(prices, sorted, "Товары должны быть отсортированы по цене Low→High");
    }
    @Test
    @DisplayName("INV-07: Сортировка High→Low отображает товары по уменьшению цены")
    void sortByPriceHighToLov() {
        inventoryPage.selectSort("hilo");
        List<Double> prices = inventoryPage.getItemPrices();
        List<Double> sorted = prices.stream().sorted(Comparator.reverseOrder()).toList();
        assertEquals(prices, sorted, "Товары должны быть отсортированы по цене High→Low");
    }*/
}

