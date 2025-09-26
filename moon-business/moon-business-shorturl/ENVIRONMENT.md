# 短链接服务环境变量配置指南

## 概述

Moon Business Short URL 服务使用环境变量进行配置，支持通过 `.env` 文件或系统环境变量进行配置。

## 配置方式

### 1. 使用 .env 文件（推荐）

复制 `.env.template` 为 `.env` 文件：
```bash
cp .env.template .env
```

然后编辑 `.env` 文件填入实际配置值。

### 2. 系统环境变量

也可以直接设置系统环境变量，优先级高于 `.env` 文件。

## 配置项说明

### 数据库配置

| 环境变量 | 描述 | 默认值 |
|---------|------|--------|
| `DB_HOST` | MySQL数据库主机地址 | `localhost` |
| `DB_PORT` | MySQL数据库端口 | `3306` |
| `DB_NAME` | 数据库名称 | `shorturl` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `password` |

### HikariCP连接池配置

| 环境变量 | 描述 | 默认值 |
|---------|------|--------|
| `DB_HIKARI_MINIMUM_IDLE` | 最小空闲连接数 | `5` |
| `DB_HIKARI_MAXIMUM_POOL_SIZE` | 最大连接池大小 | `20` |
| `DB_HIKARI_IDLE_TIMEOUT` | 空闲连接超时时间（毫秒） | `30000` |
| `DB_HIKARI_MAX_LIFETIME` | 连接最大生命周期（毫秒） | `900000` |
| `DB_HIKARI_CONNECTION_TIMEOUT` | 连接超时时间（毫秒） | `30000` |

### Redis配置

| 环境变量 | 描述 | 默认值 |
|---------|------|--------|
| `REDIS_HOST` | Redis主机地址 | `localhost` |
| `REDIS_PORT` | Redis端口 | `6379` |
| `REDIS_PASSWORD` | Redis密码（空表示无密码） | `` |
| `REDIS_DATABASE` | Redis数据库索引 | `0` |
| `REDIS_TIMEOUT` | Redis连接超时时间（毫秒） | `3000` |

### Redis连接池配置

| 环境变量 | 描述 | 默认值 |
|---------|------|--------|
| `REDIS_POOL_MAX_ACTIVE` | 最大活跃连接数 | `20` |
| `REDIS_POOL_MAX_WAIT` | 最大等待时间（毫秒） | `-1` |
| `REDIS_POOL_MAX_IDLE` | 最大空闲连接数 | `10` |
| `REDIS_POOL_MIN_IDLE` | 最小空闲连接数 | `5` |

### 应用配置

| 环境变量 | 描述 | 默认值 |
|---------|------|--------|
| `APP_DOMAIN` | 应用域名（用于生成短链接） | `http://localhost:8080` |

### 短链接配置

| 环境变量 | 描述 | 默认值 |
|---------|------|--------|
| `SHORTURL_DEFAULT_LENGTH` | 默认短链长度 | `7` |
| `SHORTURL_MAX_RETRY` | 最大重试次数 | `3` |
| `SHORTURL_DEFAULT_EXPIRE_DAYS` | 默认过期时间（天） | `365` |
| `SHORTURL_CACHE_EXPIRE_SECONDS` | 缓存过期时间（秒） | `3600` |
| `SHORTURL_MACHINE_ID` | 机器ID（用于Snowflake算法） | `1` |

### 布隆过滤器配置

| 环境变量 | 描述 | 默认值 |
|---------|------|--------|
| `BLOOM_FILTER_EXPECTED_INSERTIONS` | 预期插入数量 | `10000000` |
| `BLOOM_FILTER_FALSE_POSITIVE_RATE` | 假阳性率 | `0.001` |

## 启动命令

### 开发环境
```bash
# 确保.env文件存在并配置正确
mvn spring-boot:run
```

### 生产环境
```bash
# 方式1：使用.env文件
java -jar moon-business-shorturl.jar

# 方式2：直接使用环境变量
DB_HOST=prod-mysql-host \
DB_USERNAME=prod_user \
DB_PASSWORD=your_prod_password \
REDIS_HOST=prod-redis-host \
java -jar moon-business-shorturl.jar
```

### Docker运行
```bash
# 使用环境变量
docker run -e DB_HOST=mysql-host \
           -e DB_USERNAME=root \
           -e DB_PASSWORD=password \
           -e REDIS_HOST=redis-host \
           moon-business-shorturl

# 或者挂载.env文件
docker run -v $(pwd)/.env:/app/.env moon-business-shorturl
```

## 配置验证

启动应用时会在日志中看到配置加载信息：

```
2024-01-01 10:00:00.000 [main] INFO  c.m.s.config.EnvironmentConfig - 已加载环境变量配置，共25个变量
2024-01-01 10:00:01.000 [main] INFO  c.m.s.config.DatabaseConfig - 配置数据库连接: url=jdbc:mysql://localhost:3306/shorturl?..., username=root
2024-01-01 10:00:02.000 [main] INFO  c.m.s.config.RedisConfig - 配置Redis连接: host=localhost, port=6379, database=0
2024-01-01 10:00:03.000 [main] INFO  c.m.s.config.RedisConfig - Redis模板配置完成
```

## 安全注意事项

1. **生产环境**：确保 `.env` 文件不会被提交到版本控制系统
2. **密码安全**：使用强密码并定期更换
3. **网络安全**：在生产环境中使用内网地址或VPN
4. **权限控制**：确保数据库和Redis用户具有最小必要权限

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查 `DB_HOST`、`DB_PORT`、`DB_USERNAME`、`DB_PASSWORD` 配置
   - 确保数据库服务正在运行
   - 验证网络连通性

2. **Redis连接失败**
   - 检查 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 配置
   - 确保Redis服务正在运行
   - 验证Redis访问权限

3. **环境变量未生效**
   - 确认 `.env` 文件位于应用根目录
   - 检查环境变量名称拼写
   - 重启应用使配置生效

### 调试模式

开启调试日志查看详细配置信息：
```bash
java -jar moon-business-shorturl.jar --logging.level.com.mooncloud.shorturl=DEBUG
```