-- ============================================================
-- Moon Business AppStore 基础数据初始化
--
-- 描述: 初始化分类等基础数据
-- 作者: Moon Cloud
-- 日期: 2024-09-26
-- 版本: 1.0.0
-- ============================================================

USE `moon_appstore`;

-- ============================================================
-- 清理已有数据（可选）
-- ============================================================
-- DELETE FROM categories;

-- ============================================================
-- 初始化分类数据
-- ============================================================

-- 插入应用主分类
INSERT INTO `categories` (`id`, `category_id`, `parent_id`, `name_cn`, `name_en`, `category_type`, `sort_order`) VALUES
('cat_001', '6014', NULL, '游戏', 'Games', 'GAME', 1),
('cat_002', '6000', NULL, '商务', 'Business', 'APP', 2),
('cat_003', '6002', NULL, '工具', 'Utilities', 'APP', 3),
('cat_004', '6003', NULL, '旅行', 'Travel', 'APP', 4),
('cat_005', '6004', NULL, '体育', 'Sports', 'APP', 5),
('cat_006', '6005', NULL, '社交', 'Social Networking', 'APP', 6),
('cat_007', '6006', NULL, '参考', 'Reference', 'APP', 7),
('cat_008', '6007', NULL, '效率', 'Productivity', 'APP', 8),
('cat_009', '6008', NULL, '摄影与录像', 'Photo & Video', 'APP', 9),
('cat_010', '6009', NULL, '新闻', 'News', 'APP', 10),
('cat_011', '6010', NULL, '导航', 'Navigation', 'APP', 11),
('cat_012', '6011', NULL, '音乐', 'Music', 'APP', 12),
('cat_013', '6012', NULL, '生活', 'Lifestyle', 'APP', 13),
('cat_014', '6013', NULL, '健康健美', 'Health & Fitness', 'APP', 14),
('cat_015', '6015', NULL, '财务', 'Finance', 'APP', 15),
('cat_016', '6016', NULL, '娱乐', 'Entertainment', 'APP', 16),
('cat_017', '6017', NULL, '教育', 'Education', 'APP', 17),
('cat_018', '6018', NULL, '图书', 'Books', 'APP', 18),
('cat_019', '6020', NULL, '医疗', 'Medical', 'APP', 19),
('cat_020', '6021', NULL, '报刊杂志', 'Newsstand', 'APP', 20),
('cat_021', '6022', NULL, '美食佳饮', 'Food & Drink', 'APP', 21),
('cat_022', '6023', NULL, '天气', 'Weather', 'APP', 22),
('cat_023', '6024', NULL, '购物', 'Shopping', 'APP', 23),
('cat_024', '6026', NULL, '贴纸', 'Stickers', 'APP', 24),
('cat_025', '6027', NULL, '开发工具', 'Developer Tools', 'APP', 25),
('cat_026', '6028', NULL, '图形和设计', 'Graphics & Design', 'APP', 26)
ON DUPLICATE KEY UPDATE
  `name_cn` = VALUES(`name_cn`),
  `name_en` = VALUES(`name_en`),
  `category_type` = VALUES(`category_type`),
  `sort_order` = VALUES(`sort_order`);

-- 插入游戏子分类
INSERT INTO `categories` (`id`, `category_id`, `parent_id`, `name_cn`, `name_en`, `category_type`, `sort_order`) VALUES
('cat_g01', '7001', '6014', '动作游戏', 'Action', 'GAME', 1),
('cat_g02', '7002', '6014', '冒险游戏', 'Adventure', 'GAME', 2),
('cat_g03', '7003', '6014', '街机游戏', 'Arcade', 'GAME', 3),
('cat_g04', '7004', '6014', '桌面游戏', 'Board', 'GAME', 4),
('cat_g05', '7005', '6014', '卡牌游戏', 'Card', 'GAME', 5),
('cat_g06', '7006', '6014', '赌场游戏', 'Casino', 'GAME', 6),
('cat_g07', '7007', '6014', '休闲游戏', 'Casual', 'GAME', 7),
('cat_g08', '7008', '6014', '家庭游戏', 'Family', 'GAME', 8),
('cat_g09', '7009', '6014', '音乐游戏', 'Music', 'GAME', 9),
('cat_g10', '7010', '6014', '解谜游戏', 'Puzzle', 'GAME', 10),
('cat_g11', '7011', '6014', '赛车游戏', 'Racing', 'GAME', 11),
('cat_g12', '7012', '6014', '角色扮演', 'Role Playing', 'GAME', 12),
('cat_g13', '7013', '6014', '模拟游戏', 'Simulation', 'GAME', 13),
('cat_g14', '7014', '6014', '体育游戏', 'Sports', 'GAME', 14),
('cat_g15', '7015', '6014', '策略游戏', 'Strategy', 'GAME', 15),
('cat_g16', '7016', '6014', '小游戏', 'Trivia', 'GAME', 16),
('cat_g17', '7017', '6014', '文字游戏', 'Word', 'GAME', 17)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `name_cn` = VALUES(`name_cn`),
  `name_en` = VALUES(`name_en`),
  `category_type` = VALUES(`category_type`),
  `sort_order` = VALUES(`sort_order`);

-- ============================================================
-- 示例测试数据（可选，用于开发测试）
-- ============================================================

-- 插入示例应用数据
-- INSERT INTO `apps` (`id`, `app_id`, `name`, `developer_name`, `primary_category_id`, `primary_category_name`,
--   `current_price`, `original_price`, `rating`, `rating_count`, `is_free`, `status`)
-- VALUES
-- ('app_001', '123456789', '示例游戏', '示例开发商', '6014', '游戏', 0.00, 68.00, 4.5, 1234, 0, 1),
-- ('app_002', '987654321', '效率工具', '工具开发商', '6007', '效率', 0.00, 30.00, 4.3, 567, 0, 1);

-- 插入示例限免推广数据
-- INSERT INTO `free_promotions` (`id`, `app_id`, `appstore_app_id`, `original_price`, `promotion_price`,
--   `savings_amount`, `start_time`, `end_time`, `status`, `is_featured`, `is_hot`)
-- VALUES
-- ('promo_001', 'app_001', '123456789', 68.00, 0.00, 68.00, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'ACTIVE', 1, 1),
-- ('promo_002', 'app_002', '987654321', 30.00, 0.00, 30.00, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'ACTIVE', 0, 1);

-- ============================================================
-- 数据统计更新
-- ============================================================

-- 更新分类统计信息（执行时请根据实际数据调整）
-- UPDATE categories c
-- SET
--   app_count = (SELECT COUNT(*) FROM apps WHERE primary_category_id = c.category_id AND status = 1),
--   free_app_count = (SELECT COUNT(*) FROM apps WHERE primary_category_id = c.category_id AND current_price = 0 AND status = 1),
--   avg_rating = (SELECT AVG(rating) FROM apps WHERE primary_category_id = c.category_id AND rating IS NOT NULL AND status = 1)
-- WHERE c.is_active = 1;