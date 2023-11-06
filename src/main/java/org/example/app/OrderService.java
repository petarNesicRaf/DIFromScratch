package org.example.app;

import org.example.annotations.AutoWired;
import org.example.annotations.Bean;
import org.example.annotations.Component;
import org.example.annotations.Service;

@Service
public class OrderService {

    private OrderRepository repository;

    public Order getOrderDetails(Integer orderId) {
        return repository.getById(orderId);
    }
}
