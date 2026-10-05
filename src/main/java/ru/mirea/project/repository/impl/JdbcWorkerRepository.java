package ru.mirea.project.repository.impl;

import ru.mirea.project.exception.DatabaseOperationException;
import ru.mirea.project.model.Worker;
import ru.mirea.project.model.WorkerRole;
import ru.mirea.project.repository.WorkerRepository;
import ru.mirea.project.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-реализация репозитория сотрудников (PostgreSQL).
 */
public class JdbcWorkerRepository implements WorkerRepository {
    private final DatabaseManager databaseManager;

    public JdbcWorkerRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Worker save(Worker worker) {
        String sql = "INSERT INTO workers (full_name, phone, role, salary) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, worker.getFullName());
            ps.setString(2, worker.getPhone());
            ps.setString(3, worker.getRole().name());
            ps.setDouble(4, worker.getSalary());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    worker.setId(rs.getLong(1));
                }
            }
            return worker;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при сохранении сотрудника в БД: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Optional<Worker> findById(Long id) {
        String sql = "SELECT id, full_name, phone, role, salary FROM workers WHERE id = ?";
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
            throw new DatabaseOperationException("Ошибка при поиске сотрудника по ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<Worker> findAll() {
        String sql = "SELECT id, full_name, phone, role, salary FROM workers ORDER BY id";
        List<Worker> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при получении списка сотрудников: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public void update(Worker worker) {
        String sql = "UPDATE workers SET full_name = ?, phone = ?, role = ?, salary = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, worker.getFullName());
            ps.setString(2, worker.getPhone());
            ps.setString(3, worker.getRole().name());
            ps.setDouble(4, worker.getSalary());
            ps.setLong(5, worker.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при обновлении сотрудника ID=" + worker.getId() + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM workers WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при удалении сотрудника ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<Worker> findByRole(WorkerRole role) {
        String sql = "SELECT id, full_name, phone, role, salary FROM workers WHERE role = ? ORDER BY id";
        List<Worker> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при поиске сотрудников по должности: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    private Worker mapRow(ResultSet rs) throws SQLException {
        return new Worker(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                WorkerRole.valueOf(rs.getString("role")),
                rs.getDouble("salary")
        );
    }
}
