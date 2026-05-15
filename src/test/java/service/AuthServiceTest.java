package service;

import org.junit.jupiter.api.Test;
import validation.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    // Проверяем регистрацию и вход пользователя
    void shouldRegisterAndLoginUser() {
        var authService = new AuthService();

        var user = authService.register("user1", "password");
        var loggedUser = authService.login("user1", "password");

        assertEquals("user1", user.getLogin());
        assertNotEquals("password", user.getPasswordHash());
        assertEquals(user.getId(), loggedUser.getId());
        assertEquals(loggedUser, authService.getCurrentUser());
    }

    @Test
    // Проверяем что нельзя зарегистрировать одинаковые логины
    void shouldThrowWhenLoginIsDuplicated() {
        var authService = new AuthService();

        authService.register("user1", "password");

        assertThrows(ValidationException.class, () ->
                authService.register("user1", "other"));
    }

    @Test
    // Проверяем что без входа пользователь недоступен
    void shouldRequireCurrentUser() {
        var authService = new AuthService();

        assertThrows(ValidationException.class, authService::requireCurrentUser);
    }

    @Test
    // Проверяем что после загрузки пользователей id продолжаются правильно
    void shouldContinueIdsAfterLoad() {
        var authService = new AuthService();
        authService.register("user1", "password");

        var loadedAuthService = new AuthService();
        loadedAuthService.loadRestored(authService.snapshot());

        var nextUser = loadedAuthService.register("user2", "password");

        assertEquals(2, nextUser.getId());
    }
}
