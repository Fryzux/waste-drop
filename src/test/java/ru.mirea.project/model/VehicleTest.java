package ru.mirea.project.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование доменной модели спецтранспорта (Vehicle)")
class VehicleTest {

    @Test
    @DisplayName("Корректное создание объекта Vehicle с валидными параметрами")
    void testVehicleCreation() {
        Vehicle vehicle = new Vehicle(1L, "А101МР77", "КАМАЗ КО-440-5", 22.0, VehicleStatus.AVAILABLE);

        assertEquals(1L, vehicle.getId());
        assertEquals("А101МР77", vehicle.getLicensePlate());
        assertEquals("КАМАЗ КО-440-5", vehicle.getModelName());
        assertEquals(22.0, vehicle.getCapacityM3(), 0.001);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());
    }

    @Test
    @DisplayName("Смена статуса спецтранспорта")
    void testStatusChange() {
        Vehicle vehicle = new Vehicle("В202МР77", "МАЗ КО-449", 16.5);
        assertEquals(VehicleStatus.AVAILABLE, vehicle.getStatus());

        vehicle.setStatus(VehicleStatus.ON_ROUTE);
        assertEquals(VehicleStatus.ON_ROUTE, vehicle.getStatus());

        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        assertEquals(VehicleStatus.MAINTENANCE, vehicle.getStatus());
    }

    @Test
    @DisplayName("Корректное форматирование toString")
    void testToStringFormat() {
        Vehicle vehicle = new Vehicle(5L, "С303МР77", "Scania G410", 30.0, VehicleStatus.AVAILABLE);
        String str = vehicle.toString();
        assertTrue(str.contains("С303МР77"));
        assertTrue(str.contains("Scania G410"));
        assertTrue(str.contains("30.0"));
    }
}
