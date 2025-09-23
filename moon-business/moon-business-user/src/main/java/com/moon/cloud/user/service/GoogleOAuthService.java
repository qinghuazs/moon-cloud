package com.moon.cloud.user.service;

import com.moon.cloud.user.dto.LoginResponse;
import com.moon.cloud.user.entity.User;

/**
 * Google OAuth 服务接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
public interface GoogleOAuthService {

    /**
     * 通过Google ID Token进行登录
     *
     * @param idToken Google ID Token
     * @param ip 客户端IP
     * @param userAgent 用户代理
     * @return 登录响应
     */
    LoginResponse loginWithGoogle(String idToken, String ip, String userAgent);

    /**
     * 验证Google ID Token
     *
     * @param idToken Google ID Token
     * @return Google用户信息
     */
    GoogleUserInfo verifyGoogleToken(String idToken);

    /**
     * 根据Google用户信息创建或更新用户
     *
     * @param googleUserInfo Google用户信息
     * @return 用户实体
     */
    User createOrUpdateUserFromGoogle(GoogleUserInfo googleUserInfo);

    /**
     * Google用户信息
     */
    class GoogleUserInfo {
        private String googleId;
        private String email;
        private String name;
        private String picture;
        private Boolean emailVerified;

        public GoogleUserInfo() {}

        public GoogleUserInfo(String googleId, String email, String name, String picture, Boolean emailVerified) {
            this.googleId = googleId;
            this.email = email;
            this.name = name;
            this.picture = picture;
            this.emailVerified = emailVerified;
        }

        public String getGoogleId() {
            return googleId;
        }

        public void setGoogleId(String googleId) {
            this.googleId = googleId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public Boolean getEmailVerified() {
            return emailVerified;
        }

        public void setEmailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified;
        }
    }
}