package ru.mirea.wastemanagement.service;

/**
 * DTO (Data Transfer Object) для агрегированных статистических показателей реестра заявок.
 */
public class WasteRequestStats {
    private final long totalCount;
    private final long activeCount;
    private final long completedCount;
    private final long cancelledCount;
    private final double totalVolumeM3;
    private final double averageVolumeM3;

    public WasteRequestStats(long totalCount, long activeCount, long completedCount,
                             long cancelledCount, double totalVolumeM3, double averageVolumeM3) {
        this.totalCount = totalCount;
        this.activeCount = activeCount;
        this.completedCount = completedCount;
        this.cancelledCount = cancelledCount;
        this.totalVolumeM3 = totalVolumeM3;
        this.averageVolumeM3 = averageVolumeM3;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getActiveCount() {
        return activeCount;
    }

    public long getCompletedCount() {
        return completedCount;
    }

    public long getCancelledCount() {
        return cancelledCount;
    }

    public double getTotalVolumeM3() {
        return totalVolumeM3;
    }

    public double getAverageVolumeM3() {
        return averageVolumeM3;
    }

    @Override
    public String toString() {
        return String.format(
                "===========================================================\n" +
                "               АНАЛИТИЧЕСКАЯ СВОДКА ПО ЗАЯВКАМ             \n" +
                "===========================================================\n" +
                "  Всего зарегистрировано заявок:  %d шт.\n" +
                "  Активных заявок (в работе):      %d шт.\n" +
                "  Успешно выполненных:            %d шт.\n" +
                "  Отмененных:                     %d шт.\n" +
                "  Суммарный объем отходов:        %.2f м³\n" +
                "  Средний объем одной заявки:     %.2f м³\n" +
                "===========================================================",
                totalCount, activeCount, completedCount, cancelledCount, totalVolumeM3, averageVolumeM3);
    }
}
