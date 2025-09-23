-- Moon Cloud 用户管理系统初始数据脚本
USE `moon`;

-- 插入超级管理员用户（密码：admin123）
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `email`, `phone`, `nickname`, `status`, `provider_type`, `is_email_verified`, `created_at`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSOfvVWbazBuOWShWvheWjxVYtnpkmjNdwYKTHdAaoGC', 'admin@mooncloud.com', '13800138000', '超级管理员', 1, 'LOCAL', TRUE, NOW());

-- 插入普通用户（密码：user123）
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `email`, `phone`, `nickname`, `status`, `provider_type`, `is_email_verified`, `created_at`) VALUES
(2, 'user', '$2a$10$HiiEL2fkJcPoAsbXMXZ.OuJQjmqXaM5yzT9byKDVHI2k9QeyHpb.m', 'user@mooncloud.com', '13800138001', '测试用户', 1, 'LOCAL', TRUE, NOW());

-- 插入角色数据
INSERT INTO `sys_role` (`id`, `name`, `code`, `description`, `sort`, `status`, `remark`, `created_by`, `created_time`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1, 1, '系统内置角色，不可删除', 1, NOW()),
(2, '系统管理员', 'ADMIN', '系统管理员，拥有大部分管理权限', 2, 1, '系统管理员角色', 1, NOW()),
(3, '普通用户', 'USER', '普通用户，拥有基本功能权限', 3, 1, '普通用户角色', 1, NOW());

-- 插入权限数据
INSERT INTO `sys_permission` (`id`, `name`, `code`, `description`, `resource_type`, `resource_url`, `http_method`, `parent_id`, `sort`, `status`, `remark`, `created_by`, `created_time`) VALUES
-- 系统管理
(1, '系统管理', 'SYSTEM_MANAGE', '系统管理模块', 'MENU', '/system', NULL, 0, 1, 1, '系统管理菜单', 1, NOW()),

-- 用户管理
(10, '用户管理', 'USER_MANAGE', '用户管理模块', 'MENU', '/system/user', NULL, 1, 1, 1, '用户管理菜单', 1, NOW()),
(11, '用户查询', 'USER_QUERY', '查询用户信息', 'API', '/users/**', 'GET', 10, 1, 1, '用户查询权限', 1, NOW()),
(12, '用户新增', 'USER_CREATE', '新增用户', 'API', '/users', 'POST', 10, 2, 1, '用户新增权限', 1, NOW()),
(13, '用户修改', 'USER_UPDATE', '修改用户信息', 'API', '/users/**', 'PUT', 10, 3, 1, '用户修改权限', 1, NOW()),
(14, '用户删除', 'USER_DELETE', '删除用户', 'API', '/users/**', 'DELETE', 10, 4, 1, '用户删除权限', 1, NOW()),
(15, '用户状态', 'USER_STATUS', '修改用户状态', 'API', '/users/status', 'PUT', 10, 5, 1, '用户状态权限', 1, NOW()),
(16, '重置密码', 'USER_RESET_PASSWORD', '重置用户密码', 'API', '/users/*/reset-password', 'PUT', 10, 6, 1, '重置密码权限', 1, NOW()),

-- 角色管理
(20, '角色管理', 'ROLE_MANAGE', '角色管理模块', 'MENU', '/system/role', NULL, 1, 2, 1, '角色管理菜单', 1, NOW()),
(21, '角色查询', 'ROLE_QUERY', '查询角色信息', 'API', '/roles/**', 'GET', 20, 1, 1, '角色查询权限', 1, NOW()),
(22, '角色新增', 'ROLE_CREATE', '新增角色', 'API', '/roles', 'POST', 20, 2, 1, '角色新增权限', 1, NOW()),
(23, '角色修改', 'ROLE_UPDATE', '修改角色信息', 'API', '/roles/**', 'PUT', 20, 3, 1, '角色修改权限', 1, NOW()),
(24, '角色删除', 'ROLE_DELETE', '删除角色', 'API', '/roles/**', 'DELETE', 20, 4, 1, '角色删除权限', 1, NOW()),
(25, '角色状态', 'ROLE_STATUS', '修改角色状态', 'API', '/roles/status', 'PUT', 20, 5, 1, '角色状态权限', 1, NOW()),
(26, '分配权限', 'ROLE_ASSIGN_PERMISSION', '为角色分配权限', 'API', '/roles/*/permissions', 'POST', 20, 6, 1, '分配权限权限', 1, NOW()),

-- 权限管理
(30, '权限管理', 'PERMISSION_MANAGE', '权限管理模块', 'MENU', '/system/permission', NULL, 1, 3, 1, '权限管理菜单', 1, NOW()),
(31, '权限查询', 'PERMISSION_QUERY', '查询权限信息', 'API', '/permissions/**', 'GET', 30, 1, 1, '权限查询权限', 1, NOW()),
(32, '权限新增', 'PERMISSION_CREATE', '新增权限', 'API', '/permissions', 'POST', 30, 2, 1, '权限新增权限', 1, NOW()),
(33, '权限修改', 'PERMISSION_UPDATE', '修改权限信息', 'API', '/permissions/**', 'PUT', 30, 3, 1, '权限修改权限', 1, NOW()),
(34, '权限删除', 'PERMISSION_DELETE', '删除权限', 'API', '/permissions/**', 'DELETE', 30, 4, 1, '权限删除权限', 1, NOW()),
(35, '权限状态', 'PERMISSION_STATUS', '修改权限状态', 'API', '/permissions/status', 'PUT', 30, 5, 1, '权限状态权限', 1, NOW()),

-- 日志管理
(40, '日志管理', 'LOG_MANAGE', '日志管理模块', 'MENU', '/system/log', NULL, 1, 4, 1, '日志管理菜单', 1, NOW()),
(41, '登录日志', 'LOGIN_LOG_QUERY', '查询登录日志', 'API', '/login-logs/**', 'GET', 40, 1, 1, '登录日志权限', 1, NOW()),
(42, '日志删除', 'LOG_DELETE', '删除日志', 'API', '/login-logs/**', 'DELETE', 40, 2, 1, '日志删除权限', 1, NOW()),

-- 个人中心
(50, '个人中心', 'PROFILE_MANAGE', '个人中心模块', 'MENU', '/profile', NULL, 0, 2, 1, '个人中心菜单', 1, NOW()),
(51, '个人信息', 'PROFILE_INFO', '查看个人信息', 'API', '/auth/profile', 'GET', 50, 1, 1, '个人信息权限', 1, NOW()),
(52, '修改信息', 'PROFILE_UPDATE', '修改个人信息', 'API', '/auth/profile', 'PUT', 50, 2, 1, '修改信息权限', 1, NOW()),
(53, '修改密码', 'PROFILE_CHANGE_PASSWORD', '修改个人密码', 'API', '/auth/change-password', 'PUT', 50, 3, 1, '修改密码权限', 1, NOW());

-- 分配用户角色
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_by`, `created_time`) VALUES
(1, 1, 1, 1, NOW()),  -- admin用户分配超级管理员角色
(2, 2, 3, 1, NOW());  -- user用户分配普通用户角色

-- 分配角色权限
-- 超级管理员拥有所有权限
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `created_by`, `created_time`)
SELECT 
    ROW_NUMBER() OVER (ORDER BY id) as id,
    1 as role_id,
    id as permission_id,
    1 as created_by,
    NOW() as created_time
FROM `sys_permission`;

-- 系统管理员权限（除了超级管理员专有权限）
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `created_by`, `created_time`) VALUES
(100, 2, 1, 1, NOW()),   -- 系统管理
(101, 2, 10, 1, NOW()),  -- 用户管理
(102, 2, 11, 1, NOW()),  -- 用户查询
(103, 2, 12, 1, NOW()),  -- 用户新增
(104, 2, 13, 1, NOW()),  -- 用户修改
(105, 2, 15, 1, NOW()),  -- 用户状态
(106, 2, 20, 1, NOW()),  -- 角色管理
(107, 2, 21, 1, NOW()),  -- 角色查询
(108, 2, 22, 1, NOW()),  -- 角色新增
(109, 2, 23, 1, NOW()),  -- 角色修改
(110, 2, 25, 1, NOW()),  -- 角色状态
(111, 2, 30, 1, NOW()),  -- 权限管理
(112, 2, 31, 1, NOW()),  -- 权限查询
(113, 2, 40, 1, NOW()),  -- 日志管理
(114, 2, 41, 1, NOW()),  -- 登录日志
(115, 2, 50, 1, NOW()),  -- 个人中心
(116, 2, 51, 1, NOW()),  -- 个人信息
(117, 2, 52, 1, NOW()),  -- 修改信息
(118, 2, 53, 1, NOW());  -- 修改密码

-- 普通用户权限（只有个人中心相关权限）
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `created_by`, `created_time`) VALUES
(200, 3, 50, 1, NOW()),  -- 个人中心
(201, 3, 51, 1, NOW()),  -- 个人信息
(202, 3, 52, 1, NOW()),  -- 修改信息
(203, 3, 53, 1, NOW());  -- 修改密码