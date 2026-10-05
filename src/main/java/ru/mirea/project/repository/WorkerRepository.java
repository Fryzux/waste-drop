package ru.mirea.project.repository;

import ru.mirea.project.model.Worker;
import ru.mirea.project.model.WorkerRole;

import java.util.List;

/**
 * Репозиторий доступа к данным сотрудников регионального оператора.
 */
public interface WorkerRepository extends CrudRepository<Worker, Long> {

    /**
     * Поиск сотрудников по занимаемой должности.
     */
    List<Worker> findByRole(WorkerRole role);
}
