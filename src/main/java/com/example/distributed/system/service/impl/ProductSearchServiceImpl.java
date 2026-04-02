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
        // 从数据库查询所有商品
        List<Product> products = productMapper.findAll();
        // 转换为ProductDocument
        List<ProductDocument> documents = products.stream()
                .map(product -> {
                    ProductDocument document = new ProductDocument();
                    BeanUtils.copyProperties(product, document);
                    // 处理price字段类型转换
                    if (product.getPrice() != null) {
                        document.setPrice(product.getPrice().doubleValue());
                    }
                    return document;
                })
                .collect(Collectors.toList());
        // 保存到ElasticSearch
        productRepository.saveAll(documents);
    }
}