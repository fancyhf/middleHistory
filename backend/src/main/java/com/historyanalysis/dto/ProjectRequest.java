/**
 * 项目请求DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:05:00
 */
package com.historyanalysis.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 项目创建和更新请求DTO
 */
@Data
public class ProjectRequest {

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    @Size(min = 1, max = 100, message = "项目名称长度必须在1-100个字符之间")
    private String name;

    /**
     * 项目描述
     */
    @Size(max = 500, message = "项目描述不能超过500个字符")
    private String description;

    /**
     * 项目标签（逗号分隔）
     */
    @Size(max = 200, message = "项目标签不能超过200个字符")
    private String tags;

    /**
     * 项目设置（JSON格式）
     */
    private String settings;

    /**
     * 获取项目名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取项目描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取项目标签
     */
    public String getTags() {
        return tags;
    }

    /**
     * 获取项目设置
     */
    public String getSettings() {
        return settings;
    }
}