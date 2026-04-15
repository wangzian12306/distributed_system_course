package com.example.distributed.system.service;

import com.example.distributed.system.entity.Inventory;

public interface InventoryService {
    Inventory getInventoryByProductId(Long productId);
    boolean decreaseStock(Long productId, Integer quantity);
    boolean decreaseSeckillStock(Long productId);
    void updateStock(Long productId, Integer stock);
}
