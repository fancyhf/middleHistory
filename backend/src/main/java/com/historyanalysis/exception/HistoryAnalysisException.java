/**
 * 历史分析异常类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 19:35:00
 * @description 历史数据分析过程中的业务异常
 */
package com.historyanalysis.exception;

/**
 * 历史分析异常
 * 用于处理历史数据分析过程中的业务逻辑异常
 */
public class HistoryAnalysisException extends RuntimeException {

    /**
     * 错误代码
     */
    private String code;

    /**
     * 构造函数
     */
    public HistoryAnalysisException(String message) {
        super(message);
    }

    /**
     * 构造函数
     */
    public HistoryAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     */
    public HistoryAnalysisException(String message, String code) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数
     */
    public HistoryAnalysisException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 获取错误代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置错误代码
     */
    public void setCode(String code) {
        this.code = code;
    }
}