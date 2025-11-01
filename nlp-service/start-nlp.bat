@echo off
REM NLP微服务启动脚本
REM @author AI Agent
REM @version 1.0.0
REM @created 2024-12-29 18:10:00

echo 启动NLP微服务...
echo.

REM 检查Python环境
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Python环境，请安装Python 3.8或更高版本
    echo 下载地址: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo Python环境检查通过
echo.

REM 安装依赖
echo 正在安装Python依赖包...
pip install -r requirements.txt
if %errorlevel% neq 0 (
    echo 依赖安装失败，请检查网络连接或使用国内镜像源
    echo 可以尝试: pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple/
    pause
    exit /b 1
)

echo 依赖安装成功
echo.

REM 启动NLP服务
echo 正在启动NLP微服务...
echo 服务将在 http://localhost:5001 启动
echo 按 Ctrl+C 停止服务
echo.

python app.py

pause