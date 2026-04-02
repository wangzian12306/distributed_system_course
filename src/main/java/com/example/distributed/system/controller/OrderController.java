package com.example.distributed.system.controller;

import com.example.distributed.system.entity.Order;
import com.example.distributed.system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestParam Long userId, @RequestParam Long productId, @RequestParam Integer quantity) {
        try {
            Long orderId = orderService.createOrder(userId, productId, quantity);
            return ResponseEntity.ok(orderId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 根据订单ID查询订单
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 根据用户ID查询订单列表
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // 秒杀下单
    @PostMapping("/seckill")
    public ResponseEntity<Long> createSeckillOrder(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            Long orderId = orderService.createSeckillOrder(userId, productId);
            return ResponseEntity.ok(orderId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}