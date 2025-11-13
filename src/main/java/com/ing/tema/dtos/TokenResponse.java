package com.ing.tema.dtos;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
