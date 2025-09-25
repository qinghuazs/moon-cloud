-- Moon Cloud 用户管理系统初始数据脚本
USE `moon`;

-- 插入超级管理员用户（密码：admin123）
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `email`, `phone`, `nickname`, `status`, `provider_type`, `is_email_verified`, `created_at`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSOfvVWbazBuOWShWvheWjxVYtnpkmjNdwYKTHdAaoGC', 'admin@mooncloud.com', '13800138000', '超级管理员', 1, 'LOCAL', TRUE, NOW());

-- 插入普通用户（密码：user123）
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `email`, `phone`, `nickname`, `status`, `provider_type`, `is_email_verified`, `created_at`) VALUES
(2, 'user', '$2a$10$HiiEL2fkJcPoAsbXMXZ.OuJQjmqXaM5yzT9byKDVHI2k9QeyHpb.m', 'user@mooncloud.com', '13800138001', '测试用户', 1, 'LOCAL', TRUE, NOW());

-- 插入角色数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`, `status`, `created_at`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, NOW()),
(2, '系统管理员', 'ADMIN', '系统管理员，拥有大部分管理权限', 1, NOW()),
(3, '普通用户', 'USER', '普通用户，拥有基本功能权限', 1, NOW());

-- 插入权限数据
INSERT INTO `sys_permission` (`id`, `permission_name`, `permission_code`, `description`, `resource_type`, `resource_url`, `status`, `created_at`) VALUES
-- 系统管理
(1, '系统管理', 'SYSTEM_MANAGE', '系统管理模块', 'MENU', '/system', 1, NOW()),

-- 用户管理
(10, '用户管理', 'USER_MANAGE', '用户管理模块', 'MENU', '/system/user', 1, NOW()),
(11, '用户查询', 'USER_QUERY', '查询用户信息', 'API', '/users/**', 1, NOW()),
(12, '用户新增', 'USER_CREATE', '新增用户', 'API', '/users', 1, NOW()),
(13, '用户修改', 'USER_UPDATE', '修改用户信息', 'API', '/users/**', 1, NOW()),
(14, '用户删除', 'USER_DELETE', '删除用户', 'API', '/users/**', 1, NOW()),
(15, '用户状态', 'USER_STATUS', '修改用户状态', 'API', '/users/status', 1, NOW()),
(16, '重置密码', 'USER_RESET_PASSWORD', '重置用户密码', 'API', '/users/*/reset-password', 1, NOW()),

-- 角色管理
(20, '角色管理', 'ROLE_MANAGE', '角色管理模块', 'MENU', '/system/role', 1, NOW()),
(21, '角色查询', 'ROLE_QUERY', '查询角色信息', 'API', '/roles/**', 1, NOW()),
(22, '角色新增', 'ROLE_CREATE', '新增角色', 'API', '/roles', 1, NOW()),
(23, '角色修改', 'ROLE_UPDATE', '修改角色信息', 'API', '/roles/**', 1, NOW()),
(24, '角色删除', 'ROLE_DELETE', '删除角色', 'API', '/roles/**', 1, NOW()),
(25, '角色状态', 'ROLE_STATUS', '修改角色状态', 'API', '/roles/status', 1, NOW()),
(26, '分配权限', 'ROLE_ASSIGN_PERMISSION', '为角色分配权限', 'API', '/roles/*/permissions', 1, NOW()),

-- 权限管理
(30, '权限管理', 'PERMISSION_MANAGE', '权限管理模块', 'MENU', '/system/permission', 1, NOW()),
(31, '权限查询', 'PERMISSION_QUERY', '查询权限信息', 'API', '/permissions/**', 1, NOW()),
(32, '权限新增', 'PERMISSION_CREATE', '新增权限', 'API', '/permissions', 1, NOW()),
(33, '权限修改', 'PERMISSION_UPDATE', '修改权限信息', 'API', '/permissions/**', 1, NOW()),
(34, '权限删除', 'PERMISSION_DELETE', '删除权限', 'API', '/permissions/**', 1, NOW()),
(35, '权限状态', 'PERMISSION_STATUS', '修改权限状态', 'API', '/permissions/status', 1, NOW()),

-- 日志管理
(40, '日志管理', 'LOG_MANAGE', '日志管理模块', 'MENU', '/system/log', 1, NOW()),
(41, '登录日志', 'LOGIN_LOG_QUERY', '查询登录日志', 'API', '/login-logs/**', 1, NOW()),
(42, '日志删除', 'LOG_DELETE', '删除日志', 'API', '/login-logs/**', 1, NOW()),

-- 个人中心
(50, '个人中心', 'PROFILE_MANAGE', '个人中心模块', 'MENU', '/profile', 1, NOW()),
(51, '个人信息', 'PROFILE_INFO', '查看个人信息', 'API', '/auth/profile', 1, NOW()),
(52, '修改信息', 'PROFILE_UPDATE', '修改个人信息', 'API', '/auth/profile', 1, NOW()),
(53, '修改密码', 'PROFILE_CHANGE_PASSWORD', '修改个人密码', 'API', '/auth/change-password', 1, NOW());

-- 分配用户角色
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_at`) VALUES
(1, 1, 1, NOW()),  -- admin用户分配超级管理员角色
(2, 2, 3, NOW());  -- user用户分配普通用户角色

-- 分配角色权限
-- 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `created_at`)
SELECT
    1 as role_id,
    id as permission_id,
    NOW() as created_at
FROM `sys_permission`;

-- 系统管理员权限（除了超级管理员专有权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `created_at`) VALUES
(2, 1, NOW()),   -- 系统管理
(2, 10, NOW()),  -- 用户管理
(2, 11, NOW()),  -- 用户查询
(2, 12, NOW()),  -- 用户新增
(2, 13, NOW()),  -- 用户修改
(2, 15, NOW()),  -- 用户状态
(2, 20, NOW()),  -- 角色管理
(2, 21, NOW()),  -- 角色查询
(2, 22, NOW()),  -- 角色新增
(2, 23, NOW()),  -- 角色修改
(2, 25, NOW()),  -- 角色状态
(2, 30, NOW()),  -- 权限管理
(2, 31, NOW()),  -- 权限查询
(2, 40, NOW()),  -- 日志管理
(2, 41, NOW()),  -- 登录日志
(2, 50, NOW()),  -- 个人中心
(2, 51, NOW()),  -- 个人信息
(2, 52, NOW()),  -- 修改信息
(2, 53, NOW());  -- 修改密码

-- 普通用户权限（只有个人中心相关权限）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `created_at`) VALUES
(3, 50, NOW()),  -- 个人中心
(3, 51, NOW()),  -- 个人信息
(3, 52, NOW()),  -- 修改信息
(3, 53, NOW());  -- 修改密码