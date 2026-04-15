package com.example.distributed.system.service.impl;

import com.example.distributed.system.entity.Order;
import com.example.distributed.system.mapper.OrderMapper;
import com.example.distributed.system.service.OrderService;
import com.example.distributed.system.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public boolean payOrder(Long orderId) {
        try {
            // 1. 查询订单
            Order order = orderService.getOrderById(orderId);
            if (order == null || order.getStatus() != 1) {
                return false;
            }

            // 2. 模拟支付处理
            System.out.println("正在处理订单支付，订单ID: " + orderId);

            // 3. 发送支付成功消息到Kafka
            if (kafkaTemplate != null) {
                kafkaTemplate.send("order-payment", orderId.toString());
            }

            return true;
        } catch (Exception e) {
            System.err.println("订单支付失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, Integer status) {
        orderMapper.updateStatus(orderId, status);
    }

    // 监听支付成功消息，更新订单状态
    @KafkaListener(topics = "order-payment", groupId = "order-group")
    public void handleOrderPayment(String orderIdStr) {
        try {
            Long orderId = Long.parseLong(orderIdStr);
            // 更新订单状态为已支付（2）
            updateOrderStatus(orderId, 2);
            System.out.println("订单支付成功，订单状态已更新，订单ID: " + orderId);
        } catch (Exception e) {
            System.err.println("处理订单支付消息失败: " + e.getMessage());
        }
    }
}
