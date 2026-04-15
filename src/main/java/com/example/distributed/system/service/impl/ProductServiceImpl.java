package com.example.distributed.system.service.impl;

import com.example.distributed.system.entity.Product;
import com.example.distributed.system.mapper.ProductMapper;
import com.example.distributed.system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
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
    // 基础缓存过期时间（秒）30分钟
    private static final long BASE_CACHE_EXPIRE_TIME = 1800;
    // 缓存过期时间（秒）用于向后兼容
    private static final long CACHE_EXPIRE_TIME = 1800;
    // 缓存空值过期时间（秒）5分钟
    private static final long NULL_CACHE_EXPIRE_TIME = 300;
    // 随机偏移量（秒）±5分钟
    private static final long TTL_JITTER = 300;

    // 分布式锁前缀
    private static final String LOCK_KEY_PREFIX = "lock:product:";

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
        String nullKey = cacheKey + ":null";
        if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
            return null;
        }

        // 3. 缓存击穿处理：使用本地锁防止并发请求穿透到数据库
        if (lock.tryLock()) {
            try {
                // 双重检查：再次检查缓存，防止其他线程已经更新了缓存
                product = (Product) redisTemplate.opsForValue().get(cacheKey);
                if (product != null) {
                    return product;
                }
                // 再次检查空值缓存
                if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
                    return null;
                }

                // 4. 从数据库查询
                product = productMapper.findById(id);

                // 5. 更新缓存
                if (product != null) {
                    // 缓存雪崩处理：添加随机过期时间（TTL jitter）
                    long randomOffset = (long) (Math.random() * TTL_JITTER * 2) - TTL_JITTER;
                    long expireTime = BASE_CACHE_EXPIRE_TIME + randomOffset;
                    redisTemplate.opsForValue().set(cacheKey, product, expireTime, TimeUnit.SECONDS);
                } else {
                    // 缓存穿透处理：缓存空值（较短的过期时间）
                    redisTemplate.opsForValue().set(nullKey, "", NULL_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                }
            } finally {
                lock.unlock();
            }
        } else {
            // 6. 其他线程获取锁失败，短暂等待后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 递归重试
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

    // 秒杀库存缓存键前缀
    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";

    @Override
    public boolean decreaseSeckillStock(Long id) {
        // 1. 从Redis获取秒杀库存
        String stockKey = SECKILL_STOCK_KEY_PREFIX + id;
        Integer stock = (Integer) redisTemplate.opsForValue().get(stockKey);

        // 2. 检查库存
        if (stock == null || stock <= 0) {
            // 库存不足或Redis中无数据，从数据库加载并更新缓存
            Product product = productMapper.findById(id);
            if (product == null || product.getSeckillStock() <= 0) {
                // 更新Redis缓存为0
                redisTemplate.opsForValue().set(stockKey, 0, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return false;
            }
            // 更新Redis缓存
            redisTemplate.opsForValue().set(stockKey, product.getSeckillStock(), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            stock = product.getSeckillStock();
        }

        // 3. 使用Redis原子操作预扣减库存
        Long result = redisTemplate.opsForValue().decrement(stockKey);
        if (result < 0) {
            // 库存不足，恢复库存
            redisTemplate.opsForValue().increment(stockKey);
            return false;
        }

        // 4. 异步更新数据库库存
        new Thread(() -> {
            try {
                // 更新数据库秒杀库存
                productMapper.decreaseSeckillStock(id);
                // 更新商品缓存
                String cacheKey = PRODUCT_CACHE_KEY_PREFIX + id;
                Product product = productMapper.findById(id);
                if (product != null) {
                    redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                // 记录错误日志
                System.err.println("更新秒杀库存失败: " + e.getMessage());
                // 恢复Redis库存
                redisTemplate.opsForValue().increment(stockKey);
            }
        }).start();

        return true;
    }

    // 初始化秒杀库存到Redis
    public void initSeckillStock() {
        // 这里可以根据实际情况加载秒杀商品并初始化库存
        // 示例：加载所有秒杀商品并将库存缓存到Redis
        List<Product> seckillProducts = productMapper.findSeckillProducts();
        for (Product product : seckillProducts) {
            String stockKey = SECKILL_STOCK_KEY_PREFIX + product.getId();
            redisTemplate.opsForValue().set(stockKey, product.getSeckillStock(), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        }
    }
}