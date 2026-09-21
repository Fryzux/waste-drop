package ru.mirea.wastemanagement.model;

/**
 * Классификация типов отходов согласно регламентам обращения с отходами.
 */
public enum WasteType {
    MUNICIPAL("Твердые коммунальные отходы (ТКО)"),
    CONSTRUCTION("Строительный мусор (бой кирпича, бетон, смеси)"),
    BULKY("Крупногабаритные отходы (КГО: мебель, техника)"),
    HAZARDOUS("Опасные отходы (промышленные, аккумуляторы, ЛКМ)");

    private final String title;

    WasteType(String title) {
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
