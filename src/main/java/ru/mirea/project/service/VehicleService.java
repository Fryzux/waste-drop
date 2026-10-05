package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Vehicle;
import ru.mirea.project.model.VehicleStatus;
import ru.mirea.project.repository.VehicleRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервисный слой управления автопарком спецтехники.
 * Инкапсулирует бизнес-правила допуска спецтранспорта, операции полного CRUD,
 * прямую и обратную сортировку, а также фильтрацию через Stream API.
 */
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Регистрация нового спецтранспорта в автопарке.
     */
    public Vehicle registerVehicle(String licensePlate, String modelName, double capacityM3, VehicleStatus status) {
        validateLicensePlate(licensePlate);
        validateModelName(modelName);
        validateCapacity(capacityM3);

        String plate = licensePlate.trim().toUpperCase();
        if (vehicleRepository.findByLicensePlate(plate).isPresent()) {
            throw new BusinessException("Спецтранспорт с госномером " + plate + " уже зарегистрирован в базе!");
        }

        Vehicle vehicle = new Vehicle(null, plate, modelName.trim(), Math.round(capacityM3 * 100.0) / 100.0,
                status != null ? status : VehicleStatus.AVAILABLE);
        return vehicleRepository.save(vehicle);
    }

    /**
     * Получение всех единиц спецтранспорта.
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Поиск единицы транспорта по ID с гарантией наличия.
     */
    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Спецтранспорт с ID=" + id + " не найден в автопарке!"));
    }

    /**
     * Обновление данных спецтранспорта.
     */
    public Vehicle updateVehicle(Long id, String licensePlate, String modelName, double capacityM3, VehicleStatus status) {
        Vehicle vehicle = getById(id);
        validateLicensePlate(licensePlate);
        validateModelName(modelName);
        validateCapacity(capacityM3);

        vehicle.setLicensePlate(licensePlate.trim().toUpperCase());
        vehicle.setModelName(modelName.trim());
        vehicle.setCapacityM3(Math.round(capacityM3 * 100.0) / 100.0);
        if (status != null) {
            vehicle.setStatus(status);
        }

        vehicleRepository.update(vehicle);
        return vehicle;
    }

    /**
     * Смена статуса спецтранспорта (например, отправка на ТО или в рейс).
     */
    public Vehicle updateStatus(Long id, VehicleStatus newStatus) {
        Vehicle vehicle = getById(id);
        vehicle.setStatus(newStatus);
        vehicleRepository.update(vehicle);
        return vehicle;
    }

    /**
     * Удаление единицы транспорта из базы.
     * Бизнес-правило: нельзя удалять машину, которая прямо сейчас находится на маршруте.
     */
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getById(id);
        if (vehicle.getStatus() == VehicleStatus.ON_ROUTE) {
            throw new BusinessException("Запрещено удалять транспорт #" + id + ", находящийся на маршруте в рейсе!");
        }
        vehicleRepository.deleteById(id);
    }

    /**
     * Поиск по государственному номеру.
     */
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        if (licensePlate == null) return Optional.empty();
        return vehicleRepository.findByLicensePlate(licensePlate.trim().toUpperCase());
    }

    // ==========================================================
    // СОРТИРОВКИ (STREAM API)
    // ==========================================================

    /**
     * Сортировка транспорта по вместимости кузова (м³).
     * @param ascending true - по возрастанию объема, false - по убыванию объема
     */
    public List<Vehicle> sortByCapacity(boolean ascending) {
        Comparator<Vehicle> comparator = Comparator.comparingDouble(Vehicle::getCapacityM3);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllVehicles().stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * Сортировка транспорта по государственному регистрационному номеру.
     * @param ascending true - А-Я, false - Я-А
     */
    public List<Vehicle> sortByLicensePlate(boolean ascending) {
        Comparator<Vehicle> comparator = Comparator.comparing(Vehicle::getLicensePlate);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllVehicles().stream().sorted(comparator).collect(Collectors.toList());
    }

    // ==========================================================
    // ФИЛЬТРАЦИЯ (STREAM API)
    // ==========================================================

    /**
     * Фильтрация транспорта по статусу (например, показать только свободные машины).
     */
    public List<Vehicle> filterByStatus(VehicleStatus status) {
        if (status == null) return getAllVehicles();
        return getAllVehicles().stream()
                .filter(v -> v.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Фильтрация транспорта по минимальной грузоподъемности (>= minCapacity).
     */
    public List<Vehicle> filterByMinCapacity(double minCapacity) {
        return getAllVehicles().stream()
                .filter(v -> v.getCapacityM3() >= minCapacity)
                .collect(Collectors.toList());
    }

    // ==========================================================
    // ВАЛИДАЦИЯ БИЗНЕС-ПРАВИЛ
    // ==========================================================

    private void validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.trim().length() < 6) {
            throw new BusinessException("Госномер спецтранспорта должен содержать не менее 6 символов!");
        }
    }

    private void validateModelName(String modelName) {
        if (modelName == null || modelName.trim().length() < 2) {
            throw new BusinessException("Название модели транспорта должно содержать не менее 2 символов!");
        }
    }

    private void validateCapacity(double capacityM3) {
        if (capacityM3 < 1.0 || capacityM3 > 60.0) {
            throw new BusinessException(String.format("Недопустимая вместимость (%.1f м³)! Разрешено: от 1.0 до 60.0 м³.", capacityM3));
        }
    }
}
