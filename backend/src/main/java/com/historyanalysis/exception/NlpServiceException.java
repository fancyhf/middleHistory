/**
 * NLP服务异常类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:55:00
 */
package com.historyanalysis.exception;

/**
 * NLP服务异常
 * 当与Python NLP微服务通信出现问题时抛出
 */
public class NlpServiceException extends RuntimeException {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 构造函数
     */
    public NlpServiceException(String message) {
        super(message);
    }

    /**
     * 构造函数
     */
    public NlpServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     */
    public NlpServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     */
    public NlpServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 设置错误代码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}