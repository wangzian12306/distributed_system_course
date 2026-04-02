package com.example.distributed.system.service.impl;

import com.example.distributed.system.entity.Order;
import com.example.distributed.system.entity.Product;
import com.example.distributed.system.mapper.OrderMapper;
import com.example.distributed.system.mapper.ProductMapper;
import com.example.distributed.system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scripting.support.ResourceScriptSource;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 雪花算法生成订单ID
    private static final long START_TIMESTAMP = 1609459200000L; // 2021-01-01 00:00:00
    private static final long SEQUENCE_BIT = 12; // 序列号占用位数
    private static final long MACHINE_BIT = 5;   // 机器标识占用位数
    private static final long DATACENTER_BIT = 5; // 数据中心占用位数

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);

    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId = 1;  // 数据中心ID
    private long machineId = 1;     // 机器ID
    private long sequence = 0L;      // 序列号
    private long lastTimestamp = -1L; // 上次时间戳

    // 生成订单ID
    private synchronized long generateOrderId() {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = getNextTimestamp(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    private long getNextTimestamp(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    @Override
    public Long createOrder(Long userId, Long productId, Integer quantity) {
        // 1. 幂等性检查：防止重复下单
        String orderKey = "order:user:" + userId + ":product:" + productId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(orderKey))) {
            throw new RuntimeException("您已经购买过该商品");
        }

        // 2. 检查商品库存
        Product product = productMapper.findById(productId);
        if (product == null || product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }

        // 3. 减少库存
        int newStock = product.getStock() - quantity;
        productMapper.updateStock(productId, newStock);

        // 4. 生成订单ID
        Long orderId = generateOrderId();

        // 5. 创建订单
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice().multiply(new BigDecimal(quantity)));
        order.setStatus(1); // 订单状态：1-待支付
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        // 6. 保存订单到数据库
        orderMapper.insert(order);

        // 7. 设置幂等性标记
        redisTemplate.opsForValue().set(orderKey, "1", 24, TimeUnit.HOURS);

        // 8. 发送订单创建消息到Kafka，异步处理
        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("order-create", orderId.toString());
            } catch (Exception e) {
                // 记录错误日志，但不影响订单创建
                System.err.println("发送Kafka消息失败: " + e.getMessage());
            }
        }

        return orderId;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.findById(id);
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderMapper.findByUserId(userId);
    }

    // 秒杀下单
    public Long createSeckillOrder(Long userId, Long productId) {
        // 1. 准备Redis键
        String stockKey = "seckill:stock:" + productId;
        String orderKey = "seckill:order:user:" + userId + ":product:" + productId;

        // 2. 加载Lua脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/seckill.lua")));
        script.setResultType(Long.class);

        // 3. 执行Lua脚本
        List<String> keys = new ArrayList<>();
        keys.add(stockKey);
        keys.add(orderKey);
        List<String> args = new ArrayList<>();
        args.add(userId.toString());
        args.add(productId.toString());

        Long result = redisTemplate.execute(script, keys, args.toArray());

        // 4. 处理脚本执行结果
        if (result == -1) {
            throw new RuntimeException("您已经参与过该商品的秒杀");
        } else if (result == 0) {
            throw new RuntimeException("秒杀商品已售罄");
        } else if (result != 1) {
            throw new RuntimeException("秒杀失败，请重试");
        }

        // 5. 异步创建订单
        new Thread(() -> {
            try {
                // 生成订单ID
                Long orderId = generateOrderId();

                // 获取商品信息
                Product product = productMapper.findById(productId);
                if (product == null) {
                    // 商品不存在，记录错误
                    System.err.println("商品不存在: " + productId);
                    return;
                }

                // 创建订单
                Order order = new Order();
                order.setId(orderId);
                order.setUserId(userId);
                order.setProductId(productId);
                order.setQuantity(1); // 秒杀商品每次只能购买1件
                order.setTotalPrice(product.getPrice());
                order.setStatus(1); // 订单状态：1-待支付
                order.setCreateTime(new Date());
                order.setUpdateTime(new Date());

                // 保存订单到数据库
                orderMapper.insert(order);

                // 异步更新数据库库存
                productMapper.decreaseSeckillStock(productId);

                // 发送订单创建消息到Kafka，异步处理
                if (kafkaTemplate != null) {
                    try {
                        kafkaTemplate.send("order-create", orderId.toString());
                    } catch (Exception e) {
                        // 记录错误日志，但不影响订单创建
                        System.err.println("发送Kafka消息失败: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // 记录错误日志
                System.err.println("创建秒杀订单失败: " + e.getMessage());
                // 这里可以考虑添加补偿机制，比如将失败的订单信息存入数据库或消息队列
            }
        }).start();

        // 6. 返回临时订单ID（实际订单ID会在异步创建后生成）
        // 注意：这里返回的是一个临时ID，实际订单ID需要通过异步回调或查询获取
        return System.currentTimeMillis();
    }
}