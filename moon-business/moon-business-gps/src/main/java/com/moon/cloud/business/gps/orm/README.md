# MyBatis 拦截器说明文档

本目录包含了一系列 MyBatis 拦截器，用于实现租户隔离、软删除、版本控制、主键生成、SQL记录和执行时间统计等功能。

## 拦截器列表

### 1. TenantQueryInterceptor - 租户查询拦截器

**功能：**
- 自动为查询语句添加租户ID条件
- 自动为查询语句添加软删除标识（dr = 0）

**处理逻辑：**
- 拦截所有 SELECT 语句
- 检查SQL中是否已包含租户ID条件，如果没有则自动添加
- 自动添加软删除条件 `dr = 0`
- 跳过系统表（information_schema、performance_schema等）

**示例：**
```sql
-- 原始SQL
SELECT * FROM vehicle_info WHERE status = 1

-- 处理后SQL
SELECT * FROM vehicle_info WHERE tenant_id = 'current_tenant' AND dr = 0 AND (status = 1)
```

### 2. TenantUpdateInterceptor - 租户更新拦截器

**功能：**
- 自动为更新语句添加租户ID条件
- 自动更新版本号（乐观锁）
- 自动更新修改时间

**处理逻辑：**
- 拦截所有 UPDATE 语句
- 在SET子句中添加 `update_time = NOW()` 和 `version = version + 1`
- 在WHERE子句中添加租户ID条件和版本号条件

**示例：**
```sql
-- 原始SQL
UPDATE vehicle_info SET status = 2 WHERE id = '123'

-- 处理后SQL
UPDATE vehicle_info SET status = 2, update_time = NOW(), version = version + 1 
WHERE tenant_id = 'current_tenant' AND version = 1 AND (id = '123')
```

### 3. TenantInsertInterceptor - 租户插入拦截器

**功能：**
- 自动为插入语句添加租户ID
- 自动生成主键ID（雪花算法）
- 自动添加创建时间、更新时间、版本号、删除标识

**处理逻辑：**
- 拦截所有 INSERT 语句
- 检查是否已设置主键ID，如果没有则使用雪花算法生成
- 自动添加租户ID、创建时间、更新时间、版本号（初始值0）、删除标识（初始值0）

**示例：**
```sql
-- 原始SQL
INSERT INTO vehicle_info (vehicle_id, driver_name) VALUES ('V001', '张三')

-- 处理后SQL
INSERT INTO vehicle_info (vehicle_id, driver_name, id, tenant_id, create_time, update_time, version, dr) 
VALUES ('V001', '张三', '1234567890123456789', 'current_tenant', NOW(), NOW(), 0, 0)
```

### 4. SqlLogInterceptor - SQL日志拦截器

**功能：**
- 记录所有SQL执行信息
- 统计SQL执行时间
- 慢SQL警告
- 异常SQL记录

**记录内容：**
- SQL ID（Mapper方法）
- SQL类型（SELECT/INSERT/UPDATE/DELETE）
- 执行时间
- 原始SQL和完整SQL（替换参数后）
- 参数信息
- 执行结果（成功/失败、影响行数等）

**日志级别：**
- 正常SQL：INFO级别
- 慢SQL：WARN级别（默认阈值1000ms）
- 异常SQL：ERROR级别

## 配置类

### MybatisInterceptorConfig - 拦截器配置类

负责自动注册所有拦截器到 SqlSessionFactory 中。

**注册顺序：**
1. SqlLogInterceptor（最先执行，确保记录所有SQL）
2. TenantQueryInterceptor
3. TenantUpdateInterceptor
4. TenantInsertInterceptor

## 工具类

### TenantContext - 租户上下文工具类

**功能：**
- 管理当前线程的租户信息
- 提供租户ID和用户ID的设置/获取方法
- 支持租户上下文的传递和恢复

**主要方法：**
```java
// 设置租户ID
TenantContext.setTenantId("tenant_001");

// 获取租户ID
String tenantId = TenantContext.getTenantId();

// 设置用户ID
TenantContext.setUserId("user_001");

// 清除上下文
TenantContext.clear();

// 在指定租户上下文中执行操作
TenantContext.runWithTenant("tenant_001", () -> {
    // 业务逻辑
});

// 创建上下文快照（用于异步场景）
TenantContext.TenantContextSnapshot snapshot = TenantContext.snapshot();
snapshot.runWith(() -> {
    // 在新线程中执行
});
```

## 使用方法

### 1. 自动配置

拦截器会通过 `@Component` 注解自动注册到Spring容器中，并通过 `MybatisInterceptorConfig` 自动配置到MyBatis中。

### 2. 租户上下文设置

在请求开始时设置租户上下文：

```java
@RestController
public class VehicleController {
    
    @GetMapping("/vehicles")
    public List<Vehicle> getVehicles(HttpServletRequest request) {
        // 从请求头或JWT token中获取租户ID
        String tenantId = request.getHeader("X-Tenant-Id");
        TenantContext.setTenantId(tenantId);
        
        try {
            // 业务逻辑，SQL会自动添加租户条件
            return vehicleService.getAllVehicles();
        } finally {
            // 清除上下文
            TenantContext.clear();
        }
    }
}
```

### 3. 配置慢SQL阈值

在 `application.yml` 中配置：

```yaml
mybatis:
  configuration-properties:
    slowSqlThreshold: 2000  # 慢SQL阈值，单位毫秒
```

### 4. 数据库表结构要求

为了拦截器正常工作，数据库表需要包含以下字段：

```sql
CREATE TABLE example_table (
    id BIGINT PRIMARY KEY COMMENT '主键ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version INT DEFAULT 0 COMMENT '版本号',
    dr TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除，1-已删除',
    -- 其他业务字段
    
    INDEX idx_tenant_dr (tenant_id, dr)
);
```

## 注意事项

1. **线程安全**：租户上下文基于ThreadLocal实现，确保线程安全
2. **内存泄漏**：在请求结束时务必调用 `TenantContext.clear()` 清除上下文
3. **异步场景**：使用 `TenantContext.snapshot()` 在异步线程中传递上下文
4. **性能影响**：拦截器会对所有SQL进行处理，在高并发场景下需要注意性能
5. **SQL兼容性**：拦截器使用正则表达式解析SQL，复杂SQL可能需要特殊处理
6. **雪花算法**：主键生成使用雪花算法，需要确保workerId和datacenterId的唯一性

## 扩展说明

### 自定义租户获取逻辑

可以通过继承 `TenantContext` 或实现自定义的租户获取策略：

```java
@Component
public class CustomTenantResolver {
    
    public String getCurrentTenantId() {
        // 从JWT token、数据库或其他地方获取租户ID
        return "custom_tenant_logic";
    }
}
```

### 禁用特定表的拦截

可以在拦截器中添加表名白名单逻辑：

```java
private static final Set<String> EXCLUDED_TABLES = Set.of(
    "sys_config", "sys_log", "sequence_table"
);
```

### 自定义字段名

可以通过配置文件自定义字段名：

```yaml
tenant:
  field-names:
    tenant-id: org_id
    delete-flag: deleted
    version: revision
```