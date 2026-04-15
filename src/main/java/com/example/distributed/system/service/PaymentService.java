package com.example.distributed.system.service;

public interface PaymentService {
    boolean payOrder(Long orderId);
    void updateOrderStatus(Long orderId, Integer status);
}
