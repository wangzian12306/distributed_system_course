package com.example.distributed.system.service.impl;

import com.example.distributed.system.entity.Inventory;
import com.example.distributed.system.mapper.InventoryMapper;
import com.example.distributed.system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryMapper.findByProductId(productId);
    }

    @Override
    public boolean decreaseStock(Long productId, Integer quantity) {
        try {
            inventoryMapper.decreaseStock(productId, quantity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean decreaseSeckillStock(Long productId) {
        try {
            inventoryMapper.decreaseSeckillStock(productId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void updateStock(Long productId, Integer stock) {
        inventoryMapper.updateStock(productId, stock);
    }
}
