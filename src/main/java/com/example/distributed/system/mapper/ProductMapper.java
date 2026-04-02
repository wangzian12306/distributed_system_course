package com.example.distributed.system.mapper;

import com.example.distributed.system.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    Product findById(@Param("id") Long id);
    void updateStock(@Param("id") Long id, @Param("stock") Integer stock);
    void updateSeckillStock(@Param("id") Long id, @Param("seckillStock") Integer seckillStock);
    void decreaseSeckillStock(@Param("id") Long id);
    List<Product> findSeckillProducts();
}