package com.moon.cloud.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@TableName("sys_user")
@Schema(description = "用户实体")
public class User {

    // 状态常量
    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_ENABLED = 1;

    // 登录提供商类型常量
    public static final String PROVIDER_LOCAL = "LOCAL";
    public static final String PROVIDER_GOOGLE = "GOOGLE";

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    @TableField("username")
    @Schema(description = "用户名")
    private String username;

    @TableField("email")
    @Schema(description = "邮箱")
    private String email;

    @TableField("password_hash")
    @JsonIgnore
    @Schema(description = "密码哈希", hidden = true)
    private String passwordHash;

    @TableField("nickname")
    @Schema(description = "昵称")
    private String nickname;

    @TableField("avatar_url")
    @Schema(description = "头像URL")
    private String avatarUrl;

    @TableField("phone")
    @Schema(description = "手机号")
    private String phone;

    @TableField("status")
    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @TableField("last_login_at")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @TableField("google_id")
    @Schema(description = "Google用户ID")
    private String googleId;

    @TableField("provider_type")
    @Schema(description = "登录提供商类型：LOCAL-本地，GOOGLE-谷歌")
    private String providerType;

    @TableField("is_email_verified")
    @Schema(description = "邮箱是否已验证")
    private Boolean isEmailVerified;

    @TableField(exist = false)
    @Schema(description = "用户角色列表")
    private List<Role> roles;

    // 构造函数
    public User() {}

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = 1; // 默认启用
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getProviderType() {
        return providerType;
    }

    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}