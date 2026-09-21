package ru.mirea.wastemanagement.model;

import java.util.Objects;

/**
 * Доменная сущность клиента (заказчика услуг по вывозу отходов).
 */
public class Client {
    private Long id;
    private String name;
    private String phone;
    private String email;

    public Client() {
    }

    public Client(Long id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public Client(String name, String phone, String email) {
        this(null, name, phone, email);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[ID: %d] %s | Тел: %s | Email: %s", id, name, phone, email);
    }
}
