package com.example.distributed.system.controller;

import com.example.distributed.system.entity.Inventory;
import com.example.distributed.system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventory(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        if (inventory != null) {
            return ResponseEntity.ok(inventory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/decrease")
    public ResponseEntity<String> decreaseStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        boolean success = inventoryService.decreaseStock(productId, quantity);
        if (success) {
            return ResponseEntity.ok("库存扣减成功");
        } else {
            return ResponseEntity.badRequest().body("库存不足");
        }
    }

    @PostMapping("/decrease-seckill")
    public ResponseEntity<String> decreaseSeckillStock(@RequestParam Long productId) {
        boolean success = inventoryService.decreaseSeckillStock(productId);
        if (success) {
            return ResponseEntity.ok("秒杀库存扣减成功");
        } else {
            return ResponseEntity.badRequest().body("秒杀库存不足");
        }
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<String> updateStock(@PathVariable Long productId, @RequestParam Integer stock) {
        inventoryService.updateStock(productId, stock);
        return ResponseEntity.ok("库存更新成功");
    }
}
