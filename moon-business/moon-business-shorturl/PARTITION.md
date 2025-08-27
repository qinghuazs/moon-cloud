# 数据库分区功能说明

## 概述

短链服务支持数据库表分区功能，通过分区可以提高查询性能，简化数据维护，并支持历史数据的自动清理。

## 分区策略

### URL映射表 (url_mapping)
- **分区类型**: RANGE分区
- **分区键**: `YEAR(created_at) * 100 + MONTH(created_at)`
- **分区粒度**: 按月分区
- **命名规则**: `p202401`, `p202402`, ...
- **数据保留**: 根据配置的保留期限自动清理

### 访问日志表 (url_access_log)
- **分区类型**: RANGE分区
- **分区键**: `TO_DAYS(access_time)`
- **分区粒度**: 按天分区
- **命名规则**: `p20240101`, `p20240102`, ...
- **数据保留**: 默认保留90天

## 配置说明

在 `application.yml` 中配置分区相关参数：

```yaml
app:
  partition:
    enabled: true                    # 是否启用分区功能
    retention-days: 90               # 数据保留天数
    advance-days: 7                  # 提前创建分区的天数
    auto-maintenance: true           # 是否启用自动维护
    batch-size: 1000                 # 批处理大小
```

## 功能特性

### 1. 自动分区管理
- 应用启动时自动检查并创建必要的分区
- 定时任务自动创建未来的分区
- 自动清理过期的分区和数据

### 2. 分区维护
- 每天凌晨2点执行分区维护任务
- 每小时检查分区状态
- 每周日执行分区优化

### 3. 手动管理
- 提供REST API进行分区管理
- 支持手动创建分区
- 支持手动执行维护任务

### 4. 监控和统计
- 分区信息查询
- 分区统计数据
- 分区状态监控

## API接口

### 获取分区信息
```http
GET /api/admin/partition/url-mapping
GET /api/admin/partition/access-log
GET /api/admin/partition/statistics
```

### 分区管理操作
```http
POST /api/admin/partition/url-mapping/create?date=2024-01-01
POST /api/admin/partition/access-log/create?date=2024-01-01
POST /api/admin/partition/maintenance
POST /api/admin/partition/initialize
```

## 数据库支持

### MySQL
- 完全支持分区功能
- 支持自动分区维护
- 支持存储过程和事件调度

### H2 (开发环境)
- 不支持分区功能
- 自动降级为普通表
- 保持功能兼容性

## 部署说明

### 1. MySQL环境部署

1. 确保MySQL版本支持分区功能 (5.1+)
2. 启用事件调度器：
   ```sql
   SET GLOBAL event_scheduler = ON;
   ```
3. 执行初始化脚本：
   ```sql
   source src/main/resources/db/migration/V1__Create_partitioned_tables.sql
   ```

### 2. 配置数据源
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shorturl?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 启用分区功能
```yaml
app:
  partition:
    enabled: true
```

## 性能优化

### 1. 查询优化
- 查询时尽量包含分区键
- 利用分区裁剪提高查询效率
- 避免跨分区的复杂查询

### 2. 索引策略
- 分区表的主键必须包含分区键
- 合理设计复合索引
- 定期分析和优化索引

### 3. 维护策略
- 定期清理过期分区
- 监控分区大小和性能
- 根据业务需求调整分区策略

## 监控和告警

### 1. 分区状态监控
- 分区数量监控
- 分区大小监控
- 分区维护状态监控

### 2. 性能监控
- 查询性能监控
- 分区裁剪效果监控
- 存储空间使用监控

### 3. 告警设置
- 分区创建失败告警
- 分区维护失败告警
- 存储空间不足告警

## 故障排除

### 1. 分区创建失败
- 检查数据库权限
- 检查分区命名冲突
- 检查存储空间

### 2. 分区维护失败
- 检查事件调度器状态
- 检查存储过程权限
- 检查系统资源

### 3. 查询性能问题
- 检查查询是否利用分区裁剪
- 检查索引使用情况
- 分析执行计划

## 最佳实践

1. **分区设计**
   - 根据查询模式设计分区键
   - 避免过多的小分区
   - 考虑未来的数据增长

2. **维护策略**
   - 定期备份重要分区
   - 监控分区维护日志
   - 制定分区恢复计划

3. **性能调优**
   - 定期分析查询性能
   - 优化分区相关的查询
   - 监控系统资源使用

4. **容量规划**
   - 预估数据增长趋势
   - 规划存储容量需求
   - 制定扩容策略