# Полноценный UI фреймворк для автотестов на Playwright

## Установка

```bash
mvn exec:java -e \
  -D exec.mainClass=com.microsoft.playwright.CLI \
  -D exec.args="install"
```

## Запустить все тесты

```bash
mvn clean test
```

## Запустить один класс

```bash
mvn test -Dtest=LoginTest
```

## Запустить тест по названию метода

```bash
mvn test -Dtest="LoginTest#successfulLogin"
```

## Сгенерировать и открыть Allure-отчёт

```bash
mvn allure:serve
```

---

## Аннотации Allure в проекте

| Аннотация      | Где используется                                        |
|----------------|---------------------------------------------------------|
| `@Epic`        | Класс теста — верхний уровень иерархии                  |
| `@Feature`     | Класс теста — функциональная область                    |
| `@DisplayName` | ID + описание теста                                     |
| `@Step`        | Методы Page Object и BaseTest                           |
---

