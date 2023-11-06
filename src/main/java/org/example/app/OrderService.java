package org.example.app;

import org.example.annotations.*;

@Service
public class OrderService {
    @AutoWired(verbose = true)
    @Qualifier(value = "zoran")
    private OrderRepository repository;

    public Order getOrderDetails(Integer orderId) {
        return repository.getById(orderId);
    }
}
