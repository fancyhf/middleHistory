/**
 * Web配置类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 17:00:00
 * @description 配置静态资源映射和Web相关设置
 */
package com.historyanalysis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * 配置静态资源处理和视图控制器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理
     * 只处理静态资源，不拦截API请求
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，只处理静态文件
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // 缓存1小时
        
        // 配置上传文件访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
    }

    /**
     * 配置视图控制器
     * 处理根路径访问，重定向到index.html
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径重定向到index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}