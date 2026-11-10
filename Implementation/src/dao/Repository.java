package dao;

import util.DataAccessException;
import util.ValidationException;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository Interface demonstrating Java Generics and Interface Abstraction.
 *
 * @param <T>  The domain entity type
 * @param <ID> The primary key identifier type
 */
public interface Repository<T, ID> {

    /**
     * Persists a new entity into the database.
     */
    T save(T entity) throws DataAccessException, ValidationException;

    /**
     * Finds an entity by its unique primary key identifier.
     */
    Optional<T> findById(ID id) throws DataAccessException;

    /**
     * Retrieves all entities from persistent storage.
     */
    List<T> findAll() throws DataAccessException;

    /**
     * Updates an existing entity.
     */
    boolean update(T entity) throws DataAccessException, ValidationException;

    /**
     * Deletes an entity by its primary key identifier.
     */
    boolean deleteById(ID id) throws DataAccessException;
}

