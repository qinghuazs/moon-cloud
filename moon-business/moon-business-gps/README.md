# Moon GPS Service

GPS服务模块，主要模拟大批量GPS的生成和处理，实现类似车载押运项目的相关功能。

## 功能特性

- **GPS模拟器**: 模拟100辆车的GPS数据生成，每5秒生成一次
- **Kafka消息队列**: 将GPS数据发送到Kafka主题进行异步处理
- **GPS数据消费**: 使用线程池异步消费GPS数据
- **路线偏离检测**: 检测车辆是否偏离预定路线
- **区域驶入驶出检测**: 检测车辆驶入或驶出指定区域
- **数据持久化**: 将GPS数据存储到MySQL数据库
- **REST API**: 提供GPS数据查询接口

## 技术栈

- Spring Boot 3.2.0
- Spring Kafka
- MyBatis Plus 3.5.10.1
- MySQL 8.0.33
- Lombok
- Jackson

## 项目结构

```
src/main/java/com/moon/cloud/business/gps/
├── GpsApplication.java              # 启动类
├── controller/
│   └── GpsController.java           # REST控制器
├── consumer/
│   └── GpsDataConsumer.java         # Kafka消费者
├── dto/
│   └── GpsMessage.java              # GPS消息传输对象
├── entity/
│   └── GpsData.java                 # GPS数据实体
├── mapper/
│   └── GpsDataMapper.java           # 数据访问层
└── service/
    ├── GpsSimulatorService.java     # GPS模拟器服务
    └── GpsProcessingService.java    # GPS数据处理服务
```

## 环境要求

### 必需组件

1. **MySQL 8.0+**
   ```bash
   # 创建数据库并执行初始化脚本
   mysql -u root -p < src/main/resources/sql/init.sql
   ```

2. **Apache Kafka**
   ```bash
   # 启动Zookeeper
   bin/zookeeper-server-start.sh config/zookeeper.properties
   
   # 启动Kafka
   bin/kafka-server-start.sh config/server.properties
   
   # 创建GPS数据主题
   bin/kafka-topics.sh --create --topic gps-data --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
   ```

3. **Java 21+**

## 配置说明

### 数据库配置

在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/moon_gps?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

### Kafka配置

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
    consumer:
      group-id: gps-consumer-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

## 运行方式

### 1. 编译项目

```bash
cd /Users/xingleiwang/Documents/Code/Java/moon-cloud
mvn clean compile
```

### 2. 启动应用

```bash
cd moon-business/moon-business-gps
mvn spring-boot:run
```

或者运行主类：
```bash
java -jar target/moon-business-gps-1.0.0-SNAPSHOT.jar
```

## API接口

### 1. 健康检查

```http
GET /api/gps/health
```

### 2. 手动触发GPS数据生成

```http
POST /api/gps/generate
```

### 3. 获取车辆最新GPS数据

```http
GET /api/gps/vehicle/{vehicleId}/latest
```

示例：
```http
GET /api/gps/vehicle/V0001/latest
```

### 4. 获取车辆GPS历史数据

```http
GET /api/gps/vehicle/{vehicleId}/history?startTime={startTime}&endTime={endTime}
```

示例：
```http
GET /api/gps/vehicle/V0001/history?startTime=2024-01-01T00:00:00&endTime=2024-01-01T23:59:59
```

## 功能说明

### GPS模拟器

- 自动模拟100辆车（V0001-V0100）的GPS数据
- 每5秒生成一次GPS数据
- 模拟北京市范围内的坐标（天安门附近）
- 包含经纬度、速度、方向、海拔等信息

### 路线偏离检测

- 预定义了北京市内的一条示例路线
- 当车辆偏离路线超过500米时触发告警
- 记录偏离事件到日志

### 区域驶入驶出检测

- 预定义了北京市中心区域边界
- 检测车辆驶入或驶出指定区域
- 记录驶入驶出事件到日志

### 异步处理

- 使用Kafka进行消息队列处理
- 消费者使用20个线程的线程池异步处理GPS数据
- 保证高并发处理能力

## 监控和日志

应用启动后可以通过日志观察：

1. GPS数据生成情况
2. Kafka消息发送和消费情况
3. 路线偏离和区域驶入驶出事件
4. 数据库操作情况

## 扩展功能

可以根据实际需求扩展以下功能：

1. **更复杂的路线规划**: 支持多条路线，动态路线规划
2. **实时告警**: 集成短信、邮件等告警方式
3. **地图可视化**: 集成地图组件显示车辆位置
4. **历史轨迹回放**: 支持轨迹回放功能
5. **统计分析**: 车辆行驶里程、速度分析等
6. **围栏功能**: 支持多边形围栏，更复杂的区域检测

## 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 确认数据库连接配置正确
   - 确认数据库已创建并执行了初始化脚本

2. **Kafka连接失败**
   - 检查Kafka服务是否启动
   - 确认Kafka配置正确
   - 确认gps-data主题已创建

3. **GPS数据未生成**
   - 检查定时任务是否正常执行
   - 查看应用日志确认是否有异常
   - 可以手动调用生成接口测试