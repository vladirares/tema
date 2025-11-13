package com.ing.tema.exceptions;

public class ProductAlreadyExistsException extends RuntimeException {

    public ProductAlreadyExistsException(String sku) {
        super("Product already exists with sku: " + sku);
    }
}
