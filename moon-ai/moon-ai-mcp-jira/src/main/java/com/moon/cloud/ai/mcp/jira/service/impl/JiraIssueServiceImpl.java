package com.moon.cloud.ai.mcp.jira.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moon.cloud.ai.mcp.jira.dto.JiraIssueDTO;
import com.moon.cloud.ai.mcp.jira.dto.JiraQueryRequest;
import com.moon.cloud.ai.mcp.jira.entity.JiraIssue;
import com.moon.cloud.ai.mcp.jira.mapper.JiraIssueMapper;
import com.moon.cloud.ai.mcp.jira.service.JiraIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jira问题服务实现类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JiraIssueServiceImpl implements JiraIssueService {

    private final JiraIssueMapper jiraIssueMapper;

    @Override
    public List<JiraIssueDTO> searchIssues(JiraQueryRequest request) {
        QueryWrapper<JiraIssue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);

        // 服务名称过滤
        if (StringUtils.isNotBlank(request.getServiceName())) {
            queryWrapper.eq("service_name", request.getServiceName());
        }

        // 关键词搜索
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("summary", request.getKeyword())
                    .or().like("description", request.getKeyword())
                    .or().like("labels", request.getKeyword())
            );
        }

        // 问题类型过滤
        if (StringUtils.isNotBlank(request.getIssueType())) {
            queryWrapper.eq("issue_type", request.getIssueType());
        }

        // 状态过滤
        if (StringUtils.isNotBlank(request.getStatus())) {
            queryWrapper.eq("status", request.getStatus());
        }

        // 优先级过滤
        if (StringUtils.isNotBlank(request.getPriority())) {
            queryWrapper.eq("priority", request.getPriority());
        }

        // 只查询已解决的问题
        if (Boolean.TRUE.equals(request.getOnlyResolved())) {
            queryWrapper.isNotNull("resolution")
                    .ne("resolution", "");
        }

        // 只查询有补丁的问题
        if (Boolean.TRUE.equals(request.getOnlyWithPatch())) {
            queryWrapper.isNotNull("patch_info")
                    .ne("patch_info", "");
        }

        queryWrapper.orderByDesc("created_time");

        List<JiraIssue> issues = jiraIssueMapper.selectList(queryWrapper);
        return convertToDTO(issues);
    }

    @Override
    public List<JiraIssueDTO> getIssuesByService(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return Collections.emptyList();
        }
        List<JiraIssue> issues = jiraIssueMapper.findByServiceName(serviceName);
        return convertToDTO(issues);
    }

    @Override
    public List<JiraIssueDTO> searchSimilarIssues(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }
        List<JiraIssue> issues = jiraIssueMapper.searchByKeyword(keyword);
        return convertToDTO(issues);
    }

    @Override
    public List<JiraIssueDTO> searchIssuesByServiceAndKeyword(String serviceName, String keyword) {
        if (StringUtils.isBlank(serviceName) || StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }
        List<JiraIssue> issues = jiraIssueMapper.searchByServiceAndKeyword(serviceName, keyword);
        return convertToDTO(issues);
    }

    @Override
    public List<JiraIssueDTO> getResolvedIssues() {
        List<JiraIssue> issues = jiraIssueMapper.findResolvedIssues();
        return convertToDTO(issues);
    }

    @Override
    public List<JiraIssueDTO> getIssuesWithPatch() {
        List<JiraIssue> issues = jiraIssueMapper.findIssuesWithPatch();
        return convertToDTO(issues);
    }

    @Override
    public JiraIssueDTO getIssueById(Long id) {
        if (id == null) {
            return null;
        }
        JiraIssue issue = jiraIssueMapper.selectById(id);
        return issue != null ? convertToDTO(issue) : null;
    }

    @Override
    public JiraIssueDTO getIssueByKey(String issueKey) {
        if (StringUtils.isBlank(issueKey)) {
            return null;
        }
        QueryWrapper<JiraIssue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("issue_key", issueKey)
                .eq("deleted", 0);
        JiraIssue issue = jiraIssueMapper.selectOne(queryWrapper);
        return issue != null ? convertToDTO(issue) : null;
    }

    @Override
    public boolean saveOrUpdate(JiraIssue jiraIssue) {
        try {
            if (jiraIssue.getId() == null) {
                return jiraIssueMapper.insert(jiraIssue) > 0;
            } else {
                return jiraIssueMapper.updateById(jiraIssue) > 0;
            }
        } catch (Exception e) {
            log.error("保存或更新Jira问题失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteIssue(Long id) {
        try {
            JiraIssue issue = new JiraIssue();
            issue.setId(id);
            issue.setDeleted(1);
            return jiraIssueMapper.updateById(issue) > 0;
        } catch (Exception e) {
            log.error("删除Jira问题失败", e);
            return false;
        }
    }

    @Override
    public List<JiraIssueDTO> matchSimilarProblems(String problemDescription, String serviceName) {
        if (StringUtils.isBlank(problemDescription)) {
            return Collections.emptyList();
        }

        // 提取关键词进行匹配
        String[] keywords = extractKeywords(problemDescription);
        
        QueryWrapper<JiraIssue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        
        // 如果指定了服务名称，则限制在该服务范围内
        if (StringUtils.isNotBlank(serviceName)) {
            queryWrapper.eq("service_name", serviceName);
        }
        
        // 构建关键词匹配条件
        if (keywords.length > 0) {
            queryWrapper.and(wrapper -> {
                for (int i = 0; i < keywords.length; i++) {
                    if (i == 0) {
                        wrapper.like("summary", keywords[i])
                                .or().like("description", keywords[i])
                                .or().like("labels", keywords[i]);
                    } else {
                        wrapper.or().like("summary", keywords[i])
                                .or().like("description", keywords[i])
                                .or().like("labels", keywords[i]);
                    }
                }
            });
        }
        
        // 优先返回已解决的问题
        queryWrapper.orderByDesc("CASE WHEN resolution IS NOT NULL AND resolution != '' THEN 1 ELSE 0 END")
                .orderByDesc("created_time");
        
        List<JiraIssue> issues = jiraIssueMapper.selectList(queryWrapper);
        return convertToDTO(issues);
    }

    /**
     * 提取关键词
     */
    private String[] extractKeywords(String text) {
        if (StringUtils.isBlank(text)) {
            return new String[0];
        }
        
        // 简单的关键词提取：去除常见停用词，按空格分割
        String[] stopWords = {"的", "是", "在", "有", "和", "与", "或", "但", "如果", "因为", "所以", 
                              "the", "is", "in", "at", "and", "or", "but", "if", "because", "so"};
        
        String cleanText = text.toLowerCase()
                .replaceAll("[^\\w\\s\\u4e00-\\u9fa5]", " ") // 保留中英文字符和空格
                .replaceAll("\\s+", " ") // 多个空格合并为一个
                .trim();
        
        return Arrays.stream(cleanText.split(" "))
                .filter(word -> word.length() > 1) // 过滤单字符
                .filter(word -> !Arrays.asList(stopWords).contains(word)) // 过滤停用词
                .distinct()
                .toArray(String[]::new);
    }

    /**
     * 转换为DTO
     */
    private List<JiraIssueDTO> convertToDTO(List<JiraIssue> issues) {
        return issues.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private JiraIssueDTO convertToDTO(JiraIssue issue) {
        JiraIssueDTO dto = new JiraIssueDTO();
        dto.setId(issue.getId())
                .setIssueKey(issue.getIssueKey())
                .setSummary(issue.getSummary())
                .setDescription(issue.getDescription())
                .setIssueType(issue.getIssueType())
                .setStatus(issue.getStatus())
                .setPriority(issue.getPriority())
                .setServiceName(issue.getServiceName())
                .setComponent(issue.getComponent())
                .setVersion(issue.getVersion())
                .setFixVersion(issue.getFixVersion())
                .setReporter(issue.getReporter())
                .setAssignee(issue.getAssignee())
                .setResolution(issue.getResolution())
                .setResolutionDescription(issue.getResolutionDescription())
                .setPatchInfo(issue.getPatchInfo())
                .setPatchUrl(issue.getPatchUrl())
                .setCreatedTime(issue.getCreatedTime())
                .setUpdatedTime(issue.getUpdatedTime())
                .setResolvedTime(issue.getResolvedTime());

        // 处理标签
        if (StringUtils.isNotBlank(issue.getLabels())) {
            List<String> labelList = Arrays.stream(issue.getLabels().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            dto.setLabelList(labelList);
        }

        return dto;
    }
}