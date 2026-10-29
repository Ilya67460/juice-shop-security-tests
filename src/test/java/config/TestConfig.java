package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * TestConfig — класс для чтения настроек из файла test.properties.
 * Зачем это нужно: чтобы не хардкодить URL, логины и пароли прямо в тестах.
 * Если адрес сайта изменится — меняем одну строку в properties, а не все тесты.
 */
public class TestConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = TestConfig.class.getClassLoader()
                .getResourceAsStream("test.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load test.properties", e);
        }
    }

    public static String getBaseUrl() {
        return PROPS.getProperty("base.url");
    }

    public static String getStandardUser() {
        return PROPS.getProperty("user.standard");
    }

    public static String getInvalidUser() {
        return PROPS.getProperty("user.invalid");
    }

    public static String getNulUser() {
        return PROPS.getProperty("user.null");
    }

    public static String getLanguageUser() {
        return PROPS.getProperty("user.language");
    }

    public static String getLockedUser() {
        return PROPS.getProperty("user.locked");
    }

    public static String getAdminUser() {
        return PROPS.getProperty("user.juice");
    }

    public static String getPassword() {
        return PROPS.getProperty("user.password");
    }

    public static String getInvalidPassword1() {
        return PROPS.getProperty("user.passwordInvalid1");
    }

    public static String getInvalidPassword2() {
        return PROPS.getProperty("user.passwordInvalid2");
    }

    public static String getInvalidPassword3() {
        return PROPS.getProperty("user.passwordInvalid3");
    }

    public static String getInvalidPassword4() {
        return PROPS.getProperty("user.passwordInvalid4");
    }

    public static String getInvalidPassword41() {
        return PROPS.getProperty("user.passwordInvalid41");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(PROPS.getProperty("browser.headless"));
    }

    public static String getBrowser() {
        return PROPS.getProperty("browser.type");
    }

    public static int getTimeout() {
        return Integer.parseInt(PROPS.getProperty("browser.timeout"));
    }
}