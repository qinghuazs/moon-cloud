package com.moon.cloud.appstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.appstore.entity.SearchIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索索引表 Mapper 接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Mapper
public interface SearchIndexMapper extends BaseMapper<SearchIndex> {

    /**
     * 全文搜索
     *
     * @param keyword 搜索关键词
     * @return 搜索结果列表
     */
    @Select("SELECT * FROM search_index WHERE " +
            "MATCH(app_name, developer_name, keywords) " +
            "AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "ORDER BY search_weight DESC, popularity_score DESC " +
            "LIMIT 100")
    List<SearchIndex> fullTextSearch(@Param("keyword") String keyword);
}