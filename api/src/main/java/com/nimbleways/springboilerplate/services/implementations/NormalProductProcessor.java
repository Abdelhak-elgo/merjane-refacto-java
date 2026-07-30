package com.nimbleways.springboilerplate.services.implementations;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class NormalProductProcessor extends AbstractProductProcessor {

    public NormalProductProcessor(ProductRepository productRepository, NotificationService notificationService) {
        super(productRepository, notificationService);
    }

    @Override
    public ProductType getType() {
        return ProductType.NORMAL;
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > 0) {
            decrementStock(product);
        } else if (product.getLeadTime() > 0) {
            notifyDelay(product.getLeadTime(), product);
        }
    }
}
