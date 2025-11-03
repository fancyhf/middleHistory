#!/usr/bin/env python3
"""
测试词频分析API
"""
import requests
import json

def test_word_frequency_api():
    """测试词频分析API"""
    
    # API端点
    url = "http://localhost:8080/api/analysis/create"
    
    # 测试数据 - 模拟前端发送的请求
    test_data = {
        "analysisType": "WORD_FREQUENCY",
        "projectId": 1,
        "fileIds": [1, 2],
        "parameters": {
            "minWordLength": 2,
            "maxWords": 100,
            "description": "测试词频分析"
        }
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    print("=== 测试词频分析API ===")
    print(f"URL: {url}")
    print(f"请求数据: {json.dumps(test_data, indent=2, ensure_ascii=False)}")
    
    try:
        # 发送POST请求
        response = requests.post(url, json=test_data, headers=headers, timeout=10)
        
        print(f"\n响应状态码: {response.status_code}")
        print(f"响应头: {dict(response.headers)}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"成功响应: {json.dumps(result, indent=2, ensure_ascii=False)}")
        else:
            print(f"错误响应: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ 连接错误：无法连接到后端服务")
    except requests.exceptions.Timeout:
        print("❌ 超时错误：请求超时")
    except Exception as e:
        print(f"❌ 其他错误: {e}")

def test_health_check():
    """测试健康检查"""
    try:
        response = requests.get("http://localhost:8080/api/health", timeout=5)
        print(f"健康检查状态: {response.status_code}")
        if response.status_code == 200:
            print("✅ 后端服务正常运行")
        else:
            print("⚠️ 后端服务状态异常")
    except Exception as e:
        print(f"❌ 健康检查失败: {e}")

if __name__ == "__main__":
    test_health_check()
    print("\n" + "="*50 + "\n")
    test_word_frequency_api()