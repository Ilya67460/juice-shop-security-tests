package utils;

import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;
public final class ScreenshotsSelective {

    private ScreenshotsSelective() {}

    public static void capture(String name, Page page) {
        byte[] bytes = page.screenshot(
                new Page.ScreenshotOptions().setFullPage(true)
        );
        Allure.addAttachment(name, "image/png",
                new ByteArrayInputStream(bytes), "png");
    }
}