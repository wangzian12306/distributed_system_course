package com.example.distributed.system.service;

import com.example.distributed.system.entity.Order;

import java.util.List;

public interface OrderService {
    Long createOrder(Long userId, Long productId, Integer quantity);
    Order getOrderById(Long id);
    List<Order> getOrdersByUserId(Long userId);
    Long createSeckillOrder(Long userId, Long productId);
}