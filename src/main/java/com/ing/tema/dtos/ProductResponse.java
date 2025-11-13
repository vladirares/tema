package com.ing.tema.dtos;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        String currency,
        String description
) {
}
