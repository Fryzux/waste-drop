package ru.mirea.project.repository.impl;

import ru.mirea.project.exception.DatabaseOperationException;
import ru.mirea.project.model.Vehicle;
import ru.mirea.project.model.VehicleStatus;
import ru.mirea.project.repository.VehicleRepository;
import ru.mirea.project.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-реализация репозитория спецтранспорта (PostgreSQL).
 */
public class JdbcVehicleRepository implements VehicleRepository {
    private final DatabaseManager databaseManager;

    public JdbcVehicleRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (license_plate, model_name, capacity_m3, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, vehicle.getLicensePlate());
            ps.setString(2, vehicle.getModelName());
            ps.setDouble(3, vehicle.getCapacityM3());
            ps.setString(4, vehicle.getStatus().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    vehicle.setId(rs.getLong(1));
                }
            }
            return vehicle;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при сохранении спецтранспорта в БД: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        String sql = "SELECT id, license_plate, model_name, capacity_m3, status FROM vehicles WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при поиске транспорта по ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<Vehicle> findAll() {
        String sql = "SELECT id, license_plate, model_name, capacity_m3, status FROM vehicles ORDER BY id";
        List<Vehicle> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при получении списка транспорта: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public void update(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET license_plate = ?, model_name = ?, capacity_m3 = ?, status = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicle.getLicensePlate());
            ps.setString(2, vehicle.getModelName());
            ps.setDouble(3, vehicle.getCapacityM3());
            ps.setString(4, vehicle.getStatus().name());
            ps.setLong(5, vehicle.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при обновлении транспорта ID=" + vehicle.getId() + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при удалении транспорта ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        String sql = "SELECT id, license_plate, model_name, capacity_m3, status FROM vehicles WHERE license_plate = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, licensePlate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при поиске транспорта по госномеру: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getLong("id"),
                rs.getString("license_plate"),
                rs.getString("model_name"),
                rs.getDouble("capacity_m3"),
                VehicleStatus.valueOf(rs.getString("status"))
        );
    }
}
