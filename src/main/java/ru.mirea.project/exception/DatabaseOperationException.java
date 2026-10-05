package ru.mirea.project.exception;

/**
 * Исключение, оборачивающее низкоуровневые SQLException реляционной базы данных
 * с сохранением кода SQLState и удобным человекочитаемым описанием.
 */
public class DatabaseOperationException extends RuntimeException {
    private final String sqlState;

    public DatabaseOperationException(String message, String sqlState, Throwable cause) {
        super(message, cause);
        this.sqlState = sqlState;
    }

    public DatabaseOperationException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public String getSqlState() {
        return sqlState;
    }
}
