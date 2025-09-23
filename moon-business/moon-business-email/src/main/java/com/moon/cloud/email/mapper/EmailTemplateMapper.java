package com.moon.cloud.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moon.cloud.email.entity.EmailTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 邮件模板Mapper接口
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Mapper
public interface EmailTemplateMapper extends BaseMapper<EmailTemplate> {

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 邮件模板
     */
    @Select("SELECT * FROM email_template WHERE template_code = #{templateCode} AND status = 1")
    EmailTemplate selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据模板类型查询模板列表
     *
     * @param templateType 模板类型
     * @param status 状态
     * @return 模板列表
     */
    List<EmailTemplate> selectByTemplateType(@Param("templateType") String templateType,
                                           @Param("status") Integer status);

    /**
     * 分页查询模板
     *
     * @param page 分页参数
     * @param templateType 模板类型
     * @param status 状态
     * @param keyword 关键词
     * @return 分页结果
     */
    IPage<EmailTemplate> selectPageList(Page<EmailTemplate> page,
                                       @Param("templateType") String templateType,
                                       @Param("status") Integer status,
                                       @Param("keyword") String keyword);

    /**
     * 更新模板状态
     *
     * @param id 模板ID
     * @param status 状态
     * @return 更新行数
     */
    @Update("UPDATE email_template SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 设置默认模板
     *
     * @param id 模板ID
     * @param templateType 模板类型
     * @return 更新行数
     */
    int setDefaultTemplate(@Param("id") Long id, @Param("templateType") String templateType);

    /**
     * 获取默认模板
     *
     * @param templateType 模板类型
     * @return 默认模板
     */
    @Select("SELECT * FROM email_template WHERE template_type = #{templateType} AND is_default = 1 AND status = 1 LIMIT 1")
    EmailTemplate selectDefaultTemplate(@Param("templateType") String templateType);

    /**
     * 检查模板编码是否存在
     *
     * @param templateCode 模板编码
     * @param excludeId 排除的ID
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM email_template WHERE template_code = #{templateCode}" +
            "<if test='excludeId != null'> AND id != #{excludeId}</if>" +
            "</script>")
    int countByTemplateCode(@Param("templateCode") String templateCode, @Param("excludeId") Long excludeId);

    /**
     * 统计模板数量
     *
     * @param templateType 模板类型
     * @param status 状态
     * @return 数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM email_template WHERE 1=1" +
            "<if test='templateType != null and templateType != \"\"'> AND template_type = #{templateType}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "</script>")
    Long countTemplates(@Param("templateType") String templateType, @Param("status") Integer status);
}