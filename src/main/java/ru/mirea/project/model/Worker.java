package ru.mirea.project.model;

import java.util.Locale;
import java.util.Objects;

/**
 * Доменная сущность сотрудника оператора (водитель, грузчик, диспетчер).
 */
public class Worker {
    private Long id;
    private String fullName;
    private String phone;
    private WorkerRole role = WorkerRole.DRIVER;
    private double salary;

    public Worker() {
    }

    public Worker(Long id, String fullName, String phone, WorkerRole role, double salary) {
        this.id = id;
        this.fullName = fullName;
        setPhone(phone);
        this.role = role != null ? role : WorkerRole.DRIVER;
        this.salary = salary;
    }

    public Worker(String fullName, String phone, WorkerRole role, double salary) {
        this(null, fullName, phone, role, salary);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public WorkerRole getRole() {
        return role;
    }

    public void setRole(WorkerRole role) {
        this.role = role != null ? role : WorkerRole.DRIVER;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return Objects.equals(id, worker.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[ID: %d] %s | Должность: %s | Тел: %s | Оклад: %.2f руб.",
                id, fullName, role.getTitle(), phone, salary);
    }
}
