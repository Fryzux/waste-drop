package ru.mirea.project.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    @DisplayName("Успешное создание клиента с валидным номером телефона")
    void testValidPhone() {
        Client client = new Client(1L, "ООО ВторСырье", "+7 (999) 123-45-67", "info@vtorsyrye.ru");
        assertEquals("+7 (999) 123-45-67", client.getPhone());
    }

    @Test
    @DisplayName("Ошибка валидации: номер телефона содержит менее 10 цифр")
    void testPhoneTooFewDigits() {
        Client client = new Client();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                client.setPhone("123-456"));
        assertTrue(ex.getMessage().contains("не менее 10 цифр"));
    }

    @Test
    @DisplayName("Ошибка валидации: номер телефона null")
    void testPhoneNull() {
        Client client = new Client();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                client.setPhone(null));
        assertTrue(ex.getMessage().contains("не может быть null"));
    }
}
