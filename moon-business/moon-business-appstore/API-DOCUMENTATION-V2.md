# Moon Business AppStore API 接口文档

## 目录

1. [基础信息](#基础信息)
2. [分类管理接口](#分类管理接口)
3. [限免应用接口](#限免应用接口)
4. [搜索接口](#搜索接口)
5. [应用详情接口](#应用详情接口)
6. [爬虫管理接口](#爬虫管理接口)
7. [队列管理接口](#队列管理接口)
8. [价格历史接口](#价格历史接口)

---

## 基础信息

### 服务地址
- **开发环境**: `http://localhost:8082`
- **生产环境**: 待定

### 基础路径
- **API前缀**: `/api/appstore`

### 认证方式
- 暂无认证要求（后续可添加JWT认证）

### 响应格式
- **Content-Type**: `application/json`
- **编码**: `UTF-8`

### 通用错误码
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 分类管理接口

### 1. 获取所有分类
**接口地址**: `GET /api/appstore/categories`

**功能说明**: 获取所有App Store分类列表

**请求参数**: 无

**响应示例**:
```json
[
  {
    "id": "1",
    "categoryId": "6014",
    "categoryName": "游戏",
    "categoryNameEn": "Games",
    "categoryUrl": "https://apps.apple.com/cn/genre/ios-游戏/id6014",
    "icon": "🎮",
    "parentId": null,
    "level": 1,
    "sortOrder": 1,
    "status": 1,
    "createdAt": "2024-09-26T10:00:00",
    "updatedAt": "2024-09-26T10:00:00"
  }
]
```

### 2. 根据ID获取分类
**接口地址**: `GET /api/appstore/categories/{id}`

**功能说明**: 根据主键ID获取分类详情

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 分类主键ID |

**响应示例**:
```json
{
  "id": "1",
  "categoryId": "6014",
  "categoryName": "游戏",
  "categoryNameEn": "Games",
  "categoryUrl": "https://apps.apple.com/cn/genre/ios-游戏/id6014",
  "icon": "🎮",
  "parentId": null,
  "level": 1,
  "sortOrder": 1,
  "status": 1,
  "createdAt": "2024-09-26T10:00:00",
  "updatedAt": "2024-09-26T10:00:00"
}
```

### 3. 根据分类ID获取分类
**接口地址**: `GET /api/appstore/categories/category/{categoryId}`

**功能说明**: 根据App Store分类ID获取分类详情

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | String | 是 | App Store分类ID |

### 4. 创建分类
**接口地址**: `POST /api/appstore/categories`

**功能说明**: 创建新的分类

**请求体**:
```json
{
  "categoryId": "6000",
  "categoryName": "教育",
  "categoryNameEn": "Education",
  "categoryUrl": "https://apps.apple.com/cn/genre/ios-教育/id6000",
  "icon": "📚",
  "parentId": null,
  "level": 1,
  "sortOrder": 2
}
```

### 5. 更新分类
**接口地址**: `PUT /api/appstore/categories/{id}`

**功能说明**: 更新分类信息

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 分类主键ID |

### 6. 删除分类
**接口地址**: `DELETE /api/appstore/categories/{id}`

**功能说明**: 删除指定分类

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 分类主键ID |

---

## 限免应用接口

### 1. 获取限免应用列表
**接口地址**: `GET /api/appstore/free-apps`

**功能说明**: 获取当前限免应用列表

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 20 | 每页大小 |
| categoryId | String | 否 | 无 | 分类ID |

**响应示例**:
```json
{
  "total": 100,
  "page": 1,
  "size": 20,
  "data": [
    {
      "id": "1",
      "appId": "123456",
      "appName": "示例应用",
      "originalPrice": 18.00,
      "currentPrice": 0.00,
      "discount": 100,
      "icon": "https://example.com/icon.png",
      "category": "游戏",
      "rating": 4.5,
      "freeStartTime": "2024-09-27T00:00:00",
      "freeEndTime": "2024-09-28T00:00:00"
    }
  ]
}
```

### 2. 获取今日限免
**接口地址**: `GET /api/appstore/free-apps/today`

**功能说明**: 获取今日新增的限免应用

**请求参数**: 无

**响应示例**: 同上

### 3. 获取限免排行榜
**接口地址**: `GET /api/appstore/free-apps/ranking`

**功能说明**: 获取限免应用热度排行榜

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 10 | 返回数量 |

---

## 搜索接口

### 1. 搜索应用
**接口地址**: `GET /api/appstore/search`

**功能说明**: 根据关键词搜索应用

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页大小 |
| categoryId | String | 否 | 分类筛选 |
| priceRange | String | 否 | 价格区间(如: 0-10) |

**响应示例**:
```json
{
  "total": 50,
  "page": 1,
  "size": 20,
  "data": [
    {
      "id": "1",
      "appId": "414478124",
      "name": "微信",
      "bundleId": "com.tencent.xin",
      "icon": "https://example.com/icon.png",
      "currentPrice": 0.00,
      "rating": 4.17,
      "category": "社交",
      "developer": "WeChat"
    }
  ]
}
```

### 2. 高级搜索
**接口地址**: `POST /api/appstore/search/advanced`

**功能说明**: 支持多条件组合的高级搜索

**请求体**:
```json
{
  "keyword": "游戏",
  "categories": ["6014", "6016"],
  "priceMin": 0,
  "priceMax": 50,
  "ratingMin": 4.0,
  "isFree": false,
  "hasInAppPurchase": false,
  "languages": ["ZH", "EN"],
  "sortBy": "rating",
  "sortOrder": "desc",
  "page": 1,
  "size": 20
}
```

---

## 爬虫管理接口

### 1. 爬取分类页面
**接口地址**: `POST /api/appstore/crawler/category/{categoryId}`

**功能说明**: 爬取指定分类的应用列表

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | String | 是 | 分类ID |

**响应示例**:
```json
{
  "success": true,
  "message": "爬取任务已启动",
  "data": {
    "categoryId": "6014",
    "urlCount": 150,
    "queueName": "appstore:queue:6014"
  }
}
```

### 2. 爬取所有分类
**接口地址**: `POST /api/appstore/crawler/all`

**功能说明**: 爬取所有分类的应用

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "批量爬取任务已启动",
  "data": {
    "categoryCount": 25,
    "totalUrls": 3750
  }
}
```

### 3. 获取爬虫状态
**接口地址**: `GET /api/appstore/crawler/status`

**功能说明**: 获取当前爬虫运行状态

**响应示例**:
```json
{
  "isRunning": true,
  "currentCategory": "6014",
  "processedCount": 100,
  "remainingCount": 50,
  "successRate": 95.5,
  "lastRunTime": "2024-09-27T10:00:00"
}
```

---

## 队列管理接口

### 1. 获取队列状态
**接口地址**: `GET /api/appstore/queue/status`

**功能说明**: 获取所有队列的状态信息

**响应示例**:
```json
{
  "totalQueueSize": 150,
  "failedQueueSize": 5,
  "timestamp": 1695800000000
}
```

### 2. 获取分类队列状态
**接口地址**: `GET /api/appstore/queue/status/{categoryId}`

**功能说明**: 获取指定分类队列的状态

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | String | 是 | 分类ID |

**响应示例**:
```json
{
  "categoryId": "6014",
  "queueSize": 25,
  "timestamp": 1695800000000
}
```

### 3. 手动触发消费
**接口地址**: `POST /api/appstore/queue/consume`

**功能说明**: 手动触发所有队列的消费任务

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "消费任务已触发",
  "totalQueueSize": 150
}
```

### 4. 触发分类队列消费
**接口地址**: `POST /api/appstore/queue/consume/{categoryId}`

**功能说明**: 手动触发指定分类队列的消费

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | String | 是 | 分类ID |

### 5. 重试失败任务
**接口地址**: `POST /api/appstore/queue/retry`

**功能说明**: 重试失败队列中的任务

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "重试任务已触发",
  "failedQueueSize": 5
}
```

### 6. 处理单个URL
**接口地址**: `POST /api/appstore/queue/process`

**功能说明**: 手动处理单个App URL

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| url | String | 是 | App Store URL |

**请求示例**:
```bash
curl -X POST "http://localhost:8082/api/appstore/queue/process?url=https://apps.apple.com/cn/app/微信/id414478124"
```

**响应示例**:
```json
{
  "success": true,
  "message": "处理成功",
  "url": "https://apps.apple.com/cn/app/微信/id414478124"
}
```

---

## 应用详情接口

### 1. 获取应用详情
**接口地址**: `GET /api/appstore/app/{appId}`

**功能说明**: 根据应用ID获取完整的应用详细信息

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**响应示例**:
```json
{
  "success": true,
  "data": {
    "appId": "1",
    "appstoreId": "284882215",
    "bundleId": "com.facebook.Facebook",
    "name": "Facebook",
    "description": "Connect with friends...",
    "developerName": "Meta Platforms, Inc.",
    "developerId": "389801252",
    "iconUrl": "https://...",
    "version": "434.0",
    "rating": 3.8,
    "ratingCount": 11234567,
    "currentPrice": 0.00,
    "originalPrice": 0.00,
    "isFreeNow": true,
    "freePromotion": {
      "startTime": "2024-09-27T08:00:00",
      "endTime": "2024-09-28T20:00:00",
      "savingsAmount": 68.00,
      "remainingHours": 36,
      "isEndingSoon": false
    },
    "primaryCategory": {
      "categoryId": "6005",
      "name": "Social Networking"
    },
    "categories": [
      {"categoryId": "6005", "name": "Social Networking"}
    ],
    "fileSize": 314572800,
    "fileSizeFormatted": "300.0 MB",
    "minimumOsVersion": "12.0",
    "contentRating": "12+",
    "languages": ["ZH", "EN"],
    "supportedDevices": ["iPhone", "iPad"],
    "screenshots": ["https://..."],
    "releaseDate": "2008-07-10T00:00:00",
    "updatedDate": "2024-09-24T00:00:00"
  }
}
```

### 2. 获取价格历史图表数据
**接口地址**: `GET /api/appstore/app/{appId}/price-chart`

**功能说明**: 获取应用的价格变化历史数据，用于绘制价格走势图

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| days | Integer | 否 | 90 | 历史天数(最大365) |

**响应示例**:
```json
{
  "success": true,
  "data": {
    "appId": "284882215",
    "appName": "Facebook",
    "days": 90,
    "startTime": "2024-06-29T00:00:00",
    "endTime": "2024-09-27T00:00:00",
    "currentPrice": 0.00,
    "originalPrice": 0.00,
    "lowestPrice": 0.00,
    "highestPrice": 68.00,
    "averagePrice": 34.00,
    "changeCount": 3,
    "freeCount": 2,
    "pricePoints": [
      {
        "time": "2024-09-27T08:00:00",
        "price": 0.00,
        "isFree": true,
        "changeType": "FREE"
      }
    ],
    "priceEvents": [
      {
        "eventTime": "2024-09-27T08:00:00",
        "eventType": "FREE",
        "oldPrice": 68.00,
        "newPrice": 0.00,
        "changeAmount": -68.00,
        "changePercent": -100.00
      }
    ]
  }
}
```

### 3. 获取相似应用
**接口地址**: `GET /api/appstore/app/{appId}/similar`

**功能说明**: 获取与当前应用相似的其他应用推荐

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 10 | 返回数量(最大50) |

**响应示例**:
```json
{
  "success": true,
  "appId": "284882215",
  "count": 2,
  "data": [
    {
      "id": "2",
      "appId": "447188370",
      "name": "Snapchat",
      "iconUrl": "https://...",
      "bundleId": "com.toyopagroup.picaboo",
      "developerName": "Snap, Inc.",
      "categoryName": "Social Networking",
      "currentPrice": 0.00,
      "originalPrice": 0.00,
      "isFree": true,
      "rating": 4.0,
      "ratingCount": 5678900,
      "fileSize": 268435456,
      "version": "12.96.0",
      "recommendReason": "同分类高评分应用",
      "hasPromotion": false,
      "shortDescription": "Share the moment..."
    }
  ]
}
```

### 4. 获取同开发商应用
**接口地址**: `GET /api/appstore/app/{appId}/developer-apps`

**功能说明**: 获取同一开发商的其他应用

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 10 | 返回数量(最大50) |

**响应示例**:
```json
{
  "success": true,
  "appId": "284882215",
  "count": 1,
  "data": [
    {
      "id": "3",
      "appId": "454638411",
      "name": "Messenger",
      "developerName": "Meta Platforms, Inc.",
      "rating": 3.5,
      "currentPrice": 0.00,
      "recommendReason": "同一开发商"
    }
  ]
}
```

### 5. 获取同分类热门应用
**接口地址**: `GET /api/appstore/app/{appId}/category-top`

**功能说明**: 获取同一分类下的热门应用

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 10 | 返回数量(最大50) |

**响应示例**:
```json
{
  "success": true,
  "appId": "284882215",
  "count": 3,
  "data": [
    {
      "id": "4",
      "appId": "333903271",
      "name": "Twitter",
      "developerName": "X Corp.",
      "rating": 4.2,
      "ratingCount": 1000000,
      "currentPrice": 0.00,
      "recommendReason": "分类热门应用"
    }
  ]
}
```

### 6. 获取所有相关应用
**接口地址**: `GET /api/appstore/app/{appId}/related`

**功能说明**: 一次性获取相似应用、同开发商应用和同分类热门应用

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 5 | 每类返回数量(最大20) |

**响应示例**:
```json
{
  "success": true,
  "appId": "284882215",
  "data": {
    "similar": [...],
    "developer": [...],
    "categoryTop": [...]
  }
}
```

### 7. 记录下载
**接口地址**: `POST /api/appstore/app/{appId}/download`

**功能说明**: 记录用户点击下载按钮的行为

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**响应示例**:
```json
{
  "success": true,
  "appId": "284882215",
  "appStoreUrl": "https://apps.apple.com/cn/app/id284882215",
  "message": "下载记录成功"
}
```

### 8. 获取App Store链接
**接口地址**: `GET /api/appstore/app/{appId}/store-url`

**功能说明**: 获取应用的App Store下载链接

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID或内部ID |

**响应示例**:
```json
{
  "success": true,
  "url": "https://apps.apple.com/cn/app/id284882215",
  "appId": "284882215"
}
```

---

## 价格历史接口

### 1. 获取App价格历史
**接口地址**: `GET /api/appstore/price-history/app/{appId}`

**功能说明**: 获取指定App的价格变化历史

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID |

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startTime | DateTime | 否 | 开始时间 |
| endTime | DateTime | 否 | 结束时间 |

**响应示例**:
```json
[
  {
    "id": "1",
    "appId": "414478124",
    "appName": "微信",
    "oldPrice": 0.00,
    "newPrice": 0.00,
    "priceChange": 0.00,
    "changePercent": 0.00,
    "changeType": "INITIAL",
    "isFree": true,
    "version": "8.0.64",
    "changeTime": "2024-09-27T10:00:00",
    "developerName": "WeChat"
  }
]
```

### 2. 获取最新价格
**接口地址**: `GET /api/appstore/price-history/app/{appId}/latest`

**功能说明**: 获取App的最新价格记录

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID |

**响应示例**:
```json
{
  "id": "1",
  "appId": "414478124",
  "appName": "微信",
  "oldPrice": 0.00,
  "newPrice": 0.00,
  "priceChange": 0.00,
  "changePercent": 0.00,
  "changeType": "INITIAL",
  "isFree": true,
  "version": "8.0.64",
  "changeTime": "2024-09-27T10:00:00",
  "developerName": "WeChat"
}
```

### 3. 获取价格统计
**接口地址**: `GET /api/appstore/price-history/app/{appId}/statistics`

**功能说明**: 获取App的价格统计信息

**响应示例**:
```json
{
  "appId": "414478124",
  "currentPrice": 0.00,
  "lowestPrice": 0.00,
  "highestPrice": 18.00,
  "changeCount": 5,
  "priceRange": 18.00,
  "volatility": 100.00,
  "lastChangeTime": "2024-09-27T10:00:00",
  "changeType": "FREE"
}
```

### 4. 获取历史最低价
**接口地址**: `GET /api/appstore/price-history/app/{appId}/lowest`

**功能说明**: 获取App的历史最低价格

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID |

**响应示例**:
```json
{
  "appId": "414478124",
  "lowestPrice": 0.00
}
```

### 5. 获取历史最高价
**接口地址**: `GET /api/appstore/price-history/app/{appId}/highest`

**功能说明**: 获取App的历史最高价格

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| appId | String | 是 | App Store ID |

**响应示例**:
```json
{
  "appId": "414478124",
  "highestPrice": 18.00
}
```

### 6. 获取限免应用
**接口地址**: `GET /api/appstore/price-history/free`

**功能说明**: 获取最近的限免应用列表

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| limit | Integer | 否 | 20 | 返回数量 |

**响应示例**:
```json
[
  {
    "id": "1",
    "appId": "123456",
    "appName": "示例应用",
    "oldPrice": 18.00,
    "newPrice": 0.00,
    "changeType": "FREE",
    "changeTime": "2024-09-27T10:00:00",
    "categoryName": "游戏",
    "developerName": "开发商"
  }
]
```

### 7. 获取降价应用
**接口地址**: `GET /api/appstore/price-history/price-drops`

**功能说明**: 获取最近降价的应用列表

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| days | Integer | 否 | 7 | 天数 |
| limit | Integer | 否 | 20 | 返回数量 |

### 8. 获取分类价格变化
**接口地址**: `GET /api/appstore/price-history/category/{categoryId}`

**功能说明**: 获取指定分类的价格变化

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | String | 是 | 分类ID |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| days | Integer | 否 | 7 | 天数 |
| limit | Integer | 否 | 20 | 返回数量 |

### 9. 获取开发者价格变化
**接口地址**: `GET /api/appstore/price-history/developer/{developerName}`

**功能说明**: 获取指定开发者的应用价格变化

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| developerName | String | 是 | 开发者名称 |

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| days | Integer | 否 | 7 | 天数 |
| limit | Integer | 否 | 20 | 返回数量 |

**响应示例**:
```json
[
  {
    "id": "1",
    "appId": "123456",
    "appName": "开发者应用1",
    "oldPrice": 18.00,
    "newPrice": 12.00,
    "changeType": "DECREASE",
    "changeTime": "2024-09-27T10:00:00",
    "categoryName": "游戏",
    "developerName": "开发商名称"
  }
]
```

### 10. 获取待通知列表
**接口地址**: `GET /api/appstore/price-history/notifications/pending`

**功能说明**: 获取待通知的价格变化

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| changeTypes | String | 否 | FREE,DECREASE | 变化类型（逗号分隔） |
| limit | Integer | 否 | 50 | 返回数量 |

**响应示例**:
```json
[
  {
    "id": "1",
    "appId": "123456",
    "appName": "待通知应用",
    "oldPrice": 30.00,
    "newPrice": 0.00,
    "changeType": "FREE",
    "changeTime": "2024-09-27T09:00:00",
    "isNotified": false,
    "notifiedAt": null
  }
]
```

### 11. 标记为已通知
**接口地址**: `POST /api/appstore/price-history/notification/{id}/mark`

**功能说明**: 将价格变化记录标记为已通知

**路径参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 价格历史记录ID |

**响应示例**:
```json
{
  "success": true,
  "id": "1",
  "message": "标记成功"
}
```

### 12. 获取价格变化汇总
**接口地址**: `GET /api/appstore/price-history/summary`

**功能说明**: 获取价格变化的汇总统计信息

**请求参数**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| days | Integer | 否 | 7 | 统计天数 |

**响应示例**:
```json
{
  "freeAppCount": 15,
  "priceDropCount": 35,
  "changeTypeCount": {
    "FREE": 15,
    "DECREASE": 20,
    "INCREASE": 5
  },
  "days": 7,
  "timestamp": "2024-09-27T10:00:00"
}
```

---

## 数据模型说明

### App实体
```json
{
  "id": "主键ID",
  "appId": "App Store ID",
  "bundleId": "Bundle ID",
  "name": "应用名称",
  "description": "应用描述",
  "iconUrl": "图标URL",
  "currentPrice": "当前价格",
  "originalPrice": "原价",
  "currency": "货币类型",
  "isFree": "是否免费",
  "rating": "评分",
  "ratingCount": "评分人数",
  "version": "版本号",
  "fileSize": "文件大小",
  "releaseDate": "发布日期",
  "updatedDate": "更新日期",
  "developerName": "开发者名称",
  "developerId": "开发者ID",
  "primaryCategoryId": "主分类ID",
  "primaryCategoryName": "主分类名称",
  "categories": "分类列表",
  "screenshots": "截图列表",
  "languages": "支持语言",
  "supportedDevices": "支持设备",
  "contentRating": "内容分级",
  "status": "状态"
}
```

### Category实体
```json
{
  "id": "主键ID",
  "categoryId": "分类ID",
  "categoryName": "分类名称",
  "categoryNameEn": "英文名称",
  "categoryUrl": "分类URL",
  "icon": "图标",
  "parentId": "父分类ID",
  "level": "层级",
  "sortOrder": "排序",
  "status": "状态"
}
```

### AppPriceHistory实体
```json
{
  "id": "主键ID",
  "appId": "App Store ID",
  "appName": "应用名称",
  "oldPrice": "原价格",
  "newPrice": "新价格",
  "priceChange": "价格变化量",
  "changePercent": "变化百分比",
  "changeType": "变化类型(INCREASE/DECREASE/FREE/RESTORE/INITIAL)",
  "isFree": "是否限免",
  "version": "版本号",
  "categoryId": "分类ID",
  "categoryName": "分类名称",
  "developerName": "开发者",
  "changeTime": "变化时间",
  "source": "数据来源"
}
```

---

## 错误响应格式

所有错误响应遵循统一格式：

```json
{
  "success": false,
  "code": 400,
  "message": "错误信息描述",
  "timestamp": 1695800000000,
  "path": "/api/appstore/xxx"
}
```

---

## 使用示例

### cURL示例

#### 1. 获取所有分类
```bash
curl -X GET "http://localhost:8082/api/appstore/categories"
```

#### 2. 搜索应用
```bash
curl -X GET "http://localhost:8082/api/appstore/search?keyword=微信&page=1&size=10"
```

#### 3. 爬取分类数据
```bash
curl -X POST "http://localhost:8082/api/appstore/crawler/category/6014"
```

#### 4. 获取限免应用
```bash
curl -X GET "http://localhost:8082/api/appstore/price-history/free?limit=10"
```

#### 5. 处理单个URL
```bash
curl -X POST -G \
  http://localhost:8082/api/appstore/queue/process \
  --data-urlencode "url=https://apps.apple.com/cn/app/微信/id414478124"
```

#### 6. 获取App价格历史
```bash
curl -X GET "http://localhost:8082/api/appstore/price-history/app/414478124"
```

#### 7. 获取价格统计
```bash
curl -X GET "http://localhost:8082/api/appstore/price-history/app/414478124/statistics"
```

#### 8. 获取降价应用
```bash
curl -X GET "http://localhost:8082/api/appstore/price-history/price-drops?days=7&limit=20"
```

#### 9. 获取待通知列表
```bash
curl -X GET "http://localhost:8082/api/appstore/price-history/notifications/pending?changeTypes=FREE,DECREASE&limit=50"
```

#### 10. 标记为已通知
```bash
curl -X POST "http://localhost:8082/api/appstore/price-history/notification/1/mark"
```

### JavaScript/Axios示例

```javascript
// 获取分类列表
const getCategories = async () => {
  const response = await axios.get('http://localhost:8082/api/appstore/categories');
  return response.data;
};

// 搜索应用
const searchApps = async (keyword) => {
  const response = await axios.get('http://localhost:8082/api/appstore/search', {
    params: {
      keyword: keyword,
      page: 1,
      size: 20
    }
  });
  return response.data;
};

// 获取价格历史
const getPriceHistory = async (appId) => {
  const response = await axios.get(`http://localhost:8082/api/appstore/price-history/app/${appId}`);
  return response.data;
};
```

### Python示例

```python
import requests

# 基础URL
base_url = "http://localhost:8082/api/appstore"

# 获取所有分类
def get_categories():
    response = requests.get(f"{base_url}/categories")
    return response.json()

# 搜索应用
def search_apps(keyword):
    params = {
        "keyword": keyword,
        "page": 1,
        "size": 20
    }
    response = requests.get(f"{base_url}/search", params=params)
    return response.json()

# 获取限免应用
def get_free_apps(limit=20):
    response = requests.get(f"{base_url}/price-history/free", params={"limit": limit})
    return response.json()

# 处理单个URL
def process_url(url):
    response = requests.post(f"{base_url}/queue/process", params={"url": url})
    return response.json()
```

---

## 注意事项

1. **爬虫频率限制**: 请控制爬虫频率，避免对App Store服务器造成压力
2. **数据更新**: 价格数据每天凌晨3点自动更新
3. **队列处理**: 大量URL处理可能需要较长时间，建议使用异步方式
4. **缓存策略**: 部分查询接口使用了缓存，数据可能有延迟
5. **Express服务依赖**: 爬虫功能依赖本地Express服务(端口3090)

---

## 版本历史

### v1.0.0 (2024-09-27)
- 初始版本发布
- 实现基础的分类管理、爬虫、队列处理功能
- 添加价格历史记录功能
- 支持限免应用追踪

---

## 联系方式

- **项目地址**: Moon Cloud AppStore
- **开发团队**: Moon Cloud Team
- **更新时间**: 2024-09-27

---

## 附录

### Swagger文档
访问 `http://localhost:8082/api/appstore/swagger-ui.html` 查看在线API文档

### Postman集合
可导入 `appstore-api.postman_collection.json` 文件到Postman中进行测试