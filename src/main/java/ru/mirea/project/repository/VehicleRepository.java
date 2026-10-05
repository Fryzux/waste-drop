package ru.mirea.project.repository;

import ru.mirea.project.model.Vehicle;

import java.util.Optional;

/**
 * Репозиторий доступа к автопарку спецтранспорта.
 */
public interface VehicleRepository extends CrudRepository<Vehicle, Long> {

    /**
     * Поиск единицы спецтранспорта по государственному регистрационному номеру.
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
}
