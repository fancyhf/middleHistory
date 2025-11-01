/**
 * 认证请求DTO
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */
package com.historyanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    /**
     * 是否记住我
     */
    private Boolean rememberMe = false;

    // 手动添加getter方法
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}