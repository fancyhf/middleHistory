/**
 * 全局异常处理器
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 19:30:00
 * @description 统一处理应用程序中的异常，提供标准化的错误响应
 */
package com.historyanalysis.exception;

import com.historyanalysis.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * 统一处理应用程序中的各种异常，
 * 提供标准化的错误响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理历史分析异常
     */
    @ExceptionHandler(HistoryAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAnalysisException(HistoryAnalysisException e, HttpServletRequest request) {
        logger.error("历史分析异常: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("ANALYSIS_ERROR")
                .message(e.getMessage())
                .code(e.getCode())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理NLP服务异常
     */
    @ExceptionHandler(NlpServiceException.class)
    public ResponseEntity<ErrorResponse> handleNlpServiceException(NlpServiceException e, HttpServletRequest request) {
        logger.error("NLP服务异常: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("NLP_SERVICE_ERROR")
                .message("NLP服务暂时不可用，请稍后重试")
                .code(e.getErrorCode())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception e, HttpServletRequest request) {
        logger.warn("认证异常: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("AUTHENTICATION_ERROR")
                .message("用户名或密码错误")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.warn("访问拒绝异常: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("ACCESS_DENIED")
                .message("访问被拒绝，权限不足")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.warn("参数验证异常: {}", e.getMessage());
        
        Map<String, Object> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("VALIDATION_ERROR")
                .message("参数验证失败")
                .details(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e, HttpServletRequest request) {
        logger.warn("绑定异常: {}", e.getMessage());
        
        Map<String, Object> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("BIND_ERROR")
                .message("数据绑定失败")
                .details(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        logger.warn("约束违反异常: {}", e.getMessage());
        
        String errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("CONSTRAINT_VIOLATION")
                .message("数据约束违反: " + errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        logger.warn("文件上传大小超限: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("FILE_SIZE_EXCEEDED")
                .message("上传文件大小超过限制")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("非法参数异常: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("ILLEGAL_ARGUMENT")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("RUNTIME_ERROR")
                .message("系统运行时错误，请稍后重试")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
        logger.error("未处理的异常: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error("INTERNAL_ERROR")
                .message("系统内部错误，请联系管理员")
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}