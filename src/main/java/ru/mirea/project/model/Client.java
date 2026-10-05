package ru.mirea.project.model;

import java.util.Objects;

/**
 * Доменная сущность клиента (заказчика услуг по вывозу отходов).
 */
public class Client {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private ClientType clientType = ClientType.INDIVIDUAL;

    public Client() {
    }

    public Client(Long id, String name, String phone, String email, ClientType clientType) {
        this.id = id;
        this.name = name;
        setPhone(phone);
        this.email = email;
        this.clientType = clientType != null ? clientType : ClientType.INDIVIDUAL;
    }

    public Client(Long id, String name, String phone, String email) {
        this(id, name, phone, email, ClientType.INDIVIDUAL);
    }

    public Client(String name, String phone, String email) {
        this(null, name, phone, email, ClientType.INDIVIDUAL);
    }

    public Client(String name, String phone, String email, ClientType clientType) {
        this(null, name, phone, email, clientType);
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
        if (phone == null) {
            throw new IllegalArgumentException("Номер телефона не может быть null!");
        }
        long digitCount = phone.chars().filter(Character::isDigit).count();
        if (digitCount < 10) {
            throw new IllegalArgumentException("Номер телефона должен содержать не менее 10 цифр: " + phone);
        }
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType != null ? clientType : ClientType.INDIVIDUAL;
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
        return String.format("[ID: %d] %s (%s) | Тел: %s | Email: %s", id, name, clientType.getTitle(), phone, email);
    }
}
