#!/usr/bin/env python3
"""
调试前端文件上传问题的脚本
模拟前端的文件上传请求，捕获所有错误信息
"""

import requests
import json
import os
from pathlib import Path

def debug_frontend_upload():
    """调试前端文件上传功能"""
    print("🔍 开始调试前端文件上传问题...")
    print("=" * 60)
    
    # 测试文件路径
    test_file = Path("testdata/马可·波罗游记.doc")
    
    if not test_file.exists():
        print(f"❌ 测试文件不存在: {test_file}")
        return
    
    # 1. 首先测试后端健康状态
    print("1️⃣ 检查后端服务状态...")
    try:
        health_response = requests.get("http://localhost:8080/health", timeout=5)
        print(f"   后端健康检查: {health_response.status_code}")
        if health_response.status_code != 200:
            print(f"   ❌ 后端服务异常: {health_response.text}")
            return
        else:
            print(f"   ✅ 后端服务正常")
    except Exception as e:
        print(f"   ❌ 后端服务连接失败: {e}")
        return
    
    # 2. 测试文件上传API
    print("\n2️⃣ 测试文件上传API...")
    
    try:
        # 准备文件上传数据
        with open(test_file, 'rb') as f:
            files = {'file': (test_file.name, f, 'application/msword')}
            data = {'projectId': '1'}
            
            # 发送上传请求
            print(f"   📤 上传文件: {test_file.name}")
            print(f"   📊 请求URL: http://localhost:8080/files/upload")
            print(f"   📋 请求数据: {data}")
            
            response = requests.post(
                "http://localhost:8080/files/upload",
                files=files,
                data=data,
                timeout=30
            )
            
            print(f"   📊 响应状态码: {response.status_code}")
            print(f"   📄 响应头: {dict(response.headers)}")
            
            if response.status_code == 200:
                try:
                    response_data = response.json()
                    print(f"   ✅ 上传成功!")
                    print(f"   📄 响应数据:")
                    print(json.dumps(response_data, indent=2, ensure_ascii=False))
                    
                    # 检查响应数据结构
                    if 'data' in response_data:
                        file_data = response_data['data']
                        required_fields = ['id', 'fileName', 'fileSize', 'status', 'uploadTime']
                        missing_fields = [field for field in required_fields if field not in file_data]
                        
                        if missing_fields:
                            print(f"   ⚠️ 缺少字段: {missing_fields}")
                        else:
                            print(f"   ✅ 所有必需字段都存在")
                    
                except json.JSONDecodeError as e:
                    print(f"   ❌ JSON解析失败: {e}")
                    print(f"   📄 原始响应: {response.text}")
            else:
                print(f"   ❌ 上传失败!")
                print(f"   📄 错误响应: {response.text}")
                
                # 尝试解析错误信息
                try:
                    error_data = response.json()
                    print(f"   📄 错误详情:")
                    print(json.dumps(error_data, indent=2, ensure_ascii=False))
                except:
                    pass
                    
    except requests.exceptions.Timeout:
        print(f"   ❌ 请求超时")
    except requests.exceptions.ConnectionError:
        print(f"   ❌ 连接错误")
    except Exception as e:
        print(f"   ❌ 上传异常: {e}")
    
    # 3. 测试前端API路径
    print("\n3️⃣ 测试前端可能使用的API路径...")
    
    # 测试可能的API路径
    api_paths = [
        "http://localhost:8080/api/files/upload",
        "http://localhost:8080/files/upload",
        "http://localhost:3000/api/files/upload"
    ]
    
    for api_path in api_paths:
        try:
            print(f"   🔍 测试路径: {api_path}")
            test_response = requests.get(api_path, timeout=2)
            print(f"      状态码: {test_response.status_code}")
        except Exception as e:
            print(f"      ❌ 路径不可用: {e}")
    
    print("\n" + "=" * 60)
    print("🔍 调试完成!")

if __name__ == "__main__":
    debug_frontend_upload()