package ru.mirea.wastemanagement.util;

import ru.mirea.wastemanagement.model.StandardContainer;

/**
 * Утилитный класс-калькулятор для расчета объема отходов
 * по габаритным размерам или типовой таре ЖКХ/ГОСТ.
 */
public final class VolumeCalculator {

    private VolumeCalculator() {
    }

    /**
     * Расчет объема по линейным габаритам в метрах: Длина × Ширина × Высота.
     * Округляет результат до двух знаков после запятой.
     */
    public static double calculateByDimensions(double lengthMeters, double widthMeters, double heightMeters) {
        if (lengthMeters <= 0 || widthMeters <= 0 || heightMeters <= 0) {
            throw new IllegalArgumentException("Габариты должны быть положительными числами!");
        }
        double volume = lengthMeters * widthMeters * heightMeters;
        return round(volume);
    }

    /**
     * Расчет объема по количеству единиц стандартной тары (мешки, контейнеры, бункеры).
     */
    public static double calculateByContainers(StandardContainer container, int count) {
        if (container == null) {
            throw new IllegalArgumentException("Тара не может быть null!");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Количество тары должно быть больше 0!");
        }
        double volume = container.getVolumeM3() * count;
        return round(volume);
    }

    private static double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
