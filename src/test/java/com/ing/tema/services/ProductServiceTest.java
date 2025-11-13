package com.ing.tema.services;

import com.ing.tema.dtos.ChangePriceRequest;
import com.ing.tema.dtos.CreateProductRequest;
import com.ing.tema.dtos.ProductResponse;
import com.ing.tema.entities.Product;
import com.ing.tema.exceptions.DuplicateIdempotencyKeyException;
import com.ing.tema.exceptions.ProductAlreadyExistsException;
import com.ing.tema.exceptions.ProductNotFoundException;
import com.ing.tema.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private ProductService productService;

    private Principal principal(String name) {
        return () -> name;
    }


    @Test
    void createProduct_shouldCreateProduct_whenValidRequestAndNewSku() {
        
        CreateProductRequest request = new CreateProductRequest(
                "SKU-123",
                "Test Product",
                BigDecimal.valueOf(10.50),
                "EUR",
                "Nice product"
        );
        String idempotencyKey = "idem-1";
        Principal principal = principal("admin");
        String path = "/api/products";

        when(productRepository.existsBySku("SKU-123")).thenReturn(false);

        
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        
        ProductResponse response = productService.createProduct(request, idempotencyKey, principal, path);

        
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("SKU-123", response.sku());
        assertEquals("Test Product", response.name());
        assertEquals(BigDecimal.valueOf(10.50), response.price());
        assertEquals("EUR", response.currency());

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "POST", path);
        verify(productRepository).existsBySku("SKU-123");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowProductAlreadyExists_whenSkuExists() {
        
        CreateProductRequest request = new CreateProductRequest(
                "SKU-123",
                "Test Product",
                BigDecimal.TEN,
                "EUR",
                "Nice product"
        );
        String idempotencyKey = "idem-1";
        Principal principal = principal("admin");
        String path = "/api/products";

        when(productRepository.existsBySku("SKU-123")).thenReturn(true);

        assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.createProduct(request, idempotencyKey, principal, path)
        );

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "POST", path);
        verify(productRepository).existsBySku("SKU-123");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_shouldPropagateDuplicateIdempotencyKeyException() {
        
        CreateProductRequest request = new CreateProductRequest(
                "SKU-123",
                "Test Product",
                BigDecimal.TEN,
                "EUR",
                "Nice product"
        );
        String idempotencyKey = "idem-1";
        Principal principal = principal("admin");
        String path = "/api/products";

        doThrow(new DuplicateIdempotencyKeyException(idempotencyKey))
                .when(idempotencyService)
                .registerOrThrow(idempotencyKey, "admin", "POST", path);

        assertThrows(
                DuplicateIdempotencyKeyException.class,
                () -> productService.createProduct(request, idempotencyKey, principal, path)
        );

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "POST", path);
        verifyNoInteractions(productRepository);
    }


    @Test
    void getProductById_shouldReturnProduct_whenExists() {
        
        Product product = new Product("SKU-1", "Name", BigDecimal.ONE, "EUR", "Desc");
        product.setId(42L);

        when(productRepository.findById(42L)).thenReturn(Optional.of(product));

        
        ProductResponse response = productService.getProductById(42L);

        
        assertNotNull(response);
        assertEquals(42L, response.id());
        assertEquals("SKU-1", response.sku());
        assertEquals("Name", response.name());

        verify(productRepository).findById(42L);
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(42L)
        );

        verify(productRepository).findById(42L);
    }


    @Test
    void getProductBySku_shouldReturnProduct_whenExists() {
        Product product = new Product("SKU-1", "Name", BigDecimal.ONE, "EUR", "Desc");
        product.setId(5L);

        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductBySku("SKU-1");

        assertNotNull(response);
        assertEquals(5L, response.id());
        assertEquals("SKU-1", response.sku());

        verify(productRepository).findBySku("SKU-1");
    }

    @Test
    void getProductBySku_shouldThrow_whenNotFound() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductBySku("SKU-1")
        );

        verify(productRepository).findBySku("SKU-1");
    }


    @Test
    void listProducts_shouldReturnAllProducts() {
        
        Product p1 = new Product("SKU-1", "Name1", BigDecimal.ONE, "EUR", "Desc1");
        p1.setId(1L);
        Product p2 = new Product("SKU-2", "Name2", BigDecimal.TEN, "EUR", "Desc2");
        p2.setId(2L);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        
        List<ProductResponse> responses = productService.listProducts();

        
        assertEquals(2, responses.size());
        assertEquals("SKU-1", responses.get(0).sku());
        assertEquals("SKU-2", responses.get(1).sku());

        verify(productRepository).findAll();
    }

    

    @Test
    void changePrice_shouldUpdatePrice_whenProductExists() {
        
        Long id = 10L;
        String idempotencyKey = "idem-2";
        Principal principal = principal("admin");
        String path = "/api/products/" + id + "/price";

        Product product = new Product("SKU-10", "Name", BigDecimal.ONE, "EUR", "Desc");
        product.setId(id);

        ChangePriceRequest request = new ChangePriceRequest(BigDecimal.valueOf(99.99));

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ProductResponse response = productService.changePrice(id, request, idempotencyKey, principal, path);

        
        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(99.99), response.price());

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "PUT", path);
        verify(productRepository).findById(id);
        verify(productRepository).save(product);
    }

    @Test
    void changePrice_shouldThrow_whenProductNotFound() {
        
        Long id = 10L;
        String idempotencyKey = "idem-2";
        Principal principal = principal("admin");
        String path = "/api/products/" + id + "/price";

        ChangePriceRequest request = new ChangePriceRequest(BigDecimal.valueOf(99.99));

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.changePrice(id, request, idempotencyKey, principal, path)
        );

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "PUT", path);
        verify(productRepository).findById(id);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void changePrice_shouldPropagateDuplicateIdempotencyKeyException() {
        
        Long id = 10L;
        String idempotencyKey = "idem-2";
        Principal principal = principal("admin");
        String path = "/api/products/" + id + "/price";

        ChangePriceRequest request = new ChangePriceRequest(BigDecimal.valueOf(99.99));

        doThrow(new DuplicateIdempotencyKeyException(idempotencyKey))
                .when(idempotencyService)
                .registerOrThrow(idempotencyKey, "admin", "PUT", path);

        
        assertThrows(
                DuplicateIdempotencyKeyException.class,
                () -> productService.changePrice(id, request, idempotencyKey, principal, path)
        );

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "PUT", path);
        verifyNoInteractions(productRepository);
    }

    

    @Test
    void deleteProduct_shouldDelete_whenProductExists() {
        
        Long id = 7L;
        String idempotencyKey = "idem-3";
        Principal principal = principal("admin");
        String path = "/api/products/" + id;

        Product product = new Product("SKU-7", "Name7", BigDecimal.ONE, "EUR", "Desc7");
        product.setId(id);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        
        productService.deleteProduct(id, idempotencyKey, principal, path);

        
        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "DELETE", path);
        verify(productRepository).findById(id);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_shouldThrow_whenProductNotFound() {
        
        Long id = 7L;
        String idempotencyKey = "idem-3";
        Principal principal = principal("admin");
        String path = "/api/products/" + id;

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        
        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(id, idempotencyKey, principal, path)
        );

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "DELETE", path);
        verify(productRepository).findById(id);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deleteProduct_shouldPropagateDuplicateIdempotencyKeyException() {
        
        Long id = 7L;
        String idempotencyKey = "idem-3";
        Principal principal = principal("admin");
        String path = "/api/products/" + id;

        doThrow(new DuplicateIdempotencyKeyException(idempotencyKey))
                .when(idempotencyService)
                .registerOrThrow(idempotencyKey, "admin", "DELETE", path);

        
        assertThrows(
                DuplicateIdempotencyKeyException.class,
                () -> productService.deleteProduct(id, idempotencyKey, principal, path)
        );

        verify(idempotencyService).registerOrThrow(idempotencyKey, "admin", "DELETE", path);
        verifyNoInteractions(productRepository);
    }
}