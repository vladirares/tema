package com.ing.tema.controllers;

import com.ing.tema.dtos.ChangePriceRequest;
import com.ing.tema.dtos.CreateProductRequest;
import com.ing.tema.dtos.ProductResponse;
import com.ing.tema.services.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    public static final String IDEMPOTENCY_HEADER = "Idempotency-Id";

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyId,
            @RequestBody CreateProductRequest request,
            Principal principal
    ) {
        log.debug("Create product request, sku={}, user={}", request.sku(), principal.getName());

        ProductResponse response = productService.createProduct(
                request,
                idempotencyId,
                principal,
                "/api/products"
        );

        URI location = URI.create("/api/products/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProducts() {
        List<ProductResponse> products = productService.listProducts();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<ProductResponse> changePrice(
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyId,
            @PathVariable Long id,
            @RequestBody ChangePriceRequest request,
            Principal principal
    ) {
        log.debug("Change price request, productId={}, user={}", id, principal.getName());

        ProductResponse response = productService.changePrice(
                id,
                request,
                idempotencyId,
                principal,
                "/api/products/" + id + "/price"
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader(IDEMPOTENCY_HEADER) String idempotencyId,
            @PathVariable Long id,
            Principal principal
    ) {
        log.debug("Delete product request, productId={}, user={}", id, principal.getName());

        productService.deleteProduct(
                id,
                idempotencyId,
                principal,
                "/api/products/" + id
        );

        return ResponseEntity.noContent().build();
    }
}
