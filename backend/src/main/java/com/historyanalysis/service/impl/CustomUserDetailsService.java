/**
 * 自定义用户详情服务实现类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 21:00:00
 * @description 专门用于Spring Security的用户详情加载服务，避免循环依赖
 */
package com.historyanalysis.service.impl;

import com.historyanalysis.entity.User;
import com.historyanalysis.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 自定义用户详情服务实现类
 * 
 * 专门用于Spring Security的用户认证，避免与UserServiceImpl的循环依赖
 */
@Service("customUserDetailsService")
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户详情（Spring Security接口实现）
     * 
     * @param username 用户名（邮箱）
     * @return 用户详情
     * @throws UsernameNotFoundException 当用户不存在时
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("加载用户详情: {}", username);
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            // 尝试通过邮箱查找
            userOpt = userRepository.findByEmail(username);
        }
        
        if (!userOpt.isPresent()) {
            logger.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        User user = userOpt.get();
        logger.debug("成功加载用户: {}, 角色: {}, 状态: {}", 
                    user.getUsername(), user.getRole(), user.getStatus());
        
        return user;
    }
}