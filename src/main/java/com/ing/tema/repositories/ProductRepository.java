// src/main/java/com/example/store/product/ProductRepository.java
package com.ing.tema.repositories;

import com.ing.tema.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);
}
