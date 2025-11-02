/**
 * 历史数据统计分析工具后端主服务启动类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot应用程序启动类
 * 
 * 功能特性:
 * - 启用缓存支持
 * - 启用异步任务处理
 * - 启用定时任务调度
 * - 启用事务管理
 * - 启用JPA审计功能
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableJpaAuditing
public class HistoryAnalysisApplication {

    /**
     * 应用程序入口点
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(HistoryAnalysisApplication.class, args);
    }
}