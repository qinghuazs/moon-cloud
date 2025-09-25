# Moon Cloud Redis Docker Compose

这是为Moon Cloud项目配置的Redis Docker Compose文件，包含了生产环境所需的完整配置。

## 目录结构

```
中间件/Redis/
├── docker-compose.yml    # Docker Compose配置文件
├── conf/
│   └── redis.conf       # Redis配置文件
├── data/                # Redis数据持久化目录
├── logs/                # Redis日志目录
└── README.md           # 使用说明
```

## 配置特性

### 核心配置
- **镜像版本**: Redis 7.2 Alpine (轻量级生产版本)
- **容器名称**: moon-cloud-redis
- **端口映射**: 6379:6379
- **密码**: yourpassword (与项目配置一致)
- **网络**: moon-cloud-network

### 持久化配置
- **RDB持久化**: 启用，多种保存策略
- **AOF持久化**: 启用，每秒同步
- **数据目录**: ./data 映射到容器内 /data

### 性能优化
- **最大内存**: 1GB
- **淘汰策略**: allkeys-lru
- **最大连接数**: 10000
- **延迟监控**: 启用

### 安全配置
- **密码认证**: 必须
- **危险命令**: 已禁用 FLUSHDB, FLUSHALL, DEBUG
- **保护模式**: 关闭 (Docker容器内安全)

## 使用方法

### 启动Redis
```bash
docker compose up -d
```

### 查看状态
```bash
docker compose ps
docker compose logs -f redis
```

### 停止服务
```bash
docker compose down
```

### 连接测试
```bash
# 使用redis-cli连接
docker exec -it moon-cloud-redis redis-cli
# 输入密码
127.0.0.1:6379> AUTH yourpassword
# 测试连接
127.0.0.1:6379> ping
```

## 与Spring Boot集成

确保你的Spring Boot应用使用以下配置连接Redis:

```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
    password: yourpassword
    database: 0
    timeout: 6000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

或使用环境变量:
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=yourpassword
REDIS_DATABASE=0
```

## 监控和维护

### 健康检查
Docker Compose配置了健康检查，每30秒检查一次Redis状态。

### 日志查看
```bash
# 查看Redis日志
docker-compose logs -f redis

# 或查看映射的日志文件
tail -f logs/redis-server.log
```

### 数据备份
```bash
# 手动触发RDB保存
docker exec moon-cloud-redis redis-cli -a yourpassword BGSAVE

# 备份数据目录
tar -czf redis-backup-$(date +%Y%m%d).tar.gz data/
```

## 注意事项

1. **密码安全**: 生产环境请修改默认密码
2. **内存配置**: 根据实际需求调整maxmemory设置
3. **网络配置**: 确保防火墙配置正确
4. **数据持久化**: 定期备份data目录
5. **监控**: 建议配置Redis监控工具

## 故障排除

### 常见问题
1. **端口被占用**: 检查6379端口是否被其他程序使用
2. **权限问题**: 确保data和logs目录有正确的权限
3. **内存不足**: 调整maxmemory配置或增加系统内存
4. **连接超时**: 检查网络配置和防火墙设置

### 重置Redis
```bash
# 停止容器并删除数据
docker-compose down -v
rm -rf data/* logs/*
docker-compose up -d
```