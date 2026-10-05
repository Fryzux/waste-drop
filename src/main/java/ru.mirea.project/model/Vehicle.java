package ru.mirea.project.model;

import java.util.Objects;

/**
 * Доменная сущность единицы спецтранспорта (мусоровоз, бункеровоз, ломовоз).
 */
public class Vehicle {
    private Long id;
    private String licensePlate;
    private String modelName;
    private double capacityM3;
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    public Vehicle() {
    }

    public Vehicle(Long id, String licensePlate, String modelName, double capacityM3, VehicleStatus status) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.modelName = modelName;
        this.capacityM3 = capacityM3;
        this.status = status != null ? status : VehicleStatus.AVAILABLE;
    }

    public Vehicle(String licensePlate, String modelName, double capacityM3) {
        this(null, licensePlate, modelName, capacityM3, VehicleStatus.AVAILABLE);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public double getCapacityM3() {
        return capacityM3;
    }

    public void setCapacityM3(double capacityM3) {
        this.capacityM3 = capacityM3;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status != null ? status : VehicleStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.US, "[ID: %d] Госномер: %s | Модель: %s | Объем: %.1f м³ | Статус: %s",
                id, licensePlate, modelName, capacityM3, status.getTitle());
    }
}
