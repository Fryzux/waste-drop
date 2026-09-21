package ru.mirea.wastemanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.wastemanagement.model.StandardContainer;

import static org.junit.jupiter.api.Assertions.*;

class VolumeCalculatorTest {

    @Test
    @DisplayName("Расчет объема по габаритам (Длина * Ширина * Высота)")
    void testCalculateByDimensions() {
        // 2.5м * 1.2м * 1.0м = 3.00 м³
        double volume = VolumeCalculator.calculateByDimensions(2.5, 1.2, 1.0);
        assertEquals(3.00, volume, 0.001);

        // 1.5м * 0.8м * 0.5м = 0.60 м³
        double volumeSmall = VolumeCalculator.calculateByDimensions(1.5, 0.8, 0.5);
        assertEquals(0.60, volumeSmall, 0.001);
    }

    @Test
    @DisplayName("Валидация отрицательных или нулевых габаритов")
    void testDimensionsValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
                VolumeCalculator.calculateByDimensions(-1.0, 2.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> 
                VolumeCalculator.calculateByDimensions(1.0, 0.0, 1.0));
    }

    @Test
    @DisplayName("Расчет объема по количеству единиц типовой тары")
    void testCalculateByContainers() {
        // 10 мешков по 120л (0.12 м³) = 1.20 м³
        double bagsVolume = VolumeCalculator.calculateByContainers(StandardContainer.BAG_120L, 10);
        assertEquals(1.20, bagsVolume, 0.001);

        // 2 бункера «Лодочка» по 8.0 м³ = 16.00 м³
        double boatVolume = VolumeCalculator.calculateByContainers(StandardContainer.BOAT_BUNKER, 2);
        assertEquals(16.00, boatVolume, 0.001);
    }

    @Test
    @DisplayName("Валидация недопустимого количества тары")
    void testContainerCountValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
                VolumeCalculator.calculateByContainers(StandardContainer.EURO_BIN, 0));
        assertThrows(IllegalArgumentException.class, () -> 
                VolumeCalculator.calculateByContainers(null, 5));
    }
}
