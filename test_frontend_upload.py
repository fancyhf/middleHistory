#!/usr/bin/env python3
"""
测试前端文件上传功能
模拟前端的文件上传请求，验证数据结构是否匹配
"""

import requests
import json
import os

def test_frontend_upload():
    """测试前端文件上传功能"""
    
    # 后端API地址
    base_url = "http://localhost:8080"
    upload_url = f"{base_url}/files/upload"
    
    # 测试文件路径
    test_file = "testdata/马可·波罗游记.doc"
    
    if not os.path.exists(test_file):
        print(f"❌ 测试文件不存在: {test_file}")
        return False
    
    try:
        # 模拟前端上传请求
        with open(test_file, 'rb') as f:
            files = {
                'file': (os.path.basename(test_file), f, 'application/msword')
            }
            data = {
                'projectId': '1'
            }
            
            print(f"📤 正在上传文件: {test_file}")
            response = requests.post(upload_url, files=files, data=data)
            
            print(f"📊 响应状态码: {response.status_code}")
            print(f"📋 响应头: {dict(response.headers)}")
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 上传成功!")
                print(f"📄 响应数据结构:")
                print(json.dumps(result, indent=2, ensure_ascii=False))
                
                # 验证前端需要的字段是否存在
                required_fields = [
                    'id', 'fileName', 'originalFileName', 'fileType', 
                    'fileSize', 'formattedSize', 'status', 'processStatus',
                    'projectId', 'uploadTime', 'canAnalyze'
                ]
                
                missing_fields = []
                for field in required_fields:
                    if field not in result:
                        missing_fields.append(field)
                
                if missing_fields:
                    print(f"⚠️  缺少字段: {missing_fields}")
                    return False
                else:
                    print(f"✅ 所有必需字段都存在")
                    return True
            else:
                print(f"❌ 上传失败: {response.status_code}")
                print(f"📄 错误响应: {response.text}")
                return False
                
    except Exception as e:
        print(f"❌ 测试过程中发生错误: {str(e)}")
        return False

def test_health_check():
    """测试后端健康检查"""
    try:
        response = requests.get("http://localhost:8080/health")
        if response.status_code == 200:
            print("✅ 后端服务正常运行")
            return True
        else:
            print(f"❌ 后端服务异常: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ 无法连接到后端服务: {str(e)}")
        return False

if __name__ == "__main__":
    print("🚀 开始测试前端文件上传功能...")
    print("=" * 50)
    
    # 1. 检查后端服务
    print("1️⃣ 检查后端服务状态...")
    if not test_health_check():
        print("❌ 后端服务不可用，测试终止")
        exit(1)
    
    print()
    
    # 2. 测试文件上传
    print("2️⃣ 测试文件上传...")
    if test_frontend_upload():
        print("\n🎉 前端文件上传功能测试通过!")
    else:
        print("\n❌ 前端文件上传功能测试失败!")
        exit(1)