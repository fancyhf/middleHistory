/**
 * NLP服务响应DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:45:00
 */
package com.historyanalysis.dto.nlp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * NLP服务响应数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NlpResponse {

    /**
     * 请求是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 分析结果数据
     */
    private Map<String, Object> data;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 错误代码
     */
    @JsonProperty("error_code")
    private String errorCode;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 检查响应是否成功
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * 获取错误信息
     */
    public String getErrorMessage() {
        if (error != null) {
            return error;
        }
        if (!isSuccess() && message != null) {
            return message;
        }
        return "未知错误";
    }
    
    /**
     * 获取数据
     */
    public Map<String, Object> getData() {
        return data;
    }
}