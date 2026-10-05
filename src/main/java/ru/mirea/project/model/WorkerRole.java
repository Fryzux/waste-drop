package ru.mirea.project.model;

/**
 * Перечисление должностей сотрудников регионального оператора.
 */
public enum WorkerRole {
    DRIVER("Водитель мусоровоза"),
    LOADER("Оператор-грузчик"),
    DISPATCHER("Логист-диспетчер");

    private final String title;

    WorkerRole(String title) {
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
