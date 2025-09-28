package com.moon.cloud.appstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppListDTO;
import com.moon.cloud.appstore.service.FreeAppService;
import com.moon.cloud.appstore.vo.AppDetailVO;
import com.moon.cloud.appstore.vo.FreeAppVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 限免应用控制器
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/free-apps")
@Tag(name = "限免应用管理", description = "限免应用相关接口")
public class FreeAppController {

    private final FreeAppService freeAppService;

    @Operation(summary = "获取今日限免应用列表")
    @GetMapping("/today")
    public Page<FreeAppVO> getTodayFreeApps(@Valid FreeAppListDTO dto) {
        log.info("获取今日限免应用列表, 参数: {}", dto);
        return freeAppService.getTodayFreeApps(dto);
    }

    @Operation(summary = "获取应用详情")
    @GetMapping("/{appId}")
    public AppDetailVO getAppDetail(
            @Parameter(description = "应用ID", required = true)
            @PathVariable String appId) {
        log.info("获取应用详情, appId: {}", appId);
        AppDetailVO detail = freeAppService.getAppDetail(appId);
        // 增加浏览次数
        freeAppService.increaseViewCount(appId);
        return detail;
    }

    @Operation(summary = "记录应用点击")
    @PostMapping("/{appId}/click")
    public void recordClick(
            @Parameter(description = "应用ID", required = true)
            @PathVariable String appId) {
        log.info("记录应用点击, appId: {}", appId);
        freeAppService.increaseClickCount(appId);
    }

    @Operation(summary = "记录应用分享")
    @PostMapping("/{appId}/share")
    public void recordShare(
            @Parameter(description = "应用ID", required = true)
            @PathVariable String appId) {
        log.info("记录应用分享, appId: {}", appId);
        freeAppService.increaseShareCount(appId);
    }
}