package com.moon.cloud.drift.bottle.repository;

import com.moon.cloud.drift.bottle.entity.BottleReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 漂流瓶回复数据访问层
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Repository
public interface BottleReplyRepository extends JpaRepository<BottleReply, Long> {

    /**
     * 根据漂流瓶ID查找所有回复
     *
     * @param bottleId 漂流瓶ID
     * @param pageable 分页参数
     * @return 回复分页列表
     */
    Page<BottleReply> findByDriftBottleId(Long bottleId, Pageable pageable);

    /**
     * 根据回复者用户名查找回复
     *
     * @param replierUsername 回复者用户名
     * @param pageable 分页参数
     * @return 回复分页列表
     */
    Page<BottleReply> findByReplierUsername(String replierUsername, Pageable pageable);

    /**
     * 统计漂流瓶的回复数量
     *
     * @param bottleId 漂流瓶ID
     * @return 回复数量
     */
    long countByDriftBottleId(Long bottleId);

    /**
     * 统计用户的回复数量
     *
     * @param replierUsername 回复者用户名
     * @return 回复数量
     */
    long countByReplierUsername(String replierUsername);

    /**
     * 查找指定时间之后的回复
     *
     * @param afterTime 指定时间
     * @return 回复列表
     */
    List<BottleReply> findByReplyTimeAfter(LocalDateTime afterTime);

    /**
     * 查找漂流瓶的最新回复
     *
     * @param bottleId 漂流瓶ID
     * @return 最新回复
     */
    @Query("SELECT r FROM BottleReply r WHERE r.driftBottle.id = :bottleId ORDER BY r.replyTime DESC")
    List<BottleReply> findLatestRepliesByBottleId(@Param("bottleId") Long bottleId, Pageable pageable);

    /**
     * 删除漂流瓶的所有回复
     *
     * @param bottleId 漂流瓶ID
     */
    void deleteByDriftBottleId(Long bottleId);

    /**
     * 根据内容关键字搜索回复
     *
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 回复分页列表
     */
    @Query("SELECT r FROM BottleReply r WHERE r.replyContent LIKE %:keyword%")
    Page<BottleReply> searchByContentKeyword(@Param("keyword") String keyword, Pageable pageable);
}