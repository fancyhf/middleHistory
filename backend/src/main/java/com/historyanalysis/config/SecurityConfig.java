/**
 * Spring Security安全配置类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.config;

import com.historyanalysis.security.JwtAuthenticationEntryPoint;
import com.historyanalysis.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security安全配置类
 * 配置JWT认证、CORS、权限控制等安全相关设置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器Bean
     * 使用BCrypt算法进行密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * DAO认证提供者Bean
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * CORS配置源Bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173"
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许携带凭证
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", 
            "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（使用JWT时不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置异常处理
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // 配置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 公开端点 - 不需要认证
                .requestMatchers(
                    "/",                      // 根路径欢迎页面
                    "/api",                   // API根路径信息
                    "/api/auth/**",           // 认证相关接口
                    "/api/public/**",         // 公开接口
                    "/actuator/health",       // 健康检查端点
                    "/actuator/info",         // 应用信息端点
                    "/actuator/**",           // 所有actuator端点（临时开放用于调试）
                    "/swagger-ui/**",         // Swagger UI
                    "/api/swagger-ui/**",     // Swagger UI（API路径）
                    "/v3/api-docs/**",        // OpenAPI文档
                    "/api/v3/api-docs/**",    // OpenAPI文档（API路径）
                    "/error"                  // 错误页面
                ).permitAll()
                
                // 管理员专用接口
                .requestMatchers(
                    "/admin/**",          // 管理员接口
                    "/actuator/metrics",  // 监控指标
                    "/actuator/prometheus", // Prometheus指标
                    "/actuator/env",      // 环境信息
                    "/actuator/configprops", // 配置属性
                    "/actuator/beans",    // Bean信息
                    "/actuator/mappings"  // 映射信息
                ).hasRole("ADMIN")
                
                // 用户接口 - 需要认证
                .requestMatchers(
                    "/users/**",          // 用户管理
                    "/projects/**",       // 项目管理
                    "/files/**",          // 文件管理
                    "/analysis/**"        // 分析管理
                ).authenticated()
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            
            // 配置认证提供者
            .authenticationProvider(authenticationProvider())
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}