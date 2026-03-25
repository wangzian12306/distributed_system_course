package com.example.distributed.system.service.impl;

import com.example.distributed.system.entity.Product;
import com.example.distributed.system.entity.ProductDocument;
import com.example.distributed.system.mapper.ProductMapper;
import com.example.distributed.system.repository.ProductRepository;
import com.example.distributed.system.service.ProductSearchService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void syncProductToElasticSearch(Product product) {
        ProductDocument document = new ProductDocument();
        BeanUtils.copyProperties(product, document);
        // 处理price字段类型转换
        if (product.getPrice() != null) {
            document.setPrice(product.getPrice().doubleValue());
        }
        productRepository.save(document);
    }

    @Override
    public void deleteProductFromElasticSearch(Long productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public List<ProductDocument> searchProducts(String keyword) {
        return productRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    @Override
    public void initAllProductsToElasticSearch() {
        // 这里简化处理，实际应该从数据库查询所有商品
        // 由于我们没有实现查询所有商品的方法，这里暂时注释掉
        // List<Product> products = productMapper.findAll();
        // List<ProductDocument> documents = products.stream()
        //         .map(product -> {
        //             ProductDocument document = new ProductDocument();
        //             BeanUtils.copyProperties(product, document);
        //             return document;
        //         })
        //         .collect(Collectors.toList());
        // productRepository.saveAll(documents);
    }
}