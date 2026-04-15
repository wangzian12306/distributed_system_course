package com.example.distributed.system.controller;

import com.example.distributed.system.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<String> payOrder(@PathVariable Long orderId) {
        try {
            boolean success = paymentService.payOrder(orderId);
            if (success) {
                return ResponseEntity.ok("订单支付成功");
            } else {
                return ResponseEntity.badRequest().body("订单支付失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("支付过程出错: " + e.getMessage());
        }
    }
}
