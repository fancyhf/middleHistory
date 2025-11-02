#!/usr/bin/env python3
"""
简单的文件上传测试脚本
"""

import requests
import os

def test_upload():
    # 测试文件路径
    file_path = "testdata/马可·波罗游记.doc"
    
    if not os.path.exists(file_path):
        print(f"❌ 测试文件不存在: {file_path}")
        return
    
    # 上传URL
    url = "http://localhost:8080/files/upload"
    
    # 准备文件和数据
    with open(file_path, 'rb') as f:
        files = {'file': (os.path.basename(file_path), f, 'application/msword')}
        data = {'projectId': '1'}
        
        print(f"📤 上传文件: {os.path.basename(file_path)}")
        print(f"📍 URL: {url}")
        
        try:
            # 发送POST请求
            response = requests.post(url, files=files, data=data, timeout=30)
            
            print(f"📊 响应状态码: {response.status_code}")
            print(f"📋 响应头: {dict(response.headers)}")
            
            if response.status_code == 200:
                result = response.json()
                print("✅ 文件上传成功!")
                print(f"📄 响应内容: {result}")
            else:
                print("❌ 文件上传失败!")
                try:
                    error_info = response.json()
                    print(f"🔍 错误信息: {error_info}")
                except:
                    print(f"🔍 响应文本: {response.text}")
                    
        except requests.exceptions.RequestException as e:
            print(f"❌ 请求异常: {e}")

if __name__ == "__main__":
    test_upload()