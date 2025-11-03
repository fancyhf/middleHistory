/**
 * 分析请求DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:30:00
 */
package com.historyanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 分析任务创建请求DTO
 * 
 * 用于接收前端发送的分析任务创建请求，包含：
 * - 分析类型
 * - 项目ID
 * - 文件ID列表
 * - 分析参数
 */
public class AnalysisRequest {

    /**
     * 分析类型
     */
    @NotBlank(message = "分析类型不能为空")
    @JsonProperty("analysisType")
    private String analysisType;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @JsonProperty("projectId")
    private Long projectId;

    /**
     * 文件ID列表
     */
    @JsonProperty("fileIds")
    private List<Long> fileIds;

    /**
     * 分析参数
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    // 默认构造函数
    public AnalysisRequest() {}

    // 带参数构造函数
    public AnalysisRequest(String analysisType, Long projectId, List<Long> fileIds, Map<String, Object> parameters) {
        this.analysisType = analysisType;
        this.projectId = projectId;
        this.fileIds = fileIds;
        this.parameters = parameters;
    }

    // Getter 和 Setter 方法
    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<Long> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * 获取描述信息
     */
    public String getDescription() {
        if (parameters != null && parameters.containsKey("description")) {
            return (String) parameters.get("description");
        }
        return analysisType + "分析任务";
    }

    /**
     * 获取最小词长度（词频分析参数）
     */
    public Integer getMinWordLength() {
        if (parameters != null && parameters.containsKey("minWordLength")) {
            Object value = parameters.get("minWordLength");
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 2; // 默认值
    }

    /**
     * 获取最大词数（词频分析参数）
     */
    public Integer getMaxWords() {
        if (parameters != null && parameters.containsKey("maxWords")) {
            Object value = parameters.get("maxWords");
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return 100; // 默认值
    }

    @Override
    public String toString() {
        return "AnalysisRequest{" +
                "analysisType='" + analysisType + '\'' +
                ", projectId=" + projectId +
                ", fileIds=" + fileIds +
                ", parameters=" + parameters +
                '}';
    }
}