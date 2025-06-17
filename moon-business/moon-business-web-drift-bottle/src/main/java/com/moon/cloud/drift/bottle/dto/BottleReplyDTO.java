package com.moon.cloud.drift.bottle.dto;

import com.moon.cloud.drift.bottle.entity.BottleReply;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 漂流瓶回复数据传输对象
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public class BottleReplyDTO {

    private Long id;

    @NotBlank(message = "回复者用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String replierUsername;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容不能超过500个字符")
    private String replyContent;

    private LocalDateTime replyTime;

    @NotNull(message = "漂流瓶ID不能为空")
    private Long bottleId;

    // 构造函数
    public BottleReplyDTO() {}

    public BottleReplyDTO(String replierUsername, String replyContent, Long bottleId) {
        this.replierUsername = replierUsername;
        this.replyContent = replyContent;
        this.bottleId = bottleId;
    }

    /**
     * 从实体转换为DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    public static BottleReplyDTO fromEntity(BottleReply entity) {
        if (entity == null) {
            return null;
        }

        BottleReplyDTO dto = new BottleReplyDTO();
        dto.setId(entity.getId());
        dto.setReplierUsername(entity.getReplierUsername());
        dto.setReplyContent(entity.getReplyContent());
        dto.setReplyTime(entity.getReplyTime());
        
        if (entity.getDriftBottle() != null) {
            dto.setBottleId(entity.getDriftBottle().getId());
        }
        
        return dto;
    }

    /**
     * 转换为实体对象（不包含关联的漂流瓶）
     *
     * @return 实体对象
     */
    public BottleReply toEntity() {
        BottleReply entity = new BottleReply();
        entity.setId(this.id);
        entity.setReplierUsername(this.replierUsername);
        entity.setReplyContent(this.replyContent);
        entity.setReplyTime(this.replyTime);
        
        return entity;
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

    public Long getBottleId() {
        return bottleId;
    }

    public void setBottleId(Long bottleId) {
        this.bottleId = bottleId;
    }

    @Override
    public String toString() {
        return "BottleReplyDTO{" +
                "id=" + id +
                ", replierUsername='" + replierUsername + '\'' +
                ", replyContent='" + replyContent + '\'' +
                ", replyTime=" + replyTime +
                ", bottleId=" + bottleId +
                '}';
    }
}