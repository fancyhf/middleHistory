"""
NLP服务综合功能测试脚本
Author: AI Agent
Version: 1.0.0
Created: 2024-01-15
"""

import requests
import json
import time
from typing import Dict, List, Any, Optional
import logging
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('nlp_test_results.log', encoding='utf-8'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

class NlpServiceTester:
    """NLP服务测试器类"""
    
    def __init__(self, base_url: str = "http://127.0.0.1:5001"):
        """
        初始化测试器
        
        Args:
            base_url (str): NLP服务的基础URL
        """
        self.base_url = base_url
        self.test_results = []
        self.test_data = self._prepare_test_data()
        
    def _prepare_test_data(self) -> Dict[str, str]:
        """
        准备测试数据
        
        Returns:
            Dict[str, str]: 测试文本数据
        """
        return {
            "short_text": "秦始皇统一六国，建立了中国历史上第一个统一的封建王朝。",
            "medium_text": """
            唐朝是中国历史上的黄金时代，从公元618年到907年，历时289年。
            唐太宗李世民开创了贞观之治，国力强盛，文化繁荣。
            长安城是当时世界上最大的城市，人口超过百万。
            唐朝的丝绸之路贸易繁荣，与西域各国交流频繁。
            诗歌艺术达到顶峰，李白、杜甫等诗人留下了不朽的作品。
            """,
            "long_text": """
            明朝（1368年-1644年）是中国历史上最后一个由汉族建立的大一统王朝。
            朱元璋在南京建立明朝，年号洪武。明朝初期，朱元璋实行严厉的中央集权制度。
            永乐帝朱棣迁都北京，修建了紫禁城，并派遣郑和七下西洋。
            明朝中期，商品经济发达，出现了资本主义萌芽。
            明朝后期，政治腐败，自然灾害频发，农民起义不断。
            1644年，李自成攻占北京，崇祯帝自缢，明朝灭亡。
            同年，清军入关，建立清朝统治。
            明朝在科技、文化、艺术等方面都有重要成就。
            四大名著中的《西游记》、《水浒传》、《三国演义》都产生于明朝。
            明朝的建筑艺术也达到了很高水平，紫禁城就是其代表作。
            """
        }
    
    def _make_request(self, endpoint: str, method: str = "GET", data: Optional[Dict] = None) -> Dict[str, Any]:
        """
        发送HTTP请求
        
        Args:
            endpoint (str): API端点
            method (str): HTTP方法
            data (Optional[Dict]): 请求数据
            
        Returns:
            Dict[str, Any]: 响应结果
        """
        url = f"{self.base_url}{endpoint}"
        start_time = time.time()
        
        try:
            if method.upper() == "GET":
                response = requests.get(url, timeout=30)
            elif method.upper() == "POST":
                response = requests.post(url, json=data, timeout=30)
            else:
                raise ValueError(f"不支持的HTTP方法: {method}")
            
            end_time = time.time()
            response_time = round((end_time - start_time) * 1000, 2)  # 毫秒
            
            return {
                "success": response.status_code == 200,
                "status_code": response.status_code,
                "response_time": response_time,
                "data": response.json() if response.status_code == 200 else None,
                "error": response.text if response.status_code != 200 else None
            }
            
        except requests.exceptions.RequestException as e:
            end_time = time.time()
            response_time = round((end_time - start_time) * 1000, 2)
            
            return {
                "success": False,
                "status_code": 0,
                "response_time": response_time,
                "data": None,
                "error": str(e)
            }
    
    def test_health_check(self) -> Dict[str, Any]:
        """
        测试健康检查接口
        
        Returns:
            Dict[str, Any]: 测试结果
        """
        logger.info("开始测试健康检查接口...")
        
        result = self._make_request("/api/health")
        
        test_result = {
            "test_name": "健康检查",
            "endpoint": "/api/health",
            "method": "GET",
            "success": result["success"],
            "response_time": result["response_time"],
            "status_code": result["status_code"],
            "data": result["data"],
            "error": result["error"]
        }
        
        if result["success"]:
            logger.info(f"健康检查测试通过，响应时间: {result['response_time']}ms")
        else:
            logger.error(f"健康检查测试失败: {result['error']}")
        
        return test_result
    
    def test_text_analysis(self) -> Dict[str, Any]:
        """
        测试文本结构分析接口
        
        Returns:
            Dict[str, Any]: 测试结果
        """
        logger.info("开始测试文本结构分析接口...")
        
        test_data = {"text": self.test_data["medium_text"]}
        result = self._make_request("/api/analyze/text", "POST", test_data)
        
        test_result = {
            "test_name": "文本结构分析",
            "endpoint": "/api/analyze/text",
            "method": "POST",
            "success": result["success"],
            "response_time": result["response_time"],
            "status_code": result["status_code"],
            "data": result["data"],
            "error": result["error"],
            "input_text_length": len(test_data["text"])
        }
        
        if result["success"]:
            logger.info(f"文本结构分析测试通过，响应时间: {result['response_time']}ms")
            if result["data"]:
                logger.info(f"分析结果包含字段: {list(result['data'].keys())}")
        else:
            logger.error(f"文本结构分析测试失败: {result['error']}")
        
        return test_result
    
    def test_word_frequency_analysis(self) -> Dict[str, Any]:
        """
        测试词频统计分析接口
        
        Returns:
            Dict[str, Any]: 测试结果
        """
        logger.info("开始测试词频统计分析接口...")
        
        test_data = {"text": self.test_data["long_text"]}
        result = self._make_request("/api/analyze/word-frequency", "POST", test_data)
        
        test_result = {
            "test_name": "词频统计分析",
            "endpoint": "/api/analyze/word-frequency",
            "method": "POST",
            "success": result["success"],
            "response_time": result["response_time"],
            "status_code": result["status_code"],
            "data": result["data"],
            "error": result["error"],
            "input_text_length": len(test_data["text"])
        }
        
        if result["success"]:
            logger.info(f"词频统计分析测试通过，响应时间: {result['response_time']}ms")
            if result["data"] and "word_frequency" in result["data"]:
                word_count = len(result["data"]["word_frequency"])
                logger.info(f"识别出 {word_count} 个不同词汇")
        else:
            logger.error(f"词频统计分析测试失败: {result['error']}")
        
        return test_result
    
    def test_timeline_extraction(self) -> Dict[str, Any]:
        """
        测试时间轴事件提取接口
        
        Returns:
            Dict[str, Any]: 测试结果
        """
        logger.info("开始测试时间轴事件提取接口...")
        
        test_data = {"text": self.test_data["long_text"]}
        result = self._make_request("/api/analyze/timeline", "POST", test_data)
        
        test_result = {
            "test_name": "时间轴事件提取",
            "endpoint": "/api/analyze/timeline",
            "method": "POST",
            "success": result["success"],
            "response_time": result["response_time"],
            "status_code": result["status_code"],
            "data": result["data"],
            "error": result["error"],
            "input_text_length": len(test_data["text"])
        }
        
        if result["success"]:
            logger.info(f"时间轴事件提取测试通过，响应时间: {result['response_time']}ms")
            if result["data"] and "timeline_events" in result["data"]:
                event_count = len(result["data"]["timeline_events"])
                logger.info(f"提取出 {event_count} 个时间轴事件")
        else:
            logger.error(f"时间轴事件提取测试失败: {result['error']}")
        
        return test_result
    
    def test_geographic_analysis(self) -> Dict[str, Any]:
        """
        测试地理位置识别接口
        
        Returns:
            Dict[str, Any]: 测试结果
        """
        logger.info("开始测试地理位置识别接口...")
        
        test_data = {"text": self.test_data["medium_text"]}
        result = self._make_request("/api/analyze/geographic", "POST", test_data)
        
        test_result = {
            "test_name": "地理位置识别",
            "endpoint": "/api/analyze/geographic",
            "method": "POST",
            "success": result["success"],
            "response_time": result["response_time"],
            "status_code": result["status_code"],
            "data": result["data"],
            "error": result["error"],
            "input_text_length": len(test_data["text"])
        }
        
        if result["success"]:
            logger.info(f"地理位置识别测试通过，响应时间: {result['response_time']}ms")
            if result["data"] and "locations" in result["data"]:
                location_count = len(result["data"]["locations"])
                logger.info(f"识别出 {location_count} 个地理位置")
        else:
            logger.error(f"地理位置识别测试失败: {result['error']}")
        
        return test_result
    
    def test_text_summary(self) -> Dict[str, Any]:
        """
        测试文本摘要生成接口
        
        Returns:
            Dict[str, Any]: 测试结果
        """
        logger.info("开始测试文本摘要生成接口...")
        
        test_data = {"text": self.test_data["long_text"]}
        result = self._make_request("/api/analyze/summary", "POST", test_data)
        
        test_result = {
            "test_name": "文本摘要生成",
            "endpoint": "/api/analyze/summary",
            "method": "POST",
            "success": result["success"],
            "response_time": result["response_time"],
            "status_code": result["status_code"],
            "data": result["data"],
            "error": result["error"],
            "input_text_length": len(test_data["text"])
        }
        
        if result["success"]:
            logger.info(f"文本摘要生成测试通过，响应时间: {result['response_time']}ms")
            if result["data"] and "summary" in result["data"]:
                summary_length = len(result["data"]["summary"])
                logger.info(f"生成摘要长度: {summary_length} 字符")
        else:
            logger.error(f"文本摘要生成测试失败: {result['error']}")
        
        return test_result
    
    def test_comprehensive_analysis(self) -> Dict[str, Any]:
        """
        测试综合分析接口
        
        Returns:
            Dict[str, Any]: 测试结果
        """
        logger.info("开始测试综合分析接口...")
        
        test_data = {"text": self.test_data["long_text"]}
        result = self._make_request("/api/analyze/comprehensive", "POST", test_data)
        
        test_result = {
            "test_name": "综合分析",
            "endpoint": "/api/analyze/comprehensive",
            "method": "POST",
            "success": result["success"],
            "response_time": result["response_time"],
            "status_code": result["status_code"],
            "data": result["data"],
            "error": result["error"],
            "input_text_length": len(test_data["text"])
        }
        
        if result["success"]:
            logger.info(f"综合分析测试通过，响应时间: {result['response_time']}ms")
            if result["data"]:
                analysis_types = list(result["data"].keys())
                logger.info(f"综合分析包含: {', '.join(analysis_types)}")
        else:
            logger.error(f"综合分析测试失败: {result['error']}")
        
        return test_result
    
    def run_all_tests(self) -> List[Dict[str, Any]]:
        """
        运行所有测试
        
        Returns:
            List[Dict[str, Any]]: 所有测试结果
        """
        logger.info("=" * 60)
        logger.info("开始NLP服务综合功能测试")
        logger.info("=" * 60)
        
        test_methods = [
            self.test_health_check,
            self.test_text_analysis,
            self.test_word_frequency_analysis,
            self.test_timeline_extraction,
            self.test_geographic_analysis,
            self.test_text_summary,
            self.test_comprehensive_analysis
        ]
        
        for test_method in test_methods:
            try:
                result = test_method()
                self.test_results.append(result)
                logger.info("-" * 40)
            except Exception as e:
                logger.error(f"测试 {test_method.__name__} 时发生异常: {str(e)}")
                self.test_results.append({
                    "test_name": test_method.__name__,
                    "success": False,
                    "error": str(e)
                })
        
        return self.test_results
    
    def generate_test_report(self) -> str:
        """
        生成测试报告
        
        Returns:
            str: 测试报告内容
        """
        if not self.test_results:
            return "没有测试结果可生成报告"
        
        total_tests = len(self.test_results)
        successful_tests = sum(1 for result in self.test_results if result.get("success", False))
        failed_tests = total_tests - successful_tests
        
        report = []
        report.append("=" * 80)
        report.append("NLP服务功能测试报告")
        report.append("=" * 80)
        report.append(f"测试时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        report.append(f"测试服务: {self.base_url}")
        report.append(f"总测试数: {total_tests}")
        report.append(f"成功测试: {successful_tests}")
        report.append(f"失败测试: {failed_tests}")
        report.append(f"成功率: {(successful_tests/total_tests*100):.1f}%")
        report.append("")
        
        # 详细测试结果
        report.append("详细测试结果:")
        report.append("-" * 80)
        
        for i, result in enumerate(self.test_results, 1):
            report.append(f"{i}. {result.get('test_name', 'Unknown Test')}")
            report.append(f"   接口: {result.get('endpoint', 'N/A')}")
            report.append(f"   方法: {result.get('method', 'N/A')}")
            report.append(f"   状态: {'✓ 成功' if result.get('success') else '✗ 失败'}")
            
            if result.get("success"):
                report.append(f"   响应时间: {result.get('response_time', 'N/A')}ms")
                report.append(f"   状态码: {result.get('status_code', 'N/A')}")
                
                # 显示部分响应数据
                if result.get("data"):
                    data_keys = list(result["data"].keys())[:3]  # 只显示前3个键
                    report.append(f"   响应字段: {', '.join(data_keys)}")
            else:
                report.append(f"   错误信息: {result.get('error', 'Unknown error')}")
            
            report.append("")
        
        # 性能统计
        successful_results = [r for r in self.test_results if r.get("success") and r.get("response_time")]
        if successful_results:
            response_times = [r["response_time"] for r in successful_results]
            avg_response_time = sum(response_times) / len(response_times)
            max_response_time = max(response_times)
            min_response_time = min(response_times)
            
            report.append("性能统计:")
            report.append("-" * 40)
            report.append(f"平均响应时间: {avg_response_time:.2f}ms")
            report.append(f"最大响应时间: {max_response_time:.2f}ms")
            report.append(f"最小响应时间: {min_response_time:.2f}ms")
            report.append("")
        
        # 建议和总结
        report.append("测试总结:")
        report.append("-" * 40)
        if failed_tests == 0:
            report.append("✓ 所有测试均通过，NLP服务运行正常")
        else:
            report.append(f"✗ 有 {failed_tests} 个测试失败，需要检查相关功能")
        
        report.append("=" * 80)
        
        return "\n".join(report)
    
    def save_test_report(self, filename: str = "nlp_test_report.txt") -> None:
        """
        保存测试报告到文件
        
        Args:
            filename (str): 报告文件名
        """
        report_content = self.generate_test_report()
        
        try:
            with open(filename, 'w', encoding='utf-8') as f:
                f.write(report_content)
            logger.info(f"测试报告已保存到: {filename}")
        except Exception as e:
            logger.error(f"保存测试报告失败: {str(e)}")

def main():
    """主函数"""
    try:
        # 创建测试器实例
        tester = NlpServiceTester()
        
        # 运行所有测试
        results = tester.run_all_tests()
        
        # 生成并显示测试报告
        report = tester.generate_test_report()
        print("\n" + report)
        
        # 保存测试报告
        tester.save_test_report()
        
        # 保存详细结果为JSON
        with open('nlp_test_results.json', 'w', encoding='utf-8') as f:
            json.dump(results, f, ensure_ascii=False, indent=2)
        
        logger.info("测试完成！详细结果已保存到 nlp_test_results.json")
        
    except Exception as e:
        logger.error(f"测试过程中发生异常: {str(e)}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())