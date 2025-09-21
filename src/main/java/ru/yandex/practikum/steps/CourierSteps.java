package ru.yandex.practikum.steps;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import ru.yandex.practikum.model.Courier;

import static io.restassured.RestAssured.given;
import java.util.HashMap;
import java.util.Map;

public class CourierSteps {

    public static final String COURIER = "/api/v1/courier";
    public static final String LOGIN = "/api/v1/courier/login";
    public static final String DELETE_COURIER = "/api/v1/courier/{id}";

    @Step("Создание курьера с логином: {courier.login}")
    public ValidatableResponse createCourier(Courier courier) {
        return given()
                .body(courier)
                .when()
                .post(COURIER)
                .then();
    }

    @Step("Логин курьера с логином: {login}")
    public ValidatableResponse loginCourier(String login, String password) {
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("login", login);
        loginData.put("password", password);

        return given()
                .body(loginData)
                .when()
                .post(LOGIN)
                .then();
    }

    @Step("Получение ID курьера для логина: {login}")
    public Integer getCourierId(String login, String password) {
        Response response = given()
                .body(Map.of("login", login, "password", password))
                .when()
                .post(LOGIN)
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.jsonPath().getInt("id");
    }

    @Step("Удаление курьера с ID: {courierId}")
    public ValidatableResponse deleteCourier(int courierId) {
        return given()
                .pathParam("id", courierId)
                .when()
                .delete(DELETE_COURIER)
                .then();
    }

    @Step("Проверка существования курьера с логином: {login}")
    public boolean isCourierExists(String login) {
        try {
            loginCourier(login, "testPassword123")
                    .statusCode(200);
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }
}