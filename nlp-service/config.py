"""
NLP服务配置文件
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 17:45:00

配置项：
- 服务器配置
- 日志配置
- 分析参数配置
"""

import os
import logging
from typing import Dict, Any

class Config:
    """基础配置类"""
    
    # 服务器配置
    HOST = os.getenv('NLP_HOST', '0.0.0.0')
    PORT = int(os.getenv('NLP_PORT', 5001))
    DEBUG = os.getenv('NLP_DEBUG', 'False').lower() == 'true'
    
    # 日志配置
    LOG_LEVEL = os.getenv('LOG_LEVEL', 'INFO')
    LOG_FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    LOG_FILE = os.getenv('LOG_FILE', 'nlp_service.log')
    
    # CORS配置
    CORS_ORIGINS = os.getenv('CORS_ORIGINS', '*').split(',')
    
    # 分析参数配置
    ANALYSIS_CONFIG = {
        'text_analysis': {
            'max_text_length': 50000,  # 最大文本长度
            'min_word_length': 2,      # 最小词长
            'enable_pos_tagging': True, # 启用词性标注
            'enable_ner': True,        # 启用命名实体识别
        },
        'word_frequency': {
            'max_words': 1000,         # 最大词数
            'min_frequency': 2,        # 最小词频
            'filter_stopwords': True,  # 过滤停用词
            'filter_punctuation': True, # 过滤标点符号
        },
        'timeline': {
            'max_events': 100,         # 最大事件数
            'min_confidence': 0.3,     # 最小置信度
            'enable_dynasty_mapping': True, # 启用朝代映射
            'enable_event_classification': True, # 启用事件分类
        },
        'geography': {
            'max_locations': 50,       # 最大地点数
            'min_confidence': 0.3,     # 最小置信度
            'enable_coordinates': True, # 启用坐标获取
            'enable_historical_names': True, # 启用历史地名
        },
        'summary': {
            'max_sentences': 5,        # 最大句子数
            'max_length': 300,         # 最大摘要长度
            'num_topics': 5,           # 主题数量
            'compression_ratio': 0.3,  # 压缩比
        }
    }
    
    # 缓存配置
    CACHE_CONFIG = {
        'enable_cache': True,
        'cache_timeout': 3600,  # 1小时
        'max_cache_size': 1000  # 最大缓存条目数
    }
    
    # 性能配置
    PERFORMANCE_CONFIG = {
        'max_concurrent_requests': 10,  # 最大并发请求数
        'request_timeout': 30,          # 请求超时时间（秒）
        'enable_async': False,          # 启用异步处理
    }

class DevelopmentConfig(Config):
    """开发环境配置"""
    DEBUG = True
    LOG_LEVEL = 'DEBUG'

class ProductionConfig(Config):
    """生产环境配置"""
    DEBUG = False
    LOG_LEVEL = 'WARNING'
    
    # 生产环境性能优化
    PERFORMANCE_CONFIG = {
        'max_concurrent_requests': 50,
        'request_timeout': 60,
        'enable_async': True,
    }

class TestingConfig(Config):
    """测试环境配置"""
    DEBUG = True
    LOG_LEVEL = 'DEBUG'
    
    # 测试环境使用较小的限制
    ANALYSIS_CONFIG = {
        'text_analysis': {
            'max_text_length': 10000,
            'min_word_length': 1,
            'enable_pos_tagging': True,
            'enable_ner': True,
        },
        'word_frequency': {
            'max_words': 100,
            'min_frequency': 1,
            'filter_stopwords': True,
            'filter_punctuation': True,
        },
        'timeline': {
            'max_events': 20,
            'min_confidence': 0.1,
            'enable_dynasty_mapping': True,
            'enable_event_classification': True,
        },
        'geography': {
            'max_locations': 10,
            'min_confidence': 0.1,
            'enable_coordinates': True,
            'enable_historical_names': True,
        },
        'summary': {
            'max_sentences': 3,
            'max_length': 150,
            'num_topics': 3,
            'compression_ratio': 0.5,
        }
    }

# 配置映射
config_map = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'testing': TestingConfig,
    'default': DevelopmentConfig
}

def get_config(config_name: str = None) -> Config:
    """获取配置对象"""
    if config_name is None:
        config_name = os.getenv('FLASK_ENV', 'default')
    
    return config_map.get(config_name, DevelopmentConfig)

def setup_logging(config: Config):
    """设置日志配置"""
    logging.basicConfig(
        level=getattr(logging, config.LOG_LEVEL),
        format=config.LOG_FORMAT,
        handlers=[
            logging.StreamHandler(),  # 控制台输出
            logging.FileHandler(config.LOG_FILE, encoding='utf-8')  # 文件输出
        ]
    )
    
    # 设置第三方库日志级别
    logging.getLogger('werkzeug').setLevel(logging.WARNING)
    logging.getLogger('jieba').setLevel(logging.WARNING)

# 全局配置实例
current_config = get_config()