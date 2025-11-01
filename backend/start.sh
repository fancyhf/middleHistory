#!/bin/bash

# 历史数据统计分析工具后端启动脚本
# Author: AI Agent
# Version: 1.0.0
# Created: 2024-12-29 19:45:00

echo "========================================"
echo "历史数据统计分析工具后端启动脚本"
echo "========================================"
echo

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查Java环境
echo -e "${BLUE}[1/5] 检查Java环境...${NC}"
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ 错误: 未找到Java环境，请确保已安装Java 17或更高版本${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo -e "${RED}❌ 错误: Java版本过低，需要Java 17或更高版本${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Java环境检查通过${NC}"

# 检查Maven环境
echo
echo -e "${BLUE}[2/5] 检查Maven环境...${NC}"
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ 错误: 未找到Maven环境，请确保已安装Maven${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Maven环境检查通过${NC}"

# 清理并编译项目
echo
echo -e "${BLUE}[3/5] 清理并编译项目...${NC}"
mvn clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 错误: 项目编译失败${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 项目编译成功${NC}"

# 运行测试
echo
echo -e "${BLUE}[4/5] 运行单元测试...${NC}"
mvn test
if [ $? -ne 0 ]; then
    echo -e "${YELLOW}⚠️  警告: 部分测试失败，但继续启动服务${NC}"
else
    echo -e "${GREEN}✅ 所有测试通过${NC}"
fi

# 启动应用
echo
echo -e "${BLUE}[5/5] 启动Spring Boot应用...${NC}"
echo
echo -e "${GREEN}🚀 正在启动历史数据统计分析工具后端服务...${NC}"
echo -e "${BLUE}📍 服务地址: http://localhost:8080${NC}"
echo -e "${BLUE}📍 API文档: http://localhost:8080/swagger-ui.html${NC}"
echo -e "${BLUE}📍 健康检查: http://localhost:8080/actuator/health${NC}"
echo
echo -e "${YELLOW}按 Ctrl+C 停止服务${NC}"
echo "========================================"
echo

# 设置JVM参数
export JAVA_OPTS="-Xms512m -Xmx2g -Dspring.profiles.active=dev"

# 启动应用
mvn spring-boot:run

echo
echo -e "${YELLOW}服务已停止${NC}"