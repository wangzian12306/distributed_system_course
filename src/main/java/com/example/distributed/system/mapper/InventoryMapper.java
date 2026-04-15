package com.example.distributed.system.mapper;

import com.example.distributed.system.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryMapper {
    Inventory findByProductId(@Param("productId") Long productId);
    void decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    void decreaseSeckillStock(@Param("productId") Long productId);
    void updateStock(@Param("productId") Long productId, @Param("stock") Integer stock);
    void insert(Inventory inventory);
}
