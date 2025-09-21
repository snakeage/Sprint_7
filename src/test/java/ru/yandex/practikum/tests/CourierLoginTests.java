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

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@Feature("Работа с курьерами")
@Story("Логин курьера")
public class CourierLoginTests extends BaseTest {

    private CourierSteps courierSteps = new CourierSteps();
    private String testLogin;
    private String testPassword;
    private Integer courierId;

    @Before
    public void setUp() {
        // Создаем тестового курьера перед каждым тестом
        testLogin = "login_" + RandomStringUtils.randomAlphabetic(8);
        testPassword = RandomStringUtils.randomAlphanumeric(10);

        // Создаем курьера для успешных тестов логина
        Courier testCourier = new Courier()
                .setLogin(testLogin)
                .setPassword(testPassword)
                .setFirstName("TestLogin");

        // Создаем курьера
        courierSteps.createCourier(testCourier)
                .statusCode(201)
                .body("ok", is(true));

        // Получаем ID для удаления в @After
        courierId = courierSteps.getCourierId(testLogin, testPassword);
    }

    @Test
    @DisplayName("Курьер может успешно авторизоваться")
    public void shouldLoginCourierSuccessfully() {
        // Успешный логин
        courierSteps.loginCourier(testLogin, testPassword)
                .statusCode(HTTP_OK)  // 200
                .body("id", notNullValue()); // Возвращает ID
    }

    @Test
    @DisplayName("Для авторизации нужно передать все обязательные поля - без логина")
    public void shouldNotLoginWithoutLogin() {
        // Логин без логина (только пароль)
        courierSteps.loginCourier("", testPassword)
                .statusCode(HTTP_BAD_REQUEST)  // 400
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Для авторизации нужно передать все обязательные поля - без пароля")
    public void shouldNotLoginWithoutPassword() {
        // Логин без пароля (только логин)
        courierSteps.loginCourier(testLogin, "")
                .statusCode(HTTP_BAD_REQUEST)  // 400
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Система вернёт ошибку при неверном пароле")
    public void shouldNotLoginWithWrongPassword() {
        // Неверный пароль
        courierSteps.loginCourier(testLogin, testPassword + "wrong")
                .statusCode(HTTP_NOT_FOUND)  // 404
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Система вернёт ошибку при неверном логине")
    public void shouldNotLoginWithWrongLogin() {
        // Неверный логин
        String wrongLogin = "wrong_" + RandomStringUtils.randomAlphabetic(8);
        courierSteps.loginCourier(wrongLogin, testPassword)
                .statusCode(HTTP_NOT_FOUND)  // 404
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Логин под несуществующим пользователем возвращает ошибку")
    public void shouldNotLoginNonExistentUser() {
        // Несуществующий пользователь
        String nonExistentLogin = "nonexistent_" + RandomStringUtils.randomAlphabetic(8);
        String nonExistentPassword = RandomStringUtils.randomAlphanumeric(10);

        courierSteps.loginCourier(nonExistentLogin, nonExistentPassword)
                .statusCode(HTTP_NOT_FOUND)  // 404
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Успешный запрос возвращает ID курьера")
    public void successfulLoginReturnsId() {
        // Получаем ID из ответа логина
        Integer returnedId = courierSteps.loginCourier(testLogin, testPassword)
                .statusCode(HTTP_OK)
                .extract()
                .body()
                .path("id");

        // Проверяем, что ID не null и совпадает с сохраненным
        org.junit.Assert.assertNotNull(returnedId);
        org.junit.Assert.assertEquals(courierId, returnedId);
    }

    @Test
    @DisplayName("Логин с пустыми полями возвращает ошибку")
    public void shouldNotLoginWithEmptyFields() {
        // Пустые поля
        courierSteps.loginCourier("", "")
                .statusCode(HTTP_BAD_REQUEST)  // 400
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @After
    public void tearDown() {
        // Удаляем тестового курьера после каждого теста
        if (courierId != null) {
            try {
                courierSteps.deleteCourier(courierId)
                        .statusCode(HTTP_OK)
                        .body("ok", is(true));
            } catch (AssertionError e) {
                // Курьер уже удален - это нормально
                System.out.println("Курьер уже удален: " + e.getMessage());
            }
        }
    }
}