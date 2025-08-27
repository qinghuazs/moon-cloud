package com.moon.cloud.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.user.entity.User;
import com.moon.cloud.user.entity.UserRole;
import com.moon.cloud.user.mapper.UserMapper;
import com.moon.cloud.user.mapper.UserRoleMapper;
import com.moon.cloud.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(User user) {
        // 检查用户名是否存在
        if (isUsernameExists(user.getUsername(), null)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(user.getEmail()) && isEmailExists(user.getEmail(), null)) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 检查手机号是否存在
        if (StringUtils.hasText(user.getPhone()) && isPhoneExists(user.getPhone(), null)) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 加密密码
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(User.STATUS_ENABLED);
        }
        
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户名是否存在
        if (!existingUser.getUsername().equals(user.getUsername()) && 
            isUsernameExists(user.getUsername(), user.getId())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (StringUtils.hasText(user.getEmail()) && 
            !user.getEmail().equals(existingUser.getEmail()) && 
            isEmailExists(user.getEmail(), user.getId())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 检查手机号是否存在
        if (StringUtils.hasText(user.getPhone()) && 
            !user.getPhone().equals(existingUser.getPhone()) && 
            isPhoneExists(user.getPhone(), user.getId())) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 不更新密码字段
        user.setPasswordHash(null);
        
        userMapper.updateById(user);
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 删除用户角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 删除用户
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteUsers(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return false;
        }
        
        // 删除用户角色关联
        userRoleMapper.batchDeleteByUserIds(userIds);
        
        // 删除用户
        return userMapper.deleteBatchIds(userIds) > 0;
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectUserWithRolesByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public IPage<User> getUserPage(Page<User> page, String username, String email, Integer status) {
        return userMapper.selectUserPageWithRoles(page, username, email, status);
    }

    @Override
    public List<User> getEnabledUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, User.STATUS_ENABLED)
               .orderByDesc(User::getCreatedAt);
        return userMapper.selectList(wrapper);
    }

    @Override
    public List<User> getUsersByRoleId(Long roleId) {
        return userMapper.selectUsersByRoleId(roleId);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean batchUpdateUserStatus(List<Long> userIds, Integer status) {
        if (CollectionUtils.isEmpty(userIds)) {
            return false;
        }
        return userMapper.batchUpdateStatus(userIds, status) > 0;
    }

    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        User user = new User();
        user.setId(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("旧密码错误");
        }
        
        // 更新新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }
        
        // 删除现有角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 添加新的角色关联
        List<UserRole> userRoles = roleIds.stream()
                .map(roleId -> new UserRole(userId, roleId))
                .collect(Collectors.toList());
        
        return userRoleMapper.batchInsert(userRoles) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeRolesFromUser(Long userId, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return false;
        }
        
        for (Long roleId : roleIds) {
            userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
        }
        
        return true;
    }

    @Override
    public boolean isUsernameExists(String username, Long excludeUserId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (excludeUserId != null) {
            wrapper.ne(User::getId, excludeUserId);
        }
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isEmailExists(String email, Long excludeUserId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (excludeUserId != null) {
            wrapper.ne(User::getId, excludeUserId);
        }
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean isPhoneExists(String phone, Long excludeUserId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (excludeUserId != null) {
            wrapper.ne(User::getId, excludeUserId);
        }
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Long countUsers(Integer status) {
        return userMapper.countUsers(status);
    }

    @Override
    public boolean updateLastLoginTime(Long userId) {
        return userMapper.updateLastLoginTime(userId, LocalDateTime.now()) > 0;
    }

    // 为了兼容 UserController 中的调用，添加别名方法
    public boolean existsByUsername(String username) {
        return isUsernameExists(username, null);
    }

    public boolean existsByEmail(String email) {
        return isEmailExists(email, null);
    }

    public boolean existsByPhone(String phone) {
        return isPhoneExists(phone, null);
    }
}