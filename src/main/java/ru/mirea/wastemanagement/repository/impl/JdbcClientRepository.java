package ru.mirea.wastemanagement.repository.impl;

import ru.mirea.wastemanagement.exception.DatabaseOperationException;
import ru.mirea.wastemanagement.model.Client;
import ru.mirea.wastemanagement.repository.ClientRepository;
import ru.mirea.wastemanagement.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-реализация репозитория клиентов.
 * Реализует строгое управление ресурсами через try-with-resources
 * и безопасные параметризованные запросы PreparedStatement.
 */
public class JdbcClientRepository implements ClientRepository {
    private final DatabaseManager databaseManager;

    public JdbcClientRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public Client save(Client client) {
        String sql = "INSERT INTO clients (name, phone, email) VALUES (?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getEmail());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    client.setId(rs.getLong(1));
                }
            }
            return client;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при сохранении клиента в БД: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Optional<Client> findById(Long id) {
        String sql = "SELECT id, name, phone, email FROM clients WHERE id = ?";
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
            throw new DatabaseOperationException("Ошибка при поиске клиента по ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT id, name, phone, email FROM clients ORDER BY id";
        List<Client> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при получении списка клиентов: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Optional<Client> findByPhone(String phone) {
        String sql = "SELECT id, name, phone, email FROM clients WHERE phone = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при поиске клиента по телефону: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        String sql = "SELECT id, name, phone, email FROM clients WHERE email = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при поиске клиента по email: " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public void update(Client client) {
        String sql = "UPDATE clients SET name = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getPhone());
            ps.setString(3, client.getEmail());
            ps.setLong(4, client.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Ошибка при обновлении клиента ID=" + client.getId() + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new DatabaseOperationException("Невозможно удалить клиента ID=" + id + 
                        ", так как за ним числятся оформленные заявки (ограничение ON DELETE RESTRICT)!", e.getSQLState(), e);
            }
            throw new DatabaseOperationException("Ошибка при удалении клиента ID=" + id + ": " + e.getMessage(), e.getSQLState(), e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email")
        );
    }
}
