package com.example.distributed.system.service.impl;

import com.example.distributed.system.entity.Product;
import com.example.distributed.system.mapper.ProductMapper;
import com.example.distributed.system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String PRODUCT_CACHE_KEY_PREFIX = "product:";
    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE_TIME = 3600;
    // 缓存空值过期时间（秒）
    private static final long CACHE_NULL_EXPIRE_TIME = 60;

    // 分布式锁前缀
    private static final String LOCK_KEY_PREFIX = "lock:product:";
    // 锁过期时间（秒）
    private static final long LOCK_EXPIRE_TIME = 10;

    // 本地锁，用于防止缓存击穿
    private final Lock lock = new ReentrantLock();

    @Override
    public Product getProductById(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;

        // 1. 尝试从缓存获取
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (product != null) {
            return product;
        }

        // 2. 缓存穿透处理：检查是否为缓存空值
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey + ":null"))) {
            return null;
        }

        // 3. 缓存击穿处理：使用锁防止并发请求穿透到数据库
        if (lock.tryLock()) {
            try {
                // 再次检查缓存，防止其他线程已经更新了缓存
                product = (Product) redisTemplate.opsForValue().get(cacheKey);
                if (product != null) {
                    return product;
                }

                // 4. 从数据库查询
                product = productMapper.findById(id);

                // 5. 更新缓存
                if (product != null) {
                    // 缓存雪崩处理：添加随机过期时间
                    long expireTime = CACHE_EXPIRE_TIME + (long) (Math.random() * 300);
                    redisTemplate.opsForValue().set(cacheKey, product, expireTime, TimeUnit.SECONDS);
                } else {
                    // 缓存穿透处理：缓存空值
                    redisTemplate.opsForValue().set(cacheKey + ":null", "", CACHE_NULL_EXPIRE_TIME, TimeUnit.SECONDS);
                }
            } finally {
                lock.unlock();
            }
        } else {
            // 6. 其他线程获取锁失败，等待后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getProductById(id);
        }

        return product;
    }

    @Override
    public boolean decreaseStock(Long id, Integer quantity) {
        // 1. 减少库存
        Product product = productMapper.findById(id);
        if (product == null || product.getStock() < quantity) {
            return false;
        }

        // 2. 更新库存
        int newStock = product.getStock() - quantity;
        productMapper.updateStock(id, newStock);

        // 3. 更新缓存
        String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
        product.setStock(newStock);
        redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);

        return true;
    }

    @Override
    public boolean decreaseSeckillStock(Long id) {
        // 1. 减少秒杀库存
        Product product = productMapper.findById(id);
        if (product == null || product.getSeckillStock() <= 0) {
            return false;
        }

        // 2. 更新秒杀库存
        int newSeckillStock = product.getSeckillStock() - 1;
        productMapper.updateSeckillStock(id, newSeckillStock);

        // 3. 更新缓存
        String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
        product.setSeckillStock(newSeckillStock);
        redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);

        return true;
    }
}