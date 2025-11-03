"""
词频分析器
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 18:00:00
"""

import re
from typing import Dict, List, Tuple, Any
from collections import Counter
from datetime import datetime


class WordFrequencyAnalyzer:
    """词频分析器类"""
    
    def __init__(self):
        """初始化词频分析器"""
        # 中文停用词
        self.chinese_stopwords = {
            '的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个',
            '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好',
            '自己', '这', '那', '他', '她', '它', '们', '这个', '那个', '什么', '怎么',
            '为什么', '因为', '所以', '但是', '然后', '如果', '虽然', '虽然', '虽然',
            '可以', '应该', '能够', '必须', '需要', '想要', '希望', '觉得', '认为',
            '知道', '明白', '理解', '发现', '找到', '得到', '拿到', '做到', '达到'
        }
        
        # 英文停用词
        self.english_stopwords = {
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            'of', 'with', 'by', 'from', 'up', 'about', 'into', 'through', 'during',
            'before', 'after', 'above', 'below', 'between', 'among', 'under', 'over',
            'is', 'am', 'are', 'was', 'were', 'be', 'been', 'being', 'have', 'has',
            'had', 'do', 'does', 'did', 'will', 'would', 'could', 'should', 'may',
            'might', 'must', 'can', 'this', 'that', 'these', 'those', 'i', 'you',
            'he', 'she', 'it', 'we', 'they', 'me', 'him', 'her', 'us', 'them'
        }
    
    def analyze(self, text: str, language: str = 'auto', min_length: int = 1, 
                max_results: int = 100, remove_stopwords: bool = True) -> Dict[str, Any]:
        """
        分析词频
        
        Args:
            text (str): 输入文本
            language (str): 语言类型 ('auto', 'chinese', 'english')
            min_length (int): 最小词长度
            max_results (int): 最大结果数量
            remove_stopwords (bool): 是否移除停用词
            
        Returns:
            Dict[str, Any]: 词频分析结果
        """
        if not text or not text.strip():
            raise ValueError("输入文本不能为空")
        
        # 检测语言
        detected_language = self._detect_language(text) if language == 'auto' else language
        # 分词
        words = self._tokenize(text, detected_language)
        
        # 过滤词汇
        filtered_words = self._filter_words(words, detected_language, min_length, remove_stopwords)
        
        # 计算词频
        word_freq = Counter(filtered_words)
        
        # 获取最高频词汇
        top_words = word_freq.most_common(max_results)
        
        # 计算统计信息
        stats = self._calculate_statistics(words, filtered_words, word_freq)
        
        # 词汇分类
        word_categories = self._categorize_words(top_words, detected_language)
        
        result = {
            'word_frequency': [{'word': word, 'count': count, 'frequency': count / len(filtered_words)} 
                              for word, count in top_words] if filtered_words else [],
            'statistics': stats,
            'word_categories': word_categories,
            'language': detected_language,
            'parameters': {
                'min_length': min_length,
                'max_results': max_results,
                'remove_stopwords': remove_stopwords
            },
            'timestamp': datetime.now().isoformat()
        }
        
        return result
    
    def _detect_language(self, text: str) -> str:
        """检测文本语言"""
        chinese_chars = sum(1 for char in text if '\u4e00' <= char <= '\u9fff')
        english_chars = sum(1 for char in text if char.isalpha() and ord(char) < 256)
        
        total_chars = len(text.replace(' ', '').replace('\n', '').replace('\t', ''))
        
        if total_chars == 0:
            return 'unknown'
        
        chinese_ratio = chinese_chars / total_chars
        english_ratio = english_chars / total_chars
        
        if chinese_ratio > 0.1:
            return 'chinese'
        elif english_ratio > 0.5:
            return 'english'
        else:
            return 'mixed'
    
    def _tokenize(self, text: str, language: str) -> List[str]:
        """分词"""
        if language == 'chinese' or language == 'mixed':
            return self._tokenize_chinese(text)
        elif language == 'english':
            return self._tokenize_english(text)
        else:
            # 默认使用简单分词
            return self._tokenize_simple(text)
    
    def _tokenize_chinese(self, text: str) -> List[str]:
        """中文分词（简单实现）"""
        # 移除标点符号和特殊字符，但保留中文字符
        text = re.sub(r'[^\u4e00-\u9fff\w\s]', ' ', text)
        
        # 简化的中文分词：直接按字符分割中文，按空格分割英文
        words = []
        i = 0
        while i < len(text):
            char = text[i]
            
            if '\u4e00' <= char <= '\u9fff':  # 中文字符
                # 对于中文，先尝试组合成词，如果不行就按单字处理
                # 尝试匹配2-3字的常见词汇
                found_word = False
                for length in range(3, 1, -1):  # 从3字词到2字词
                    if i + length <= len(text):
                        word = text[i:i+length]
                        is_common = self._is_common_chinese_word(word)
                        if length == 2 or is_common:
                            words.append(word)
                            i += length
                            found_word = True
                            break
                
                if not found_word:
                    # 添加单个中文字符
                    words.append(char)
                    i += 1
            elif char.isalpha():  # 英文字符
                # 提取完整的英文单词
                word_start = i
                while i < len(text) and text[i].isalpha():
                    i += 1
                word = text[word_start:i].lower()
                if word:
                    words.append(word)
            elif char.isspace():
                i += 1
            else:
                i += 1
        
        return [word for word in words if word.strip()]
    
    def _tokenize_english(self, text: str) -> List[str]:
        """英文分词"""
        # 转换为小写并移除标点符号
        text = re.sub(r'[^\w\s]', ' ', text.lower())
        
        # 按空白字符分割
        words = text.split()
        
        return [word for word in words if word.strip()]
    
    def _tokenize_simple(self, text: str) -> List[str]:
        """简单分词"""
        # 移除标点符号，保留中英文字符和数字
        text = re.sub(r'[^\u4e00-\u9fff\w\s]', ' ', text)
        
        # 按空白字符分割
        words = text.split()
        
        return [word for word in words if word.strip()]
    
    def _is_valid_chinese_word(self, word: str) -> bool:
        """判断是否为有效的中文词汇"""
        # 对于中文词汇，只要包含中文字符就认为是有效的
        if any('\u4e00' <= char <= '\u9fff' for char in word):
            return True
        
        # 对于非中文词汇，长度至少为2
        return len(word) >= 2
    
    def _is_common_chinese_word(self, word: str) -> bool:
        """判断是否为常见的中文词汇"""
        # 常见的中文词汇列表
        common_words = {
            '中国', '历史', '文化', '发展', '社会', '人民', '政治', '经济', '科学', '技术',
            '教育', '医疗', '环境', '资源', '能源', '交通', '通信', '网络', '信息', '数据',
            '系统', '管理', '服务', '产品', '市场', '企业', '公司', '组织', '机构', '部门',
            '工作', '学习', '研究', '分析', '设计', '开发', '建设', '改革', '创新', '进步',
            '问题', '解决', '方法', '方式', '途径', '措施', '政策', '法律', '制度', '规则',
            '悠久', '灿烂', '古代', '现代', '朝代', '王朝', '皇帝', '大臣', '官员', '百姓',
            '夏商', '秦汉', '唐宋', '明清', '统一', '繁荣', '衰落', '兴起', '变迁', '演变'
        }
        
        return word in common_words
    
    def _filter_words(self, words: List[str], language: str, min_length: int, 
                     remove_stopwords: bool) -> List[str]:
        """过滤词汇"""
        filtered = []
        
        for word in words:
            # 长度过滤
            if len(word) < min_length:
                continue
            
            # 停用词过滤
            if remove_stopwords:
                if language == 'chinese' and word in self.chinese_stopwords:
                    continue
                elif language == 'english' and word.lower() in self.english_stopwords:
                    continue
                elif language == 'mixed':
                    if (word in self.chinese_stopwords or 
                        word.lower() in self.english_stopwords):
                        continue
            
            # 过滤纯数字和单个字符（除非是中文）
            if word.isdigit() and len(word) == 1:
                continue
            
            filtered.append(word)
        
        return filtered
    
    def _calculate_statistics(self, original_words: List[str], filtered_words: List[str], 
                            word_freq: Counter) -> Dict[str, Any]:
        """计算统计信息"""
        total_words = len(original_words)
        unique_words = len(word_freq)
        filtered_total = len(filtered_words)
        
        # 计算词汇丰富度（类型-标记比）
        lexical_diversity = unique_words / filtered_total if filtered_total > 0 else 0
        
        # 计算平均词频
        avg_frequency = sum(word_freq.values()) / len(word_freq) if word_freq else 0
        
        # 获取频率分布
        freq_distribution = {}
        for count in word_freq.values():
            freq_distribution[count] = freq_distribution.get(count, 0) + 1
        
        return {
            'total_words': total_words,
            'filtered_words': filtered_total,
            'unique_words': unique_words,
            'lexical_diversity': lexical_diversity,
            'average_frequency': avg_frequency,
            'max_frequency': max(word_freq.values()) if word_freq else 0,
            'min_frequency': min(word_freq.values()) if word_freq else 0,
            'frequency_distribution': freq_distribution
        }
    
    def _categorize_words(self, top_words: List[Tuple[str, int]], language: str) -> Dict[str, List[str]]:
        """词汇分类"""
        categories = {
            'high_frequency': [],    # 高频词（前10%）
            'medium_frequency': [],  # 中频词（10%-50%）
            'low_frequency': [],     # 低频词（50%以后）
            'single_occurrence': [], # 只出现一次的词
            'long_words': [],        # 长词（>5个字符）
            'short_words': []        # 短词（<=3个字符）
        }
        
        if not top_words:
            return categories
        
        total_words = len(top_words)
        high_threshold = max(1, total_words // 10)
        medium_threshold = max(1, total_words // 2)
        
        for i, (word, count) in enumerate(top_words):
            # 按频率分类
            if i < high_threshold:
                categories['high_frequency'].append(word)
            elif i < medium_threshold:
                categories['medium_frequency'].append(word)
            else:
                categories['low_frequency'].append(word)
            
            # 按出现次数分类
            if count == 1:
                categories['single_occurrence'].append(word)
            
            # 按长度分类
            if len(word) > 5:
                categories['long_words'].append(word)
            elif len(word) <= 3:
                categories['short_words'].append(word)
        
        return categories