/**
 * NLP服务配置类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:40:00
 */
package com.historyanalysis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

/**
 * NLP服务配置类
 * 配置与Python NLP微服务的连接参数
 */
@Configuration
@ConfigurationProperties(prefix = "nlp.service")
public class NlpServiceConfig {

    /**
     * NLP服务基础URL
     */
    private String baseUrl = "http://127.0.0.1:5001";

    /**
     * 连接超时时间（秒）
     */
    private int connectTimeout = 30;

    /**
     * 读取超时时间（秒）
     */
    private int readTimeout = 60;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval = 1000;

    /**
     * 创建RestTemplate Bean
     */
    @Bean("nlpRestTemplate")
    public RestTemplate nlpRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(connectTimeout))
                .setReadTimeout(Duration.ofSeconds(readTimeout))
                .build();
    }

    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }
}