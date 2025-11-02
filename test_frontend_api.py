#!/usr/bin/env python3
"""
测试前端API配置是否正确
"""

import requests
import json

def test_backend_endpoints():
    """测试后端各个端点"""
    base_url = "http://localhost:8080"
    
    print("🔍 测试后端API端点...")
    
    # 测试健康检查
    try:
        response = requests.get(f"{base_url}/actuator/health", timeout=5)
        print(f"✅ 健康检查: {response.status_code} - {response.json()}")
    except Exception as e:
        print(f"❌ 健康检查失败: {e}")
    
    # 测试文件上传端点（GET请求，应该返回405 Method Not Allowed）
    try:
        response = requests.get(f"{base_url}/files/upload", timeout=5)
        print(f"✅ 文件上传端点存在: {response.status_code}")
    except Exception as e:
        print(f"❌ 文件上传端点测试失败: {e}")
    
    # 测试错误的API路径（应该返回404）
    try:
        response = requests.get(f"{base_url}/api/files/upload", timeout=5)
        print(f"⚠️  错误的API路径: {response.status_code}")
    except Exception as e:
        print(f"❌ 错误API路径测试失败: {e}")

def test_file_upload():
    """测试文件上传"""
    print("\n📤 测试文件上传...")
    
    try:
        with open('testdata/马可·波罗游记.doc', 'rb') as f:
            files = {'file': f}
            data = {'projectId': '1'}
            
            response = requests.post(
                'http://localhost:8080/files/upload',
                files=files,
                data=data,
                timeout=30
            )
            
            print(f"📊 上传状态码: {response.status_code}")
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 上传成功: {result.get('message', '')}")
                print(f"📄 文件信息: {result.get('data', {}).get('fileName', '')}")
            else:
                print(f"❌ 上传失败: {response.text}")
                
    except Exception as e:
        print(f"❌ 文件上传测试失败: {e}")

if __name__ == "__main__":
    test_backend_endpoints()
    test_file_upload()