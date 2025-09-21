package ru.yandex.practikum.tests;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.junit4.DisplayName;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practikum.model.Courier;
import ru.yandex.practikum.steps.CourierSteps;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@Feature("Работа с курьерами")
@Story("Создание курьера")
public class CourierCreateTests extends BaseTest {

    private CourierSteps courierSteps = new CourierSteps();
    private Courier courier;
    private Integer courierId;
    private String testLogin;
    private String testPassword;

    @Before
    public void setUp() {
        // Создаем базового курьера для тестов
        testLogin = "test_" + RandomStringUtils.randomAlphabetic(8);
        testPassword = RandomStringUtils.randomAlphanumeric(10);

        courier = new Courier()
                .setLogin(testLogin)
                .setPassword(testPassword)
                .setFirstName(RandomStringUtils.randomAlphabetic(6));
    }

    @Test
    @DisplayName("Можно создать курьера с полным набором данных")
    public void shouldCreateCourierWithAllFields() {
        // Выполняем создание курьера
        courierSteps.createCourier(courier)
                .statusCode(HTTP_CREATED)  // Проверяем код 201
                .body("ok", is(true));     // Проверяем тело ответа

        // Проверяем, что курьер создался (может залогиниться)
        courierSteps.loginCourier(testLogin, testPassword)
                .statusCode(HTTP_OK)
                .body("id", notNullValue()); // ID не должен быть null

        // Сохраняем ID для удаления в @After
        courierId = courierSteps.getCourierId(testLogin, testPassword);
    }

    @Test
    @DisplayName("Нельзя создать двух курьеров с одинаковым логином")
    public void shouldNotCreateDuplicateCourier() {
        // Создаем первого курьера
        courierSteps.createCourier(courier)
                .statusCode(HTTP_CREATED);

        // Сохраняем ID для удаления в @After
        courierId = courierSteps.getCourierId(testLogin, testPassword);

        // Пытаемся создать второго с тем же логином
        courierSteps.createCourier(courier)
                .statusCode(409)  // Conflict для дубликата
                // ✅ ИСПРАВЛЕНО: точное сообщение из API
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @DisplayName("Нельзя создать курьера без логина")
    public void shouldNotCreateCourierWithoutLogin() {
        // Создаем копию курьера без логина
        Courier invalidCourier = createCourierCopy();
        invalidCourier.setLogin(null);

        courierSteps.createCourier(invalidCourier)
                .statusCode(400)  // Bad Request
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Нельзя создать курьера без пароля")
    public void shouldNotCreateCourierWithoutPassword() {
        // Создаем копию курьера без пароля
        Courier invalidCourier = createCourierCopy();
        invalidCourier.setPassword(null);

        courierSteps.createCourier(invalidCourier)
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Можно создать курьера без имени (необязательное поле)")
    public void shouldCreateCourierWithoutFirstName() {
        // Создаем копию курьера без имени
        Courier validCourier = createCourierCopy();
        validCourier.setFirstName(null);

        // Имя необязательное, должно создаться
        courierSteps.createCourier(validCourier)
                .statusCode(HTTP_CREATED)
                .body("ok", is(true));

        // Сохраняем ID для удаления в @After
        // ✅ ВАЖНО: используем оригинальный логин/пароль, а не от validCourier
        courierId = courierSteps.getCourierId(testLogin, testPassword);
    }

    @Test
    @DisplayName("Нельзя создать курьера с пустым логином")
    public void shouldNotCreateCourierWithEmptyLogin() {
        // Создаем копию курьера с пустым логином
        Courier invalidCourier = createCourierCopy();
        invalidCourier.setLogin("");

        courierSteps.createCourier(invalidCourier)
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Нельзя создать курьера с пустым паролем")
    public void shouldNotCreateCourierWithEmptyPassword() {
        // Создаем копию курьера с пустым паролем
        Courier invalidCourier = createCourierCopy();
        invalidCourier.setPassword("");

        courierSteps.createCourier(invalidCourier)
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @After
    public void tearDown() {
        // Удаляем курьера после каждого теста для независимости
        if (courierId != null) {
            try {
                courierSteps.deleteCourier(courierId)
                        .statusCode(HTTP_OK)
                        .body("ok", is(true));
            } catch (AssertionError e) {
                // Курьер уже удален или не существует - это нормально
                System.out.println("Курьер уже удален или не существует: " + e.getMessage());
            }
        }
    }

    // Вспомогательный метод для создания копии курьера
    private Courier createCourierCopy() {
        return new Courier()
                .setLogin(testLogin)
                .setPassword(testPassword)
                .setFirstName(courier.getFirstName());
    }
}