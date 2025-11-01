/**
 * 简单启动测试
 * @author AI Agent
 * @version 1.0.0
 * @created 2025-10-30
 */
package com.historyanalysis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SimpleStartupTest {

    @Test
    public void contextLoads() {
        // 这个测试只是验证Spring上下文能否正常加载
    }
}