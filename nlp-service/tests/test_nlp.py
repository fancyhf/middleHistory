#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
NLP服务测试脚本
Author: AI Agent
Version: 1.0.0
Created: 2024-01-15
"""

import requests
import json
import time

NLP_BASE_URL = 'http://localhost:5001/api'

def test_nlp_service():
    """测试NLP服务的各项功能"""
    print('开始测试NLP服务...')
    
    test_text = '明朝永乐年间，郑和率领庞大的船队七次下西洋，访问了东南亚、印度洋、阿拉伯海、红海等地区的30多个国家和地区。这些航海活动不仅展示了中国古代的航海技术和造船工艺，也促进了中外文化交流和贸易往来。'
    
    tests = [
        {
            'name': '健康检查',
            'endpoint': '/health',
            'method': 'GET'
        },
        {
            'name': '词频分析',
            'endpoint': '/analyze/word-frequency',
            'method': 'POST',
            'body': {
                'text': test_text,
                'max_results': 10,
                'min_length': 2
            }
        },
        {
            'name': '时间轴分析',
            'endpoint': '/analyze/timeline',
            'method': 'POST',
            'body': {
                'text': test_text
            }
        },
        {
            'name': '地理分析',
            'endpoint': '/analyze/geographic',
            'method': 'POST',
            'body': {
                'text': test_text
            }
        },
        {
            'name': '文本摘要',
            'endpoint': '/analyze/summary',
            'method': 'POST',
            'body': {
                'text': test_text,
                'summary_type': 'extractive',
                'max_sentences': 2
            }
        }
    ]
    
    results = []
    
    for test in tests:
        print(f'\n正在测试: {test["name"]}')
        start_time = time.time()
        
        try:
            url = f'{NLP_BASE_URL}{test["endpoint"]}'
            
            if test['method'] == 'GET':
                response = requests.get(url, timeout=30)
            else:
                response = requests.post(url, json=test['body'], timeout=30)
            
            duration = int((time.time() - start_time) * 1000)
            
            if response.status_code == 200:
                data = response.json()
                print(f'✅ {test["name"]} 成功 ({duration}ms)')
                print(f'响应数据: {json.dumps(data, ensure_ascii=False, indent=2)}')
                results.append({
                    'name': test['name'],
                    'success': True,
                    'duration': duration,
                    'data': data
                })
            else:
                print(f'❌ {test["name"]} 失败 ({duration}ms)')
                print(f'状态码: {response.status_code}')
                print(f'错误信息: {response.text}')
                results.append({
                    'name': test['name'],
                    'success': False,
                    'duration': duration,
                    'error': f'HTTP {response.status_code}: {response.text}'
                })
        except Exception as error:
            duration = int((time.time() - start_time) * 1000)
            print(f'❌ {test["name"]} 异常 ({duration}ms)')
            print(f'异常信息: {str(error)}')
            results.append({
                'name': test['name'],
                'success': False,
                'duration': duration,
                'error': str(error)
            })
    
    # 输出测试总结
    print('\n=== 测试总结 ===')
    success_count = sum(1 for r in results if r['success'])
    total_count = len(results)
    print(f'总测试数: {total_count}')
    print(f'成功数: {success_count}')
    print(f'失败数: {total_count - success_count}')
    print(f'成功率: {(success_count / total_count * 100):.1f}%')
    
    return results

if __name__ == '__main__':
    test_nlp_service()