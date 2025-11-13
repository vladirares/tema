package com.ing.tema.repositories;

import com.ing.tema.entities.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    boolean existsByKeyAndOwner(String key, String owner);
}
