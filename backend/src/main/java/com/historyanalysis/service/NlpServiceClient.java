/**
 * NLP服务客户端
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:50:00
 */
package com.historyanalysis.service;

import com.historyanalysis.config.NlpServiceConfig;
import com.historyanalysis.dto.nlp.NlpRequest;
import com.historyanalysis.dto.nlp.NlpResponse;
import com.historyanalysis.exception.NlpServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * NLP服务客户端
 * 负责与Python NLP微服务进行通信
 */
@Service
public class NlpServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(NlpServiceClient.class);

    private final RestTemplate restTemplate;
    private final NlpServiceConfig config;

    public NlpServiceClient(@Qualifier("nlpRestTemplate") RestTemplate restTemplate, 
                           NlpServiceConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            String url = config.getBaseUrl() + "/api/health";
            ResponseEntity<NlpResponse> response = restTemplate.getForEntity(url, NlpResponse.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("NLP服务健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 文本分析
     */
    public Map<String, Object> analyzeText(String text, String analysisType) {
        NlpRequest request = new NlpRequest(text, analysisType);
        return callNlpService("/api/analyze/text", request);
    }

    /**
     * 词频分析
     */
    public Map<String, Object> analyzeWordFrequency(String text, Integer topN, Integer minLength) {
        NlpRequest request = new NlpRequest();
        request.setText(text);
        request.setTopN(topN != null ? topN : 50);
        request.setMinLength(minLength != null ? minLength : 2);
        
        return callNlpService("/api/analyze/word-frequency", request);
    }

    /**
     * 时间轴分析
     */
    public Map<String, Object> analyzeTimeline(String text) {
        NlpRequest request = new NlpRequest(text);
        return callNlpService("/api/analyze/timeline", request);
    }

    /**
     * 地理位置分析
     */
    public Map<String, Object> analyzeGeographic(String text) {
        NlpRequest request = new NlpRequest(text);
        return callNlpService("/api/analyze/geographic", request);
    }

    /**
     * 文本摘要分析
     */
    public Map<String, Object> analyzeSummary(String text, String summaryType, Integer maxSentences) {
        NlpRequest request = new NlpRequest();
        request.setText(text);
        request.setType(summaryType != null ? summaryType : "comprehensive");
        request.setMaxSentences(maxSentences != null ? maxSentences : 5);
        
        return callNlpService("/api/analyze/summary", request);
    }

    /**
     * 综合分析
     */
    public Map<String, Object> analyzeComprehensive(String text) {
        NlpRequest request = new NlpRequest(text);
        return callNlpService("/api/analyze/comprehensive", request);
    }

    /**
     * 多维度分析
     */
    public Map<String, Object> analyzeMultidimensional(String text) {
        NlpRequest request = new NlpRequest(text);
        return callNlpService("/api/analyze/multidimensional", request);
    }

    /**
     * 调用NLP服务的通用方法
     */
    private Map<String, Object> callNlpService(String endpoint, NlpRequest request) {
        String url = config.getBaseUrl() + endpoint;
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<NlpRequest> httpEntity = new HttpEntity<>(request, headers);
        
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= config.getMaxRetries()) {
            try {
                logger.info("调用NLP服务: {} (第{}次尝试)", endpoint, retries + 1);
                
                ResponseEntity<NlpResponse> response = restTemplate.postForEntity(
                    url, httpEntity, NlpResponse.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    NlpResponse nlpResponse = response.getBody();
                    
                    if (nlpResponse != null && nlpResponse.isSuccess()) {
                        logger.info("NLP服务调用成功: {}", endpoint);
                        return nlpResponse.getData();
                    } else {
                        String errorMsg = nlpResponse != null ? nlpResponse.getErrorMessage() : "未知错误";
                        throw new NlpServiceException("NLP服务返回错误: " + errorMsg);
                    }
                } else {
                    throw new NlpServiceException("NLP服务返回错误状态码: " + response.getStatusCode());
                }
                
            } catch (RestClientException e) {
                lastException = e;
                logger.warn("NLP服务调用失败 (第{}次尝试): {}", retries + 1, e.getMessage());
                
                if (retries < config.getMaxRetries()) {
                    try {
                        Thread.sleep(config.getRetryInterval());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new NlpServiceException("调用被中断", ie);
                    }
                }
                retries++;
            }
        }
        
        // 所有重试都失败了
        String errorMsg = String.format("NLP服务调用失败，已重试%d次", config.getMaxRetries());
        logger.error(errorMsg, lastException);
        throw new NlpServiceException(errorMsg, lastException);
    }
}