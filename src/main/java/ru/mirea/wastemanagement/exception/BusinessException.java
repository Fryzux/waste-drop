package ru.mirea.wastemanagement.exception;

/**
 * Базовое контролируемое бизнес-исключение предметной области.
 * Выбрасывается при нарушении правил бизнес-логики (невалидный объем, недопустимый статус).
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
