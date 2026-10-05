package ru.mirea.project.model;

/**
 * Перечисление организационно-правовых типов клиентов.
 */
public enum ClientType {
    INDIVIDUAL("Физическое лицо"),
    LEGAL_ENTITY("Юридическое лицо"),
    MUNICIPAL_ORG("Муниципальная организация");

    private final String title;

    ClientType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
