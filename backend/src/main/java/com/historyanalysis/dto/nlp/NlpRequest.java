/**
 * NLP服务请求DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:45:00
 */
package com.historyanalysis.dto.nlp;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * NLP服务请求数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NlpRequest {
    
    /**
     * 要分析的文本内容
     */
    @NotBlank(message = "文本内容不能为空")
    private String text;

    /**
     * 分析类型（可选）
     */
    private String type;

    /**
     * 返回结果数量（词频分析用）
     */
    private Integer topN;

    /**
     * 最小词长度（词频分析用）
     */
    private Integer minLength;

    /**
     * 最大句子数（摘要分析用）
     */
    private Integer maxSentences;

    /**
     * 构造函数 - 仅文本
     */
    public NlpRequest(String text) {
        this.text = text;
    }

    /**
     * 构造函数 - 文本和类型
     */
    public NlpRequest(String text, String type) {
        this.text = text;
        this.type = type;
    }
    
    // Setter methods
    public void setText(String text) {
        this.text = text;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setTopN(Integer topN) {
        this.topN = topN;
    }
    
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }
    
    public void setMaxSentences(Integer maxSentences) {
        this.maxSentences = maxSentences;
    }
}