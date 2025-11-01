"""
NLP微服务主应用
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 18:00:00
"""

import os
import logging
import traceback
from datetime import datetime
from flask import Flask, request, jsonify
from flask_cors import CORS
from dotenv import load_dotenv

# 导入服务模块
from services.text_analyzer import TextAnalyzer
from services.word_frequency import WordFrequencyAnalyzer
from services.timeline_extractor import TimelineExtractor
from services.geography_analyzer import GeographyAnalyzer
from services.text_summarizer import TextSummarizer

# 加载环境变量
load_dotenv()

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('nlp_service.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

# 创建Flask应用
app = Flask(__name__)

# 配置CORS
CORS(app, origins=['http://localhost:3000', 'http://127.0.0.1:3000'])

# 配置
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max file size

# 数据清理函数
def _clean_dict_for_json(obj):
    """清理字典数据，移除None键和不可序列化的值"""
    if isinstance(obj, dict):
        cleaned = {}
        for key, value in obj.items():
            if key is not None:  # 移除None键
                cleaned_value = _clean_dict_for_json(value)
                if cleaned_value is not None:  # 移除None值
                    cleaned[str(key)] = cleaned_value  # 确保键是字符串
        return cleaned
    elif isinstance(obj, list):
        return [_clean_dict_for_json(item) for item in obj if item is not None]
    elif isinstance(obj, (str, int, float, bool)):
        return obj
    elif obj is None:
        return None
    else:
        # 对于其他类型，尝试转换为字符串
        try:
            return str(obj)
        except:
            return None

# 初始化各个分析器
text_analyzer = TextAnalyzer()
word_freq_analyzer = WordFrequencyAnalyzer()
timeline_extractor = TimelineExtractor()
geographic_extractor = GeographyAnalyzer()
text_summarizer = TextSummarizer()

@app.route('/api/health', methods=['GET'])
def health_check():
    """健康检查接口"""
    try:
        return jsonify({
            'status': 'healthy',
            'service': 'NLP微服务',
            'version': '1.0.0',
            'timestamp': datetime.now().isoformat(),
            'message': '服务运行正常'
        }), 200
    except Exception as e:
        logger.error(f"健康检查失败: {str(e)}")
        return jsonify({
            'status': 'unhealthy',
            'error': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@app.route('/api/analyze/text', methods=['POST'])
def analyze_text():
    """文本分析接口"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'message': '请提供要分析的文本内容',
                'error_code': 'MISSING_TEXT'
            }), 400
        
        text = data['text']
        analysis_type = data.get('type', 'comprehensive')  # 默认综合分析
        
        if not text.strip():
            return jsonify({
                'success': False,
                'message': '文本内容不能为空',
                'error_code': 'EMPTY_TEXT'
            }), 400
        
        logger.info(f"开始文本分析, 文本长度: {len(text)}, 分析类型: {analysis_type}")
        
        # 执行文本分析
        result = text_analyzer.analyze(text, analysis_type)
        
        logger.info(f"文本分析完成, 结果: {len(result)} 项")
        
        return jsonify({
            'success': True,
            'message': '文本分析完成',
            'data': result,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"文本分析失败: {str(e)}")
        logger.error(f"错误详情: {traceback.format_exc()}")
        return jsonify({
            'success': False,
            'message': '文本分析过程中发生错误',
            'error': str(e),
            'error_code': 'ANALYSIS_ERROR'
        }), 500

@app.route('/api/analyze/word-frequency', methods=['POST'])
def analyze_word_frequency():
    """词频分析接口"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'message': '请提供要分析的文本内容',
                'error_code': 'MISSING_TEXT'
            }), 400
        
        text = data['text']
        top_n = data.get('top_n', 50)  # 默认返回前50个高频词
        min_length = data.get('min_length', 2)  # 最小词长度
        
        if not text.strip():
            return jsonify({
                'success': False,
                'message': '文本内容不能为空',
                'error_code': 'EMPTY_TEXT'
            }), 400
        
        logger.info(f"开始词频分析, 文本长度: {len(text)}, top_n: {top_n}")
        
        # 执行词频分析
        result = word_freq_analyzer.analyze(text, top_n, min_length)
        
        logger.info(f"词频分析完成, 找到 {len(result.get('word_frequencies', []))} 个词")
        
        return jsonify({
            'success': True,
            'message': '词频分析完成',
            'data': result,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"词频分析失败: {str(e)}")
        logger.error(f"错误详情: {traceback.format_exc()}")
        return jsonify({
            'success': False,
            'message': '词频分析过程中发生错误',
            'error': str(e),
            'error_code': 'WORD_FREQUENCY_ERROR'
        }), 500

@app.route('/api/analyze/timeline', methods=['POST'])
def analyze_timeline():
    """时间轴分析接口"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'message': '请提供要分析的文本内容',
                'error_code': 'MISSING_TEXT'
            }), 400
        
        text = data['text']
        
        if not text.strip():
            return jsonify({
                'success': False,
                'message': '文本内容不能为空',
                'error_code': 'EMPTY_TEXT'
            }), 400
        
        logger.info(f"开始时间轴分析, 文本长度: {len(text)}")
        
        # 执行时间轴分析
        result = timeline_extractor.analyze(text)
        
        logger.info(f"时间轴分析完成, 找到 {len(result.get('events', []))} 个事件")
        
        return jsonify({
            'success': True,
            'message': '时间轴分析完成',
            'data': result,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"时间轴分析失败: {str(e)}")
        logger.error(f"错误详情: {traceback.format_exc()}")
        return jsonify({
            'success': False,
            'message': '时间轴分析过程中发生错误',
            'error': str(e),
            'error_code': 'TIMELINE_ERROR'
        }), 500

@app.route('/api/analyze/geographic', methods=['POST'])
def analyze_geographic():
    """地理位置分析接口"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'message': '请提供要分析的文本内容',
                'error_code': 'MISSING_TEXT'
            }), 400
        
        text = data['text']
        
        if not text.strip():
            return jsonify({
                'success': False,
                'message': '文本内容不能为空',
                'error_code': 'EMPTY_TEXT'
            }), 400
        
        logger.info(f"开始地理位置分析, 文本长度: {len(text)}")
        
        # 执行地理位置分析
        result = geographic_extractor.analyze(text)
        
        logger.info(f"地理位置分析完成, 找到 {len(result.get('locations', []))} 个位置")
        
        return jsonify({
            'success': True,
            'message': '地理位置分析完成',
            'data': result,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"地理位置分析失败: {str(e)}")
        logger.error(f"错误详情: {traceback.format_exc()}")
        return jsonify({
            'success': False,
            'message': '地理位置分析过程中发生错误',
            'error': str(e),
            'error_code': 'GEOGRAPHIC_ERROR'
        }), 500

@app.route('/api/analyze/summary', methods=['POST'])
def analyze_summary():
    """文本摘要分析接口"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'message': '请提供要分析的文本内容',
                'error_code': 'MISSING_TEXT'
            }), 400
        
        text = data['text']
        summary_type = data.get('type', 'comprehensive')  # 摘要类型
        max_sentences = data.get('max_sentences', 5)  # 最大句子数
        
        if not text.strip():
            return jsonify({
                'success': False,
                'message': '文本内容不能为空',
                'error_code': 'EMPTY_TEXT'
            }), 400
        
        logger.info(f"开始文本摘要分析, 文本长度: {len(text)}, 类型: {summary_type}")
        
        # 执行文本摘要分析
        options = {
            'type': summary_type,
            'max_sentences': max_sentences
        }
        result = text_summarizer.analyze(text, options)
        
        logger.info(f"文本摘要分析完成")
        
        return jsonify({
            'success': True,
            'message': '文本摘要分析完成',
            'data': result,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"文本摘要分析失败: {str(e)}")
        logger.error(f"错误详情: {traceback.format_exc()}")
        return jsonify({
            'success': False,
            'message': '文本摘要分析过程中发生错误',
            'error': str(e),
            'error_code': 'SUMMARY_ERROR'
        }), 500

@app.route('/api/analyze/comprehensive', methods=['POST'])
def analyze_comprehensive():
    """综合分析接口 - 一次性执行所有分析"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'success': False,
                'message': '请提供要分析的文本内容',
                'error_code': 'MISSING_TEXT'
            }), 400
        
        text = data['text']
        
        if not text.strip():
            return jsonify({
                'success': False,
                'message': '文本内容不能为空',
                'error_code': 'EMPTY_TEXT'
            }), 400
        
        logger.info(f"开始综合分析, 文本长度: {len(text)}")
        
        # 执行所有分析
        results = {}
        
        # 1. 基础文本分析
        try:
            results['text_analysis'] = text_analyzer.analyze(text, 'comprehensive')
        except Exception as e:
            logger.error(f"文本分析失败: {str(e)}")
            results['text_analysis'] = {'error': str(e)}
        
        # 2. 词频分析
        try:
            results['word_frequency'] = word_freq_analyzer.analyze(text, max_results=50, min_length=2)
        except Exception as e:
            logger.error(f"词频分析失败: {str(e)}")
            results['word_frequency'] = {'error': str(e)}
        
        # 3. 时间轴分析
        try:
            results['timeline'] = timeline_extractor.analyze(text)
        except Exception as e:
            logger.error(f"时间轴分析失败: {str(e)}")
            results['timeline'] = {'error': str(e)}
        
        # 4. 地理位置分析
        try:
            results['geographic'] = geographic_extractor.analyze(text)
        except Exception as e:
            logger.error(f"地理位置分析失败: {str(e)}")
            results['geographic'] = {'error': str(e)}
        
        # 5. 文本摘要
        try:
            summary_options = {'type': 'comprehensive', 'max_sentences': 5}
            results['summary'] = text_summarizer.analyze(text, summary_options)
        except Exception as e:
            logger.error(f"文本摘要失败: {str(e)}")
            results['summary'] = {'error': str(e)}
        
        # 清理结果数据，移除None键
        cleaned_results = _clean_dict_for_json(results)
        
        logger.info("综合分析完成")
        
        return jsonify({
            'success': True,
            'message': '综合分析完成',
            'data': cleaned_results,
            'timestamp': datetime.now().isoformat()
        }), 200
        
    except Exception as e:
        logger.error(f"综合分析失败: {str(e)}")
        logger.error(f"错误详情: {traceback.format_exc()}")
        return jsonify({
            'success': False,
            'message': '综合分析过程中发生错误',
            'error': str(e),
            'error_code': 'COMPREHENSIVE_ERROR'
        }), 500

@app.errorhandler(404)
def not_found(error):
    """404错误处理"""
    return jsonify({
        'success': False,
        'message': '请求的接口不存在',
        'error_code': 'NOT_FOUND'
    }), 404

@app.errorhandler(405)
def method_not_allowed(error):
    """405错误处理"""
    return jsonify({
        'success': False,
        'message': '请求方法不被允许',
        'error_code': 'METHOD_NOT_ALLOWED'
    }), 405

@app.errorhandler(500)
def internal_error(error):
    """500错误处理"""
    logger.error(f"内部服务器错误: {str(error)}")
    return jsonify({
        'success': False,
        'message': '内部服务器错误',
        'error_code': 'INTERNAL_ERROR'
    }), 500

if __name__ == '__main__':
    # 获取配置
    host = os.getenv('FLASK_HOST', '0.0.0.0')
    port = int(os.getenv('FLASK_PORT', 5001))
    debug = os.getenv('FLASK_DEBUG', 'True').lower() == 'true'
    
    logger.info(f"启动NLP微服务...")
    logger.info(f"服务地址: http://{host}:{port}")
    logger.info(f"调试模式: {debug}")
    
    # 启动Flask应用
    app.run(
        host=host,
        port=port,
        debug=debug,
        threaded=True  # 支持多线程
    )