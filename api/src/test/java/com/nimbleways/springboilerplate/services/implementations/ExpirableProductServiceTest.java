package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
class ExpirableProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ProductService productService;

    @Test
    void givenExpiredProduct() {
        // GIVEN — product has stock but is expired
        LocalDate expiryDate = LocalDate.now().minusDays(2);
        Product product = new Product(null, 10, 6, ProductType.EXPIRABLE, "Milk", expiryDate, null, null);
        when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleExpiredProduct(product);

        // THEN
        verify(notificationService, times(1)).sendExpirationNotification("Milk", expiryDate);
        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void givenOutOfStockNotExpired() {
        // GIVEN — product is not expired but has no stock left
        LocalDate expiryDate = LocalDate.now().plusDays(5);
        Product product = new Product(null, 10, 0, ProductType.EXPIRABLE, "Butter", expiryDate, null, null);
        when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleExpiredProduct(product);

        // THEN
        verify(notificationService, times(1)).sendExpirationNotification("Butter", expiryDate);
        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
    }
}
