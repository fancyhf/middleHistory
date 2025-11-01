"""
NLP API测试脚本
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 18:00:00
"""

import requests
import json
from datetime import datetime

# NLP服务基础URL
BASE_URL = "http://127.0.0.1:5001"

def test_health_check():
    """测试健康检查接口"""
    print("=== 测试健康检查接口 ===")
    try:
        response = requests.get(f"{BASE_URL}/health")
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_text_analysis():
    """测试文本分析接口"""
    print("\n=== 测试文本分析接口 ===")
    test_text = """
    中华人民共和国成立于1949年10月1日。新中国的成立标志着中国人民从此站起来了。
    在中国共产党的领导下，中国人民经过艰苦奋斗，取得了举世瞩目的成就。
    改革开放以来，中国经济快速发展，人民生活水平不断提高。
    """
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/analyze/text",
            json={"text": test_text, "analysis_type": "comprehensive"}
        )
        print(f"状态码: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"文本长度: {result.get('basic_statistics', {}).get('total_characters', 0)}")
            print(f"句子数量: {result.get('sentence_analysis', {}).get('total_sentences', 0)}")
            print(f"段落数量: {result.get('paragraph_analysis', {}).get('total_paragraphs', 0)}")
        else:
            print(f"错误响应: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_word_frequency():
    """测试词频分析接口"""
    print("\n=== 测试词频分析接口 ===")
    test_text = """
    人工智能是计算机科学的一个分支。人工智能技术在各个领域都有广泛应用。
    机器学习是人工智能的重要组成部分。深度学习是机器学习的一个子领域。
    自然语言处理技术可以帮助计算机理解人类语言。
    """
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/analyze/word-frequency",
            json={
                "text": test_text,
                "language": "chinese",
                "max_results": 20,
                "remove_stopwords": True
            }
        )
        print(f"状态码: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"总词数: {result.get('statistics', {}).get('filtered_words', 0)}")
            print(f"唯一词数: {result.get('statistics', {}).get('unique_words', 0)}")
            print("高频词汇:")
            for word_info in result.get('word_frequency', [])[:5]:
                print(f"  {word_info['word']}: {word_info['count']} 次 ({word_info['frequency']:.3f})")
        else:
            print(f"错误响应: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_timeline_extraction():
    """测试时间轴提取接口"""
    print("\n=== 测试时间轴提取接口 ===")
    test_text = """
    1949年10月1日，中华人民共和国成立。1978年12月，中国开始实行改革开放政策。
    2001年中国加入世界贸易组织。2008年北京成功举办奥运会。
    2020年中国全面建成小康社会。
    """
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/analyze/timeline",
            json={"text": test_text}
        )
        print(f"状态码: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"发现事件数量: {result.get('total_events', 0)}")
            print("时间轴事件:")
            for event in result.get('timeline_events', [])[:3]:
                print(f"  {event['date']}: {event['event']}")
        else:
            print(f"错误响应: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_geographic_extraction():
    """测试地理位置提取接口"""
    print("\n=== 测试地理位置提取接口 ===")
    test_text = """
    北京是中国的首都，位于华北平原。上海是中国最大的城市，位于长江三角洲。
    广州是广东省的省会城市。深圳是中国的经济特区之一。
    杭州以西湖闻名，是浙江省的省会。
    """
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/analyze/geographic",
            json={"text": test_text}
        )
        print(f"状态码: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"发现地点数量: {result.get('total_locations', 0)}")
            print("地理位置:")
            for location in result.get('locations', [])[:3]:
                print(f"  {location['name']} ({location['type']}): 提及{location['mentions']}次")
        else:
            print(f"错误响应: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_text_summary():
    """测试文本摘要接口"""
    print("\n=== 测试文本摘要接口 ===")
    test_text = """
    人工智能（Artificial Intelligence，AI）是计算机科学的一个分支，它企图了解智能的实质，
    并生产出一种新的能以人类智能相似的方式做出反应的智能机器。该领域的研究包括机器人、
    语言识别、图像识别、自然语言处理和专家系统等。人工智能从诞生以来，理论和技术日益成熟，
    应用领域也不断扩大，可以设想，未来人工智能带来的科技产品，将会是人类智慧的"容器"。
    人工智能可以对人的意识、思维的信息过程的模拟。人工智能不是人的智能，但能像人那样思考、
    也可能超过人的智能。
    """
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/analyze/summary",
            json={"text": test_text, "max_length": 100}
        )
        print(f"状态码: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"原文长度: {result.get('original_length', 0)}")
            print(f"摘要长度: {result.get('summary_length', 0)}")
            print(f"压缩比: {result.get('compression_ratio', 0):.2f}")
            print(f"摘要: {result.get('summary', '')}")
        else:
            print(f"错误响应: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def test_comprehensive_analysis():
    """测试综合分析接口"""
    print("\n=== 测试综合分析接口 ===")
    test_text = """
    中国古代历史悠久，文化灿烂。从夏朝开始，中国就有了成文的历史记录。
    秦始皇统一中国后，建立了中央集权制度。汉朝开创了丝绸之路，促进了东西方文化交流。
    唐朝是中国历史上的鼎盛时期，长安成为世界性的大都市。
    宋朝在科技和文化方面取得了巨大成就。明朝郑和七下西洋，展现了中国的海上实力。
    """
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/analyze/comprehensive",
            json={"text": test_text}
        )
        print(f"状态码: {response.status_code}")
        if response.status_code == 200:
            result = response.json()
            print(f"分析组件: {result.get('analysis_summary', {}).get('components_analyzed', [])}")
            print(f"文本长度: {result.get('analysis_summary', {}).get('text_length', 0)}")
            
            # 显示文本分析结果
            text_analysis = result.get('text_analysis', {})
            if 'basic_statistics' in text_analysis:
                stats = text_analysis['basic_statistics']
                print(f"基础统计 - 字符数: {stats.get('total_characters', 0)}, 行数: {stats.get('total_lines', 0)}")
            
            # 显示词频分析结果
            word_freq = result.get('word_frequency_analysis', {})
            if 'statistics' in word_freq:
                stats = word_freq['statistics']
                print(f"词频统计 - 总词数: {stats.get('filtered_words', 0)}, 唯一词数: {stats.get('unique_words', 0)}")
        else:
            print(f"错误响应: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"错误: {e}")
        return False

def main():
    """主测试函数"""
    print(f"开始NLP API测试 - {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)
    
    tests = [
        ("健康检查", test_health_check),
        ("文本分析", test_text_analysis),
        ("词频分析", test_word_frequency),
        ("时间轴提取", test_timeline_extraction),
        ("地理位置提取", test_geographic_extraction),
        ("文本摘要", test_text_summary),
        ("综合分析", test_comprehensive_analysis)
    ]
    
    passed = 0
    total = len(tests)
    
    for test_name, test_func in tests:
        try:
            if test_func():
                print(f"✅ {test_name} - 通过")
                passed += 1
            else:
                print(f"❌ {test_name} - 失败")
        except Exception as e:
            print(f"❌ {test_name} - 异常: {e}")
    
    print("\n" + "=" * 60)
    print(f"测试完成: {passed}/{total} 通过")
    print(f"成功率: {passed/total*100:.1f}%")

if __name__ == "__main__":
    main()