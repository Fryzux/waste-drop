package ru.mirea.project.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование доменной модели сотрудника (Worker)")
class WorkerTest {

    @Test
    @DisplayName("Корректное создание объекта Worker с валидными параметрами")
    void testWorkerCreation() {
        Worker worker = new Worker(1L, "Смирнов Алексей Петрович", "+7 (916) 111-22-33", WorkerRole.DRIVER, 75000.0);

        assertEquals(1L, worker.getId());
        assertEquals("Смирнов Алексей Петрович", worker.getFullName());
        assertEquals("+7 (916) 111-22-33", worker.getPhone());
        assertEquals(WorkerRole.DRIVER, worker.getRole());
        assertEquals(75000.0, worker.getSalary(), 0.001);
    }

    @Test
    @DisplayName("Ошибка валидации: номер телефона null")
    void testPhoneNull() {
        Worker worker = new Worker();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> worker.setPhone(null));
        assertTrue(ex.getMessage().contains("не может быть null"));
    }

    @Test
    @DisplayName("Ошибка валидации: номер телефона менее 10 цифр")
    void testPhoneTooFewDigits() {
        Worker worker = new Worker();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> worker.setPhone("123-45"));
        assertTrue(ex.getMessage().contains("не менее 10 цифр"));
    }

    @Test
    @DisplayName("Смена роли и оклада сотрудника")
    void testRoleAndSalaryChange() {
        Worker worker = new Worker("Петров Иван Сергеевич", "+7 (926) 222-33-44", WorkerRole.LOADER, 55000.0);
        assertEquals(WorkerRole.LOADER, worker.getRole());

        worker.setRole(WorkerRole.DISPATCHER);
        worker.setSalary(65000.0);

        assertEquals(WorkerRole.DISPATCHER, worker.getRole());
        assertEquals(65000.0, worker.getSalary(), 0.001);
    }

    @Test
    @DisplayName("Корректное форматирование toString")
    void testToStringFormat() {
        Worker worker = new Worker(10L, "Иванов Иван", "+7 (999) 000-11-22", WorkerRole.DRIVER, 80000.0);
        String str = worker.toString();
        assertTrue(str.contains("Иванов Иван"));
        assertTrue(str.contains("Водитель мусоровоза"));
        assertTrue(str.contains("80000.00"));
    }
}
