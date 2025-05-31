package com.moon.cloud.ai.mcp.jira.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moon.cloud.ai.mcp.jira.entity.JiraIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Jira问题 Mapper 接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface JiraIssueMapper extends BaseMapper<JiraIssue> {

    /**
     * 根据服务名称查询问题列表
     *
     * @param serviceName 服务名称
     * @return 问题列表
     */
    @Select("SELECT * FROM jira_issue WHERE service_name = #{serviceName} AND deleted = 0 ORDER BY created_time DESC")
    List<JiraIssue> findByServiceName(@Param("serviceName") String serviceName);

    /**
     * 根据关键词搜索问题（在标题、描述、标签中搜索）
     *
     * @param keyword 关键词
     * @return 问题列表
     */
    @Select("SELECT * FROM jira_issue WHERE (summary LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR labels LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY created_time DESC")
    List<JiraIssue> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据服务名称和关键词搜索问题
     *
     * @param serviceName 服务名称
     * @param keyword 关键词
     * @return 问题列表
     */
    @Select("SELECT * FROM jira_issue WHERE service_name = #{serviceName} " +
            "AND (summary LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%') " +
            "OR labels LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY created_time DESC")
    List<JiraIssue> searchByServiceAndKeyword(@Param("serviceName") String serviceName, 
                                              @Param("keyword") String keyword);

    /**
     * 根据问题类型查询
     *
     * @param issueType 问题类型
     * @return 问题列表
     */
    @Select("SELECT * FROM jira_issue WHERE issue_type = #{issueType} AND deleted = 0 ORDER BY created_time DESC")
    List<JiraIssue> findByIssueType(@Param("issueType") String issueType);

    /**
     * 根据状态查询
     *
     * @param status 状态
     * @return 问题列表
     */
    @Select("SELECT * FROM jira_issue WHERE status = #{status} AND deleted = 0 ORDER BY created_time DESC")
    List<JiraIssue> findByStatus(@Param("status") String status);

    /**
     * 查询已解决的问题（有解决方案的）
     *
     * @return 问题列表
     */
    @Select("SELECT * FROM jira_issue WHERE resolution IS NOT NULL AND resolution != '' " +
            "AND deleted = 0 ORDER BY resolved_time DESC")
    List<JiraIssue> findResolvedIssues();

    /**
     * 查询有补丁的问题
     *
     * @return 问题列表
     */
    @Select("SELECT * FROM jira_issue WHERE patch_info IS NOT NULL AND patch_info != '' " +
            "AND deleted = 0 ORDER BY created_time DESC")
    List<JiraIssue> findIssuesWithPatch();
}