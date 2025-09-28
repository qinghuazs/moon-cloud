-- 更新 apps 表中缺失 app_url 的记录
-- 为所有 app_url 为空的记录生成默认的 App Store URL

-- 查看有多少记录缺失 app_url
SELECT COUNT(*) AS missing_url_count
FROM apps
WHERE app_url IS NULL OR app_url = '';

-- 更新缺失的 app_url
UPDATE apps
SET app_url = CONCAT('https://apps.apple.com/cn/app/id', app_id),
    updated_at = NOW()
WHERE (app_url IS NULL OR app_url = '')
  AND app_id IS NOT NULL;

-- 验证更新结果
SELECT
    id,
    app_id,
    name,
    app_url,
    updated_at
FROM apps
WHERE updated_at >= DATE_SUB(NOW(), INTERVAL 1 MINUTE)
ORDER BY updated_at DESC
LIMIT 10;

-- 再次统计缺失 app_url 的记录数
SELECT COUNT(*) AS remaining_missing_count
FROM apps
WHERE app_url IS NULL OR app_url = '';