package com.ing.tema.services;

import com.ing.tema.entities.IdempotencyKeyEntity;
import com.ing.tema.exceptions.DuplicateIdempotencyKeyException;
import com.ing.tema.repositories.IdempotencyKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyKeyRepository repository;

    public IdempotencyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    /**
     * Register an idempotency key for a user or throw if already used.
     */
    @Transactional
    public void registerOrThrow(String key, String owner, String httpMethod, String path) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be null or blank");
        }

        try {
            if (repository.existsByKeyAndOwner(key, owner)) {
                log.warn("Idempotency key already used. key={}, owner={}", key, owner);
                throw new DuplicateIdempotencyKeyException(key);
            }

            IdempotencyKeyEntity entity = new IdempotencyKeyEntity(key, owner, httpMethod, path);
            repository.save(entity);

            log.debug("Registered idempotency key. key={}, owner={}", key, owner);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Data integrity violation for idempotency key. key={}, owner={}", key, owner);
            throw new DuplicateIdempotencyKeyException(key);
        }
    }
}
