package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.RequestStatus;
import ru.mirea.project.model.WasteRequest;
import ru.mirea.project.model.WasteType;
import ru.mirea.project.repository.ClientRepository;
import ru.mirea.project.repository.WasteRequestRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный слой управления заявками на вывоз отходов.
 * Инкапсулирует 5 ключевых бизнес-правил и операции обработки данных на Stream API.
 */
public class WasteRequestService {
    private final WasteRequestRepository requestRepository;
    private final ClientRepository clientRepository;

    public WasteRequestService(WasteRequestRepository requestRepository, ClientRepository clientRepository) {
        this.requestRepository = requestRepository;
        this.clientRepository = clientRepository;
    }

    /**
     * Создание новой заявки с проверкой бизнес-правил:
     * 1. Валидация адреса (не пустой, >= 5 символов).
     * 2. Валидация объема (0.1 <= V <= 100.0 м³).
     * 3. Проверка существования клиента по ID.
     */
    public WasteRequest createRequest(Long clientId, String address, WasteType wasteType, double volumeM3) {
        validateAddress(address);
        validateVolume(volumeM3);
        validateClientExists(clientId);

        WasteRequest request = new WasteRequest(
                null,
                clientId,
                address.trim(),
                wasteType,
                Math.round(volumeM3 * 100.0) / 100.0,
                RequestStatus.NEW,
                LocalDateTime.now()
        );

        return requestRepository.save(request);
    }

    /**
     * Смена статуса заявки с проверкой конечного автомата (FSM).
     * Запрещено изменять статус завершенной заявки.
     */
    public WasteRequest updateStatus(Long requestId, RequestStatus newStatus) {
        WasteRequest request = getById(requestId);

        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BusinessException("Заявка #" + requestId + " уже выполнена (COMPLETED)! Изменение статуса запрещено.");
        }

        if (!request.getStatus().canTransitionTo(newStatus)) {
            throw new BusinessException(String.format(
                    "Недопустимый переход статуса: из '%s' в '%s'!",
                    request.getStatus().getTitle(), newStatus.getTitle()));
        }

        request.setStatus(newStatus);
        requestRepository.update(request);
        return request;
    }

    /**
     * Удаление заявки по ID.
     * Бизнес-правило 4: Запрещено удалять выполненные (COMPLETED) заявки.
     */
    public void deleteRequest(Long requestId) {
        WasteRequest request = getById(requestId);

        if (request.getStatus() == RequestStatus.COMPLETED) {
            throw new BusinessException("Запрещено удалять выполненную заявку #" + requestId + " (требование бухгалтерского учета)!");
        }

        requestRepository.deleteById(requestId);
    }

    /**
     * Получение заявки по ID с гарантией наличия.
     */
    public WasteRequest getById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка с ID=" + requestId + " не найдена в реестре!"));
    }

    /**
     * Получение всех заявок в системе.
     */
    public List<WasteRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    // ==========================================================
    // ОПЕРАЦИИ STREAM API: ПОИСК, ФИЛЬТРАЦИЯ, СОРТИРОВКА
    // ==========================================================

    /**
     * Регистронезависимый поиск заявок по фрагменту адреса.
     */
    public List<WasteRequest> searchByAddress(String addressQuery) {
        if (addressQuery == null || addressQuery.trim().isEmpty()) {
            return getAllRequests();
        }
        String q = addressQuery.trim().toLowerCase();
        return getAllRequests().stream()
                .filter(r -> r.getAddress() != null && r.getAddress().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    /**
     * Поиск всех заявок конкретного клиента.
     */
    public List<WasteRequest> searchByClientId(Long clientId) {
        return getAllRequests().stream()
                .filter(r -> r.getClientId().equals(clientId))
                .collect(Collectors.toList());
    }

    /**
     * Фильтрация заявок по статусу.
     */
    public List<WasteRequest> filterByStatus(RequestStatus status) {
        if (status == null) return getAllRequests();
        return getAllRequests().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Фильтрация заявок по типу отходов.
     */
    public List<WasteRequest> filterByWasteType(WasteType wasteType) {
        if (wasteType == null) return getAllRequests();
        return getAllRequests().stream()
                .filter(r -> r.getWasteType() == wasteType)
                .collect(Collectors.toList());
    }

    /**
     * Фильтрация заявок по минимальному объему отходов (≥ minVolumeM3).
     * Бизнес-требование: позволяет отобрать крупные партии для приоритетного планирования.
     */
    public List<WasteRequest> filterByMinVolume(double minVolume) {
        return getAllRequests().stream()
                .filter(r -> r.getVolumeM3() >= minVolume)
                .sorted(Comparator.comparingDouble(WasteRequest::getVolumeM3).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Сортировка заявок по дате создания.
     * @param ascending true - от старых к новым, false - от новых к старым
     */
    public List<WasteRequest> sortByDate(boolean ascending) {
        Comparator<WasteRequest> comparator = Comparator.comparing(WasteRequest::getCreatedAt);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllRequests().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Сортировка заявок по объему отходов.
     * @param ascending true - по возрастанию, false - по убыванию
     */
    public List<WasteRequest> sortByVolume(boolean ascending) {
        Comparator<WasteRequest> comparator = Comparator.comparingDouble(WasteRequest::getVolumeM3);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllRequests().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Расчет 5 аналитических показателей реестра на Stream API.
     */
    public WasteRequestStats getStatistics() {
        List<WasteRequest> all = getAllRequests();

        long totalCount = all.size();

        long activeCount = all.stream()
                .filter(r -> r.getStatus() == RequestStatus.NEW || r.getStatus() == RequestStatus.IN_PROGRESS)
                .count();

        long completedCount = all.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();

        long cancelledCount = all.stream()
                .filter(r -> r.getStatus() == RequestStatus.CANCELLED)
                .count();

        double totalVolumeM3 = all.stream()
                .mapToDouble(WasteRequest::getVolumeM3)
                .sum();

        double averageVolumeM3 = all.stream()
                .mapToDouble(WasteRequest::getVolumeM3)
                .average()
                .orElse(0.0);

        return new WasteRequestStats(
                totalCount,
                activeCount,
                completedCount,
                cancelledCount,
                Math.round(totalVolumeM3 * 100.0) / 100.0,
                Math.round(averageVolumeM3 * 100.0) / 100.0
        );
    }

    // ==========================================================
    // ВНУТРЕННЯЯ ВАЛИДАЦИЯ БИЗНЕС-ПРАВИЛ
    // ==========================================================

    private void validateAddress(String address) {
        if (address == null || address.trim().length() < 5) {
            throw new BusinessException("Адрес вывоза отходов должен содержать не менее 5 символов!");
        }
    }

    private void validateVolume(double volumeM3) {
        if (volumeM3 < 0.1 || volumeM3 > 100.0) {
            throw new BusinessException(String.format(
                    "Недопустимый объем отходов (%.2f м³)! Разрешенный диапазон: от 0.1 до 100.0 м³.", volumeM3));
        }
    }

    private void validateClientExists(Long clientId) {
        if (clientId == null) {
            throw new BusinessException("Идентификатор клиента не может быть null!");
        }
        if (clientRepository.findById(clientId).isEmpty()) {
            throw new EntityNotFoundException("Клиент с указанным ID=" + clientId + " не существует в базе данных!");
        }
    }
}
