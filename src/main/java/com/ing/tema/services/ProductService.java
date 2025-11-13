package com.ing.tema.services;

import com.ing.tema.dtos.ChangePriceRequest;
import com.ing.tema.dtos.CreateProductRequest;
import com.ing.tema.dtos.ProductResponse;
import com.ing.tema.entities.Product;
import com.ing.tema.exceptions.ProductAlreadyExistsException;
import com.ing.tema.exceptions.ProductNotFoundException;
import com.ing.tema.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final IdempotencyService idempotencyService;

    public ProductService(ProductRepository productRepository,
                          IdempotencyService idempotencyService) {
        this.productRepository = productRepository;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request,
                                         String idempotencyKey,
                                         Principal principal,
                                         String path) {
        String owner = principal.getName();
        idempotencyService.registerOrThrow(idempotencyKey, owner, "POST", path);

        if (productRepository.existsBySku(request.sku())) {
            throw new ProductAlreadyExistsException(request.sku());
        }

        Product product = new Product(
                request.sku(),
                request.name(),
                request.price(),
                request.currency(),
                request.description()
        );

        product = productRepository.save(product);
        log.info("Created product id={} sku={}", product.getId(), product.getSku());

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse changePrice(Long id,
                                       ChangePriceRequest request,
                                       String idempotencyKey,
                                       Principal principal,
                                       String path) {
        String owner = principal.getName();
        idempotencyService.registerOrThrow(idempotencyKey, owner, "PUT", path);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setPrice(request.newPrice());
        product = productRepository.save(product);
        log.info("Changed price for product id={} newPrice={}", product.getId(), product.getPrice());

        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id,
                              String idempotencyKey,
                              Principal principal,
                              String path) {
        String owner = principal.getName();
        idempotencyService.registerOrThrow(idempotencyKey, owner, "DELETE", path);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);
        log.info("Deleted product id={}", id);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getCurrency(),
                product.getDescription()
        );
    }
}
