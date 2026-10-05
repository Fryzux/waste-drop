package ru.mirea.project.repository;

import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.WasteRequest;

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
