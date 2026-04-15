# 分布式商品库存与秒杀系统 - 实际项目文档

## 项目简介

本项目是一个基于 Spring Boot 的分布式商品库存与秒杀系统，实现了分布式系统的核心功能，包括分布式缓存、读写分离、消息队列、搜索引擎等。

### 已实现的功能

- ✅ 用户注册登录（JWT认证）
- ✅ 商品管理（CRUD）
- ✅ 库存管理
- ✅ 订单管理（普通订单 + 秒杀订单）
- ✅ 支付服务
- ✅ 分布式缓存（Redis）
- ✅ 缓存三重保护（穿透/击穿/雪崩）
- ✅ MySQL读写分离（@ReadOnly注解）
- ✅ ElasticSearch商品搜索（启动自动同步）
- ✅ Kafka消息队列
- ✅ 秒杀功能（Lua脚本原子操作）
- ✅ 雪花算法生成订单ID
- ✅ 幂等性控制
- ✅ 实例ID拦截器（负载均衡验证）
- ✅ 容器化部署（Docker）
- ✅ Nginx负载均衡和动静分离

## 项目结构

```
fenbu/
├── src/
│   ├── main/
│   │   ├── java/com/example/distributed/system/
│   │   │   ├── Application.java              # 主启动类
│   │   │   ├── config/                       # 配置类
│   │   │   │   ├── ReadOnly.java            # 读操作注解
│   │   │   │   ├── DataSourceNames.java    # 数据源名称常量
│   │   │   │   ├── DataSourceContextHolder.java # 数据源上下文
│   │   │   │   ├── ReadOnlyRoutingAspect.java # 读操作切面
│   │   │   │   ├── DataSourceConfig.java    # 读写分离配置
│   │   │   │   ├── ElasticSearchConfig.java # ES配置
│   │   │   │   ├── RedisConfig.java         # Redis配置
│   │   │   │   ├── WebSecurityConfig.java   # 安全配置
│   │   │   │   ├── InstanceIdInterceptor.java # 实例ID拦截器
│   │   │   │   └── WebMvcConfig.java        # Web MVC配置
│   │   │   ├── controller/                   # 控制器
│   │   │   ├── service/                      # 服务层
│   │   │   ├── mapper/                       # 数据访问层
│   │   │   ├── entity/                       # 实体类
│   │   │   ├── security/                     # 安全相关
│   │   │   └── dto/                          # 数据传输对象
│   │   └── resources/
│   │       ├── application.yml               # 应用配置
│   │       ├── schema.sql                    # 数据库脚本
│   │       ├── lua/seckill.lua               # 秒杀Lua脚本
│   │       └── mapper/                       # MyBatis映射文件
├── static/                                    # 静态资源
│   ├── index.html
│   ├── css/style.css
│   └── js/main.js
├── Dockerfile                                 # Docker镜像配置
├── docker-compose.yml                         # Docker编排配置
├── nginx.conf                                 # Nginx配置
├── pom.xml                                    # Maven配置
└── start.bat                                  # Windows启动脚本
```

## 技术栈

### 后端技术
- **框架**: Spring Boot 2.7.15
- **持久层**: MyBatis 2.3.1
- **数据库**: MySQL 8.0.33
- **缓存**: Redis
- **搜索引擎**: ElasticSearch
- **消息队列**: Kafka
- **安全**: Spring Security + JWT
- **分库分表**: ShardingSphere-JDBC 5.3.2（依赖已引入）
- **AOP**: Spring AOP

### 运维工具
- Docker
- Docker Compose
- Nginx

## 快速开始

### 前置条件
- Java 8+
- Maven 3.8+
- MySQL 8.0+
- Redis 5.0+
- ElasticSearch 7.x+
- Kafka 2.x+（可选，功能降级可用）

### 方式一：本地运行

1. **初始化数据库**
```bash
# 创建数据库并执行schema.sql
mysql -u root -p < src/main/resources/schema.sql
```

2. **修改配置**
编辑 `src/main/resources/application.yml`，配置数据库、Redis、ElasticSearch等连接信息。

3. **编译运行**
```bash
# Windows
start.bat

# 或使用Maven
mvn clean spring-boot:run
```

### 方式二：Docker Compose运行

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## API接口文档

### 1. 用户服务

#### 用户注册
```
POST /api/users/register
Content-Type: application/json
{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com"
}
```

#### 用户登录
```
POST /api/users/login
Content-Type: application/json
{
  "username": "testuser",
  "password": "123456"
}
```

### 2. 商品服务

#### 获取商品详情（带Redis缓存）
```
GET /api/products/{id}
```

#### 获取所有商品
```
GET /api/products
```

#### 创建商品
```
POST /api/products
Content-Type: application/json
{
  "name": "商品名称",
  "description": "商品描述",
  "price": 100.00,
  "stock": 100,
  "seckillStock": 10
}
```

### 3. 库存服务

#### 获取库存
```
GET /api/inventory/{productId}
```

#### 扣减库存
```
POST /api/inventory/decrease?productId=1&quantity=1
```

#### 扣减秒杀库存
```
POST /api/inventory/decrease-seckill?productId=1
```

### 4. 订单服务

#### 创建普通订单
```
POST /api/orders
Content-Type: application/json
{
  "userId": 1,
  "productId": 1,
  "quantity": 1
}
```

#### 秒杀下单
```
POST /api/orders/seckill?userId=1&productId=1
```

#### 获取订单详情
```
GET /api/orders/{id}
```

#### 获取用户订单列表
```
GET /api/orders/user/{userId}
```

### 5. 支付服务

#### 订单支付
```
POST /api/payments/pay/{orderId}
```

### 6. 商品搜索

#### 搜索商品
```
GET /api/search?keyword=商品
```

## 核心功能实现

### 1. 分布式缓存（Redis）- 三重保护

**实现位置**: `ProductServiceImpl.getProductById()`

**缓存三重保护策略**：

- **缓存穿透**：缓存空值（5分钟过期），防止恶意请求不存在的商品ID直接穿透到数据库
- **缓存击穿**：本地锁 + 双重检查，防止热点key失效时大量请求同时穿透到数据库
- **缓存雪崩**：TTL抖动（30分钟±5分钟），防止大量缓存同时过期

**具体实现**：
```java
// 缓存穿透：缓存空值
if (Boolean.TRUE.equals(redisTemplate.hasKey(nullKey))) {
    return null;
}

// 缓存击穿：本地锁 + 双重检查
if (lock.tryLock()) {
    try {
        // 双重检查
        product = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (product != null) {
            return product;
        }
        // 从数据库查询...
    } finally {
        lock.unlock();
    }
}

// 缓存雪崩：随机过期时间
long randomOffset = (long) (Math.random() * TTL_JITTER * 2) - TTL_JITTER;
long expireTime = BASE_CACHE_EXPIRE_TIME + randomOffset;
redisTemplate.opsForValue().set(cacheKey, product, expireTime, TimeUnit.SECONDS);
```

### 2. 读写分离（MySQL）- @ReadOnly注解

**实现位置**: 
- `config/ReadOnly.java` - 自定义注解
- `config/ReadOnlyRoutingAspect.java` - AOP切面
- `config/DataSourceContextHolder.java` - 数据源上下文
- `config/DataSourceConfig.java` - 动态数据源配置

**实现方式**：

- 主数据源（master）：处理所有写操作（insert/update/delete）
- 从数据源（slave）：处理读操作（@ReadOnly注解标记）

**使用示例**：
```java
@Mapper
public interface ProductMapper {
    @ReadOnly  // 标记为读操作，使用从库
    Product findById(@Param("id") Long id);
    
    // 写操作不标记，使用主库
    void updateStock(@Param("id") Long id, @Param("stock") Integer stock);
}
```

### 3. 实例ID拦截器（负载均衡验证）

**实现位置**: `config/InstanceIdInterceptor.java` + `config/WebMvcConfig.java`

**功能**：在所有HTTP响应中添加 `X-Instance-Id` 响应头，用于验证Nginx负载均衡是否生效。

**响应头示例**：
```
X-Instance-Id: hostname-8080
```

### 4. ElasticSearch商品搜索 - 启动自动同步

**实现位置**: `ProductSearchServiceImpl.java`

**功能**：
- 启动时自动同步所有商品到ElasticSearch（@PostConstruct）
- 商品文档同步
- 全文检索（商品名称、描述）
- 支持模糊搜索

```java
@PostConstruct
public void init() {
    try {
        System.out.println("开始同步商品数据到ElasticSearch...");
        initAllProductsToElasticSearch();
        System.out.println("商品数据同步到ElasticSearch完成");
    } catch (Exception e) {
        System.err.println("同步商品数据到ElasticSearch失败: " + e.getMessage());
    }
}
```

### 5. 秒杀功能（Lua脚本原子操作）

**实现位置**: `OrderServiceImpl.createSeckillOrder()` + `lua/seckill.lua`

- Redis预扣减库存
- Lua脚本保证原子性
- 幂等性控制：同一用户同一商品只能秒杀一次
- 异步创建订单：Kafka削峰填谷
- 雪花算法生成订单ID

## 数据库设计

### 表结构

#### user（用户表）
```sql
CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### product（商品表）
```sql
CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  price DECIMAL(10, 2) NOT NULL,
  stock INT DEFAULT 0,
  seckill_stock INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### inventory（库存表）
```sql
CREATE TABLE inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL UNIQUE,
  total_stock INT DEFAULT 0,
  available_stock INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### order（订单表）
```sql
CREATE TABLE `order` (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  total_price DECIMAL(10, 2) NOT NULL,
  status INT DEFAULT 0 COMMENT '0-待支付, 1-已支付, 2-已取消',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_product_id (product_id)
);
```

#### payment（支付记录表）
```sql
CREATE TABLE payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  amount DECIMAL(10, 2) NOT NULL,
  status INT DEFAULT 0 COMMENT '0-待支付, 1-支付成功, 2-支付失败',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 配置说明

### application.yml 主要配置

```yaml
spring:
  datasource:
    # 主数据源（写操作）
    master:
      url: jdbc:mysql://localhost:3306/distributed_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
      username: root
      password: root
      driver-class-name: com.mysql.cj.jdbc.Driver
    # 从数据源（读操作）
    slave:
      url: jdbc:mysql://localhost:3306/distributed_system?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
      username: root
      password: root
      driver-class-name: com.mysql.cj.jdbc.Driver
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
    database: 0
  
  # ElasticSearch配置
  data:
    elasticsearch:
      client:
        reactive:
          endpoints: localhost:9200
  
  # Kafka配置（可选）
  kafka:
    bootstrap-servers: localhost:9092
```

## 项目亮点

1. **缓存三重保护**：完美解决缓存穿透、击穿、雪崩问题
2. **注解驱动的读写分离**：使用@ReadOnly注解，简单易用
3. **ElasticSearch自动同步**：启动时自动同步商品数据
4. **Lua脚本秒杀**：原子性操作，防止超卖
5. **实例ID拦截器**：便于负载均衡验证
6. **完整的功能覆盖**：用户、商品、库存、订单、支付、搜索全链路

## 常见问题

### 1. 如何验证读写分离是否生效？

在 `ReadOnlyRoutingAspect.java` 中添加日志，查看方法执行时是否切换到从数据源。

### 2. 如何验证负载均衡是否生效？

查看HTTP响应头中的 `X-Instance-Id`，多次刷新应该看到不同的实例ID。

### 3. Kafka不可用时会影响功能吗？

不会。代码中有null检查和异常处理，Kafka不可用时会降级为同步处理。

## 许可证

MIT License
