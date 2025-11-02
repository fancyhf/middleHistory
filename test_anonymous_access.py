#!/usr/bin/env python3
"""
测试匿名用户访问核心功能
Author: AI Agent
Version: 1.0.0
Created: 2025-11-02 12:30:00
"""

import requests
import json
import os

def test_anonymous_access():
    """测试匿名用户访问核心功能"""
    base_url = "http://localhost:8080"
    
    print("🔍 测试匿名用户访问核心功能...")
    
    # 1. 测试健康检查
    try:
        response = requests.get(f"{base_url}/actuator/health")
        print(f"✅ 健康检查: {response.status_code}")
    except Exception as e:
        print(f"❌ 健康检查失败: {e}")
    
    # 2. 测试文件上传
    try:
        # 创建测试文件
        test_content = "这是一个测试文档，用于验证文件上传功能。包含一些历史相关内容：明朝、清朝、唐朝等朝代信息。"
        
        files = {
            'file': ('test_document.txt', test_content, 'text/plain')
        }
        
        response = requests.post(f"{base_url}/files/upload", files=files)
        print(f"✅ 文件上传: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            print(f"📄 上传结果: {result.get('message', 'Unknown')}")
            file_id = result.get('data', {}).get('id')
            if file_id:
                print(f"📋 文件ID: {file_id}")
                return file_id
        else:
            print(f"❌ 上传失败: {response.text}")
            
    except Exception as e:
        print(f"❌ 文件上传异常: {e}")
    
    # 3. 测试分析接口（如果存在）
    try:
        # 测试文本分析
        analysis_data = {
            "text": "明朝是中国历史上的一个重要朝代，由朱元璋建立。",
            "analysisType": "word_frequency"
        }
        
        response = requests.post(
            f"{base_url}/api/analysis/text", 
            json=analysis_data,
            headers={'Content-Type': 'application/json'}
        )
        print(f"✅ 文本分析: {response.status_code}")
        
        if response.status_code == 200:
            print("📊 分析成功")
        else:
            print(f"⚠️  分析响应: {response.text[:200]}")
            
    except Exception as e:
        print(f"❌ 文本分析异常: {e}")
    
    # 4. 测试NLP接口
    try:
        nlp_data = {
            "text": "北京是中国的首都，位于华北平原。",
            "task": "word_frequency"
        }
        
        response = requests.post(
            f"{base_url}/api/nlp/analyze", 
            json=nlp_data,
            headers={'Content-Type': 'application/json'}
        )
        print(f"✅ NLP分析: {response.status_code}")
        
        if response.status_code == 200:
            print("🧠 NLP分析成功")
        else:
            print(f"⚠️  NLP响应: {response.text[:200]}")
            
    except Exception as e:
        print(f"❌ NLP分析异常: {e}")
    
    print("\n🎉 匿名用户功能测试完成！")

if __name__ == "__main__":
    test_anonymous_access()