package ru.mirea.wastemanagement.repository;

import ru.mirea.wastemanagement.model.Client;

import java.util.Optional;

/**
 * Репозиторий доступа к клиентам.
 */
public interface ClientRepository extends CrudRepository<Client, Long> {

    /**
     * Поиск клиента по номеру телефона.
     */
    Optional<Client> findByPhone(String phone);

    /**
     * Поиск клиента по адресу электронной почты.
     */
    Optional<Client> findByEmail(String email);
}
