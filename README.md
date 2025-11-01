# 历史数据统计分析工具

基于 Spring Boot + React + Python NLP 的全栈历史数据分析应用

## 📋 项目简介

这是一个专门用于历史数据统计分析的全栈Web应用，集成了现代化的前后端技术栈和自然语言处理能力。系统支持历史文档上传、文本分析、词频统计、情感分析等功能，为历史研究提供数据驱动的分析工具。

## 🚀 技术栈

### 后端技术
- **Spring Boot 3.x** - 主要后端框架
- **Spring Security** - 安全认证与授权
- **Spring Data JPA** - 数据持久化
- **MySQL 8.x** - 主数据库
- **Redis** - 缓存和会话存储
- **Maven** - 项目构建管理

### 前端技术
- **React 18** - 前端UI框架
- **TypeScript** - 类型安全的JavaScript
- **Ant Design** - UI组件库
- **Vite** - 前端构建工具
- **Axios** - HTTP客户端

### 数据分析
- **Python 3.x** - 数据分析引擎
- **Flask** - Python Web框架
- **jieba** - 中文分词
- **pandas** - 数据处理
- **numpy** - 数值计算

## 📁 项目结构

```
midHis/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/      # Java 源码
│   ├── src/main/resources/ # 配置文件
│   └── pom.xml            # Maven 配置
├── frontend/               # React 前端
│   ├── src/               # 前端源码
│   ├── public/            # 静态资源
│   └── package.json       # npm 配置
├── python-service/         # Python 分析服务
│   ├── app.py            # Flask 应用
│   ├── requirements.txt   # Python 依赖
│   └── analysis/          # 分析模块
└── docs/                  # 项目文档
```

## 🛠️ 安装与配置

### 环境要求

- **Java**: JDK 17 或更高版本
- **Node.js**: 16.x 或更高版本
- **Python**: 3.8 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.x 或更高版本

### 1. 克隆项目

```bash
git clone https://github.com/fancyhf/middleHistory.git
cd middleHistory
```

### 2. 数据库配置

#### 创建MySQL数据库

```sql
CREATE DATABASE history_analysis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'history_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON history_analysis.* TO 'history_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 配置数据库连接

复制配置模板并修改数据库连接信息：

```bash
cd backend/src/main/resources/
cp application-mysql.yml.example application-mysql.yml
```

编辑 `application-mysql.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/history_analysis?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: history_user
    password: your_password  # 替换为实际密码
```

### 3. 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 4. 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端应用将在 `http://localhost:5173` 启动

### 5. Python分析服务启动

```bash
cd python-service
pip install -r requirements.txt
python app.py
```

Python服务将在 `http://localhost:5000` 启动

## 🎯 主要功能

### 📄 文档管理
- 支持多种格式文档上传（PDF、DOC、TXT等）
- 文档内容提取和预处理
- 文档分类和标签管理

### 📊 文本分析
- **词频统计**: 统计文档中词汇出现频率
- **关键词提取**: 自动提取文档关键信息
- **情感分析**: 分析文本情感倾向
- **主题建模**: 发现文档主题分布

### 📈 数据可视化
- 词云图生成
- 统计图表展示
- 时间序列分析
- 交互式数据探索

### 🔐 用户管理
- 用户注册和登录
- JWT令牌认证
- 角色权限管理
- 操作日志记录

## 🔧 API接口

### 健康检查
```
GET /actuator/health
```

### 文件上传
```
POST /api/files/upload
Content-Type: multipart/form-data
```

### 文本分析
```
POST /api/analysis/text
Content-Type: application/json
{
  "text": "要分析的文本内容",
  "analysisType": "frequency|sentiment|keyword"
}
```

### 获取分析结果
```
GET /api/analysis/results/{id}
```

## 🧪 测试

### 后端测试
```bash
cd backend
mvn test
```

### 前端测试
```bash
cd frontend
npm run test
```

## 📦 部署

### Docker部署（推荐）

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d
```

### 手动部署

1. **后端部署**
```bash
cd backend
mvn clean package
java -jar target/history-analysis-backend-1.0.0.jar
```

2. **前端部署**
```bash
cd frontend
npm run build
# 将 dist/ 目录部署到 Web 服务器
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📝 开发规范

- 遵循 [Java编码规范](docs/java-coding-standards.md)
- 遵循 [React开发规范](docs/react-coding-standards.md)
- 遵循 [Python编码规范](docs/python-coding-standards.md)
- 提交信息使用 [约定式提交](https://www.conventionalcommits.org/)

## 🐛 问题反馈

如果您发现任何问题或有改进建议，请：

1. 查看 [Issues](https://github.com/fancyhf/middleHistory/issues) 是否已有相关问题
2. 如果没有，请创建新的 Issue
3. 详细描述问题和复现步骤

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 👥 作者

- **fancyhf** - *项目创建者* - [GitHub](https://github.com/fancyhf)

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！

---

**项目状态**: 🚧 开发中

**最后更新**: 2024年12月

如有任何问题，欢迎通过 GitHub Issues 联系我们！