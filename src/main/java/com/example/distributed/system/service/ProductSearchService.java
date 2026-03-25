package com.example.distributed.system.service;

import com.example.distributed.system.entity.Product;
import com.example.distributed.system.entity.ProductDocument;

import java.util.List;

public interface ProductSearchService {
    // 将商品同步到ElasticSearch
    void syncProductToElasticSearch(Product product);
    
    // 从ElasticSearch删除商品
    void deleteProductFromElasticSearch(Long productId);
    
    // 搜索商品
    List<ProductDocument> searchProducts(String keyword);
    
    // 初始化所有商品到ElasticSearch
    void initAllProductsToElasticSearch();
}