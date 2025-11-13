package com.ing.tema.dtos;

import java.math.BigDecimal;

public record ChangePriceRequest(
        BigDecimal newPrice
) {
}
