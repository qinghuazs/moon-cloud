-- 为 apps 表添加 app_url 字段
-- 用于存储应用的 App Store 链接

-- 添加 app_url 字段
ALTER TABLE apps
ADD COLUMN app_url VARCHAR(500) COMMENT 'App Store链接URL' AFTER app_id;

-- 创建索引以提高查询性能（如果需要通过URL查询）
CREATE INDEX idx_app_url ON apps(app_url);

-- 更新现有数据，生成默认的 App Store URL
UPDATE apps
SET app_url = CONCAT('https://apps.apple.com/cn/app/id', app_id)
WHERE app_id IS NOT NULL AND app_url IS NULL;

-- 验证更新结果
SELECT
    COUNT(*) as total_apps,
    COUNT(app_url) as apps_with_url,
    COUNT(*) - COUNT(app_url) as apps_without_url
FROM apps;

-- 查看几条示例数据
SELECT
    id,
    app_id,
    app_url,
    name,
    bundle_id
FROM apps
LIMIT 10;