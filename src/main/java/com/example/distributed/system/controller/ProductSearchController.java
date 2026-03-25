package com.example.distributed.system.controller;

import com.example.distributed.system.entity.ProductDocument;
import com.example.distributed.system.service.ProductSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class ProductSearchController {

    @Autowired
    private ProductSearchService productSearchService;

    @GetMapping
    public List<ProductDocument> searchProducts(@RequestParam String keyword) {
        return productSearchService.searchProducts(keyword);
    }

    @GetMapping("/init")
    public String initProducts() {
        productSearchService.initAllProductsToElasticSearch();
        return "初始化商品到ElasticSearch成功";
    }
}