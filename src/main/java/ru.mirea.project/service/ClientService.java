package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Client;
import ru.mirea.project.repository.ClientRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой управления клиентами (аналог UserService).
 * Инкапсулирует бизнес-правила валидации данных клиентов перед сохранением в репозиторий.
 */
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Регистрация нового клиента с проверкой бизнес-правил.
     */
    public Client registerClient(String name, String phone, String email) {
        validateName(name);
        validateEmail(email);

        Client client = new Client(null, name.trim(), phone.trim(), email.trim());
        return clientRepository.save(client);
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
