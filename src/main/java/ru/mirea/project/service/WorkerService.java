package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Worker;
import ru.mirea.project.model.WorkerRole;
import ru.mirea.project.repository.WorkerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный слой управления персоналом и экипажами спецтехники.
 * Инкапсулирует операции полного CRUD, прямую и обратную сортировку,
 * а также фильтрацию и валидацию данных через Stream API.
 */
public class WorkerService {
    private final WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    /**
     * Регистрация нового сотрудника в штате.
     */
    public Worker registerWorker(String fullName, String phone, WorkerRole role, double salary) {
        validateFullName(fullName);
        validateSalary(salary);

        Worker worker = new Worker(null, fullName.trim(), phone.trim(),
                role != null ? role : WorkerRole.DRIVER,
                Math.round(salary * 100.0) / 100.0);
        return workerRepository.save(worker);
    }

    /**
     * Получение всех сотрудников.
     */
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    /**
     * Поиск сотрудника по ID с гарантией наличия.
     */
    public Worker getById(Long id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник с ID=" + id + " не найден в штатном расписании!"));
    }

    /**
     * Обновление данных сотрудника.
     */
    public Worker updateWorker(Long id, String fullName, String phone, WorkerRole role, double salary) {
        Worker worker = getById(id);
        validateFullName(fullName);
        validateSalary(salary);

        worker.setFullName(fullName.trim());
        worker.setPhone(phone.trim());
        if (role != null) {
            worker.setRole(role);
        }
        worker.setSalary(Math.round(salary * 100.0) / 100.0);

        workerRepository.update(worker);
        return worker;
    }

    /**
     * Удаление (увольнение) сотрудника из базы.
     */
    public void deleteWorker(Long id) {
        getById(id); // проверка существования
        workerRepository.deleteById(id);
    }

    // ==========================================================
    // СОРТИРОВКИ (STREAM API)
    // ==========================================================

    /**
     * Сортировка сотрудников по ФИО.
     * @param ascending true - А-Я, false - Я-А
     */
    public List<Worker> sortByName(boolean ascending) {
        Comparator<Worker> comparator = Comparator.comparing(Worker::getFullName, String.CASE_INSENSITIVE_ORDER);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllWorkers().stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * Сортировка сотрудников по размеру оклада.
     * @param ascending true - по возрастанию, false - по убыванию
     */
    public List<Worker> sortBySalary(boolean ascending) {
        Comparator<Worker> comparator = Comparator.comparingDouble(Worker::getSalary);
        if (!ascending) {
            comparator = comparator.reversed();
        }
        return getAllWorkers().stream().sorted(comparator).collect(Collectors.toList());
    }

    // ==========================================================
    // ФИЛЬТРАЦИЯ (STREAM API)
    // ==========================================================

    /**
     * Фильтрация сотрудников по занимаемой должности.
     */
    public List<Worker> filterByRole(WorkerRole role) {
        if (role == null) return getAllWorkers();
        return getAllWorkers().stream()
                .filter(w -> w.getRole() == role)
                .collect(Collectors.toList());
    }

    /**
     * Поиск сотрудников по фрагменту ФИО.
     */
    public List<Worker> searchByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllWorkers();
        }
        String q = query.trim().toLowerCase();
        return getAllWorkers().stream()
                .filter(w -> w.getFullName() != null && w.getFullName().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().length() < 3) {
            throw new BusinessException("ФИО сотрудника должно содержать не менее 3 символов!");
        }
    }

    private void validateSalary(double salary) {
        if (salary < 20000.0 || salary > 500000.0) {
            throw new BusinessException(String.format("Недопустимый оклад (%.2f руб.)! Разрешенный диапазон: от 20 000 до 500 000 руб.", salary));
        }
    }
}
