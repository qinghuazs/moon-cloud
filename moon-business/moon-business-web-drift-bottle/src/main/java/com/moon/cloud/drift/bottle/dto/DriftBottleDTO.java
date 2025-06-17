package com.moon.cloud.drift.bottle.dto;

import com.moon.cloud.drift.bottle.entity.DriftBottle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 漂流瓶数据传输对象
 *
 * @author Moon Cloud
 * @since 1.0.0
 */
public class DriftBottleDTO {

    private Long id;

    @NotBlank(message = "发送者用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String senderUsername;

    @NotBlank(message = "漂流瓶内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000个字符")
    private String content;

    private LocalDateTime createTime;
    private String status;
    private String currentHolder;
    private Integer passCount;
    private LocalDateTime lastUpdateTime;
    private List<BottleReplyDTO> replies;

    // 构造函数
    public DriftBottleDTO() {}

    public DriftBottleDTO(String senderUsername, String content) {
        this.senderUsername = senderUsername;
        this.content = content;
    }

    /**
     * 从实体转换为DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    public static DriftBottleDTO fromEntity(DriftBottle entity) {
        if (entity == null) {
            return null;
        }

        DriftBottleDTO dto = new DriftBottleDTO();
        dto.setId(entity.getId());
        dto.setSenderUsername(entity.getSenderUsername());
        dto.setContent(entity.getContent());
        dto.setCreateTime(entity.getCreateTime());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setCurrentHolder(entity.getCurrentHolder());
        dto.setPassCount(entity.getPassCount());
        dto.setLastUpdateTime(entity.getLastUpdateTime());
        
        if (entity.getReplies() != null) {
            dto.setReplies(entity.getReplies().stream()
                    .map(BottleReplyDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    /**
     * 转换为实体对象
     *
     * @return 实体对象
     */
    public DriftBottle toEntity() {
        DriftBottle entity = new DriftBottle();
        entity.setId(this.id);
        entity.setSenderUsername(this.senderUsername);
        entity.setContent(this.content);
        entity.setCreateTime(this.createTime);
        
        if (this.status != null) {
            entity.setStatus(DriftBottle.BottleStatus.valueOf(this.status));
        }
        
        entity.setCurrentHolder(this.currentHolder);
        entity.setPassCount(this.passCount);
        entity.setLastUpdateTime(this.lastUpdateTime);
        
        return entity;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(String currentHolder) {
        this.currentHolder = currentHolder;
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

    public List<BottleReplyDTO> getReplies() {
        return replies;
    }

    public void setReplies(List<BottleReplyDTO> replies) {
        this.replies = replies;
    }

    @Override
    public String toString() {
        return "DriftBottleDTO{" +
                "id=" + id +
                ", senderUsername='" + senderUsername + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", status='" + status + '\'' +
                ", currentHolder='" + currentHolder + '\'' +
                ", passCount=" + passCount +
                ", lastUpdateTime=" + lastUpdateTime +
                ", repliesCount=" + (replies != null ? replies.size() : 0) +
                '}';
    }
}