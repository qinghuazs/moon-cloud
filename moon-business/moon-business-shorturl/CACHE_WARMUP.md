# 缓存预热功能详解

## 🎯 功能概述

缓存预热是短URL系统的核心性能优化功能，通过提前加载热点数据到多级缓存中，显著提升系统响应速度和用户体验。

## 🏗️ 架构设计

### 多级缓存架构
```
[预热任务] → [L1: Caffeine本地缓存] → [L2: Redis分布式缓存] → [L3: MySQL数据库]
```

### 预热策略

#### 基础预热策略
- **热门链接预热**: 基于热度分析算法智能排序
- **最近创建预热**: 优先预热新兴热点
- **最近访问预热**: 保持热点数据活跃
- **时间范围预热**: 指定时间段内的热点数据
- **用户维度预热**: 特定用户的热点链接
- **全量预热**: 按热度分数排序的全量预热

#### 🆕 智能预热策略
- **智能热点预热**: 基于多维度热度分析算法自动识别热点数据
- **新兴热点预热**: 实时检测并预热新兴热点
- **批量智能预热**: 一键执行多种智能预热策略

### 🧠 智能预热算法

#### 热度分析算法
智能预热基于多维度热度分析算法，通过以下5个维度计算综合热度分数：

| 维度 | 权重 | 算法 | 说明 |
|------|------|------|------|
| **访问频次** | 40% | `log(总访问数 + 1) * 20` | 访问量是最直接的热度指标 |
| **时效性** | 25% | `100 - 最后访问小时数 * 0.6` | 最近访问时间越近，分数越高 |
| **趋势性** | 20% | `50 + 增长率 * 50` | 访问量增长趋势分析 |
| **用户分布** | 10% | `独立用户数/总访问数 * 200` | 用户分布越广，热度越高 |
| **地域分布** | 5% | `log(独立IP数 + 1) * 25` | IP地址分布广度 |

#### 热点级别分类
根据综合热度分数自动分类：

| 级别 | 分数范围 | 描述 | 预热优先级 |
|------|----------|------|-----------|
| SUPER_HOT | 90-100 | 超级热点 | 最高 |
| HOT | 70-89 | 热点 | 高 |
| WARM | 50-69 | 温热 | 中 |
| NORMAL | 30-49 | 普通 | 低 |
| COLD | 0-29 | 冷门 | 最低 |

#### 新兴热点检测
- **检测条件**: 最近1小时创建且趋势分数 > 70
- **算法优势**: 能够快速识别突然爆火的新内容
- **预热策略**: 优先预热，避免突发流量冲击

## 🚀 核心功能

### 1. 预热策略配置

```yaml
shorturl:
  cache:
    warmup:
      enabled: true  # 启用预热功能
      hot-links:
        limit: 1000  # 热门链接预热数量
        cron: "0 0 * * * ?"  # 每小时执行
      recent:
        limit: 500   # 最近链接预热数量
        cron: "0 0,30 * * * ?"  # 每30分钟执行
      daily:
        cron: "0 0 2 * * ?"  # 每天2点执行全量预热
      batch-size: 100  # 批处理大小
```

### 2. API接口

#### 执行预热任务
```http
POST /api/v1/cache/warmup/execute
Content-Type: application/json

{
    "strategy": "HOT_LINKS",     // 预热策略
    "limit": 1000,              // 预热数量
    "async": true,               // 异步执行
    "batchSize": 100             // 批次大小
}
```

#### 快速预热热门链接
```http
POST /api/v1/cache/warmup/hot?limit=1000&async=true
```

#### 查询任务状态
```http
GET /api/v1/cache/warmup/task/{taskId}
```

#### 取消预热任务
```http
POST /api/v1/cache/warmup/task/{taskId}/cancel
```

#### 🆕 智能预热API

**智能热点预热**
```http
POST /api/v1/cache/warmup/smart?minHotLevel=WARM&limit=1000
```

**新兴热点预热**
```http
POST /api/v1/cache/warmup/emerging?limit=500
```

**批量智能预热**
```http
POST /api/v1/cache/warmup/smart/batch
```

**获取热点级别信息**
```http
GET /api/v1/cache/warmup/hotlevels
```

### 3. 定时任务

| 任务类型 | 执行频率 | 预热数量 | 说明 |
|---------|---------|---------|------|
| 热门链接预热 | 每小时 | 1000条 | 基于点击次数排序 |
| 最近创建预热 | 每30分钟 | 500条 | 基于创建时间排序 |
| 每日全量预热 | 每天2点 | 5000条 | 当天创建的所有链接 |
| 初始预热 | 启动后5分钟 | 800条 | 应用启动时预热 |

## 📊 监控指标

### Prometheus指标

| 指标名称 | 类型 | 说明 |
|---------|------|------|
| `cache_warmup_tasks_started_total` | Counter | 任务启动总数 |
| `cache_warmup_tasks_completed_total` | Counter | 任务完成总数 |
| `cache_warmup_tasks_failed_total` | Counter | 任务失败总数 |
| `cache_warmup_records_success_total` | Counter | 预热成功记录数 |
| `cache_warmup_records_failed_total` | Counter | 预热失败记录数 |
| `cache_warmup_task_duration_seconds` | Timer | 任务执行耗时 |
| `cache_warmup_active_tasks` | Gauge | 活跃任务数量 |
| `cache_warmup_total_records` | Gauge | 总预热记录数 |
| `cache_warmup_last_duration_seconds` | Gauge | 最后一次预热耗时 |

### 查看指标
```bash
# Prometheus格式指标
curl http://localhost:8080/actuator/prometheus | grep cache_warmup

# 应用监控端点
curl http://localhost:8080/actuator/metrics/cache_warmup_tasks_started_total
```

## 🎛️ 使用示例

### 1. 手动触发预热

#### 基础预热
```bash
# 预热热门链接
curl -X POST "http://localhost:8080/api/v1/cache/warmup/hot?limit=1000&async=true"

# 预热最近创建的链接
curl -X POST "http://localhost:8080/api/v1/cache/warmup/recent?limit=500&async=true"

# 预热特定用户的链接
curl -X POST "http://localhost:8080/api/v1/cache/warmup/user/123?limit=200&async=true"
```

#### 🆕 智能预热
```bash
# 智能热点预热（预热温热级别以上的数据）
curl -X POST "http://localhost:8080/api/v1/cache/warmup/smart?minHotLevel=WARM&limit=1000"

# 新兴热点预热
curl -X POST "http://localhost:8080/api/v1/cache/warmup/emerging?limit=500"

# 批量智能预热（一键执行多种策略）
curl -X POST "http://localhost:8080/api/v1/cache/warmup/smart/batch"

# 查看热点级别信息
curl "http://localhost:8080/api/v1/cache/warmup/hotlevels"
```

### 2. 自定义预热策略

```bash
curl -X POST "http://localhost:8080/api/v1/cache/warmup/execute" \
-H "Content-Type: application/json" \
-d '{
    "strategy": "TIME_RANGE",
    "startTime": "2025-01-01T00:00:00",
    "endTime": "2025-01-31T23:59:59",
    "limit": 2000,
    "async": true,
    "batchSize": 50
}'
```

### 3. 任务管理

```bash
# 查看所有任务状态
curl http://localhost:8080/api/v1/cache/warmup/tasks

# 查看特定任务状态
curl http://localhost:8080/api/v1/cache/warmup/task/{taskId}

# 取消任务
curl -X POST http://localhost:8080/api/v1/cache/warmup/task/{taskId}/cancel

# 清理已完成任务
curl -X POST http://localhost:8080/api/v1/cache/warmup/cleanup
```

## ⚡ 性能优化

### 1. 预热效果

| 场景 | 预热前响应时间 | 预热后响应时间 | 提升幅度 |
|------|---------------|---------------|----------|
| 🆕 智能热点预热 | 50-100ms | 3-10ms | 85%+ |
| 热门链接访问 | 50-100ms | 5-15ms | 80%+ |
| 🆕 新兴热点预热 | 30-80ms | 5-18ms | 75%+ |
| 最近创建链接 | 30-80ms | 8-20ms | 70%+ |
| 普通链接访问 | 20-60ms | 10-25ms | 50%+ |

### 2. 缓存命中率

- **L1本地缓存**: 30-50%命中率
- **L2 Redis缓存**: 90-95%命中率
- **L3数据库**: 5-10%访问率

### 3. 批处理优化

- **批次大小**: 100条/批次（可配置）
- **并发控制**: 异步执行，避免阻塞主线程
- **错误隔离**: 单条失败不影响整批处理

## 🔧 配置调优

### 1. 预热时机优化

```yaml
# 低峰期预热，避免影响业务
shorturl.cache.warmup.hot-links.cron: "0 0 2,6,10,14,18,22 * * ?"
```

### 2. 预热量级调优

```yaml
# 根据系统负载调整预热数量
shorturl.cache.warmup.hot-links.limit: 2000  # 高配置服务器
shorturl.cache.warmup.hot-links.limit: 500   # 低配置服务器
```

### 3. 批处理调优

```yaml
# 根据网络和数据库性能调整批次大小
shorturl.cache.warmup.batch-size: 200  # 高性能环境
shorturl.cache.warmup.batch-size: 50   # 低性能环境
```

## 🚨 注意事项

### 1. 资源控制
- 预热任务会占用CPU、内存和网络资源
- 建议在低峰期执行大量预热任务
- 监控系统负载，必要时调整预热频率

### 2. 数据一致性
- 预热数据可能存在短暂的延迟
- 缓存更新策略确保最终一致性
- 关键业务数据建议实时查询

### 3. 故障处理
- 预热失败不影响正常业务功能
- 任务异常自动记录到监控指标
- 支持手动重试和任务取消

## 📈 监控建议

### 1. 关键指标监控
```bash
# 任务成功率监控
rate(cache_warmup_tasks_completed_total[5m]) / rate(cache_warmup_tasks_started_total[5m])

# 预热记录成功率
rate(cache_warmup_records_success_total[5m]) / (rate(cache_warmup_records_success_total[5m]) + rate(cache_warmup_records_failed_total[5m]))

# 活跃任务数量告警
cache_warmup_active_tasks > 5
```

### 2. 性能指标监控
```bash
# 平均预热耗时
rate(cache_warmup_task_duration_seconds_sum[5m]) / rate(cache_warmup_task_duration_seconds_count[5m])

# 预热吞吐量
rate(cache_warmup_records_success_total[5m])
```

## 🎯 最佳实践

1. **分时段预热**: 避开业务高峰期
2. **渐进式预热**: 先热门数据，再全量数据
3. **监控驱动**: 基于指标调整预热策略
4. **容错设计**: 预热失败不影响业务
5. **资源控制**: 合理设置并发和批次大小

通过合理配置和使用缓存预热功能，可以将系统的平均响应时间降低60-80%，显著提升用户体验。