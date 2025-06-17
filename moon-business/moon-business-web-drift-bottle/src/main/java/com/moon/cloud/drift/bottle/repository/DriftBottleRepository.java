package com.moon.cloud.drift.bottle.repository;

import com.moon.cloud.drift.bottle.entity.DriftBottle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 漂流瓶数据访问层
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Repository
public interface DriftBottleRepository extends JpaRepository<DriftBottle, Long> {

    /**
     * 根据发送者用户名查找漂流瓶
     *
     * @param senderUsername 发送者用户名
     * @param pageable 分页参数
     * @return 漂流瓶分页列表
     */
    Page<DriftBottle> findBySenderUsername(String senderUsername, Pageable pageable);

    /**
     * 根据当前持有者查找漂流瓶
     *
     * @param currentHolder 当前持有者
     * @param pageable 分页参数
     * @return 漂流瓶分页列表
     */
    Page<DriftBottle> findByCurrentHolder(String currentHolder, Pageable pageable);

    /**
     * 根据状态查找漂流瓶
     *
     * @param status 漂流瓶状态
     * @param pageable 分页参数
     * @return 漂流瓶分页列表
     */
    Page<DriftBottle> findByStatus(DriftBottle.BottleStatus status, Pageable pageable);

    /**
     * 查找漂流中的漂流瓶（随机获取）
     *
     * @param excludeUsername 排除的用户名（不能捡到自己的瓶子）
     * @param limit 限制数量
     * @return 漂流瓶列表
     */
    @Query("SELECT b FROM DriftBottle b WHERE b.status = 'FLOATING' " +
           "AND b.senderUsername != :excludeUsername " +
           "AND (b.currentHolder IS NULL OR b.currentHolder != :excludeUsername) " +
           "ORDER BY FUNCTION('RANDOM')")
    List<DriftBottle> findRandomFloatingBottles(@Param("excludeUsername") String excludeUsername, 
                                               Pageable pageable);

    /**
     * 查找指定时间之前创建的漂流瓶
     *
     * @param beforeTime 指定时间
     * @return 漂流瓶列表
     */
    List<DriftBottle> findByCreateTimeBefore(LocalDateTime beforeTime);

    /**
     * 统计用户发送的漂流瓶数量
     *
     * @param senderUsername 发送者用户名
     * @return 数量
     */
    long countBySenderUsername(String senderUsername);

    /**
     * 统计用户接收的漂流瓶数量
     *
     * @param currentHolder 当前持有者
     * @return 数量
     */
    long countByCurrentHolder(String currentHolder);

    /**
     * 查找用户最近发送的漂流瓶
     *
     * @param senderUsername 发送者用户名
     * @return 最近的漂流瓶
     */
    Optional<DriftBottle> findFirstBySenderUsernameOrderByCreateTimeDesc(String senderUsername);

    /**
     * 查找用户最近接收的漂流瓶
     *
     * @param currentHolder 当前持有者
     * @return 最近的漂流瓶
     */
    Optional<DriftBottle> findFirstByCurrentHolderOrderByLastUpdateTimeDesc(String currentHolder);

    /**
     * 查找传递次数超过指定值的漂流瓶
     *
     * @param passCount 传递次数
     * @return 漂流瓶列表
     */
    List<DriftBottle> findByPassCountGreaterThan(Integer passCount);

    /**
     * 根据内容关键字搜索漂流瓶
     *
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 漂流瓶分页列表
     */
    @Query("SELECT b FROM DriftBottle b WHERE b.content LIKE %:keyword%")
    Page<DriftBottle> searchByContentKeyword(@Param("keyword") String keyword, Pageable pageable);
}