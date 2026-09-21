package ru.mirea.wastemanagement.repository;

import ru.mirea.wastemanagement.model.RequestStatus;
import ru.mirea.wastemanagement.model.WasteRequest;

import java.util.List;

/**
 * Репозиторий доступа к заявкам на вывоз отходов.
 */
public interface WasteRequestRepository extends CrudRepository<WasteRequest, Long> {

    /**
     * Поиск всех заявок конкретного клиента.
     */
    List<WasteRequest> findByClientId(Long clientId);

    /**
     * Поиск заявок по определенному статусу.
     */
    List<WasteRequest> findByStatus(RequestStatus status);
}
