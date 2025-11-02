#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试上传马可·波罗游记.doc文件
"""

import requests
import os
import mimetypes

def test_doc_upload():
    # 文件路径
    file_path = "testdata/马可·波罗游记.doc"
    
    # 检查文件是否存在
    if not os.path.exists(file_path):
        print(f"文件不存在: {file_path}")
        return
    
    # 获取文件信息
    file_size = os.path.getsize(file_path)
    mime_type, _ = mimetypes.guess_type(file_path)
    
    print(f"文件路径: {file_path}")
    print(f"文件大小: {file_size} bytes ({file_size / 1024 / 1024:.2f} MB)")
    print(f"MIME类型: {mime_type}")
    
    # 上传URL
    upload_url = "http://localhost:8080/files/upload"
    
    try:
        # 准备文件和数据
        with open(file_path, 'rb') as f:
            files = {
                'file': ('马可·波罗游记.doc', f, mime_type or 'application/msword')
            }
            data = {
                'projectId': '1'
            }
            
            print(f"\n开始上传到: {upload_url}")
            print(f"项目ID: {data['projectId']}")
            
            # 发送POST请求
            response = requests.post(upload_url, files=files, data=data, timeout=30)
            
            print(f"\n响应状态码: {response.status_code}")
            print(f"响应头: {dict(response.headers)}")
            
            if response.status_code == 200:
                print("✅ 文件上传成功!")
                try:
                    result = response.json()
                    print(f"响应数据: {result}")
                except:
                    print(f"响应内容: {response.text}")
            else:
                print("❌ 文件上传失败!")
                try:
                    error = response.json()
                    print(f"错误信息: {error}")
                except:
                    print(f"错误内容: {response.text}")
                    
    except requests.exceptions.RequestException as e:
        print(f"❌ 请求异常: {e}")
    except Exception as e:
        print(f"❌ 其他异常: {e}")

if __name__ == "__main__":
    test_doc_upload()