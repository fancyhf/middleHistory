# AI Agent 编程规范

## 1. 代码生成质量标准

### 1.1 多语言代码生成规范

#### Java 代码规范
- **MUST**: 使用PascalCase命名类名，如 `HistoryDataAnalyzer`
- **MUST**: 使用camelCase命名方法和变量，如 `analyzeWordFrequency()`
- **MUST**: 每个类必须包含完整的JavaDoc注释
- **MUST**: 使用Spring Boot注解规范：`@Service`, `@Controller`, `@Repository`
- **MUST**: 异常处理使用try-catch-finally结构
- **FORBIDDEN**: 使用原始类型，优先使用包装类型

```java
/**
 * 历史数据分析服务
 * @author AI Agent
 * @version 1.0.0
 * @since [使用MCP time组件获取的时间]
 */
@Service
public class HistoryDataAnalyzer {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoryDataAnalyzer.class);
    
    /**
     * 分析文本词频
     * @param text 输入文本
     * @return 词频统计结果
     */
    public Map<String, Integer> analyzeWordFrequency(String text) {
        // 实现代码
    }
}
```

#### Python 代码规范
- **MUST**: 使用snake_case命名函数和变量，如 `analyze_text_sentiment()`
- **MUST**: 使用PascalCase命名类，如 `TextAnalyzer`
- **MUST**: 每个函数必须包含docstring文档
- **MUST**: 使用类型提示（Type Hints）
- **MUST**: 遵循PEP 8编码规范
- **MUST**: 使用Flask蓝图组织路由

```python
"""
文本分析模块
Author: AI Agent
Version: 1.0.0
Created: [使用MCP time组件获取的时间]
"""

from typing import Dict, List, Optional
import logging

logger = logging.getLogger(__name__)

class TextAnalyzer:
    """文本分析器类"""
    
    def analyze_word_frequency(self, text: str) -> Dict[str, int]:
        """
        分析文本词频
        
        Args:
            text (str): 输入文本
            
        Returns:
            Dict[str, int]: 词频统计结果
            
        Raises:
            ValueError: 当输入文本为空时
        """
        if not text:
            raise ValueError("输入文本不能为空")
        
        # 实现代码
        return {}
```

#### TypeScript/React 代码规范
- **MUST**: 使用PascalCase命名组件，如 `WordCloudComponent`
- **MUST**: 使用camelCase命名变量和函数，如 `handleDataUpdate`
- **MUST**: 所有组件必须使用TypeScript类型定义
- **MUST**: 使用函数式组件和React Hooks
- **MUST**: 组件必须包含PropTypes或TypeScript接口定义
- **MUST**: 使用Ant Design组件库规范

```typescript
/**
 * 词云组件
 * @author AI Agent
 * @version 1.0.0
 * @created [使用MCP time组件获取的时间]
 */

import React, { useState, useEffect } from 'react';
import { Card, Spin } from 'antd';

interface WordCloudProps {
  data: Array<{ text: string; value: number }>;
  loading?: boolean;
  onWordClick?: (word: string) => void;
}

const WordCloudComponent: React.FC<WordCloudProps> = ({
  data,
  loading = false,
  onWordClick
}) => {
  const [processedData, setProcessedData] = useState<WordCloudProps['data']>([]);

  useEffect(() => {
    // 数据处理逻辑
    setProcessedData(data);
  }, [data]);

  const handleWordClick = (word: string) => {
    onWordClick?.(word);
  };

  return (
    <Card title="词频分析" loading={loading}>
      {/* 组件实现 */}
    </Card>
  );
};

export default WordCloudComponent;
```

### 1.2 代码结构要求

#### 文件头注释标准
- **MUST**: 每个代码文件必须包含标准文件头
- **MUST**: 包含文件功能描述、作者、版本、创建时间（使用MCP time组件获取）
- **MUST**: 包含主要依赖和使用说明
- **MUST**: 时间信息必须使用MCP time组件获取，不得使用系统时间或手动输入

#### 函数/方法注释标准
- **MUST**: 所有public方法必须有完整注释
- **MUST**: 包含参数说明、返回值说明、异常说明
- **MUST**: 复杂逻辑必须有行内注释

#### 代码组织结构
- **MUST**: 一个文件只负责一个主要功能
- **MUST**: 相关功能的代码放在同一个包/模块中
- **MUST**: 工具类和业务逻辑分离

## 2. 命名约定规范

### 2.1 项目级命名规范

#### 包/模块命名
- **Java包名**: `com.historyanalysis.{module}`
- **Python模块**: `history_analysis.{module}`
- **TypeScript模块**: `@/components/{Module}`

#### 数据库命名
- **表名**: 使用snake_case，如 `history_documents`, `word_frequency_results`
- **字段名**: 使用snake_case，如 `created_at`, `document_id`
- **索引名**: 使用格式 `idx_{table}_{column}`

### 2.2 变量和常量命名

#### 常量命名
- **Java**: 使用UPPER_SNAKE_CASE，如 `MAX_DOCUMENT_SIZE`
- **Python**: 使用UPPER_SNAKE_CASE，如 `DEFAULT_ANALYSIS_TYPE`
- **TypeScript**: 使用UPPER_SNAKE_CASE，如 `API_BASE_URL`

#### 配置项命名
- **应用配置**: 使用kebab-case，如 `spring.datasource.url`
- **环境变量**: 使用UPPER_SNAKE_CASE，如 `DATABASE_URL`

## 3. 错误处理和日志规范

### 3.1 异常处理标准

#### Java异常处理
- **MUST**: 使用自定义异常类
- **MUST**: 异常信息必须包含上下文信息
- **MUST**: 记录异常堆栈信息

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(HistoryAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAnalysisException(HistoryAnalysisException e) {
        logger.error("历史分析异常: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(e.getCode(), e.getMessage()));
    }
}
```

#### Python异常处理
- **MUST**: 使用具体的异常类型
- **MUST**: 提供有意义的错误信息
- **MUST**: 记录异常详情

```python
class AnalysisError(Exception):
    """分析异常基类"""
    
    def __init__(self, message: str, error_code: str = None):
        self.message = message
        self.error_code = error_code
        super().__init__(self.message)

def analyze_document(document: str) -> Dict:
    """分析文档"""
    try:
        # 分析逻辑
        return result
    except ValueError as e:
        logger.error(f"文档分析失败: {e}")
        raise AnalysisError(f"无效的文档格式: {e}")
    except Exception as e:
        logger.error(f"未知错误: {e}")
        raise AnalysisError("分析过程中发生未知错误")
```

#### TypeScript错误处理
- **MUST**: 使用Error类型或自定义错误类
- **MUST**: 异步操作使用try-catch
- **MUST**: 提供用户友好的错误信息

```typescript
interface ApiError {
  code: string;
  message: string;
  details?: any;
}

class AnalysisService {
  async analyzeText(text: string): Promise<AnalysisResult> {
    try {
      const response = await fetch('/api/analyze', {
        method: 'POST',
        body: JSON.stringify({ text }),
        headers: { 'Content-Type': 'application/json' }
      });
      
      if (!response.ok) {
        const error: ApiError = await response.json();
        throw new Error(`分析失败: ${error.message}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('文本分析错误:', error);
      throw new Error('文本分析服务暂时不可用，请稍后重试');
    }
  }
}
```

### 3.2 日志记录规范

#### 日志级别使用
- **ERROR**: 系统错误、异常情况
- **WARN**: 警告信息、性能问题
- **INFO**: 重要业务流程、状态变更
- **DEBUG**: 调试信息、详细执行过程

#### 日志格式标准
- **MUST**: 包含时间戳、日志级别、类名、消息
- **MUST**: 敏感信息必须脱敏处理
- **MUST**: 使用结构化日志格式

```java
// Java日志示例
logger.info("开始分析文档, documentId={}, userId={}", documentId, userId);
logger.debug("词频分析结果: wordCount={}, topWords={}", wordCount, topWords);
logger.error("数据库连接失败: {}", e.getMessage(), e);
```

```python
# Python日志示例
logger.info(f"开始文本分析, text_length={len(text)}, user_id={user_id}")
logger.debug(f"分词结果: {words[:10]}...")  # 只记录前10个词
logger.error(f"NLP服务调用失败: {str(e)}")
```

## 4. 性能和安全规范

### 4.1 性能优化要求

#### 数据库操作优化
- **MUST**: 使用连接池管理数据库连接
- **MUST**: 避免N+1查询问题
- **MUST**: 大数据量操作使用分页
- **MUST**: 合理使用数据库索引

#### 缓存策略
- **MUST**: 频繁查询的数据使用Redis缓存
- **MUST**: 设置合理的缓存过期时间
- **MUST**: 实现缓存更新策略

#### 前端性能优化
- **MUST**: 组件懒加载
- **MUST**: 图片和资源压缩
- **MUST**: 使用React.memo优化渲染
- **MUST**: 大列表使用虚拟滚动

### 4.2 安全规范

#### 输入验证
- **MUST**: 所有用户输入必须验证
- **MUST**: 使用参数化查询防止SQL注入
- **MUST**: 文件上传类型和大小限制

#### 数据保护
- **MUST**: 敏感数据加密存储
- **MUST**: API接口使用认证和授权
- **MUST**: 日志中不记录敏感信息

## 5. 测试规范

### 5.1 单元测试要求
- **MUST**: 核心业务逻辑必须有单元测试
- **MUST**: 测试覆盖率不低于80%
- **MUST**: 测试用例包含正常和异常情况

### 5.2 集成测试要求
- **MUST**: API接口必须有集成测试
- **MUST**: 数据库操作必须有测试
- **MUST**: 外部服务调用必须有Mock测试

## 6. 文档生成规范

### 6.1 API文档
- **MUST**: 使用Swagger/OpenAPI规范
- **MUST**: 包含请求/响应示例
- **MUST**: 包含错误码说明

### 6.2 代码文档
- **MUST**: 复杂算法必须有详细说明
- **MUST**: 配置项必须有说明文档
- **MUST**: 部署步骤必须有文档

## 7. 文档管理规范（仅适用于文档，代码使用Git）

### 7.1 文档版本控制规范
- **MUST**: 文档必须使用版本命名管理（v1.0, v1.1, v2.0等）
- **MUST**: 创建文档前检查是否存在同名文档
- **MUST**: 更新文档时创建备份版本
- **MUST**: 文档存储在指定目录（.trae/documents/ 或 docs/）
- **FORBIDDEN**: 直接覆盖现有文档

### 7.2 文档命名和存储规范
- **MUST**: 使用kebab-case命名文档，如 `api-specification.md`
- **MUST**: 版本文档格式：`文档名_v1.0.md`
- **MUST**: 备份文档格式：`文档名_backup_YYYYMMDD.md`
- **MUST**: 在文档头部添加版本元数据
- **FORBIDDEN**: 在项目根目录创建文档

### 7.3 代码版本控制规范
- **MUST**: 代码使用Git进行版本控制
- **MUST**: 提交信息使用约定式提交格式
- **MUST**: 每次提交包含相关测试
- **MUST**: 大功能分多次小提交
- **FORBIDDEN**: 对代码文件使用版本后缀命名

### 7.4 版本发布规范
- **MUST**: 使用语义化版本号
- **MUST**: 发布前完成所有测试
- **MUST**: 提供版本更新说明

## 8. 监控和维护

### 8.1 应用监控
- **MUST**: 关键业务指标监控
- **MUST**: 系统性能监控
- **MUST**: 错误率和响应时间监控

### 8.2 代码维护
- **MUST**: 定期代码审查
- **MUST**: 及时更新依赖版本
- **MUST**: 清理无用代码和注释

## 9. 工具、包、文件安装规范

### 9.1 安装位置约束

- **MUST**: AI Agent在项目开发过程中安装的所有第三方工具、工具包、开发环境必须安装到D盘或H盘
- **FORBIDDEN**: 不允许将任何第三方工具、工具包安装到C盘
- **MUST**: 推荐的安装路径：
  - 主要开发工具：`H:\开发环境\{工具名称}`
  - 项目依赖包：`H:\开发环境\dependencies\{项目名称}`
  - 临时工具：`D:\temp\tools\{工具名称}`
  - 缓存文件：`D:\cache\{工具名称}`

### 9.2 工具类型覆盖范围

以下所有类型的工具和包都必须遵守安装位置约束：

#### 编程语言和运行时
- **Node.js** 及其全局包 (npm, yarn, pnpm)
- **Python** 及 pip 安装的所有包
- **Java JDK/JRE** 及 Maven/Gradle 依赖
- **Go** 及其模块
- **.NET Core/Framework** 及 NuGet 包

#### 开发工具和 IDE
- **Visual Studio Code** 及其插件
- **IntelliJ IDEA** 等 JetBrains 工具
- **Git** 及相关工具
- **Docker Desktop**
- **Postman** 等 API 测试工具

#### 数据库和中间件
- **MySQL Workbench**
- **Redis Desktop Manager**
- **MongoDB Compass**
- **Elasticsearch** 客户端

#### 测试和自动化工具
- **Puppeteer** 及 Chromium
- **Selenium WebDriver**
- **Jest, Mocha** 等测试框架
- **Cypress** 等 E2E 测试工具

### 9.3 安装前强制检查

- **MUST**: 执行任何安装命令前，必须检查目标安装路径
- **MUST**: 如果检测到安装路径指向C盘，必须立即中断安装并重新指定路径
- **MUST**: 提供替代的D盘或H盘安装路径建议
- **MUST**: 记录所有安装操作的日志，包括工具名称、版本、安装路径

### 9.4 环境变量管理

- **MUST**: 安装工具后自动检查和更新环境变量
- **MUST**: 确保PATH环境变量中不包含C盘的工具路径
- **MUST**: 自动配置工具特定的环境变量指向正确的D盘或H盘路径
- **MUST**: 在项目配置文件中更新工具路径引用

### 9.5 已安装工具管理

- **MUST**: 维护一个完整的已安装工具清单，包括：
  - 工具名称和版本
  - 安装路径
  - 安装日期
  - 依赖关系
- **MUST**: 定期检查工具安装路径的有效性
- **MUST**: 提供工具迁移和清理功能

### 9.6 违规检测和处理

- **MUST**: 实时监控系统中是否有工具被安装到C盘
- **MUST**: 发现违规安装时立即报告并提供解决方案
- **MUST**: 提供自动迁移工具的功能（从 C盘到 D盘/H盘）
- **MUST**: 记录所有违规尝试和处理结果

### 9.7 特殊情况处理

- **系统级工具**: 如果某些工具必须安装在系统目录，必须先征得用户同意
- **遗留工具**: 对于C盘上已存在的工具，提供迁移建议和指导
- **空间不足**: 如果D盘或H盘空间不足，提供清理建议和替代方案

---

**注意**: 这些规则适用于所有AI Agent的开发活动，无论是新项目创建还是现有项目维护。

**文档版本**: v1.1.0  
**创建时间**: 2025-10-19  
**最后更新**: 2025-10-30  
**适用项目**: 历史数据统计分析工具  
**维护者**: AI Agent