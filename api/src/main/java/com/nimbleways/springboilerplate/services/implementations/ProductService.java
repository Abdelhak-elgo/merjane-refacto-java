package com.nimbleways.springboilerplate.services.implementations;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

@Service
public class ProductService {

    private final Map<ProductType, ProductProcessor> processorsByType;

    public ProductService(List<ProductProcessor> processors) {
        this.processorsByType = processors.stream()
                .collect(Collectors.toMap(ProductProcessor::getType, Function.identity()));
    }

    public void processProduct(Product product) {
        processorsByType.get(product.getType()).process(product);
    }
}
