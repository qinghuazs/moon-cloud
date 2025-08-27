package com.moon.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 密码修改请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Schema(description = "密码修改请求")
public class PasswordChangeRequest {

    @Schema(description = "旧密码", example = "123456")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", example = "654321")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;

    @Schema(description = "确认新密码", example = "654321")
    @NotBlank(message = "确认新密码不能为空")
    private String confirmPassword;
}