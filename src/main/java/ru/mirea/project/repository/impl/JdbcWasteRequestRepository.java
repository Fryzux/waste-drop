package ru.mirea.project.repository.impl;

import ru.mirea.project.exception.DatabaseOperationException;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.WasteRequest;
import ru.mirea.project.model.WasteType;
import ru.mirea.project.repository.WasteRequestRepository;
import ru.mirea.project.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-реализация репозитория заявок на вывоз отходов.
 * Защита от утечек соединений (try-with-resources) и маппинг SQL-ошибок.
 */
public class JdbcWasteRequestRepository implements WasteRequestRepository {
    private final DatabaseManager databaseManager;

    private static final String SELECT_BASE = 
            "SELECT r.id, r.client_id, r.address, r.waste_type, r.volume_m3, r.status, r.created_at, c.name AS client_name " +
            "FROM waste_requests r " +
            "LEFT JOIN clients c ON r.client_id = c.id ";

    public JdbcWasteRequestRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public WasteRequest save(WasteRequest request) {
        String sql = "INSERT INTO waste_requests (client_id, address, waste_type, volume_m3, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, request.getClientId());
            ps.setString(2, request.getAddress());
            ps.setString(3, request.getWasteType().name());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(request.getVolumeM3()));
            ps.setString(5, request.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(request.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    request.setId(rs.getLong(1));
                }
            }
            return request;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new DatabaseOperationException("Клиент с указанным ID=" + request.getClientId() + 
                        " не существует в базе данных!", e.getSQLState(), e);
            }
            if ("23514".equals(e.getSQLState())) {
                throw new DatabaseOperationException("Нарушено ограничение СУБД: объем отходов должен быть от 0.1 до 100.0 м³!", 
                        e.getSQLState(), e);
            }
            throw new DatabaseOperationException("Ошибка при сохранении заявки в БД: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Optional<WasteRequest> findById(Long id) {
        String sql = SELECT_BASE + "WHERE r.id = ?";
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
            throw new DatabaseOperationException("Ошибка при поиске заявки ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<WasteRequest> findAll() {
        String sql = SELECT_BASE + "ORDER BY r.id";
        List<WasteRequest> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при чтении реестра заявок: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<WasteRequest> findByClientId(Long clientId) {
        String sql = SELECT_BASE + "WHERE r.client_id = ? ORDER BY r.id";
        List<WasteRequest> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при поиске заявок клиента ID=" + clientId + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<WasteRequest> findByStatus(RequestStatus status) {
        String sql = SELECT_BASE + "WHERE r.status = ? ORDER BY r.id";
        List<WasteRequest> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при выборке заявок по статусу: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public void update(WasteRequest request) {
        String sql = "UPDATE waste_requests SET client_id = ?, address = ?, waste_type = ?, " +
                     "volume_m3 = ?, status = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, request.getClientId());
            ps.setString(2, request.getAddress());
            ps.setString(3, request.getWasteType().name());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(request.getVolumeM3()));
            ps.setString(5, request.getStatus().name());
            ps.setLong(6, request.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при обновлении заявки ID=" + request.getId() + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM waste_requests WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при удалении заявки ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    private WasteRequest mapRow(ResultSet rs) throws SQLException {
        WasteRequest req = new WasteRequest(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("address"),
                WasteType.valueOf(rs.getString("waste_type")),
                rs.getBigDecimal("volume_m3").doubleValue(),
                RequestStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
        req.setClientName(rs.getString("client_name"));
        return req;
    }
}
