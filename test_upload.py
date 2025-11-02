#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
文件上传测试脚本
Author: AI Agent
Version: 1.0.0
Created: 2025-11-02
"""

import requests
import os

def test_file_upload():
    """测试文件上传功能"""
    # 文件路径 - 使用简单的文本文件
    file_path = r"H:\projects\midHis\testdata\test.txt"
    
    # 检查文件是否存在
    if not os.path.exists(file_path):
        print(f"文件不存在: {file_path}")
        return False
    
    # 上传URL
    upload_url = "http://localhost:8080/files/upload"
    
    try:
        # 准备文件和参数
        with open(file_path, 'rb') as f:
            files = {'file': (os.path.basename(file_path), f, 'application/octet-stream')}
            data = {'projectId': '1'}  # 使用默认项目ID
            
            # 发送上传请求
            print(f"正在上传文件: {os.path.basename(file_path)}")
            response = requests.post(upload_url, files=files, data=data)
            
            print(f"响应状态码: {response.status_code}")
            print(f"响应头: {response.headers}")
            print(f"响应内容: {response.text}")
            
            if response.status_code == 200:
                print("文件上传成功！")
                return True
            else:
                print(f"文件上传失败，状态码: {response.status_code}")
                # 尝试解析错误信息
                try:
                    error_data = response.json()
                    print(f"错误详情: {error_data}")
                except:
                    print("无法解析错误响应")
                return False
                
    except Exception as e:
        print(f"上传过程中发生错误: {str(e)}")
        return False

if __name__ == "__main__":
    test_file_upload()