package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
class ProductServiceTest {

    @Mock
    private ProductProcessor normalProcessor;
    @Mock
    private ProductProcessor seasonalProcessor;
    @Mock
    private ProductProcessor expirableProcessor;

    private ProductService productService;

    private void givenRegisteredProcessors() {
        when(normalProcessor.getType()).thenReturn(ProductType.NORMAL);
        when(seasonalProcessor.getType()).thenReturn(ProductType.SEASONAL);
        when(expirableProcessor.getType()).thenReturn(ProductType.EXPIRABLE);
        productService = new ProductService(List.of(normalProcessor, seasonalProcessor, expirableProcessor));
    }

    @Test
    void dispatchesToTheProcessorMatchingTheProductType() {
        // GIVEN
        givenRegisteredProcessors();
        Product product = new Product(null, 10, 5, ProductType.SEASONAL, "Watermelon", null, null, null);

        // WHEN
        productService.processProduct(product);

        // THEN
        verify(seasonalProcessor, times(1)).process(product);
        verify(normalProcessor, never()).process(any());
        verify(expirableProcessor, never()).process(any());
    }
}
