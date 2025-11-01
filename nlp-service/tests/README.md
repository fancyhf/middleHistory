# NLP服务测试目录

## 📁 目录说明

本目录包含历史数据统计分析工具的NLP服务后端测试脚本。

## 📋 测试文件列表

### 🐍 Python测试脚本

- **`test_nlp.py`** - NLP服务基础功能测试
  - 测试文本分析基础功能
  - 词频分析测试
  - 文本处理测试

- **`test_nlp_api.py`** - NLP API接口测试
  - 测试REST API端点
  - 请求响应验证
  - 错误处理测试

- **`test_nlp_comprehensive.py`** - NLP服务综合测试
  - 完整功能测试套件
  - 性能测试
  - 集成测试

### 🟨 JavaScript测试脚本

- **`test-nlp-direct.js`** - NLP服务直接调用测试
  - Node.js环境下的NLP服务测试
  - 直接API调用测试

## 🚀 运行测试

### Python测试

```bash
# 进入NLP服务目录
cd nlp-service

# 安装测试依赖
pip install pytest requests

# 运行单个测试文件
python -m pytest tests/test_nlp.py -v

# 运行所有Python测试
python -m pytest tests/ -v

# 运行API测试
python tests/test_nlp_api.py

# 运行综合测试
python tests/test_nlp_comprehensive.py
```

### JavaScript测试

```bash
# 进入测试目录
cd nlp-service/tests

# 运行Node.js测试
node test-nlp-direct.js
```

## 🧪 测试覆盖范围

### 核心功能测试
- ✅ 词频分析功能
- ✅ 文本摘要功能
- ✅ 时间轴提取功能
- ✅ 地理信息分析功能
- ✅ 文本预处理功能

### API接口测试
- ✅ `/api/analyze` - 文本分析接口
- ✅ `/api/word-frequency` - 词频分析接口
- ✅ `/api/timeline` - 时间轴提取接口
- ✅ `/api/geography` - 地理分析接口
- ✅ `/api/summary` - 文本摘要接口

### 性能测试
- 响应时间测试
- 并发请求测试
- 大文本处理测试
- 内存使用测试

## 🔧 测试配置

### 环境要求
- Python 3.8+
- Flask框架
- 相关NLP库（jieba, numpy等）
- Node.js 16+（用于JavaScript测试）

### 测试数据
- 测试文本样本
- 预期结果数据
- 边界条件测试数据

## 📊 测试报告

测试运行后会生成：
- 控制台输出结果
- 测试覆盖率报告
- 性能测试数据
- 错误日志文件

## 🐛 故障排除

### 常见问题

1. **NLP服务未启动**
   ```bash
   # 启动NLP服务
   cd nlp-service
   python app.py
   ```

2. **依赖包缺失**
   ```bash
   # 安装依赖
   pip install -r requirements.txt
   ```

3. **端口冲突**
   - 检查NLP服务端口配置
   - 确认服务正常运行在指定端口

4. **测试数据问题**
   - 检查测试数据格式
   - 验证输入数据有效性

## 📝 添加新测试

### Python测试
1. 在对应的测试文件中添加新的测试方法
2. 使用pytest装饰器标记测试
3. 添加断言验证结果

### JavaScript测试
1. 在test-nlp-direct.js中添加新的测试函数
2. 使用async/await处理异步调用
3. 添加错误处理和结果验证

---

**文档版本**: v1.0.0  
**创建时间**: 2025-10-30  
**维护者**: AI Agent  
**适用项目**: 历史数据统计分析工具