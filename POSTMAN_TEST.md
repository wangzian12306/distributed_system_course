# Postman 接口测试文档

## 目录
- [环境准备](#环境准备)
- [导入Postman集合](#导入postman集合)
- [测试流程](#测试流程)
- [接口详情](#接口详情)
- [测试场景](#测试场景)
- [常见问题](#常见问题)

---

## 环境准备

### 1. 确保服务已启动
确保所有Docker服务已成功启动：
```bash
docker-compose ps
```

### 2. 配置环境变量
在Postman中创建以下环境变量：

| 变量名 | 示例值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost` | 基础URL（通过Nginx访问） |
| `backend1Url` | `http://localhost:8081` | 后端服务1直接访问 |
| `backend2Url` | `http://localhost:8082` | 后端服务2直接访问 |
| `token` | `eyJhbGciOiJIUzI1NiIs...` | JWT Token（登录后获取） |
| `userId` | `1` | 测试用户ID |
| `productId` | `1` | 测试商品ID |
| `orderId` | `1234567890` | 测试订单ID |

---

## 导入Postman集合

### 手动创建集合
1. 打开Postman
2. 点击 "New Collection"
3. 命名为 "分布式商品库存与秒杀系统"
4. 按照下面的接口详情逐个添加请求

### 使用环境变量
在每个请求的URL中使用 `{{baseUrl}}` 代替硬编码的URL。

---

## 测试流程

### 完整测试顺序
1. **用户服务** - 注册用户 → 用户登录
2. **商品服务** - 创建商品 → 获取商品列表 → 获取商品详情
3. **库存服务** - 获取库存 → 扣减库存
4. **订单服务** - 创建普通订单 → 获取订单详情 → 获取用户订单列表
5. **秒杀服务** - 秒杀下单
6. **支付服务** - 订单支付
7. **搜索服务** - 搜索商品
8. **缓存验证** - 验证Redis缓存
9. **负载均衡验证** - 验证Nginx负载均衡

---

## 接口详情

### 一、用户服务

#### 1.1 用户注册
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/users/register`
-  Headers：
  ```
  Content-Type: application/json
  ```
- Body（raw JSON）：
  ```json
  {
    "username": "testuser001",
    "password": "123456",
    "email": "testuser001@example.com"
  }
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体：
  ```json
  {
    "code": 200,
    "message": "注册成功",
    "data": {
      "id": 1,
      "username": "testuser001",
      "email": "testuser001@example.com"
    }
  }
  ```

**Tests（Postman脚本）**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has user data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data).to.be.an('object');
    pm.environment.set("userId", jsonData.data.id);
});
```

---

#### 1.2 用户登录
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/users/login`
- Headers：
  ```
  Content-Type: application/json
  ```
- Body（raw JSON）：
  ```json
  {
    "username": "testuser001",
    "password": "123456"
  }
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体：
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "user": {
        "id": 1,
        "username": "testuser001"
      }
    }
  }
  ```

**Tests（Postman脚本）**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has token", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.token).to.exist;
    pm.environment.set("token", jsonData.data.token);
});
```

---

### 二、商品服务

#### 2.1 创建商品
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/products`
- Headers：
  ```
  Content-Type: application/json
  Authorization: Bearer {{token}}
  ```
- Body（raw JSON）：
  ```json
  {
    "name": "iPhone 15 Pro",
    "description": "苹果最新款手机，A17芯片，钛金属边框",
    "price": 9999.00,
    "stock": 100,
    "seckillStock": 10
  }
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体：
  ```json
  {
    "code": 200,
    "message": "创建成功",
    "data": {
      "id": 1,
      "name": "iPhone 15 Pro",
      "price": 9999.00
    }
  }
  ```

**Tests（Postman脚本）**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Save productId", function () {
    var jsonData = pm.response.json();
    pm.environment.set("productId", jsonData.data.id);
});
```

---

#### 2.2 获取商品列表
**请求信息**
- 方法：`GET`
- URL：`{{baseUrl}}/api/products`
- Headers：
  ```
  Authorization: Bearer {{token}}
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体包含商品数组

**Tests（Postman脚本）**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has product list", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data).to.be.an('array');
});

// 检查X-Instance-Id响应头（验证负载均衡）
pm.test("Has X-Instance-Id header", function () {
    pm.response.to.have.header("X-Instance-Id");
    console.log("Instance ID:", pm.response.headers.get("X-Instance-Id"));
});
```

---

#### 2.3 获取商品详情（带Redis缓存）
**请求信息**
- 方法：`GET`
- URL：`{{baseUrl}}/api/products/{{productId}}`
- Headers：
  ```
  Authorization: Bearer {{token}}
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体包含完整商品信息

**测试说明**
- 第一次请求：从数据库读取并缓存到Redis
- 第二次请求：直接从Redis读取（响应更快）

**Tests（Postman脚本）**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has product data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data).to.be.an('object');
    pm.expect(jsonData.data.id).to.eql(parseInt(pm.environment.get("productId")));
});
```

---

### 三、库存服务

#### 3.1 获取库存
**请求信息**
- 方法：`GET`
- URL：`{{baseUrl}}/api/inventory/{{productId}}`
- Headers：
  ```
  Authorization: Bearer {{token}}
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体包含库存信息

---

#### 3.2 扣减库存
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/inventory/decrease`
- Headers：
  ```
  Content-Type: application/json
  Authorization: Bearer {{token}}
  ```
- Params（Query Parameters）：
  ```
  productId: {{productId}}
  quantity: 1
  ```

**预期响应**
- 状态码：`200 OK`

---

#### 3.3 扣减秒杀库存
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/inventory/decrease-seckill`
- Headers：
  ```
  Content-Type: application/json
  Authorization: Bearer {{token}}
  ```
- Params（Query Parameters）：
  ```
  productId: {{productId}}
  ```

---

### 四、订单服务

#### 4.1 创建普通订单
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/orders`
- Headers：
  ```
  Content-Type: application/json
  Authorization: Bearer {{token}}
  ```
- Body（raw JSON）：
  ```json
  {
    "userId": {{userId}},
    "productId": {{productId}},
    "quantity": 1
  }
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体包含订单ID

**Tests（Postman脚本）**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Save orderId", function () {
    var jsonData = pm.response.json();
    pm.environment.set("orderId", jsonData.data.id);
});
```

---

#### 4.2 秒杀下单
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/orders/seckill`
- Headers：
  ```
  Content-Type: application/json
  Authorization: Bearer {{token}}
  ```
- Params（Query Parameters）：
  ```
  userId: {{userId}}
  productId: {{productId}}
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体包含临时订单ID

**测试说明**
- 使用Lua脚本在Redis中原子化操作
- 预扣减库存，防止超卖
- 异步创建订单到数据库

---

#### 4.3 获取订单详情
**请求信息**
- 方法：`GET`
- URL：`{{baseUrl}}/api/orders/{{orderId}}`
- Headers：
  ```
  Authorization: Bearer {{token}}
  ```

---

#### 4.4 获取用户订单列表
**请求信息**
- 方法：`GET`
- URL：`{{baseUrl}}/api/orders/user/{{userId}}`
- Headers：
  ```
  Authorization: Bearer {{token}}
  ```

---

### 五、支付服务

#### 5.1 订单支付
**请求信息**
- 方法：`POST`
- URL：`{{baseUrl}}/api/payments/pay/{{orderId}}`
- Headers：
  ```
  Authorization: Bearer {{token}}
  ```

**预期响应**
- 状态码：`200 OK`
- 订单状态更新为已支付

---

### 六、搜索服务

#### 6.1 搜索商品
**请求信息**
- 方法：`GET`
- URL：`{{baseUrl}}/api/search`
- Headers：
  ```
  Authorization: Bearer {{token}}
  ```
- Params（Query Parameters）：
  ```
  keyword: iPhone
  ```

**预期响应**
- 状态码：`200 OK`
- 响应体包含匹配的商品列表

**测试说明**
- 使用ElasticSearch进行全文检索
- 支持商品名称和描述的模糊搜索

---

## 测试场景

### 场景1：完整购物流程
**步骤**
1. 注册用户
2. 登录获取Token
3. 创建商品
4. 获取商品详情
5. 创建订单
6. 支付订单

**预期结果**
- 订单状态变为"已支付"
- 库存相应扣减

---

### 场景2：高并发秒杀测试
**前置条件**
- 商品有秒杀库存（如10件）
- 准备多个用户账号

**测试步骤**
1. 使用Postman Runner
2. 设置多个迭代（如50次）
3. 每次迭代使用不同用户ID
4. 同时发起秒杀请求

**预期结果**
- 只有前10个请求成功
- 后续请求返回"秒杀商品已售罄"
- 不会出现超卖

---

### 场景3：缓存验证
**测试步骤**
1. 第一次请求商品详情（记录响应时间）
2. 第二次请求商品详情（响应时间应该更短）
3. 检查Redis中是否有缓存数据

**验证方法**
```bash
# 进入Redis容器
docker exec -it redis redis-cli

# 查看所有键
KEYS product:*

# 查看商品缓存
GET product:1
```

---

### 场景4：负载均衡验证
**测试步骤**
1. 连续多次请求商品列表接口
2. 观察响应头中的 `X-Instance-Id`
3. 验证是否在 backend1 和 backend2 之间切换

**预期结果**
- `X-Instance-Id` 应该交替出现
- 显示不同的实例ID

---

### 场景5：ElasticSearch搜索验证
**测试步骤**
1. 创建多个商品（不同名称和描述）
2. 等待启动同步完成（或手动触发同步）
3. 使用不同关键词搜索
4. 验证搜索结果的相关性

**验证方法**
```bash
# 直接查询ElasticSearch
curl http://localhost:9200/product/_search?q=name:iPhone
```

---

## 常见问题

### Q1: 提示"未登录"或"Token无效"
**A**: 确保已执行登录接口，并将返回的token设置到环境变量中。

### Q2: 秒杀提示"您已经参与过该商品的秒杀"
**A**: 同一用户对同一商品只能秒杀一次，请更换用户ID或商品ID。

### Q3: 搜索不到商品
**A**: 
1. 确认ElasticSearch服务正常运行
2. 等待启动同步完成（约10-30秒）
3. 或者手动调用商品创建接口触发同步

### Q4: 如何查看Redis缓存？
**A**: 
```bash
docker exec -it redis redis-cli
KEYS *
GET product:1
```

### Q5: 如何验证读写分离？
**A**: 在 `ReadOnlyRoutingAspect.java` 中添加日志，或查看MySQL的慢查询日志。

---

## 附录

### 测试数据建议

#### 用户数据
- 用户1：testuser001 / 123456
- 用户2：testuser002 / 123456
- 用户3：testuser003 / 123456

#### 商品数据
- 商品1：iPhone 15 Pro / ¥9999 / 库存100 / 秒杀库存10
- 商品2：MacBook Pro / ¥14999 / 库存50 / 秒杀库存5
- 商品3：AirPods Pro / ¥1999 / 库存200 / 秒杀库存20

---

## 联系方式

如有问题，请查看项目README.md或联系开发团队。
