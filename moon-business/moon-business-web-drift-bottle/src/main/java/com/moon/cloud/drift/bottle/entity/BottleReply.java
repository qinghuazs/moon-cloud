package com.moon.cloud.drift.bottle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 漂流瓶回复实体类
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
@Entity
@Table(name = "bottle_reply")
public class BottleReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 回复者用户名
     */
    @NotBlank(message = "回复者用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Column(name = "replier_username", nullable = false, length = 50)
    private String replierUsername;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500个字符")
    @Column(name = "reply_content", nullable = false, length = 500)
    private String replyContent;

    /**
     * 回复时间
     */
    @Column(name = "reply_time", nullable = false)
    private LocalDateTime replyTime;

    /**
     * 关联的漂流瓶
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bottle_id", nullable = false)
    private DriftBottle driftBottle;

    // 构造函数
    public BottleReply() {
        this.replyTime = LocalDateTime.now();
    }

    public BottleReply(String replierUsername, String replyContent, DriftBottle driftBottle) {
        this();
        this.replierUsername = replierUsername;
        this.replyContent = replyContent;
        this.driftBottle = driftBottle;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReplierUsername() {
        return replierUsername;
    }

    public void setReplierUsername(String replierUsername) {
        this.replierUsername = replierUsername;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public LocalDateTime getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(LocalDateTime replyTime) {
        this.replyTime = replyTime;
    }

    public DriftBottle getDriftBottle() {
        return driftBottle;
    }

    public void setDriftBottle(DriftBottle driftBottle) {
        this.driftBottle = driftBottle;
    }

    @Override
    public String toString() {
        return "BottleReply{" +
                "id=" + id +
                ", replierUsername='" + replierUsername + '\'' +
                ", replyContent='" + replyContent + '\'' +
                ", replyTime=" + replyTime +
                ", bottleId=" + (driftBottle != null ? driftBottle.getId() : null) +
                '}';
    }
}