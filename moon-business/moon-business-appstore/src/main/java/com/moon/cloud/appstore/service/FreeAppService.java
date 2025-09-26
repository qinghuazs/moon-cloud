package com.moon.cloud.appstore.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.appstore.dto.FreeAppListDTO;
import com.moon.cloud.appstore.vo.AppDetailVO;
import com.moon.cloud.appstore.vo.FreeAppVO;

/**
 * 限免应用服务接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
public interface FreeAppService {

    /**
     * 获取今日限免应用列表
     *
     * @param dto 查询参数
     * @return 限免应用列表
     */
    Page<FreeAppVO> getTodayFreeApps(FreeAppListDTO dto);

    /**
     * 获取应用详情
     *
     * @param appId 应用ID
     * @return 应用详情信息
     */
    AppDetailVO getAppDetail(String appId);

    /**
     * 增加应用浏览次数
     *
     * @param appId 应用ID
     */
    void increaseViewCount(String appId);

    /**
     * 增加应用点击次数
     *
     * @param appId 应用ID
     */
    void increaseClickCount(String appId);

    /**
     * 增加应用分享次数
     *
     * @param appId 应用ID
     */
    void increaseShareCount(String appId);
}