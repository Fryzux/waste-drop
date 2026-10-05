package ru.mirea.project.model;

/**
 * Статусы жизненного цикла заявки на вывоз отходов.
 * Реализует паттерн детерминированного конечного автомата (Finite State Machine).
 */
public enum RequestStatus {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    COMPLETED("Выполнена"),
    CANCELLED("Отменена");

    private final String title;

    RequestStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Проверка допустимости перехода в целевой статус согласно графу состояний.
     * 
     * NEW -> IN_PROGRESS, CANCELLED
     * IN_PROGRESS -> COMPLETED, CANCELLED
     * COMPLETED -> (терминальное состояние, переходы запрещены)
     * CANCELLED -> (терминальное состояние, переходы запрещены)
     */
    public boolean canTransitionTo(RequestStatus next) {
        if (next == null || next == this) {
            return false;
        }
        return switch (this) {
            case NEW -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    @Override
    public String toString() {
        return title;
    }
}
