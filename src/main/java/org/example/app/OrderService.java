package org.example.app;

import org.example.annotations.AutoWired;
import org.example.annotations.Bean;
import org.example.annotations.Component;

@Bean(scope = false)
public class OrderService {

    @AutoWired(verbose = true)
    private OrderRepository repository;

    public Order getOrderDetails(Integer orderId) {
        return repository.getById(orderId);
    }
}
