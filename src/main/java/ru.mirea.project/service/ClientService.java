package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Client;
import ru.mirea.project.model.ClientType;
import ru.mirea.project.repository.ClientRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервисный слой управления клиентами (аналог UserService).
 * Инкапсулирует бизнес-правила валидации данных клиентов перед сохранением в репозиторий,
 * операции полного CRUD, сортировки и фильтрации на Stream API.
 */
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Регистрация нового клиента с проверкой бизнес-правил.
     */
    public Client registerClient(String name, String phone, String email, ClientType clientType) {
        validateName(name);
        validateEmail(email);

        Client client = new Client(null, name.trim(), phone.trim(), email.trim(), clientType != null ? clientType : ClientType.INDIVIDUAL);
        return clientRepository.save(client);
    }

    public Client registerClient(String name, String phone, String email) {
        return registerClient(name, phone, email, ClientType.INDIVIDUAL);
    }

    /**
     * Получение всех зарегистрированных клиентов.
     */
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    /**
     * Поиск клиента по идентификатору с гарантией наличия.
     */
    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с ID=" + id + " не найден в реестре!"));
    }

    /**
     * Обновление данных существующего клиента (Update).
     */
    public Client updateClient(Long id, String name, String phone, String email, ClientType clientType) {
        Client existing = getById(id);
        validateName(name);
        validateEmail(email);

        existing.setName(name.trim());
        existing.setPhone(phone.trim());
        existing.setEmail(email.trim());
        if (clientType != null) {
            existing.setClientType(clientType);
        }

        clientRepository.update(existing);
        return existing;
    }

    /**
     * Удаление клиента по ID (Delete).
     */
    public void deleteClient(Long id) {
        getById(id); // проверка существования
        clientRepository.deleteById(id);
    }

    /**
     * Поиск клиента по номеру телефона.
     */
    public Optional<Client> findByPhone(String phone) {
        return clientRepository.findByPhone(phone);
    }

    /**
     * Поиск клиента по адресу электронной почты.
     */
    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    // ==========================================================
    // СОРТИРОВКИ (STREAM API)
    // ==========================================================

    /**
     * Сортировка клиентов по наименованию (ФИО / Название компании).
     * @param ascending true - А-Я, false - Я-А
     */
    public List<Client> sortByName(boolean ascending) {
        Comparator<Client> comparator = Comparator.comparing(Client::getName, String.CASE_INSENSITIVE_ORDER);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllClients().stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * Сортировка клиентов по ID.
     * @param ascending true - по возрастанию ID, false - по убыванию ID
     */
    public List<Client> sortById(boolean ascending) {
        Comparator<Client> comparator = Comparator.comparing(Client::getId);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllClients().stream().sorted(comparator).collect(Collectors.toList());
    }

    // ==========================================================
    // ФИЛЬТРАЦИЯ (STREAM API)
    // ==========================================================

    /**
     * Фильтрация клиентов по типу организации / физического лица.
     */
    public List<Client> filterByType(ClientType type) {
        if (type == null) return getAllClients();
        return getAllClients().stream()
                .filter(c -> c.getClientType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Поиск клиентов по фрагменту имени или названия компании.
     */
    public List<Client> searchByName(String nameQuery) {
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            return getAllClients();
        }
        String q = nameQuery.trim().toLowerCase();
        return getAllClients().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    private void validateName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new BusinessException("Наименование клиента должно содержать не менее 2 символов!");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new BusinessException("Некорректный адрес электронной почты клиента: " + email);
        }
    }
}
