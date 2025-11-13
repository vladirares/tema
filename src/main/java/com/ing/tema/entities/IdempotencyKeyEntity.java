
package com.ing.tema.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "idempotency_keys",
        uniqueConstraints = @UniqueConstraint(
                name = "uc_idempotency_key_owner",
                columnNames = {"idempotency_key", "owner"}
        )
)
public class IdempotencyKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String key;

    @Column(name = "owner", nullable = false, length = 64)
    private String owner;

    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod;

    @Column(name = "path", nullable = false, length = 255)
    private String path;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public IdempotencyKeyEntity() {
    }

    public IdempotencyKeyEntity(String key, String owner, String httpMethod, String path) {
        this.key = key;
        this.owner = owner;
        this.httpMethod = httpMethod;
        this.path = path;
        this.createdAt = Instant.now();
    }

    

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getOwner() {
        return owner;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
