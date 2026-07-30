package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    
    // NORMAL product cases
    

    @Test
    public void processOrder_normalProductInStock_decrementsStock() throws Exception {
        Product product = saveProduct(new Product(null, 15, 30, ProductType.NORMAL, "USB Cable", null, null, null));
        Order order = saveOrder(product);

        callProcessOrder(order.getId());

        assertEquals(29, reloadProduct(product).getAvailable());
    }

    @Test
    public void processOrder_normalProductOutOfStock_notifiesDelay() throws Exception {
        Product product = saveProduct(new Product(null, 10, 0, ProductType.NORMAL, "USB Dongle", null, null, null));
        Order order = saveOrder(product);

        callProcessOrder(order.getId());

        verify(notificationService, times(1)).sendDelayNotification(10, "USB Dongle");
    }

    
    // EXPIRABLE product cases
    

    @Test
    public void processOrder_expirableProductNotExpired_decrementsStock() throws Exception {
        Product product = saveProduct(new Product(null, 15, 30, ProductType.EXPIRABLE, "Butter",
                LocalDate.now().plusDays(26), null, null));
        Order order = saveOrder(product);

        callProcessOrder(order.getId());

        assertEquals(29, reloadProduct(product).getAvailable());
    }

    @Test
    public void processOrder_expirableProductExpired_notifiesExpirationAndZerosStock() throws Exception {
        LocalDate expiryDate = LocalDate.now().minusDays(2);
        Product product = saveProduct(new Product(null, 90, 6, ProductType.EXPIRABLE, "Milk", expiryDate, null, null));
        Order order = saveOrder(product);

        callProcessOrder(order.getId());

        verify(notificationService, times(1)).sendExpirationNotification("Milk", expiryDate);
        assertEquals(0, reloadProduct(product).getAvailable());
    }

    
    // SEASONAL product cases
    

    @Test
    public void processOrder_seasonalProductInSeason_decrementsStock() throws Exception {
        Product product = saveProduct(new Product(null, 15, 30, ProductType.SEASONAL, "Watermelon",
                null, LocalDate.now().minusDays(2), LocalDate.now().plusDays(58)));
        Order order = saveOrder(product);

        callProcessOrder(order.getId());

        assertEquals(29, reloadProduct(product).getAvailable());
    }

    @Test
    public void processOrder_seasonalProductBeforeSeason_notifiesOutOfStock() throws Exception {
        Product product = saveProduct(new Product(null, 15, 30, ProductType.SEASONAL, "Grapes",
                null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240)));
        Order order = saveOrder(product);

        callProcessOrder(order.getId());

        verify(notificationService, times(1)).sendOutOfStockNotification("Grapes");
    }

    
    // Helpers
    

    private Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    private Order saveOrder(Product... products) {
        Order order = new Order();
        order.setItems(Set.of(products));
        return orderRepository.save(order);
    }

    private void callProcessOrder(Long orderId) throws Exception {
        mockMvc.perform(post("/orders/{orderId}/processOrder", orderId)
                .contentType("application/json"))
                .andExpect(status().isOk());
    }

    private Product reloadProduct(Product product) {
        return productRepository.findById(product.getId()).orElseThrow();
    }
}
