package com.moon.cloud.appstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.appstore.entity.FreePromotion;
import com.moon.cloud.appstore.vo.FreeAppVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 限免推广记录表 Mapper 接口
 *
 * @author Moon Cloud
 * @since 2024-09-26
 */
@Mapper
public interface FreePromotionMapper extends BaseMapper<FreePromotion> {

    /**
     * 联表查询今天的限免应用
     */
    List<FreeAppVO> selectTodayFreeApps(@Param("status") String status,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("isFeatured") Boolean isFeatured,
                                        @Param("isHot") Boolean isHot,
                                        @Param("categoryId") String categoryId,
                                        @Param("minRating") Double minRating,
                                        @Param("minOriginalPrice") Double minOriginalPrice,
                                        @Param("maxOriginalPrice") Double maxOriginalPrice,
                                        @Param("sortBy") String sortBy,
                                        @Param("offset") int offset,
                                        @Param("pageSize") int pageSize);

    /**
     * 统计今天的限免应用总数
     */
    Long countTodayFreeApps(@Param("status") String status,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime,
                           @Param("isFeatured") Boolean isFeatured,
                           @Param("isHot") Boolean isHot,
                           @Param("categoryId") String categoryId,
                           @Param("minRating") Double minRating,
                           @Param("minOriginalPrice") Double minOriginalPrice,
                           @Param("maxOriginalPrice") Double maxOriginalPrice);
}