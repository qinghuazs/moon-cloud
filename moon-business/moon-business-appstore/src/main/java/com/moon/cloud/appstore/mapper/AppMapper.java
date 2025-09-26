package com.moon.cloud.appstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.appstore.entity.App;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用信息表 Mapper 接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Mapper
public interface AppMapper extends BaseMapper<App> {

}