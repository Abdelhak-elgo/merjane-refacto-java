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
class SeasonalProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ProductService productService;

    @Test
    void givenInSeasonOutOfStockLeadTimeWithinSeason() {
        // GIVEN — in season, out of stock, leadTime (5 days) fits before end of season (30 days)
        Product product = new Product(null, 5, 0, ProductType.SEASONAL, "Watermelon", null,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN
        verify(notificationService, times(1)).sendDelayNotification(5, "Watermelon");
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void givenOutOfStockLeadTimeExceedsSeason() {
        // GIVEN — leadTime (60 days) goes past season end (30 days away)
        Product product = new Product(null, 60, 0, ProductType.SEASONAL, "Watermelon", null,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN
        verify(notificationService, times(1)).sendOutOfStockNotification("Watermelon");
        assertEquals(0, product.getAvailable());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void givenBeforeSeasonLeadTimeWithinSeason() {
        // GIVEN — season hasn't started yet, leadTime (15) fits before end (100 days)
        Product product = new Product(null, 15, 30, ProductType.SEASONAL, "Grapes", null,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(100));
        when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.handleSeasonalProduct(product);

        // THEN — stock is not zeroed when product is simply not yet in season
        verify(notificationService, times(1)).sendOutOfStockNotification("Grapes");
        assertEquals(30, product.getAvailable());
        verify(productRepository, times(1)).save(product);
    }
}
