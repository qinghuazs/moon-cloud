package com.mooncloud.shorturl.repository;

import com.mooncloud.shorturl.entity.UrlMappingEntity;
import com.mooncloud.shorturl.enums.UrlStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * URL映射仓库接口
 * 
 * @author mooncloud
 */
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMappingEntity, Long> {
    
    /**
     * 根据短链查找映射
     * 
     * @param shortUrl 短链标识符
     * @return URL映射实体
     */
    Optional<UrlMappingEntity> findByShortUrl(String shortUrl);
    
    /**
     * 根据URL哈希查找映射
     * 
     * @param urlHash URL哈希值
     * @return URL映射实体
     */
    Optional<UrlMappingEntity> findByUrlHash(String urlHash);
    
    /**
     * 检查短链是否存在
     * 
     * @param shortUrl 短链标识符
     * @return 是否存在
     */
    boolean existsByShortUrl(String shortUrl);
    
    /**
     * 根据用户ID查找URL映射列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return URL映射分页列表
     */
    Page<UrlMappingEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和状态查找URL映射列表
     * 
     * @param userId 用户ID
     * @param status URL状态
     * @param pageable 分页参数
     * @return URL映射分页列表
     */
    Page<UrlMappingEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, UrlStatus status, Pageable pageable);
    
    /**
     * 查找过期的URL映射
     * 
     * @param currentTime 当前时间
     * @return 过期的URL映射列表
     */
    @Query("SELECT u FROM UrlMappingEntity u WHERE u.expiresAt < :currentTime AND u.status = 'ACTIVE'")
    List<UrlMappingEntity> findExpiredUrls(@Param("currentTime") Date currentTime);
    
    /**
     * 批量更新过期URL状态
     * 
     * @param currentTime 当前时间
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE UrlMappingEntity u SET u.status = 'EXPIRED' WHERE u.expiresAt < :currentTime AND u.status = 'ACTIVE'")
    int updateExpiredUrls(@Param("currentTime") Date currentTime);
    
    /**
     * 增加点击次数
     * 
     * @param shortUrl 短链标识符
     * @return 更新的记录数
     */
    @Modifying
    @Query("UPDATE UrlMappingEntity u SET u.clickCount = u.clickCount + 1 WHERE u.shortUrl = :shortUrl")
    int incrementClickCount(@Param("shortUrl") String shortUrl);
    
    /**
     * 根据用户ID统计URL数量
     * 
     * @param userId 用户ID
     * @return URL数量
     */
    long countByUserId(Long userId);

    /**
     * 根据用户ID和状态统计URL数量
     * 
     * @param userId 用户ID
     * @param status URL状态
     * @return URL数量
     */
    long countByUserIdAndStatus(Long userId, UrlStatus status);
    
    /**
     * 查找热门链接（按点击次数排序）
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 热门链接列表
     */
    Page<UrlMappingEntity> findByUserIdAndStatusOrderByClickCountDesc(Long userId, UrlStatus status, Pageable pageable);
    
    /**
     * 根据关键词搜索用户的URL
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    @Query("SELECT u FROM UrlMappingEntity u WHERE u.userId = :userId AND " +
           "(u.originalUrl LIKE %:keyword% OR u.title LIKE %:keyword% OR u.description LIKE %:keyword%)")
    Page<UrlMappingEntity> searchByUserIdAndKeyword(@Param("userId") Long userId, 
                                                   @Param("keyword") String keyword, 
                                                   Pageable pageable);
    
    /**
     * 根据原始URL或短链包含关键词搜索
     * 
     * @param originalUrlKeyword 原始URL关键词
     * @param shortUrlKeyword 短链关键词
     * @param pageable 分页参数
     * @return 搜索结果
     */
    Page<UrlMappingEntity> findByOriginalUrlContainingOrShortUrlContaining(String originalUrlKeyword, 
                                                                           String shortUrlKeyword, 
                                                                           Pageable pageable);
    
    /**
     * 根据状态查找URL映射
     * 
     * @param status URL状态
     * @param pageable 分页参数
     * @return URL映射分页列表
     */
    Page<UrlMappingEntity> findByStatus(UrlStatus status, Pageable pageable);
    
    /**
     * 根据状态统计URL数量
     * 
     * @param status URL状态
     * @return URL数量
     */
    long countByStatus(UrlStatus status);
    
    /**
     * 根据创建时间范围统计URL数量
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return URL数量
     */
    long countByCreatedAtBetween(Date startTime, Date endTime);
    
    /**
     * 根据用户ID查找URL映射（分页）
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return URL映射分页结果
     */
    Page<UrlMappingEntity> findByUserId(Long userId, Pageable pageable);
}