package ru.mirea.project.model;

/**
 * Перечисление эксплуатационных статусов спецтранспорта (мусоровозов).
 */
public enum VehicleStatus {
    AVAILABLE("Готов к рейсу / Свободен"),
    ON_ROUTE("На маршруте / В рейсе"),
    MAINTENANCE("На техническом обслуживании");

    private final String title;

    VehicleStatus(String title) {
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
