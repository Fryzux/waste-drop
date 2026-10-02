package ru.mirea.project.repository;

import java.util.List;
import java.util.Optional;

/**
 * Базовый контракт типобезопасного CRUD-репозитория (Repository Pattern).
 *
 * @param <T>  Тип доменной сущности
 * @param <ID> Тип первичного ключа
 */
public interface CrudRepository<T, ID> {

    /**
     * Сохранение новой сущности в хранилище.
     * @return Сохраненная сущность с присвоенным ID.
     */
    T save(T entity);

    /**
     * Поиск сущности по уникальному идентификатору.
     */
    Optional<T> findById(ID id);

    /**
     * Получение всех записей сущности.
     */
    List<T> findAll();

    /**
     * Обновление существующей записи.
     */
    void update(T entity);

    /**
     * Удаление записи по идентификатору.
     * @return true, если запись была найдена и успешно удалена.
     */
    boolean deleteById(ID id);
}
