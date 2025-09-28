-- 诊断 app_url 字段的问题

-- 1. 统计 app_url 的填充情况
SELECT
    COUNT(*) AS total_apps,
    SUM(CASE WHEN app_url IS NOT NULL AND app_url != '' THEN 1 ELSE 0 END) AS with_url,
    SUM(CASE WHEN app_url IS NULL OR app_url = '' THEN 1 ELSE 0 END) AS without_url,
    ROUND(100.0 * SUM(CASE WHEN app_url IS NOT NULL AND app_url != '' THEN 1 ELSE 0 END) / COUNT(*), 2) AS url_fill_rate
FROM apps;

-- 2. 查看最近爬取的数据是否有 app_url
SELECT
    id,
    app_id,
    name,
    app_url,
    last_crawled_at,
    created_at,
    updated_at
FROM apps
WHERE last_crawled_at IS NOT NULL
ORDER BY last_crawled_at DESC
LIMIT 20;

-- 3. 查看不同时间段创建的数据的 app_url 填充情况
SELECT
    DATE(created_at) AS create_date,
    COUNT(*) AS total,
    SUM(CASE WHEN app_url IS NOT NULL AND app_url != '' THEN 1 ELSE 0 END) AS with_url,
    SUM(CASE WHEN app_url IS NULL OR app_url = '' THEN 1 ELSE 0 END) AS without_url
FROM apps
GROUP BY DATE(created_at)
ORDER BY create_date DESC
LIMIT 30;

-- 4. 查看没有 app_url 的记录示例
SELECT
    id,
    app_id,
    name,
    bundle_id,
    created_at,
    updated_at,
    last_crawled_at
FROM apps
WHERE app_url IS NULL OR app_url = ''
LIMIT 10;

-- 5. 检查是否有 app_id 为空的记录（这些记录无法生成默认 URL）
SELECT
    COUNT(*) AS count_without_app_id
FROM apps
WHERE app_id IS NULL OR app_id = '';

-- 6. 查看有 app_url 的记录的 URL 格式
SELECT
    DISTINCT LEFT(app_url, 30) AS url_pattern,
    COUNT(*) AS count
FROM apps
WHERE app_url IS NOT NULL AND app_url != ''
GROUP BY LEFT(app_url, 30)
ORDER BY count DESC;