package com.moon.cloud.appstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.appstore.entity.AppCrawlFailure;
import org.apache.ibatis.annotations.Mapper;

/**
 * App爬取失败记录Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-09-27
 */
@Mapper
public interface AppCrawlFailureMapper extends BaseMapper<AppCrawlFailure> {

}