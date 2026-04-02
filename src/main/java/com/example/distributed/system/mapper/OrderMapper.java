package com.example.distributed.system.mapper;

import com.example.distributed.system.entity.Order;

import java.util.List;

public interface OrderMapper {
    void insert(Order order);
    Order findById(Long id);
    List<Order> findByUserId(Long userId);
}