package com.moon.cloud.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 邮件发送响应DTO
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Schema(description = "邮件发送响应")
public class EmailSendResponse {

    @Schema(description = "邮件记录ID")
    private Long emailRecordId;

    @Schema(description = "发送状态")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDescription;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "是否异步发送")
    private Boolean async;

    public EmailSendResponse() {}

    public EmailSendResponse(Long emailRecordId, Integer status, String statusDescription) {
        this.emailRecordId = emailRecordId;
        this.status = status;
        this.statusDescription = statusDescription;
    }

    public EmailSendResponse(Long emailRecordId, Integer status, String statusDescription, String errorMessage) {
        this.emailRecordId = emailRecordId;
        this.status = status;
        this.statusDescription = statusDescription;
        this.errorMessage = errorMessage;
    }

    // Getters and Setters
    public Long getEmailRecordId() {
        return emailRecordId;
    }

    public void setEmailRecordId(Long emailRecordId) {
        this.emailRecordId = emailRecordId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }
}