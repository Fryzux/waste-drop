package ru.mirea.project.model;

/**
 * Справочник стандартной тары и ориентиров для калькулятора объема отходов.
 */
public enum StandardContainer {
    BAG_120L("Строительный мешок для мусора (120 л)", 0.12),
    EURO_BIN("Дворовый евроконтейнер пластиковый (1.1 м³)", 1.10),
    BOAT_BUNKER("Бункер «Лодочка» открытый (8.0 м³)", 8.00),
    GAZELLE_TRUCK("Кузов тентованной ГАЗели (9.0 м³)", 9.00),
    PUHTO_20("Крупногабаритный контейнер ПУХТО (20.0 м³)", 20.00);

    private final String description;
    private final double volumeM3;

    StandardContainer(String description, double volumeM3) {
        this.description = description;
        this.volumeM3 = volumeM3;
    }

    public String getDescription() {
        return description;
    }

    public double getVolumeM3() {
        return volumeM3;
    }

    @Override
    public String toString() {
        return String.format("%s [номинал: %.2f м³]", description, volumeM3);
    }
}
