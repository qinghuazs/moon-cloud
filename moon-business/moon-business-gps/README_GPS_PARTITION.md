# GPS数据分表功能使用说明

## 概述

本功能为GPS数据提供了按日期自动分表的解决方案，包括：

1. **gps_data_realtime表**：GPS实时数据表及其对应的实体类和Mapper
2. **GPS数据分表系统**：自动按日期创建gps_data分表（格式：gps_data_YYYYMMDD）
3. **MyBatis拦截器**：自动根据gps_time字段将数据路由到对应的分表
4. **分表管理服务**：提供分表的创建、清理和统计功能
5. **REST API接口**：提供分表管理的HTTP接口

## 文件结构

```
src/main/java/com/moon/cloud/business/gps/
├── entity/
│   └── GpsDataRealtime.java              # GPS实时数据实体类
├── mapper/
│   └── GpsDataRealtimeMapper.java        # GPS实时数据Mapper接口
├── plugin/
│   └── GpsDataPartitionInterceptor.java  # GPS数据分表拦截器
├── config/
│   └── MyBatisConfig.java                # MyBatis配置类
├── service/
│   └── GpsPartitionService.java          # 分表管理服务
└── controller/
    └── GpsPartitionController.java       # 分表管理REST API

src/main/resources/sql/
└── gps_data_partition_trigger.sql         # 数据库触发器和存储过程
```

## 功能特性

### 1. GPS实时数据表 (gps_data_realtime)

- **用途**：存储最新的GPS数据，用于实时查询
- **特点**：数据量相对较小，查询性能高
- **实体类**：`GpsDataRealtime.java`
- **Mapper**：`GpsDataRealtimeMapper.java`

#### 主要查询方法：
- `selectLatestByVehicleId()` - 查询指定车辆的最新GPS数据
- `selectLatestByVehicleIds()` - 批量查询多个车辆的最新GPS数据
- `selectAllLatest()` - 查询所有车辆的最新GPS数据
- `selectByVehicleIdAndTimeRange()` - 查询指定车辆和时间范围的GPS数据

### 2. GPS数据分表系统

#### 分表规则
- **表名格式**：`gps_data_YYYYMMDD`
- **分表依据**：GPS时间（gps_time字段）
- **自动创建**：通过触发器和存储过程自动创建

#### 数据库组件
- **存储过程**：
  - `CreateGpsDataPartition(date)` - 创建指定日期的分表
  - `CreateFuturePartitions(days)` - 批量创建未来几天的分表
  - `CleanOldPartitions(days)` - 清理指定天数前的分表
- **触发器**：`gps_data_partition_trigger` - 在插入数据时自动创建分表
- **日志表**：`gps_partition_log` - 记录分表创建和删除的日志

### 3. MyBatis拦截器

`GpsDataPartitionInterceptor` 自动拦截对 `gps_data` 表的操作，根据 `gps_time` 字段将SQL路由到对应的分表。

#### 工作原理
1. 拦截所有对 `gps_data` 表的SQL操作
2. 从参数中提取 `gps_time` 字段
3. 根据时间生成对应的分表名
4. 替换SQL中的表名
5. 执行修改后的SQL

#### 支持的参数类型
- GPS数据实体对象
- Map参数（包含gpsTime字段）
- 嵌套对象（Map中包含GPS实体）

### 4. 分表管理服务

`GpsPartitionService` 提供分表的管理功能：

#### 主要方法
- `createPartitionTable(date)` - 创建指定日期的分表
- `createFuturePartitions(days)` - 批量创建未来分表
- `cleanOldPartitions(days)` - 清理旧分表
- `getPartitionInfo()` - 获取分表信息
- `getPartitionStats(startDate, endDate)` - 获取分表统计

#### 定时任务
- **每天凌晨2点**：自动创建未来7天的分表
- **每周日凌晨3点**：自动清理30天前的分表

### 5. REST API接口

`GpsPartitionController` 提供HTTP接口管理分表：

#### 接口列表
- `POST /api/gps/partition/create` - 创建指定日期的分表
- `POST /api/gps/partition/create-future` - 批量创建未来分表
- `DELETE /api/gps/partition/clean` - 清理旧分表
- `GET /api/gps/partition/list` - 获取分表信息列表
- `GET /api/gps/partition/stats` - 获取分表统计信息
- `POST /api/gps/partition/create-today` - 创建今天的分表
- `POST /api/gps/partition/create-tomorrow` - 创建明天的分表

## 使用指南

### 1. 初始化设置

#### 执行SQL脚本
```sql
-- 执行分表相关的存储过程和触发器
source src/main/resources/sql/gps_data_partition_trigger.sql
```

#### 启用定时任务
确保Spring Boot应用启用了定时任务：
```java
@EnableScheduling
@SpringBootApplication
public class GpsApplication {
    // ...
}
```

### 2. 数据操作

#### 插入GPS数据
```java
@Autowired
private GpsDataMapper gpsDataMapper;

// 插入数据时，拦截器会自动路由到对应日期的分表
GpsData gpsData = new GpsData();
gpsData.setVehicleId("V0001");
gpsData.setGpsTime(LocalDateTime.now());
// ... 设置其他字段
gpsDataMapper.insert(gpsData);
```

#### 查询GPS数据
```java
// 查询会自动路由到对应的分表
List<GpsData> dataList = gpsDataMapper.selectByVehicleIdAndTimeRange(
    "V0001", 
    LocalDateTime.of(2024, 1, 15, 0, 0),
    LocalDateTime.of(2024, 1, 15, 23, 59)
);
```

#### 使用实时数据表
```java
@Autowired
private GpsDataRealtimeMapper realtimeMapper;

// 查询车辆最新位置
GpsDataRealtime latest = realtimeMapper.selectLatestByVehicleId("V0001");

// 查询所有车辆最新位置
List<GpsDataRealtime> allLatest = realtimeMapper.selectAllLatest();
```

### 3. 分表管理

#### 通过服务类管理
```java
@Autowired
private GpsPartitionService partitionService;

// 创建指定日期的分表
boolean success = partitionService.createPartitionTable(LocalDate.of(2024, 1, 15));

// 批量创建未来7天的分表
partitionService.createFuturePartitions(7);

// 清理30天前的分表
partitionService.cleanOldPartitions(30);

// 获取分表信息
List<Map<String, Object>> partitionInfo = partitionService.getPartitionInfo();
```

#### 通过REST API管理
```bash
# 创建指定日期的分表
curl -X POST "http://localhost:8080/api/gps/partition/create?date=2024-01-15"

# 批量创建未来7天的分表
curl -X POST "http://localhost:8080/api/gps/partition/create-future?daysAhead=7"

# 清理30天前的分表
curl -X DELETE "http://localhost:8080/api/gps/partition/clean?daysToKeep=30"

# 获取分表信息
curl -X GET "http://localhost:8080/api/gps/partition/list"

# 获取分表统计
curl -X GET "http://localhost:8080/api/gps/partition/stats?startDate=2024-01-01&endDate=2024-01-31"
```

## 配置说明

### MyBatis配置

拦截器已通过 `MyBatisConfig` 自动注册，无需额外配置。

### 定时任务配置

可以通过修改 `GpsPartitionService` 中的 `@Scheduled` 注解来调整定时任务的执行时间：

```java
// 每天凌晨2点执行
@Scheduled(cron = "0 0 2 * * ?")
public void scheduledCreatePartitions() {
    // ...
}

// 每周日凌晨3点执行
@Scheduled(cron = "0 0 3 ? * SUN")
public void scheduledCleanPartitions() {
    // ...
}
```

## 性能优化建议

### 1. 索引优化
- 每个分表都会自动创建必要的索引
- 根据查询模式可以添加复合索引

### 2. 数据清理策略
- 建议保留30-90天的历史数据
- 可以根据业务需求调整清理周期

### 3. 分表预创建
- 建议提前创建未来7-30天的分表
- 避免在高峰期创建分表影响性能

### 4. 监控和告警
- 监控分表创建和清理的执行情况
- 监控各分表的数据量和查询性能

## 故障排除

### 1. 分表创建失败
- 检查数据库权限
- 检查存储过程是否正确执行
- 查看 `gps_partition_log` 表的错误日志

### 2. 数据路由失败
- 检查 `gps_time` 字段是否正确设置
- 检查拦截器是否正常工作
- 查看应用日志中的警告信息

### 3. 性能问题
- 检查分表的索引是否正确创建
- 检查查询是否跨越多个分表
- 考虑优化查询条件

## 注意事项

1. **时区问题**：确保应用和数据库使用相同的时区
2. **事务处理**：跨分表的事务需要特别注意
3. **数据一致性**：实时表和历史分表之间的数据同步
4. **备份策略**：制定针对分表的备份和恢复策略
5. **监控告警**：建立完善的监控和告警机制

## 扩展功能

### 1. 数据归档
可以扩展实现将旧分表数据归档到其他存储系统。

### 2. 读写分离
可以配置读写分离，将查询操作分散到从库。

### 3. 分表合并
可以实现将多个小分表合并为更大的分表。

### 4. 动态分表策略
可以根据数据量动态调整分表策略（按小时、按周等）。