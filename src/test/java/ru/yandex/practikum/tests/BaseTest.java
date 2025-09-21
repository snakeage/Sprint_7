package ru.yandex.practikum.tests;

import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.http.ContentType;
import org.junit.BeforeClass;
import ru.yandex.practikum.config.RestConfig;

@Feature("Базовые тесты API")
public class BaseTest {

    @BeforeClass
    public static void setup() {
        // Настраиваем глобальную конфигурацию (логирование)
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails());

        // Настраиваем спецификацию запросов
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(RestConfig.HOST)
                .setContentType(ContentType.JSON)
                .build();
    }
}