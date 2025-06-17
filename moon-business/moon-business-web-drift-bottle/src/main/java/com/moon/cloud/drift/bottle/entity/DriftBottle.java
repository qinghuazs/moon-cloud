package com.moon.cloud.drift.bottle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 漂流瓶实体类
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Entity
@Table(name = "drift_bottle")
public class DriftBottle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发送者用户名
     */
    @NotBlank(message = "发送者用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Column(name = "sender_username", nullable = false, length = 50)
    private String senderUsername;

    /**
     * 纸条内容
     */
    @NotBlank(message = "纸条内容不能为空")
    @Size(max = 500, message = "纸条内容不能超过500个字符")
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 漂流瓶状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BottleStatus status;

    /**
     * 当前持有者用户名
     */
    @Column(name = "current_holder", length = 50)
    private String currentHolder;

    /**
     * 传递次数
     */
    @Column(name = "pass_count", nullable = false)
    private Integer passCount = 0;

    /**
     * 最后更新时间
     */
    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;

    /**
     * 回复内容列表
     */
    @OneToMany(mappedBy = "driftBottle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BottleReply> replies = new ArrayList<>();

    /**
     * 漂流瓶状态枚举
     */
    public enum BottleStatus {
        FLOATING,    // 漂流中
        PICKED_UP,   // 被捡起
        REPLIED,     // 已回复
        COMPLETED,   // 已完成
        DISCARDED,   // 已丢弃
        EXPIRED      // 已过期
    }

    // 构造函数
    public DriftBottle() {
        this.createTime = LocalDateTime.now();
        this.lastUpdateTime = LocalDateTime.now();
        this.status = BottleStatus.FLOATING;
    }

    public DriftBottle(String senderUsername, String content) {
        this();
        this.senderUsername = senderUsername;
        this.content = content;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public BottleStatus getStatus() {
        return status;
    }

    public void setStatus(BottleStatus status) {
        this.status = status;
        this.lastUpdateTime = LocalDateTime.now();
    }

    public String getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(String currentHolder) {
        this.currentHolder = currentHolder;
        this.lastUpdateTime = LocalDateTime.now();
    }

    public Integer getPassCount() {
        return passCount;
    }

    public void setPassCount(Integer passCount) {
        this.passCount = passCount;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public List<BottleReply> getReplies() {
        return replies;
    }

    public void setReplies(List<BottleReply> replies) {
        this.replies = replies;
    }

    /**
     * 增加传递次数
     */
    public void incrementPassCount() {
        this.passCount++;
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * 添加回复
     */
    public void addReply(BottleReply reply) {
        this.replies.add(reply);
        reply.setDriftBottle(this);
        this.lastUpdateTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "DriftBottle{" +
                "id=" + id +
                ", senderUsername='" + senderUsername + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                ", currentHolder='" + currentHolder + '\'' +
                ", passCount=" + passCount +
                ", lastUpdateTime=" + lastUpdateTime +
                '}';
    }
}