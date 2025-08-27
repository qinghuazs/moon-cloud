package com.moon.cloud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Data
@Schema(description = "批量操作请求")
public class BatchRequest {

    @Schema(description = "ID列表", example = "[1, 2, 3]")
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}