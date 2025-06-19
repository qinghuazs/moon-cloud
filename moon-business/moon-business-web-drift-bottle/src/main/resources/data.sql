-- 漂流瓶应用初始化数据

-- 插入测试用户的漂流瓶数据
INSERT INTO drift_bottle (sender_username, content, create_time, status, current_holder, pass_count, last_update_time) VALUES
('alice', '今天天气真好，希望收到这个漂流瓶的人也有美好的一天！', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('bob', '我在海边看日落，想和远方的朋友分享这份美好。', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('charlie', '刚刚完成了一个重要的项目，感觉很有成就感！', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('diana', '今天读了一本很棒的书，推荐给大家《百年孤独》。', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('eve', '想念家乡的味道，特别是妈妈做的红烧肉。', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('frank', '刚刚学会了一首新歌，心情特别好！', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('grace', '今天遇到了一只很可爱的小猫，它让我想起了童年。', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('henry', '正在学习编程，虽然很难但是很有趣！', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('iris', '刚刚看了一部很感人的电影，眼泪都流出来了。', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP),
('jack', '明天要去旅行了，好期待啊！', CURRENT_TIMESTAMP, 'FLOATING', NULL, 0, CURRENT_TIMESTAMP);

-- 插入一些已被捡起的漂流瓶
INSERT INTO drift_bottle (sender_username, content, create_time, status, current_holder, pass_count, last_update_time) VALUES
('alice', '希望世界和平，每个人都能快乐生活。', CURRENT_TIMESTAMP, 'PICKED_UP', 'bob', 1, CURRENT_TIMESTAMP),
('charlie', '今天是我的生日，虽然一个人过，但还是很开心。', CURRENT_TIMESTAMP, 'PICKED_UP', 'diana', 2, CURRENT_TIMESTAMP),
('eve', '刚刚完成了马拉松比赛，虽然累但很有成就感！', CURRENT_TIMESTAMP, 'PICKED_UP', 'frank', 1, CURRENT_TIMESTAMP);

-- 插入一些有回复的漂流瓶
INSERT INTO drift_bottle (sender_username, content, create_time, status, current_holder, pass_count, last_update_time) VALUES
('grace', '今天心情不太好，希望有人能给我一些鼓励。', CURRENT_TIMESTAMP, 'REPLIED', 'grace', 3, CURRENT_TIMESTAMP),
('henry', '刚刚失恋了，感觉整个世界都灰暗了。', CURRENT_TIMESTAMP, 'REPLIED', 'henry', 2, CURRENT_TIMESTAMP);

-- 插入回复数据
INSERT INTO bottle_reply (replier_username, reply_content, reply_time, bottle_id) VALUES
('iris', '不要难过，每个人都会遇到低谷，但阳光总会再次照耀！加油！', CURRENT_TIMESTAMP, (SELECT id FROM drift_bottle WHERE sender_username = 'grace' AND status = 'REPLIED' LIMIT 1)),
('jack', '失恋虽然痛苦，但也是成长的一部分。相信你会遇到更好的人！', CURRENT_TIMESTAMP, (SELECT id FROM drift_bottle WHERE sender_username = 'henry' AND status = 'REPLIED' LIMIT 1)),
('alice', '送你一个拥抱，希望你能重新找到快乐！', CURRENT_TIMESTAMP, (SELECT id FROM drift_bottle WHERE sender_username = 'grace' AND status = 'REPLIED' LIMIT 1));

-- 插入一些已完成的漂流瓶（达到最大传递次数）
INSERT INTO drift_bottle (sender_username, content, create_time, status, current_holder, pass_count, last_update_time) VALUES
('bob', '这是一个测试漂流瓶，已经传递了很多次。', CURRENT_TIMESTAMP, 'COMPLETED', NULL, 10, CURRENT_TIMESTAMP),
('diana', '感谢所有捡到这个漂流瓶的朋友们！', CURRENT_TIMESTAMP, 'COMPLETED', NULL, 10, CURRENT_TIMESTAMP);

-- 提交事务
COMMIT;