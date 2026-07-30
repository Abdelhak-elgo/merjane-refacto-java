package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class SeasonalProductProcessor extends AbstractProductProcessor {

    public SeasonalProductProcessor(ProductRepository productRepository, NotificationService notificationService) {
        super(productRepository, notificationService);
    }

    @Override
    public ProductType getType() {
        return ProductType.SEASONAL;
    }

    @Override
    public void process(Product product) {
        LocalDate today = LocalDate.now();
        boolean inSeason = today.isAfter(product.getSeasonStartDate())
                && today.isBefore(product.getSeasonEndDate());
        if (inSeason && product.getAvailable() > 0) {
            decrementStock(product);
        } else {
            handleOutOfSeason(product);
        }
    }

    private void handleOutOfSeason(Product product) {
        if (LocalDate.now().plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);
        } else if (product.getSeasonStartDate().isAfter(LocalDate.now())) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }
}
