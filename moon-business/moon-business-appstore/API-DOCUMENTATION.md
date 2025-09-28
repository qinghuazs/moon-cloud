# Moon Business AppStore API 文档

## 概述

Moon Business AppStore 是一个 APP Store 限免应用推荐后端服务，提供限免应用展示、搜索、分类筛选等功能。

### 基本信息

- **基础路径**: `/api/appstore`
- **默认端口**: `8082`
- **Swagger UI**: `http://localhost:8082/api/appstore/swagger-ui.html`

## API 接口列表

### 1. 限免应用管理

#### 1.1 获取今日限免应用列表

**接口地址**: `GET /api/appstore/free-apps/today`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 示例 |
|-----|------|-----|------|------|
| page | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页数量，默认20 | 20 |
| categoryId | String | 否 | 分类ID | 6014 |
| sortBy | String | 否 | 排序方式: discovery/savings/rating | discovery |
| filter | String | 否 | 筛选条件: featured/hot/ending | featured |
| minRating | Double | 否 | 最低评分 | 4.0 |
| minOriginalPrice | Double | 否 | 最低原价 | 0 |
| maxOriginalPrice | Double | 否 | 最高原价 | 999 |

**响应示例**:

```json
{
  "records": [
    {
      "appId": "app_123",
      "appstoreId": "123456789",
      "name": "示例应用",
      "subtitle": "优质应用副标题",
      "developerName": "开发商名称",
      "iconUrl": "https://example.com/icon.png",
      "categoryName": "游戏",
      "fileSizeFormatted": "156.2 MB",
      "version": "2.1.0",
      "rating": 4.5,
      "ratingCount": 1234,
      "originalPrice": 68.00,
      "currentPrice": 0.00,
      "savingsAmount": 68.00,
      "freeStartTime": "2024-09-26 10:00:00",
      "freeEndTime": "2024-09-27 10:00:00",
      "remainingHours": 12,
      "statusTags": ["新发现", "热门"],
      "isFeatured": true,
      "isHot": true,
      "isEndingSoon": false,
      "isNewFound": true,
      "supportedDevices": ["iPhone", "iPad"],
      "hasInAppPurchase": false,
      "hasAds": false
    }
  ],
  "total": 100,
  "size": 20,
  "current": 1
}
```

#### 1.2 获取应用详情

**接口地址**: `GET /api/appstore/free-apps/{appId}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | 应用ID |

**响应示例**:

```json
{
  "appId": "app_123",
  "appstoreId": "123456789",
  "bundleId": "com.example.app",
  "name": "示例应用",
  "subtitle": "优质应用副标题",
  "description": "这是一个优秀的应用，提供了丰富的功能...",
  "developerName": "开发商名称",
  "developerId": "dev_123",
  "developerUrl": "https://developer.example.com",
  "iconUrl": "https://example.com/icon.png",
  "screenshots": [
    "https://example.com/screenshot1.png",
    "https://example.com/screenshot2.png"
  ],
  "primaryCategory": {
    "categoryId": "6014",
    "name": "游戏"
  },
  "version": "2.1.0",
  "releaseDate": "2023-01-15 10:00:00",
  "updatedDate": "2024-09-20 15:30:00",
  "releaseNotes": "- 修复了已知问题\n- 优化了性能",
  "fileSize": 163840000,
  "fileSizeFormatted": "156.2 MB",
  "minimumOsVersion": "13.0",
  "rating": 4.5,
  "ratingCount": 1234,
  "currentVersionRating": 4.6,
  "currentVersionRatingCount": 456,
  "originalPrice": 68.00,
  "currentPrice": 0.00,
  "currency": "CNY",
  "isFreeNow": true,
  "freePromotion": {
    "startTime": "2024-09-26 10:00:00",
    "endTime": "2024-09-27 10:00:00",
    "savingsAmount": 68.00,
    "remainingHours": 12,
    "isEndingSoon": false
  },
  "contentRating": "4+",
  "languages": ["中文", "英文"],
  "supportedDevices": ["iPhone", "iPad"],
  "features": ["Game Center", "iCloud"],
  "hasInAppPurchase": false,
  "hasAds": false,
  "priceHistory": [
    {
      "price": 0.00,
      "recordTime": "2024-09-26 10:00:00",
      "changeType": "FREE"
    },
    {
      "price": 68.00,
      "recordTime": "2024-09-25 10:00:00",
      "changeType": "NORMAL"
    }
  ]
}
```

#### 1.3 记录应用点击

**接口地址**: `POST /api/appstore/free-apps/{appId}/click`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | 应用ID |

**响应**: 无返回内容，状态码 200 表示成功

#### 1.4 记录应用分享

**接口地址**: `POST /api/appstore/free-apps/{appId}/share`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | 应用ID |

**响应**: 无返回内容，状态码 200 表示成功

### 2. 应用详情管理

#### 2.1 获取应用详细信息
**接口地址**: `GET /api/appstore/app/{appId}`

**功能说明**: 获取应用的完整详细信息

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID或内部ID |

**响应示例**: 见上文 "1.2 获取应用详情"

#### 2.2 获取价格历史图表数据
**接口地址**: `GET /api/appstore/app/{appId}/price-chart`

**功能说明**: 获取应用的价格变化历史数据，用于绘制价格走势图

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|--------|------|
| days | Integer | 否 | 90 | 查询天数(最大365) |

**响应示例**:
```json
{
  "success": true,
  "data": {
    "appId": "414478124",
    "appName": "微信",
    "days": 90,
    "startTime": "2024-06-27T00:00:00",
    "endTime": "2024-09-27T00:00:00",
    "currentPrice": 0.00,
    "originalPrice": 0.00,
    "lowestPrice": 0.00,
    "highestPrice": 18.00,
    "averagePrice": 6.50,
    "changeCount": 5,
    "freeCount": 2,
    "pricePoints": [
      {
        "time": "2024-09-01T10:00:00",
        "price": 18.00,
        "isFree": false,
        "changeType": "INITIAL"
      }
    ],
    "priceEvents": [
      {
        "eventTime": "2024-09-15T10:00:00",
        "eventType": "FREE",
        "oldPrice": 18.00,
        "newPrice": 0.00,
        "changeAmount": -18.00,
        "changePercent": -100.0
      }
    ]
  }
}
```

#### 2.3 获取相似应用
**接口地址**: `GET /api/appstore/app/{appId}/similar`

**功能说明**: 获取与当前应用相似的其他应用推荐

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|--------|------|
| limit | Integer | 否 | 10 | 返回数量(最大50) |

**响应示例**:
```json
{
  "success": true,
  "appId": "414478124",
  "count": 10,
  "data": [
    {
      "id": "2",
      "appId": "351091731",
      "name": "QQ",
      "iconUrl": "https://...",
      "bundleId": "com.tencent.mqq",
      "developerName": "Tencent",
      "categoryName": "社交",
      "currentPrice": 0.00,
      "originalPrice": 0.00,
      "isFree": true,
      "rating": 4.5,
      "ratingCount": 3000000,
      "fileSize": 419430400,
      "version": "9.0.0",
      "recommendReason": "同分类高评分应用",
      "hasPromotion": false,
      "shortDescription": "QQ是腾讯公司推出的..."
    }
  ]
}
```

#### 2.4 获取同开发商应用
**接口地址**: `GET /api/appstore/app/{appId}/developer-apps`

**功能说明**: 获取同一开发商的其他应用

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|--------|------|
| limit | Integer | 否 | 10 | 返回数量(最大50) |

#### 2.5 获取同分类热门应用
**接口地址**: `GET /api/appstore/app/{appId}/category-top`

**功能说明**: 获取同一分类下的热门应用

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|--------|------|
| limit | Integer | 否 | 10 | 返回数量(最大50) |

#### 2.6 获取所有相关应用
**接口地址**: `GET /api/appstore/app/{appId}/related`

**功能说明**: 一次性获取相似应用、同开发商应用和同分类热门应用

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|-----|--------|------|
| limit | Integer | 否 | 5 | 每类返回数量(最大20) |

**响应示例**:
```json
{
  "success": true,
  "appId": "414478124",
  "data": {
    "similar": [...],
    "developer": [...],
    "categoryTop": [...]
  }
}
```

#### 2.7 记录下载
**接口地址**: `POST /api/appstore/app/{appId}/download`

**功能说明**: 记录用户点击下载按钮的行为

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID |

**响应示例**:
```json
{
  "success": true,
  "appId": "414478124",
  "appStoreUrl": "https://apps.apple.com/cn/app/id414478124",
  "message": "下载记录成功"
}
```

#### 2.8 获取App Store链接
**接口地址**: `GET /api/appstore/app/{appId}/store-url`

**功能说明**: 获取应用的App Store下载链接

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| appId | String | 是 | App Store ID |

**响应示例**:
```json
{
  "success": true,
  "url": "https://apps.apple.com/cn/app/id414478124",
  "appId": "414478124"
}
```

### 3. 搜索管理

注意：搜索功能分为两组接口：
- 基础搜索接口：`/api/appstore/search/*` - 提供基础搜索功能
- 高级搜索接口：`/api/appstore/app/search/*` - 提供高级搜索和索引管理功能

#### 3.1 基础搜索应用

**接口地址**: `GET /api/appstore/search`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 示例 |
|-----|------|-----|------|------|
| keyword | String | 是 | 搜索关键词 | 游戏 |
| searchType | String | 否 | 搜索类型: all/name/developer | all |
| page | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页数量，默认20 | 20 |
| onlyFree | Boolean | 否 | 是否只搜索限免应用 | true |
| categoryId | String | 否 | 分类ID筛选 | 6014 |
| minRating | Double | 否 | 最低评分筛选 | 4.0 |

**请求头**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| User-Id | String | 否 | 用户ID |
| Device-Id | String | 否 | 设备ID |

**响应**: 同限免应用列表格式

#### 3.2 获取搜索建议

**接口地址**: `GET /api/appstore/search/suggestions`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| keyword | String | 是 | 关键词 |

**响应示例**:

```json
[
  "游戏大作",
  "游戏中心",
  "游戏手柄"
]
```

#### 3.3 获取热门搜索词

**接口地址**: `GET /api/appstore/search/hot`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|-----|------|-----|------|--------|
| limit | Integer | 否 | 返回数量 | 10 |

**响应示例**:

```json
[
  "王者荣耀",
  "原神",
  "和平精英",
  "微信",
  "抖音"
]
```

#### 3.4 获取用户搜索历史

**接口地址**: `GET /api/appstore/search/history`

**请求头**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| User-Id | String | 否 | 用户ID |
| Device-Id | String | 否 | 设备ID |

**请求参数**:

| 参数 | 类型 | 必填 | 说明 | 默认值 |
|-----|------|-----|------|--------|
| limit | Integer | 否 | 返回数量 | 10 |

**响应示例**:

```json
[
  "游戏",
  "效率工具",
  "照片编辑"
]
```

#### 3.5 清除用户搜索历史

**接口地址**: `DELETE /api/appstore/search/history`

**请求头**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| User-Id | String | 否 | 用户ID |
| Device-Id | String | 否 | 设备ID |

**响应**: 无返回内容，状态码 200 表示成功

### 3. 分类管理

#### 3.1 获取所有分类（树形结构）

**接口地址**: `GET /api/appstore/categories`

**响应示例**:

```json
[
  {
    "categoryId": "6014",
    "nameCn": "游戏",
    "nameEn": "Games",
    "categoryType": "GAME",
    "iconUrl": null,
    "appCount": 1500,
    "freeAppCount": 23,
    "avgRating": 4.3,
    "children": [
      {
        "categoryId": "7001",
        "nameCn": "动作游戏",
        "nameEn": "Action",
        "categoryType": "GAME",
        "appCount": 200,
        "freeAppCount": 5,
        "avgRating": 4.2
      }
    ]
  },
  {
    "categoryId": "6007",
    "nameCn": "效率",
    "nameEn": "Productivity",
    "categoryType": "APP",
    "appCount": 800,
    "freeAppCount": 12,
    "avgRating": 4.5
  }
]
```

#### 3.2 获取应用分类列表

**接口地址**: `GET /api/appstore/categories/apps`

**响应**: 仅返回类型为 APP 的分类列表

#### 3.3 获取游戏分类列表

**接口地址**: `GET /api/appstore/categories/games`

**响应**: 返回游戏主分类及其子分类

#### 3.4 根据ID获取分类详情

**接口地址**: `GET /api/appstore/categories/{categoryId}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| categoryId | String | 是 | 分类ID |

#### 3.5 新增分类

**接口地址**: `POST /api/appstore/categories`

**Content-Type**: `application/json`

**请求体**:

```json
{
  "categoryId": "6004",              // 必填：App Store分类ID
  "parentId": null,                  // 可选：父分类ID
  "nameCn": "体育",                  // 必填：中文名称
  "nameEn": "Sports",                // 必填：英文名称
  "categoryType": "APP",             // 必填：分类类型
  "iconUrl": null,                   // 可选：分类图标URL
  "categoriesUrl": "https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid",  // 可选：分类页面URL
  "sortOrder": 1,                    // 可选：排序权重
  "isActive": true,                  // 可选：是否启用，默认true
  "description": "体育相关应用"       // 可选：分类描述
}
```

**响应示例**:

```json
"新增分类成功"
```

#### 3.6 更新分类信息

**接口地址**: `PUT /api/appstore/categories/{id}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | String | 是 | 分类主键ID |

**请求体**:

```json
{
  "categoryId": "6004",              // 可选：App Store分类ID
  "parentId": null,                  // 可选：父分类ID
  "nameCn": "体育运动",              // 必填：中文名称
  "nameEn": "Sports & Fitness",      // 必填：英文名称
  "categoryType": "APP",             // 可选：分类类型
  "iconUrl": null,                   // 可选：分类图标URL
  "categoriesUrl": "https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid",  // 可选：分类页面URL
  "sortOrder": 1,                    // 可选：排序权重
  "isActive": true,                  // 可选：是否启用
  "description": "体育和健身相关应用" // 可选：分类描述
}
```

**响应示例**:

```json
"更新分类成功"
```

#### 3.7 删除分类

**接口地址**: `DELETE /api/appstore/categories/{id}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | String | 是 | 分类主键ID |

**响应示例**:

```json
"删除分类成功"
```

#### 3.8 根据主键ID获取分类详情

**接口地址**: `GET /api/appstore/categories/detail/{id}`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| id | String | 是 | 分类主键ID |

**响应示例**:

```json
{
  "id": "1234567890abcdef",
  "categoryId": "6004",
  "parentId": null,
  "nameCn": "体育",
  "nameEn": "Sports",
  "categoryType": "APP",
  "iconUrl": null,
  "categoriesUrl": "https://apps.apple.com/cn/charts/iphone/%E4%BD%93%E8%82%B2-apps/6004?chart=top-paid",
  "sortOrder": 1,
  "isActive": true,
  "description": "体育相关应用分类",
  "appCount": 0,
  "freeAppCount": 0,
  "avgRating": null,
  "createdAt": "2024-09-26T10:30:00",
  "updatedAt": "2024-09-26T10:30:00"
}
```

#### 3.9 更新分类统计信息

**接口地址**: `POST /api/appstore/categories/{categoryId}/statistics`

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| categoryId | String | 是 | 分类ID |

**响应**: 无返回内容，状态码 200 表示成功

### 4. 爬虫管理接口

#### 4.1 爬取指定分类的应用

**接口地址**: `POST /api/appstore/crawler/crawl/category/{categoryId}`

**描述**: 根据分类ID爬取该分类下的所有应用链接并存入Redis队列

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| categoryId | String | 是 | 分类主键ID |

**响应示例**:

```json
{
  "success": true,
  "categoryId": "test-id-123",
  "crawledCount": 50,
  "message": "成功爬取 50 个应用链接"
}
```

#### 4.2 爬取所有分类的应用

**接口地址**: `POST /api/appstore/crawler/crawl/all`

**描述**: 爬取所有激活分类下的应用链接并存入Redis队列

**响应示例**:

```json
{
  "success": true,
  "totalCrawled": 1250,
  "message": "成功爬取所有分类，共 1250 个应用链接"
}
```

#### 4.3 获取队列大小

**接口地址**: `GET /api/appstore/crawler/queue/{queueName}/size`

**描述**: 获取Redis队列中的链接数量

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| queueName | String | 是 | 队列名称 |

**响应示例**:

```json
{
  "queueName": "appstore:queue:6004",
  "size": 50
}
```

#### 4.4 清空队列

**接口地址**: `DELETE /api/appstore/crawler/queue/{queueName}`

**描述**: 清空指定的Redis队列

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| queueName | String | 是 | 队列名称 |

**响应示例**:

```json
{
  "queueName": "appstore:queue:6004",
  "success": true,
  "message": "队列清空成功"
}
```

#### 4.5 测试链接提取

**接口地址**: `POST /api/appstore/crawler/test/extract`

**描述**: 测试从HTML内容中提取应用链接的功能

**请求体**: 原始HTML内容（text/plain）

**响应示例**:

```json
{
  "success": true,
  "extractedCount": 10,
  "links": [
    "https://apps.apple.com/cn/app/id123456",
    "https://apps.apple.com/cn/app/id789012"
  ]
}
```

#### 4.6 测试页面抓取

**接口地址**: `GET /api/appstore/crawler/test/fetch`

**描述**: 测试从指定URL抓取页面内容的功能

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| url | String | 是 | 页面URL |

**响应示例**:

```json
{
  "success": true,
  "url": "https://apps.apple.com/cn/genre/ios-体育/id6004",
  "contentLength": 125678,
  "contentPreview": "<!DOCTYPE html>..."
}
```

## 环境配置

### 数据库配置

创建 `.env` 文件并配置以下参数：

```env
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=moon_appstore
DB_USERNAME=root
DB_PASSWORD=root123456

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

### 初始化数据库

执行 `src/main/resources/db/init.sql` 脚本初始化数据库结构和基础数据。

### 初始化分类数据

执行 `src/main/resources/sql/categories_data_init.sql` 脚本初始化 App Store 分类数据，包含25个官方分类：

- 体育、效率、财务、健康健美、天气、教育等常用应用分类
- 每个分类包含中英文名称和对应的 App Store URL
- 支持数据覆盖更新，重复执行不会产生重复数据

### 启动服务

```bash
# 编译项目
mvn clean compile

# 运行服务
mvn spring-boot:run
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项

1. 所有时间字段均使用 `yyyy-MM-dd HH:mm:ss` 格式，时区为 GMT+8
2. 价格单位为人民币（CNY），精确到小数点后两位
3. 搜索功能支持中文、拼音和英文搜索
4. 建议使用 Device-Id 或 User-Id 标识用户，以支持个性化功能
5. 分页查询的页码从 1 开始，每页最大数量限制为 100

## 开发调试

### Swagger UI

访问 `http://localhost:8082/api/appstore/swagger-ui.html` 可查看和测试所有API接口。

### 日志查看

日志文件位置：`logs/moon-business-appstore.log`

### 数据库监控

可通过以下SQL查看核心数据统计：

```sql
-- 查看今日限免应用数量
SELECT COUNT(*) FROM free_promotions
WHERE status = 'ACTIVE' AND DATE(start_time) = CURDATE();

-- 查看热门搜索词
SELECT search_query, COUNT(*) as count
FROM search_history
WHERE searched_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY search_query
ORDER BY count DESC
LIMIT 10;
```