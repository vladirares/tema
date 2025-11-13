package com.ing.tema.dtos;

import java.math.BigDecimal;

public record CreateProductRequest(
        String sku,
        String name,
        BigDecimal price,
        String currency,
        String description
) {
}
