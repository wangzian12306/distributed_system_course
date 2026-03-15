package com.example.distributed.system.service;

import com.example.distributed.system.entity.Product;

public interface ProductService {
    Product getProductById(Long id);
    boolean decreaseStock(Long id, Integer quantity);
    boolean decreaseSeckillStock(Long id);
}