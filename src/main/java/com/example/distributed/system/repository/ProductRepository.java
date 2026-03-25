package com.example.distributed.system.repository;

import com.example.distributed.system.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {
    // 根据名称搜索商品
    List<ProductDocument> findByNameContaining(String name);
    
    // 根据描述搜索商品
    List<ProductDocument> findByDescriptionContaining(String description);
    
    // 根据名称或描述搜索商品
    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
}