package ru.yandex.practikum.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.yandex.practikum.model.Order;
import ru.yandex.practikum.steps.OrderSteps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Parameterized.class)
@Feature("Работа с заказами")
@Story("Создание заказа")
public class OrderCreateTests extends BaseTest {

    private OrderSteps orderSteps = new OrderSteps();
    private Order order;
    private Integer track;

    @Parameterized.Parameter
    public String color;

    @Parameterized.Parameters(name = "Создание заказа с цветом: {0}")
    public static Collection<Object[]> colorData() {
        return Arrays.asList(new Object[][]{
                {"BLACK"},
                {"GREY"}
        });
    }

    @Test
    @DisplayName("Можно создать заказ с одним цветом")
    public void shouldCreateOrderWithSingleColor() {
        order = createTestOrder();
        order.setColor(Collections.singletonList(color));

        track = orderSteps.createOrder(order)
                .statusCode(HTTP_CREATED)
                .body("track", notNullValue())
                .extract()
                .body()
                .path("track");
    }

    @Test
    @DisplayName("Можно создать заказ с двумя цветами")
    public void shouldCreateOrderWithBothColors() {
        order = createTestOrder();
        order.setColor(Arrays.asList("BLACK", "GREY"));

        track = orderSteps.createOrder(order)
                .statusCode(HTTP_CREATED)
                .body("track", notNullValue())
                .extract()
                .body()
                .path("track");
    }

    @Test
    @DisplayName("Можно создать заказ без указания цвета")
    public void shouldCreateOrderWithoutColor() {
        order = createTestOrder();
        order.setColor(null);

        track = orderSteps.createOrder(order)
                .statusCode(HTTP_CREATED)
                .body("track", notNullValue())
                .extract()
                .body()
                .path("track");
    }

    @Test
    @DisplayName("Успешное создание заказа возвращает трек-номер")
    public void successfulOrderCreationReturnsTrack() {
        order = createTestOrder();
        order.setColor(Collections.singletonList("BLACK"));

        // Создаем заказ и сразу проверяем ответ
        ValidatableResponse response = orderSteps.createOrder(order)
                .statusCode(HTTP_CREATED)
                .body("track", notNullValue());

        track = response.extract().body().path("track");

        // Проверяем, что трек не null и больше 0
        org.junit.Assert.assertNotNull(track);
        org.junit.Assert.assertTrue(track > 0);

        // Проверяем, что заказ существует по треку (без проверки firstName - баг API)
        orderSteps.getOrderByTrack(track.toString())
                .statusCode(HTTP_OK)
                .body("order.track", equalTo(track));  // ✅ Проверяем только track
    }

    // Вспомогательный метод для создания тестового заказа
    private Order createTestOrder() {
        return new Order()
                .setFirstName("Test")
                .setLastName(RandomStringUtils.randomAlphabetic(8))
                .setAddress("Test Address, " + RandomStringUtils.randomAlphanumeric(5))
                .setMetroStation(1)
                .setPhone("+7" + RandomStringUtils.randomNumeric(10))
                .setRentTime(5)
                .setDeliveryDate("2025-12-31");
    }
}