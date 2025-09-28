-- 测试数据：从 apps 表中每个分类选取 5 条数据插入到 free_promotions 表
-- 用于测试限免功能

-- 清理已有的测试数据（可选）
-- DELETE FROM free_promotions WHERE promotion_type = 'TEST';

-- 插入限免测试数据
-- 使用窗口函数为每个分类选取前5个应用
INSERT INTO free_promotions (
    id,
    app_id,
    appstore_app_id,
    app_name,
    bundle_id,
    icon_url,
    developer_name,
    category_id,
    category_name,
    original_price,
    current_price,
    discount_percent,
    savings_amount,
    promotion_type,
    start_time,
    end_time,
    status,
    is_featured,
    is_hot,
    is_ending_soon,
    is_new_found,
    view_count,
    download_count,
    created_at,
    updated_at
)
SELECT
    UUID() as id,
    a.id as app_id,
    a.app_id as appstore_app_id,
    a.name as app_name,
    a.bundle_id,
    a.icon_url,
    a.developer_name,
    a.primary_category_id as category_id,
    a.primary_category_name as category_name,
    CASE
        WHEN a.original_price = 0 OR a.original_price IS NULL
        THEN ROUND(RAND() * 60 + 6, 2)  -- 随机生成 6-66 元的原价
        ELSE a.original_price
    END as original_price,
    0.00 as current_price,  -- 限免价格为0
    100 as discount_percent,  -- 100% 折扣
    CASE
        WHEN a.original_price = 0 OR a.original_price IS NULL
        THEN ROUND(RAND() * 60 + 6, 2)
        ELSE a.original_price
    END as savings_amount,
    'FREE' as promotion_type,  -- 限免类型
    NOW() as start_time,  -- 从当前时间开始
    DATE_ADD(NOW(), INTERVAL 1 + FLOOR(RAND() * 7) DAY) as end_time,  -- 随机1-7天后结束
    'ACTIVE' as status,
    IF(RAND() > 0.7, 1, 0) as is_featured,  -- 30%概率为精选
    IF(RAND() > 0.8, 1, 0) as is_hot,  -- 20%概率为热门
    IF(RAND() > 0.9, 1, 0) as is_ending_soon,  -- 10%概率即将结束
    IF(RAND() > 0.6, 1, 0) as is_new_found,  -- 40%概率为新发现
    FLOOR(RAND() * 1000) as view_count,  -- 随机查看次数
    FLOOR(RAND() * 500) as download_count,  -- 随机下载次数
    NOW() as created_at,
    NOW() as updated_at
FROM (
    SELECT
        a.*,
        ROW_NUMBER() OVER (PARTITION BY a.primary_category_id ORDER BY a.rating DESC, RAND()) as row_num
    FROM apps a
    WHERE a.is_free = 0  -- 选择付费应用来模拟限免
        AND a.original_price > 0
        AND a.primary_category_id IS NOT NULL
        AND a.name IS NOT NULL
) a
WHERE a.row_num <= 5;  -- 每个分类选5个

-- 如果付费应用不够，可以使用下面的SQL选择所有应用（包括免费应用）
INSERT INTO free_promotions (
    id,
    app_id,
    appstore_app_id,
    app_name,
    bundle_id,
    icon_url,
    developer_name,
    category_id,
    category_name,
    original_price,
    current_price,
    discount_percent,
    savings_amount,
    promotion_type,
    start_time,
    end_time,
    status,
    is_featured,
    is_hot,
    is_ending_soon,
    is_new_found,
    view_count,
    download_count,
    created_at,
    updated_at
)
SELECT
    UUID() as id,
    a.id as app_id,
    a.app_id as appstore_app_id,
    a.name as app_name,
    a.bundle_id,
    a.icon_url,
    a.developer_name,
    a.primary_category_id as category_id,
    a.primary_category_name as category_name,
    ROUND(RAND() * 100 + 6, 2) as original_price,  -- 随机生成 6-106 元的原价
    0.00 as current_price,  -- 限免价格为0
    100 as discount_percent,  -- 100% 折扣
    ROUND(RAND() * 100 + 6, 2) as savings_amount,
    'FREE' as promotion_type,  -- 限免类型
    NOW() as start_time,  -- 从当前时间开始
    DATE_ADD(NOW(), INTERVAL 1 + FLOOR(RAND() * 7) DAY) as end_time,  -- 随机1-7天后结束
    'ACTIVE' as status,
    IF(RAND() > 0.7, 1, 0) as is_featured,  -- 30%概率为精选
    IF(RAND() > 0.8, 1, 0) as is_hot,  -- 20%概率为热门
    IF(RAND() > 0.9, 1, 0) as is_ending_soon,  -- 10%概率即将结束
    IF(RAND() > 0.6, 1, 0) as is_new_found,  -- 40%概率为新发现
    FLOOR(RAND() * 1000) as view_count,  -- 随机查看次数
    FLOOR(RAND() * 500) as download_count,  -- 随机下载次数
    NOW() as created_at,
    NOW() as updated_at
FROM (
    SELECT
        a.*,
        ROW_NUMBER() OVER (PARTITION BY a.primary_category_id ORDER BY a.rating DESC, RAND()) as row_num
    FROM apps a
    WHERE a.primary_category_id IS NOT NULL
        AND a.name IS NOT NULL
        AND NOT EXISTS (
            SELECT 1 FROM free_promotions fp
            WHERE fp.appstore_app_id = a.app_id
            AND fp.status = 'ACTIVE'
        )  -- 排除已经在限免中的应用
) a
WHERE a.row_num <= 5;  -- 每个分类选5个

-- 查询插入的结果
SELECT
    category_name,
    COUNT(*) as count,
    GROUP_CONCAT(app_name SEPARATOR ', ') as apps
FROM free_promotions
WHERE status = 'ACTIVE'
    AND created_at >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
GROUP BY category_name
ORDER BY category_name;

-- 查看总数
SELECT
    COUNT(*) as total_count,
    COUNT(DISTINCT category_id) as category_count,
    MIN(original_price) as min_price,
    MAX(original_price) as max_price,
    AVG(original_price) as avg_price
FROM free_promotions
WHERE status = 'ACTIVE';

-- 更新一些应用为即将结束状态（结束时间在6小时内）
UPDATE free_promotions
SET
    end_time = DATE_ADD(NOW(), INTERVAL FLOOR(1 + RAND() * 6) HOUR),
    is_ending_soon = 1
WHERE status = 'ACTIVE'
    AND RAND() < 0.2  -- 随机选择20%的应用
LIMIT 10;

-- 设置一些降价促销（不是完全免费）
INSERT INTO free_promotions (
    id,
    app_id,
    appstore_app_id,
    app_name,
    bundle_id,
    icon_url,
    developer_name,
    category_id,
    category_name,
    original_price,
    current_price,
    discount_percent,
    savings_amount,
    promotion_type,
    start_time,
    end_time,
    status,
    is_featured,
    is_hot,
    is_ending_soon,
    is_new_found,
    view_count,
    download_count,
    created_at,
    updated_at
)
SELECT
    UUID() as id,
    a.id as app_id,
    a.app_id as appstore_app_id,
    a.name as app_name,
    a.bundle_id,
    a.icon_url,
    a.developer_name,
    a.primary_category_id as category_id,
    a.primary_category_name as category_name,
    ROUND(RAND() * 100 + 30, 2) as original_price,  -- 原价 30-130
    ROUND((RAND() * 100 + 30) * (0.3 + RAND() * 0.4), 2) as current_price,  -- 折扣价 30%-70%
    ROUND((1 - (0.3 + RAND() * 0.4)) * 100, 0) as discount_percent,  -- 折扣百分比
    ROUND((RAND() * 100 + 30) * (0.3 + RAND() * 0.4), 2) as savings_amount,
    'DISCOUNT' as promotion_type,  -- 降价类型
    NOW() as start_time,
    DATE_ADD(NOW(), INTERVAL 1 + FLOOR(RAND() * 5) DAY) as end_time,
    'ACTIVE' as status,
    IF(RAND() > 0.6, 1, 0) as is_featured,
    IF(RAND() > 0.7, 1, 0) as is_hot,
    IF(RAND() > 0.8, 1, 0) as is_ending_soon,
    IF(RAND() > 0.5, 1, 0) as is_new_found,
    FLOOR(RAND() * 800) as view_count,
    FLOOR(RAND() * 400) as download_count,
    NOW() as created_at,
    NOW() as updated_at
FROM (
    SELECT
        a.*,
        ROW_NUMBER() OVER (PARTITION BY a.primary_category_id ORDER BY RAND()) as row_num
    FROM apps a
    WHERE a.primary_category_id IS NOT NULL
        AND a.name IS NOT NULL
        AND NOT EXISTS (
            SELECT 1 FROM free_promotions fp
            WHERE fp.appstore_app_id = a.app_id
            AND fp.status = 'ACTIVE'
        )
) a
WHERE a.row_num <= 3  -- 每个分类选3个降价应用
LIMIT 30;

-- 最终统计
SELECT
    promotion_type,
    COUNT(*) as count,
    AVG(original_price) as avg_original_price,
    AVG(current_price) as avg_current_price,
    AVG(discount_percent) as avg_discount
FROM free_promotions
WHERE status = 'ACTIVE'
GROUP BY promotion_type;

-- 查看分类分布
SELECT
    category_name,
    promotion_type,
    COUNT(*) as app_count
FROM free_promotions
WHERE status = 'ACTIVE'
GROUP BY category_name, promotion_type
ORDER BY category_name, promotion_type;