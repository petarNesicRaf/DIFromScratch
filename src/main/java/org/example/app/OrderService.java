package org.example.app;

import org.example.annotations.AutoWired;

public class OrderService {

    @AutoWired(verbose = true)
    private OrderRepository repository;

    public Order getOrderDetails(Integer orderId) {
        return repository.getById(orderId);
    }
}
