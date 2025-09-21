package ru.yandex.practikum.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;
import ru.yandex.practikum.steps.OrderSteps;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.notNullValue;

@Feature("Работа с заказами")
@Story("Список заказов")
public class OrderListTests extends BaseTest {

    private OrderSteps orderSteps = new OrderSteps();

    @Test
    @DisplayName("В тело ответа возвращается список заказов")
    public void shouldReturnOrdersListInResponse() {
        orderSteps.getOrdersList()
                .statusCode(HTTP_OK)
                .body("orders", notNullValue());  // ✅ Требование задания
    }
}