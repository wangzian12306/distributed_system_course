# Docker 部署文档

## 架构概览

本项目使用 Docker Compose 部署完整的分布式系统，包含以下服务：

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| MySQL | mysql:8.0 | 3306 | 数据库 |
| Redis | redis:latest | 6379 | 缓存 |
| ElasticSearch | elasticsearch:7.17.10 | 9200, 9300 | 搜索引擎 |
| Zookeeper | zookeeper:latest | 2181 | Kafka依赖 |
| Kafka | wurstmeister/kafka:latest | 9092 | 消息队列 |
| Backend1 | 自定义 | 8081:8080 | 后端服务实例1 |
| Backend2 | 自定义 | 8082:8080 | 后端服务实例2 |
| Nginx | nginx:latest | 80 | 负载均衡、反向代理 |

## 前置条件

- Docker 20.10+
- Docker Compose 1.29+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

## 快速开始

### 1. 克隆项目
```bash
cd d:\desktop\大三下\分布式\code\fenbu
```

### 2. 构建并启动所有服务
```bash
docker-compose up -d --build
```

### 3. 查看服务状态
```bash
docker-compose ps
```

### 4. 查看服务日志
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend1
docker-compose logs -f backend2
```

### 5. 访问应用
- 前端页面：http://localhost
- API接口：http://localhost/api
- Backend1直接访问：http://localhost:8081
- Backend2直接访问：http://localhost:8082

## 服务配置详解

### MySQL 配置
- 数据库名：distributed_system
- 用户名：root
- 密码：root
- 端口：3306
- 数据持久化：mysql_data 卷

### Redis 配置
- 无密码
- 端口：6379
- 数据库：0

### ElasticSearch 配置
- 单节点模式
- 端口：9200（HTTP）、9300（TCP）
- 内存限制：512MB
- 数据持久化：es_data 卷

### Kafka 配置
- Zookeeper：zookeeper:2181
- 监听地址：0.0.0.0:9092
- 广告地址：kafka:9092

### Nginx 配置
- 负载均衡算法：轮询（weight=1）
- 后端服务：backend1:8080, backend2:8080
- 动静分离：/static/ 路径直接访问静态文件
- API代理：/api/ 路径转发到后端

### 后端服务配置
环境变量：
- `SPRING_DATASOURCE_URL`：数据库连接
- `SPRING_REDIS_HOST`：Redis主机
- `SPRING_DATA_ELASTICSEARCH_CLIENT_REACTIVE_ENDPOINTS`：ElasticSearch地址
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`：Kafka地址

## 常用操作命令

### 停止所有服务
```bash
docker-compose stop
```

### 启动已停止的服务
```bash
docker-compose start
```

### 重启所有服务
```bash
docker-compose restart
```

### 删除所有容器和网络（保留数据）
```bash
docker-compose down
```

### 删除所有容器、网络和数据卷
```bash
docker-compose down -v
```

### 进入容器
```bash
# 进入MySQL容器
docker exec -it mysql bash

# 进入Redis容器
docker exec -it redis redis-cli

# 进入后端容器
docker exec -it backend1 bash
```

### 查看资源使用
```bash
docker stats
```

## 验证部署

### 1. 验证MySQL
```bash
docker exec -it mysql mysql -uroot -proot -e "SHOW DATABASES;"
```

### 2. 验证Redis
```bash
docker exec -it redis redis-cli PING
# 返回 PONG 表示正常
```

### 3. 验证ElasticSearch
```bash
curl http://localhost:9200
```

### 4. 验证后端服务
```bash
# 访问健康检查接口
curl http://localhost:8081/api/products
curl http://localhost:8082/api/products
```

### 5. 验证Nginx负载均衡
多次执行以下命令，观察 `X-Instance-Id` 响应头的变化：
```bash
curl -I http://localhost/api/products
```

## 故障排查

### 问题1：后端服务启动失败
**症状**：backend1/backend2 容器不断重启

**排查步骤**：
```bash
# 查看后端日志
docker-compose logs backend1

# 检查数据库连接
docker exec -it mysql mysql -uroot -proot -e "SELECT 1;"
```

### 问题2：ElasticSearch 启动失败
**症状**：elasticsearch 容器退出

**可能原因**：内存不足

**解决方法**：
- 确保宿主机有足够内存
- 调整 `docker-compose.yml` 中的 `ES_JAVA_OPTS` 参数

### 问题3：端口冲突
**症状**：端口已被占用

**解决方法**：
- 修改 `docker-compose.yml` 中的端口映射
- 或者停止占用端口的进程

### 问题4：数据丢失
**症状**：重启后数据消失

**原因**：没有使用数据卷

**解决方法**：确保 `docker-compose.yml` 中配置了 volumes

## 性能优化

### 1. 内存分配
根据服务器资源调整各服务的内存限制：
```yaml
services:
  elasticsearch:
    environment:
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
```

### 2. 网络优化
使用 host 网络模式（不推荐用于生产）：
```yaml
services:
  mysql:
    network_mode: host
```

### 3. 日志优化
限制日志文件大小：
```yaml
services:
  backend1:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

## 安全建议

1. **修改默认密码**：在生产环境中修改 MySQL、Redis 等的默认密码
2. **限制端口暴露**：只暴露必要的端口
3. **使用网络隔离**：将服务放在不同的网络中
4. **定期更新镜像**：保持 Docker 镜像为最新版本
5. **数据备份**：定期备份数据卷

## 备份与恢复

### 备份MySQL数据
```bash
docker exec mysql mysqldump -uroot -proot distributed_system > backup.sql
```

### 恢复MySQL数据
```bash
docker exec -i mysql mysql -uroot -proot distributed_system < backup.sql
```

### 备份数据卷
```bash
# 备份MySQL数据卷
docker run --rm -v mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz /data

# 备份ES数据卷
docker run --rm -v es_data:/data -v $(pwd):/backup alpine tar czf /backup/es_backup.tar.gz /data
```

## 下一步

部署成功后，你可以：
1. 使用 Postman 测试 API（见 POSTMAN_TEST.md）
2. 查看前端页面 http://localhost
3. 测试秒杀功能
4. 验证缓存效果
5. 测试搜索功能
