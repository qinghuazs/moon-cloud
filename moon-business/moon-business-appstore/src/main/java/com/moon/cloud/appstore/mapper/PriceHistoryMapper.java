package com.moon.cloud.appstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.appstore.entity.PriceHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格历史表 Mapper 接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Mapper
public interface PriceHistoryMapper extends BaseMapper<PriceHistory> {

}