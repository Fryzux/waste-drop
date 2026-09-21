package ru.mirea.wastemanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.wastemanagement.exception.BusinessException;
import ru.mirea.wastemanagement.exception.EntityNotFoundException;
import ru.mirea.wastemanagement.model.Client;
import ru.mirea.wastemanagement.model.RequestStatus;
import ru.mirea.wastemanagement.model.WasteRequest;
import ru.mirea.wastemanagement.model.WasteType;
import ru.mirea.wastemanagement.repository.ClientRepository;
import ru.mirea.wastemanagement.repository.WasteRequestRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class WasteRequestServiceTest {

    private WasteRequestService service;
    private InMemoryClientRepository clientRepository;
    private InMemoryWasteRequestRepository requestRepository;

    @BeforeEach
    void setUp() {
        clientRepository = new InMemoryClientRepository();
        requestRepository = new InMemoryWasteRequestRepository();
        service = new WasteRequestService(requestRepository, clientRepository);

        // Добавляем тестового клиента
        clientRepository.save(new Client(1L, "ООО Тест", "+79991234567", "test@test.ru"));
    }

    @Test
    @DisplayName("Успешное создание заявки при валидных данных")
    void testCreateRequestSuccess() {
        WasteRequest req = service.createRequest(1L, "г. Москва, ул. Удальцова, 10", WasteType.MUNICIPAL, 15.5);
        assertNotNull(req);
        assertEquals(1L, req.getClientId());
        assertEquals(15.5, req.getVolumeM3());
        assertEquals(RequestStatus.NEW, req.getStatus());
        assertEquals("г. Москва, ул. Удальцова, 10", req.getAddress());
    }

    @Test
    @DisplayName("Ошибка валидации объема: объем < 0.1 м³")
    void testCreateRequestVolumeTooLow() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                service.createRequest(1L, "г. Москва, ул. Удальцова, 10", WasteType.MUNICIPAL, 0.05));
        assertTrue(ex.getMessage().contains("Недопустимый объем"));
    }

    @Test
    @DisplayName("Ошибка валидации объема: объем > 100.0 м³")
    void testCreateRequestVolumeTooHigh() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                service.createRequest(1L, "г. Москва, ул. Удальцова, 10", WasteType.MUNICIPAL, 105.0));
        assertTrue(ex.getMessage().contains("Недопустимый объем"));
    }

    @Test
    @DisplayName("Ошибка валидации адреса: менее 5 символов")
    void testCreateRequestAddressTooShort() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                service.createRequest(1L, "Мск", WasteType.MUNICIPAL, 10.0));
        assertTrue(ex.getMessage().contains("не менее 5 символов"));
    }

    @Test
    @DisplayName("Ошибка: Клиент не существует в базе")
    void testCreateRequestClientNotFound() {
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                service.createRequest(999L, "г. Москва, ул. Удальцова, 10", WasteType.MUNICIPAL, 10.0));
        assertTrue(ex.getMessage().contains("не существует в базе данных"));
    }

    @Test
    @DisplayName("Смена статуса: допустимый переход NEW -> IN_PROGRESS -> COMPLETED")
    void testValidStatusTransitions() {
        WasteRequest req = service.createRequest(1L, "г. Москва, ул. Удальцова, 10", WasteType.MUNICIPAL, 10.0);
        
        WasteRequest inProgress = service.updateStatus(req.getId(), RequestStatus.IN_PROGRESS);
        assertEquals(RequestStatus.IN_PROGRESS, inProgress.getStatus());

        WasteRequest completed = service.updateStatus(req.getId(), RequestStatus.COMPLETED);
        assertEquals(RequestStatus.COMPLETED, completed.getStatus());
    }

    @Test
    @DisplayName("Запрет смены статуса и удаления выполненной заявки (COMPLETED)")
    void testCompletedRequestImmutability() {
        WasteRequest req = service.createRequest(1L, "г. Москва, ул. Удальцова, 10", WasteType.MUNICIPAL, 10.0);
        service.updateStatus(req.getId(), RequestStatus.IN_PROGRESS);
        service.updateStatus(req.getId(), RequestStatus.COMPLETED);

        // Попытка сменить статус после COMPLETED
        assertThrows(BusinessException.class, () ->
                service.updateStatus(req.getId(), RequestStatus.CANCELLED));

        // Попытка удалить COMPLETED заявку
        assertThrows(BusinessException.class, () ->
                service.deleteRequest(req.getId()));
    }

    @Test
    @DisplayName("Расчет аналитической статистики (Stream API)")
    void testStatisticsCalculation() {
        service.createRequest(1L, "Адрес 1 длинный", WasteType.MUNICIPAL, 10.0);
        WasteRequest r2 = service.createRequest(1L, "Адрес 2 длинный", WasteType.CONSTRUCTION, 20.0);
        service.updateStatus(r2.getId(), RequestStatus.IN_PROGRESS);
        service.updateStatus(r2.getId(), RequestStatus.COMPLETED);

        WasteRequestStats stats = service.getStatistics();
        assertEquals(2, stats.getTotalCount());
        assertEquals(1, stats.getActiveCount());
        assertEquals(1, stats.getCompletedCount());
        assertEquals(30.0, stats.getTotalVolumeM3(), 0.01);
        assertEquals(15.0, stats.getAverageVolumeM3(), 0.01);
    }

    // =========================================================================
    // IN-MEMORY STUB REPOSITORIES FOR FAST INDEPENDENT UNIT TESTING
    // =========================================================================

    private static class InMemoryClientRepository implements ClientRepository {
        private final Map<Long, Client> storage = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public Client save(Client client) {
            if (client.getId() == null) client.setId(seq.getAndIncrement());
            storage.put(client.getId(), client);
            return client;
        }

        @Override
        public Optional<Client> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Client> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Optional<Client> findByPhone(String phone) {
            return storage.values().stream().filter(c -> c.getPhone().equals(phone)).findFirst();
        }

        @Override
        public void update(Client client) {
            storage.put(client.getId(), client);
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }

    private static class InMemoryWasteRequestRepository implements WasteRequestRepository {
        private final Map<Long, WasteRequest> storage = new HashMap<>();
        private final AtomicLong seq = new AtomicLong(1);

        @Override
        public WasteRequest save(WasteRequest entity) {
            if (entity.getId() == null) entity.setId(seq.getAndIncrement());
            storage.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public Optional<WasteRequest> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<WasteRequest> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public List<WasteRequest> findByClientId(Long clientId) {
            List<WasteRequest> list = new ArrayList<>();
            for (WasteRequest r : storage.values()) {
                if (r.getClientId().equals(clientId)) list.add(r);
            }
            return list;
        }

        @Override
        public List<WasteRequest> findByStatus(RequestStatus status) {
            List<WasteRequest> list = new ArrayList<>();
            for (WasteRequest r : storage.values()) {
                if (r.getStatus() == status) list.add(r);
            }
            return list;
        }

        @Override
        public void update(WasteRequest entity) {
            storage.put(entity.getId(), entity);
        }

        @Override
        public boolean deleteById(Long id) {
            return storage.remove(id) != null;
        }
    }
}
