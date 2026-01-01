package com.evaluate.service;

import java.util.Map;

/**
 * Word模板处理服务接口
 *
 * @author System
 * @since 2025-12-18
 */
public interface IWordTemplateService {

    /**
     * 基于模板生成Word报告
     *
     * @param variables 要替换的变量映射
     * @return 生成的Word文件字节数组
     */
    byte[] generateReportFromTemplate(Map<String, Object> variables);

    /**
     * 验证模板文件是否存在
     *
     * @return 模板文件是否存在
     */
    boolean templateExists();

    /**
     * 获取模板文件中的所有变量
     *
     * @return 变量列表
     */
    java.util.List<String> getTemplateVariables();

    /**
     * 调试模板文件内容 - 返回原始段落文本
     *
     * @return 包含模板文件内容的调试信息
     */
    Map<String, Object> debugTemplateContent();

    /**
     * 获取Word模板的实际内容用于预览
     *
     * @return 包含Word实际内容的预览数据
     */
    Map<String, Object> getRealTemplateContent();

    /**
     * 获取模板文件的字节数组
     *
     * @return 模板文件的字节数组
     */
    byte[] getTemplateFile();

    /**
     * 保存编辑后的Word文档内容
     *
     * @param htmlContent 编辑后的HTML内容
     * @return 保存结果
     */
    boolean saveEditedContent(String htmlContent);

    /**
     * 将HTML内容转换为Word文档字节数组
     *
     * @param htmlContent HTML内容
     * @return Word文档字节数组
     */
    byte[] convertHtmlToWord(String htmlContent);

    /**
     * 基于模板生成Word报告并替换专题图图片
     *
     * @param variables 要替换的变量映射
     * @param thematicMapImagePath 专题图图片路径（可为null）
     * @return 生成的Word文件字节数组
     */
    byte[] generateReportFromTemplate(Map<String, Object> variables, String thematicMapImagePath);

    /**
     * 基于模板生成Word报告并替换多张专题图图片
     *
     * @param variables 要替换的变量映射
     * @param thematicMapImages 专题图图片路径Map (级别 -> 路径)，可为null
     * @return 生成的Word文件字节数组
     */
    byte[] generateReportFromTemplate(Map<String, Object> variables, Map<String, String> thematicMapImages);
}