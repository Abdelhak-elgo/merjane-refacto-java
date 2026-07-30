package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

public interface ProductProcessor {
    ProductType getType();

    void process(Product product);
}
