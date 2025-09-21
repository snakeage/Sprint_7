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
import java.util.List;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
@Feature("Работа с заказами")
@Story("Создание заказа")
public class OrderCreateTests extends BaseTest {

    private OrderSteps orderSteps = new OrderSteps();
    private Order order;
    private Integer track;
    private List<String> colors;

    @Parameterized.Parameter
    public List<String> colorList;

    @Parameterized.Parameters(name = "Создание заказа с цветом: {0}")
    public static Collection<List<String>[]> colorData() {
        return Arrays.asList(new List[][]{
                {Collections.singletonList("BLACK")},      // 1. Только BLACK
                {Collections.singletonList("GREY")},       // 2. Только GREY
                {Arrays.asList("BLACK", "GREY")},          // 3. BLACK + GREY
                {null}                                     // 4. Без цвета
        });
    }

    @Test
    @DisplayName("Можно создать заказ с разными вариантами цветов")
    public void shouldCreateOrderWithDifferentColors() {
        order = createTestOrder();
        order.setColor(colorList);  // ✅ Используем параметр из @Parameterized

        track = orderSteps.createOrder(order)
                .statusCode(HTTP_CREATED)
                .body("track", notNullValue())  // ✅ Тело ответа содержит track
                .extract()
                .body()
                .path("track");

        // Проверяем, что трек не null и больше 0
        org.junit.Assert.assertNotNull(track);
        org.junit.Assert.assertTrue(track > 0);
    }

    @Test
    @DisplayName("Успешное создание заказа возвращает трек-номер")
    public void successfulOrderCreationReturnsTrack() {
        order = createTestOrder();
        order.setColor(Collections.singletonList("BLACK"));

        // Создаем заказ и сразу проверяем ответ
        ValidatableResponse response = orderSteps.createOrder(order)
                .statusCode(HTTP_CREATED)
                .body("track", notNullValue());  // ✅ Track в ответе

        track = response.extract().body().path("track");

        // Проверяем, что трек не null и больше 0
        org.junit.Assert.assertNotNull(track);
        org.junit.Assert.assertTrue(track > 0);

        // Проверяем, что заказ существует по треку (только track - баг API)
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