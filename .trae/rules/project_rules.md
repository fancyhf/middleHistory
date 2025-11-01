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

## 9. 第三方工具和依赖安装规范

### 9.1 工具安装位置约束

- **MUST**: AI Agent在项目开发过程中安装的所有第三方工具、工具包、开发环境必须安装到D盘或H盘
- **FORBIDDEN**: 不允许将任何第三方工具、工具包安装到C盘
- **MUST**: 推荐的安装路径：
  - 开发工具：`H:\开发环境\{工具名称}`
  - 项目依赖：`H:\开发环境\dependencies\{项目名称}`
  - 临时工具：`D:\temp\tools\{工具名称}`

### 9.2 工具类型约束

包括但不限于以下工具类型：
- **Node.js及相关包管理器** (npm, yarn, pnpm)
- **Python及相关包** (pip安装的包)
- **Java开发工具** (Maven, Gradle依赖)
- **数据库工具** (MySQL, Redis客户端)
- **测试工具** (Puppeteer, Selenium等)
- **构建工具** (Webpack, Vite等)
- **代码分析工具** (ESLint, SonarQube等)

### 9.3 安装前检查规则

- **MUST**: 安装任何工具前，必须检查目标安装路径
- **MUST**: 如果检测到安装路径指向C盘，必须重新指定到D盘或H盘
- **MUST**: 安装完成后记录工具安装位置和版本信息
- **MUST**: 在项目文档中维护已安装工具的清单

### 9.4 环境变量配置

- **MUST**: 安装工具后正确配置环境变量指向D盘或H盘路径
- **MUST**: 确保PATH环境变量不包含C盘的工具路径
- **MUST**: 项目配置文件中的工具路径必须指向正确的安装位置

### 9.5 违规处理机制

- **MUST**: 检测到C盘安装路径时，立即停止安装操作
- **MUST**: 提示用户重新选择D盘或H盘路径
- **MUST**: 记录违规尝试并在操作日志中标记
- **MUST**: 提供自动迁移工具路径的建议

### 9.6 智能镜像使用策略

- **MUST**: 当遇到任何官方网站无法访问或下载速度过慢的情况时，AI Agent必须自动启动镜像搜索机制
- **MUST**: 建立通用的镜像搜索和验证流程：
  1. 首先尝试官方源
  2. 如果失败，自动搜索中国主流镜像网站
  3. 验证镜像源的可用性和完整性
  4. 选择最优的镜像源进行下载
- **MUST**: AI Agent必须具备自主学习和发现新镜像源的能力
- **MUST**: 建立镜像源优先级评估机制：
  - 网络连接速度
  - 数据完整性
  - 更新频率
  - 可靠性历史记录

#### 通用镜像发现策略

**搜索策略**：
- 使用web搜索查找 "{工具名} 中国镜像" 或 "{工具名} mirror china"
- 检查知名镜像提供商：清华大学、阿里云、腾讯云、华为云、网易等
- 验证镜像源的官方认证状态

**验证机制**：
- 检查镜像源的SSL证书
- 验证文件哈希值（如果可用）
- 测试下载速度和稳定性
- 检查镜像同步时间

**自动切换逻辑**：
- 设置超时阈值（如30秒无响应）
- 设置速度阈值（如低于100KB/s）
- 建立失败重试机制
- 记录成功的镜像源供后续使用

#### 镜像使用记录和学习

- **MUST**: 维护一个动态的镜像源数据库，记录：
  - 工具/包名称
  - 成功使用的镜像源
  - 使用时间和成功率
  - 下载速度统计
- **MUST**: 基于历史数据优化镜像选择策略
- **MUST**: 定期验证已记录镜像源的可用性
- **MUST**: 在项目文档中记录实际使用的镜像源

### 9.7 AI Agent镜像使用执行机制

#### 自动镜像发现流程

- **MUST**: AI Agent在遇到下载失败时，必须按以下步骤执行：
  1. **初始尝试**: 使用官方源进行下载，设置30秒超时
  2. **失败检测**: 检测到超时、连接失败或速度过慢时触发镜像搜索
  3. **智能搜索**: 使用web_search工具搜索相关镜像源
  4. **镜像验证**: 对找到的镜像源进行可用性测试
  5. **最优选择**: 根据速度和可靠性选择最佳镜像源
  6. **执行下载**: 使用选定的镜像源完成下载
  7. **记录学习**: 将成功的镜像源记录到项目文档中

#### 镜像搜索关键词策略

- **MUST**: 使用以下搜索模式查找镜像源：
  - "{工具名称} 中国镜像"
  - "{工具名称} mirror china"
  - "{工具名称} 清华镜像"
  - "{工具名称} 阿里云镜像"
  - "{包管理器名称} 镜像源配置"

#### 镜像验证标准

- **MUST**: 对发现的镜像源进行以下验证：
  - **连通性测试**: 检查URL是否可访问
  - **速度测试**: 测试下载速度是否满足阈值要求
  - **完整性验证**: 如果可能，验证文件哈希值
  - **SSL安全性**: 检查HTTPS证书有效性
  - **同步时间**: 检查镜像更新时间是否及时

#### 动态学习和优化

- **MUST**: AI Agent必须具备以下学习能力：
  - **成功记录**: 记录每次成功使用的镜像源和性能数据
  - **失败分析**: 分析失败原因并避免重复尝试无效镜像
  - **性能优化**: 基于历史数据优先选择高性能镜像源
  - **自动更新**: 定期重新验证已记录镜像源的可用性

#### 特殊情况处理

- **MUST**: 处理以下特殊情况：
  - **全部镜像失败**: 提示用户手动干预或稍后重试
  - **网络环境变化**: 重新评估镜像源优先级
  - **新工具首次安装**: 主动搜索并建立镜像源记录
  - **镜像源失效**: 自动移除失效镜像并寻找替代方案

---

**文档版本**: v1.0.0  
**创建时间**: 2025-10-29  
**适用项目**: 历史数据统计分析工具  
**维护者**: AI Agent