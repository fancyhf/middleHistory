/**
 * 错误响应DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 19:40:00
 * @description 统一的错误响应格式
 */
package com.historyanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 错误响应DTO
 * 
 * 提供统一的错误响应格式，
 * 包含错误信息、错误代码、时间戳等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * 请求是否成功
     */
    @Builder.Default
    private Boolean success = false;

    /**
     * 错误类型
     */
    private String error;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 错误代码
     */
    private String code;

    /**
     * 错误详情
     */
    private Map<String, Object> details;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 创建简单错误响应
     */
    public static ErrorResponse of(String error, String message) {
        return ErrorResponse.builder()
                .success(false)
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建带代码的错误响应
     */
    public static ErrorResponse of(String error, String message, String code) {
        return ErrorResponse.builder()
                .success(false)
                .error(error)
                .message(message)
                .code(code)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建带详情的错误响应
     */
    public static ErrorResponse of(String error, String message, Map<String, Object> details) {
        return ErrorResponse.builder()
                .success(false)
                .error(error)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
}