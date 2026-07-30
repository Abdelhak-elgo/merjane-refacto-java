package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ExpirableProductProcessor extends AbstractProductProcessor {

    public ExpirableProductProcessor(ProductRepository productRepository, NotificationService notificationService) {
        super(productRepository, notificationService);
    }

    @Override
    public ProductType getType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decrementStock(product);
        } else {
            handleExpiredProduct(product);
        }
    }

    private void handleExpiredProduct(Product product) {
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }
}
