package com.example.distributed.system.mapper;

import com.example.distributed.system.entity.Order;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface OrderMapper {
    void insert(Order order);
    Order findById(Long id);
    List<Order> findByUserId(Long userId);
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}