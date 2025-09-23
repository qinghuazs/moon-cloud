package com.moon.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Google OAuth 登录请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Schema(description = "Google OAuth 登录请求")
public class GoogleLoginRequest {

    @NotBlank(message = "Google ID Token不能为空")
    @Schema(description = "Google ID Token", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    private String idToken;

    public GoogleLoginRequest() {}

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}