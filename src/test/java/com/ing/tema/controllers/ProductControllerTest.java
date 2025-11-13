package com.ing.tema.controllers;

import com.ing.tema.dtos.ChangePriceRequest;
import com.ing.tema.dtos.CreateProductRequest;
import com.ing.tema.dtos.ProductResponse;
import com.ing.tema.services.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private Principal principal(String name) {
        return () -> name;
    }


    @Test
    void createProduct_shouldReturnCreatedWithLocationAndBody() {

        String idempotencyId = "idem-1";
        Principal principal = principal("admin");

        CreateProductRequest request = new CreateProductRequest("SKU-123", "Test Product", BigDecimal.valueOf(15.99), "EUR", "Some description");

        ProductResponse serviceResponse = new ProductResponse(1L, "SKU-123", "Test Product", BigDecimal.valueOf(15.99), "EUR", "Some description");

        when(productService.createProduct(eq(request), eq(idempotencyId), eq(principal), eq("/api/products"))).thenReturn(serviceResponse);


        ResponseEntity<ProductResponse> response = productController.createProduct(idempotencyId, request, principal);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("SKU-123", response.getBody().sku());

        URI location = response.getHeaders().getLocation();
        assertNotNull(location);
        assertEquals("/api/products/1", location.toString());

        verify(productService).createProduct(eq(request), eq(idempotencyId), eq(principal), eq("/api/products"));
    }


    @Test
    void getProductById_shouldReturnOkAndBody() {

        Long id = 10L;

        ProductResponse serviceResponse = new ProductResponse(id, "SKU-10", "Product 10", BigDecimal.TEN, "EUR", "Desc");

        when(productService.getProductById(id)).thenReturn(serviceResponse);


        ResponseEntity<ProductResponse> response = productController.getProductById(id);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(id, response.getBody().id());
        assertEquals("SKU-10", response.getBody().sku());

        verify(productService).getProductById(id);
    }


    @Test
    void getProductBySku_shouldReturnOkAndBody() {

        String sku = "SKU-XYZ";

        ProductResponse serviceResponse = new ProductResponse(5L, sku, "Some Product", BigDecimal.ONE, "EUR", "Desc");

        when(productService.getProductBySku(sku)).thenReturn(serviceResponse);


        ResponseEntity<ProductResponse> response = productController.getProductBySku(sku);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sku, response.getBody().sku());
        assertEquals(5L, response.getBody().id());

        verify(productService).getProductBySku(sku);
    }


    @Test
    void listProducts_shouldReturnOkAndList() {

        ProductResponse p1 = new ProductResponse(1L, "SKU-1", "P1", BigDecimal.ONE, "EUR", "D1");
        ProductResponse p2 = new ProductResponse(2L, "SKU-2", "P2", BigDecimal.TEN, "EUR", "D2");

        when(productService.listProducts()).thenReturn(List.of(p1, p2));


        ResponseEntity<List<ProductResponse>> response = productController.listProducts();


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("SKU-1", response.getBody().get(0).sku());
        assertEquals("SKU-2", response.getBody().get(1).sku());

        verify(productService).listProducts();
    }


    @Test
    void changePrice_shouldReturnOkAndUpdatedProduct() {

        Long id = 7L;
        String idempotencyId = "idem-2";
        Principal principal = principal("admin");

        ChangePriceRequest request = new ChangePriceRequest(BigDecimal.valueOf(99.99));

        ProductResponse serviceResponse = new ProductResponse(id, "SKU-7", "P7", BigDecimal.valueOf(99.99), "EUR", "Desc7");

        String expectedPath = "/api/products/" + id + "/price";

        when(productService.changePrice(eq(id), eq(request), eq(idempotencyId), eq(principal), eq(expectedPath))).thenReturn(serviceResponse);


        ResponseEntity<ProductResponse> response = productController.changePrice(idempotencyId, id, request, principal);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(99.99), response.getBody().price());
        assertEquals(id, response.getBody().id());

        verify(productService).changePrice(eq(id), eq(request), eq(idempotencyId), eq(principal), eq(expectedPath));
    }


    @Test
    void deleteProduct_shouldReturnNoContent() {

        Long id = 3L;
        String idempotencyId = "idem-3";
        Principal principal = principal("admin");

        String expectedPath = "/api/products/" + id;


        ResponseEntity<Void> response = productController.deleteProduct(idempotencyId, id, principal);


        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(productService).deleteProduct(eq(id), eq(idempotencyId), eq(principal), eq(expectedPath));
    }
}
