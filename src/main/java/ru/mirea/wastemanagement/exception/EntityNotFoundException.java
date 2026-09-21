package ru.mirea.wastemanagement.exception;

/**
 * Исключение, сигнализирующее об отсутствии запрошенной сущности в репозитории/БД.
 */
public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
