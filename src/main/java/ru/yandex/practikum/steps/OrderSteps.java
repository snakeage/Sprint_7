package ru.yandex.practikum.steps;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import ru.yandex.practikum.model.Order;

import static io.restassured.RestAssured.given;

public class OrderSteps {

    public static final String CREATE_ORDER = "/api/v1/orders";
    public static final String GET_ORDERS = "/api/v1/orders";
    public static final String GET_ORDER_BY_TRACK = "/api/v1/orders/track";

    @Step("Создание заказа для клиента: {order.firstName} {order.lastName}")
    public ValidatableResponse createOrder(Order order) {
        return given()
                .body(order)
                .when()
                .post(CREATE_ORDER)
                .then();
    }

    @Step("Получение списка всех заказов")
    public ValidatableResponse getOrdersList() {
        return given()
                .when()
                .get(GET_ORDERS)
                .then();
    }

    @Step("Получение заказа по трек-номеру: {track}")
    public ValidatableResponse getOrderByTrack(String track) {
        return given()
                .queryParam("t", track)
                .when()
                .get(GET_ORDER_BY_TRACK)
                .then();
    }
}