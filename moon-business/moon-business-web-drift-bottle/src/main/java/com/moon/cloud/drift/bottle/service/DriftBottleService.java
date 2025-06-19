package com.moon.cloud.drift.bottle.service;

import com.moon.cloud.drift.bottle.dto.BottleReplyDTO;
import com.moon.cloud.drift.bottle.dto.DriftBottleDTO;
import com.moon.cloud.drift.bottle.entity.BottleReply;
import com.moon.cloud.drift.bottle.entity.DriftBottle;
import com.moon.cloud.drift.bottle.repository.BottleReplyRepository;
import com.moon.cloud.drift.bottle.repository.DriftBottleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 漂流瓶业务服务层
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Service
@Transactional
public class DriftBottleService {

    private static final Logger logger = LoggerFactory.getLogger(DriftBottleService.class);
    
    private static final int MAX_PASS_COUNT = 10; // 最大传递次数
    private static final int RANDOM_RECIPIENTS = 10; // 随机传递给10个人

    @Autowired
    private DriftBottleRepository driftBottleRepository;

    @Autowired
    private BottleReplyRepository bottleReplyRepository;

    /**
     * 创建并投放漂流瓶
     *
     * @param bottleDTO 漂流瓶DTO
     * @return 创建的漂流瓶DTO
     */
    public DriftBottleDTO createAndThrowBottle(DriftBottleDTO bottleDTO) {
        logger.info("用户 {} 创建漂流瓶: {}", bottleDTO.getSenderUsername(), bottleDTO.getContent());
        
        DriftBottle bottle = new DriftBottle(
            bottleDTO.getSenderUsername(),
            bottleDTO.getContent()
        );
        
        // 设置为漂流状态
        bottle.setStatus(DriftBottle.BottleStatus.FLOATING);
        bottle.setPassCount(0);
        
        DriftBottle savedBottle = driftBottleRepository.save(bottle);
        
        logger.info("漂流瓶创建成功，ID: {}", savedBottle.getId());
        return DriftBottleDTO.fromEntity(savedBottle);
    }

    /**
     * 捡到漂流瓶（随机获取）
     *
     * @param username 用户名
     * @return 捡到的漂流瓶DTO，如果没有则返回null
     */
    public DriftBottleDTO pickUpBottle(String username) {
        logger.info("用户 {} 尝试捡漂流瓶", username);
        
        // 随机获取一个漂流中的瓶子（排除自己发送的）
        Pageable pageable = PageRequest.of(0, 1);
        List<DriftBottle> bottles = driftBottleRepository.findRandomFloatingBottles(username, pageable);
        
        if (bottles.isEmpty()) {
            logger.info("用户 {} 没有找到可捡的漂流瓶", username);
            return null;
        }
        
        DriftBottle bottle = bottles.get(0);
        
        // 更新漂流瓶状态
        bottle.setCurrentHolder(username);
        bottle.setStatus(DriftBottle.BottleStatus.PICKED_UP);
        bottle.setPassCount(bottle.getPassCount() + 1);
        bottle.setLastUpdateTime(LocalDateTime.now());
        
        DriftBottle updatedBottle = driftBottleRepository.save(bottle);
        
        logger.info("用户 {} 捡到漂流瓶，ID: {}, 传递次数: {}", 
                   username, updatedBottle.getId(), updatedBottle.getPassCount());
        
        return DriftBottleDTO.fromEntity(updatedBottle);
    }

    /**
     * 丢弃漂流瓶
     *
     * @param bottleId 漂流瓶ID
     * @param username 用户名
     * @return 是否成功丢弃
     */
    public boolean discardBottle(Long bottleId, String username) {
        logger.info("用户 {} 尝试丢弃漂流瓶 {}", username, bottleId);
        
        Optional<DriftBottle> bottleOpt = driftBottleRepository.findById(bottleId);
        if (bottleOpt.isEmpty()) {
            logger.warn("漂流瓶不存在，ID: {}", bottleId);
            return false;
        }
        
        DriftBottle bottle = bottleOpt.get();
        
        // 验证是否是当前持有者
        if (!username.equals(bottle.getCurrentHolder())) {
            logger.warn("用户 {} 不是漂流瓶 {} 的当前持有者", username, bottleId);
            return false;
        }
        
        // 检查传递次数是否已达上限
        if (bottle.getPassCount() >= MAX_PASS_COUNT) {
            // 达到上限，设置为已完成
            bottle.setStatus(DriftBottle.BottleStatus.COMPLETED);
            logger.info("漂流瓶 {} 达到最大传递次数，设置为已完成", bottleId);
        } else {
            // 重新设置为漂流状态
            bottle.setStatus(DriftBottle.BottleStatus.FLOATING);
            bottle.setCurrentHolder(null);
        }
        
        bottle.setLastUpdateTime(LocalDateTime.now());
        driftBottleRepository.save(bottle);
        
        logger.info("用户 {} 成功丢弃漂流瓶 {}", username, bottleId);
        return true;
    }

    /**
     * 回复漂流瓶
     *
     * @param replyDTO 回复DTO
     * @return 回复DTO
     */
    public BottleReplyDTO replyToBottle(BottleReplyDTO replyDTO) {
        logger.info("用户 {} 回复漂流瓶 {}: {}", 
                   replyDTO.getReplierUsername(), replyDTO.getBottleId(), replyDTO.getReplyContent());
        
        Optional<DriftBottle> bottleOpt = driftBottleRepository.findById(replyDTO.getBottleId());
        if (bottleOpt.isEmpty()) {
            throw new IllegalArgumentException("漂流瓶不存在，ID: " + replyDTO.getBottleId());
        }
        
        DriftBottle bottle = bottleOpt.get();
        
        // 验证是否是当前持有者
        if (!replyDTO.getReplierUsername().equals(bottle.getCurrentHolder())) {
            throw new IllegalArgumentException("只有当前持有者才能回复漂流瓶");
        }
        
        // 创建回复
        BottleReply reply = new BottleReply(
            replyDTO.getReplierUsername(),
            replyDTO.getReplyContent(),
            bottle
        );
        
        BottleReply savedReply = bottleReplyRepository.save(reply);
        
        // 更新漂流瓶状态 - 回复后回到发送者手中
        bottle.setCurrentHolder(bottle.getSenderUsername());
        bottle.setStatus(DriftBottle.BottleStatus.REPLIED);
        bottle.setLastUpdateTime(LocalDateTime.now());
        
        driftBottleRepository.save(bottle);
        
        logger.info("回复创建成功，漂流瓶 {} 已回到发送者 {} 手中", 
                   bottle.getId(), bottle.getSenderUsername());
        
        return BottleReplyDTO.fromEntity(savedReply);
    }

    /**
     * 获取用户发送的漂流瓶列表
     *
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 漂流瓶分页列表
     */
    @Transactional(readOnly = true)
    public Page<DriftBottleDTO> getSentBottles(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<DriftBottle> bottles = driftBottleRepository.findBySenderUsername(username, pageable);
        
        return bottles.map(DriftBottleDTO::fromEntity);
    }

    /**
     * 获取用户接收的漂流瓶列表
     *
     * @param username 用户名
     * @param page 页码
     * @param size 每页大小
     * @return 漂流瓶分页列表
     */
    @Transactional(readOnly = true)
    public Page<DriftBottleDTO> getReceivedBottles(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastUpdateTime"));
        Page<DriftBottle> bottles = driftBottleRepository.findByCurrentHolder(username, pageable);
        
        return bottles.map(DriftBottleDTO::fromEntity);
    }

    /**
     * 获取漂流瓶详情（包含回复）
     *
     * @param bottleId 漂流瓶ID
     * @param username 用户名（用于权限验证）
     * @return 漂流瓶DTO
     */
    @Transactional(readOnly = true)
    public DriftBottleDTO getBottleDetail(Long bottleId, String username) {
        Optional<DriftBottle> bottleOpt = driftBottleRepository.findById(bottleId);
        if (bottleOpt.isEmpty()) {
            throw new IllegalArgumentException("漂流瓶不存在，ID: " + bottleId);
        }
        
        DriftBottle bottle = bottleOpt.get();
        
        // 验证权限：只有发送者或当前持有者可以查看详情
        if (!username.equals(bottle.getSenderUsername()) && 
            !username.equals(bottle.getCurrentHolder())) {
            throw new IllegalArgumentException("无权限查看此漂流瓶详情");
        }
        
        return DriftBottleDTO.fromEntity(bottle);
    }

    /**
     * 获取漂流瓶的回复列表
     *
     * @param bottleId 漂流瓶ID
     * @param page 页码
     * @param size 每页大小
     * @return 回复分页列表
     */
    @Transactional(readOnly = true)
    public Page<BottleReplyDTO> getBottleReplies(Long bottleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "replyTime"));
        Page<BottleReply> replies = bottleReplyRepository.findByDriftBottleId(bottleId, pageable);
        
        return replies.map(BottleReplyDTO::fromEntity);
    }

    /**
     * 获取用户统计信息
     *
     * @param username 用户名
     * @return 统计信息Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(String username) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("sentCount", driftBottleRepository.countBySenderUsername(username));
        stats.put("receivedCount", driftBottleRepository.countByCurrentHolder(username));
        stats.put("replyCount", bottleReplyRepository.countByReplierUsername(username));
        
        return stats;
    }

    /**
     * 清理过期的漂流瓶（超过30天未被捡起的）
     */
    @Transactional
    public void cleanupExpiredBottles() {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
        List<DriftBottle> expiredBottles = driftBottleRepository.findByCreateTimeBefore(expireTime);
        
        for (DriftBottle bottle : expiredBottles) {
            if (bottle.getStatus() == DriftBottle.BottleStatus.FLOATING) {
                bottle.setStatus(DriftBottle.BottleStatus.EXPIRED);
                driftBottleRepository.save(bottle);
                logger.info("漂流瓶 {} 已过期", bottle.getId());
            }
        }
    }
}