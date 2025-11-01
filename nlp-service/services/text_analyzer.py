"""
文本分析器
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 18:00:00
"""

import re
from typing import Dict, List, Any
from datetime import datetime


class TextAnalyzer:
    """文本分析器类"""
    
    def __init__(self):
        """初始化文本分析器"""
        self.sentence_patterns = [
            r'[。！？；]',  # 中文句号、感叹号、问号、分号
            r'[.!?;]',      # 英文句号、感叹号、问号、分号
        ]
        
    def analyze(self, text: str, analysis_type: str = 'comprehensive') -> Dict[str, Any]:
        """
        分析文本
        
        Args:
            text (str): 输入文本
            analysis_type (str): 分析类型
            
        Returns:
            Dict[str, Any]: 分析结果
        """
        if not text or not text.strip():
            raise ValueError("输入文本不能为空")
        
        # 基础统计
        basic_stats = self._get_basic_statistics(text)
        
        # 句子分析
        sentences = self._split_sentences(text)
        sentence_stats = self._analyze_sentences(sentences)
        
        # 字符分析
        char_stats = self._analyze_characters(text)
        
        # 段落分析
        paragraphs = self._split_paragraphs(text)
        paragraph_stats = self._analyze_paragraphs(paragraphs)
        
        result = {
            'basic_statistics': basic_stats,
            'sentence_analysis': sentence_stats,
            'character_analysis': char_stats,
            'paragraph_analysis': paragraph_stats,
            'analysis_type': analysis_type,
            'timestamp': datetime.now().isoformat()
        }
        
        return result
    
    def _get_basic_statistics(self, text: str) -> Dict[str, Any]:
        """获取基础统计信息"""
        # 去除空白字符的文本
        clean_text = text.strip()
        
        # 字符统计
        total_chars = len(text)
        clean_chars = len(clean_text)
        whitespace_chars = total_chars - clean_chars
        
        # 行数统计
        lines = text.split('\n')
        total_lines = len(lines)
        non_empty_lines = len([line for line in lines if line.strip()])
        
        return {
            'total_characters': total_chars,
            'clean_characters': clean_chars,
            'whitespace_characters': whitespace_chars,
            'total_lines': total_lines,
            'non_empty_lines': non_empty_lines,
            'empty_lines': total_lines - non_empty_lines
        }
    
    def _split_sentences(self, text: str) -> List[str]:
        """分割句子"""
        sentences = []
        
        # 使用正则表达式分割句子
        pattern = '|'.join(self.sentence_patterns)
        parts = re.split(f'({pattern})', text)
        
        current_sentence = ""
        for part in parts:
            if re.match(pattern, part):
                # 这是句子结束符
                current_sentence += part
                if current_sentence.strip():
                    sentences.append(current_sentence.strip())
                current_sentence = ""
            else:
                # 这是句子内容
                current_sentence += part
        
        # 处理最后一个句子（可能没有结束符）
        if current_sentence.strip():
            sentences.append(current_sentence.strip())
        
        return sentences
    
    def _analyze_sentences(self, sentences: List[str]) -> Dict[str, Any]:
        """分析句子"""
        if not sentences:
            return {
                'total_sentences': 0,
                'average_length': 0,
                'longest_sentence': '',
                'shortest_sentence': '',
                'sentence_lengths': []
            }
        
        lengths = [len(sentence) for sentence in sentences]
        
        return {
            'total_sentences': len(sentences),
            'average_length': sum(lengths) / len(lengths),
            'longest_sentence': max(sentences, key=len),
            'shortest_sentence': min(sentences, key=len),
            'sentence_lengths': lengths,
            'max_length': max(lengths),
            'min_length': min(lengths)
        }
    
    def _analyze_characters(self, text: str) -> Dict[str, Any]:
        """分析字符"""
        char_counts = {}
        chinese_chars = 0
        english_chars = 0
        digits = 0
        punctuation = 0
        spaces = 0
        
        for char in text:
            # 统计字符频率
            char_counts[char] = char_counts.get(char, 0) + 1
            
            # 分类统计
            if '\u4e00' <= char <= '\u9fff':  # 中文字符
                chinese_chars += 1
            elif char.isalpha():  # 英文字符
                english_chars += 1
            elif char.isdigit():  # 数字
                digits += 1
            elif char in '，。！？；：""''（）【】《》、':  # 中文标点
                punctuation += 1
            elif char in ',.!?;:"\'()[]<>/-':  # 英文标点
                punctuation += 1
            elif char.isspace():  # 空白字符
                spaces += 1
        
        # 获取最常见的字符
        most_common_chars = sorted(char_counts.items(), key=lambda x: x[1], reverse=True)[:10]
        
        return {
            'chinese_characters': chinese_chars,
            'english_characters': english_chars,
            'digits': digits,
            'punctuation': punctuation,
            'spaces': spaces,
            'unique_characters': len(char_counts),
            'most_common_characters': most_common_chars,
            'character_distribution': {
                'chinese_ratio': chinese_chars / len(text) if text else 0,
                'english_ratio': english_chars / len(text) if text else 0,
                'digit_ratio': digits / len(text) if text else 0,
                'punctuation_ratio': punctuation / len(text) if text else 0,
                'space_ratio': spaces / len(text) if text else 0
            }
        }
    
    def _split_paragraphs(self, text: str) -> List[str]:
        """分割段落"""
        # 按双换行符分割段落
        paragraphs = re.split(r'\n\s*\n', text)
        # 过滤空段落
        paragraphs = [p.strip() for p in paragraphs if p.strip()]
        return paragraphs
    
    def _analyze_paragraphs(self, paragraphs: List[str]) -> Dict[str, Any]:
        """分析段落"""
        if not paragraphs:
            return {
                'total_paragraphs': 0,
                'average_length': 0,
                'longest_paragraph': '',
                'shortest_paragraph': '',
                'paragraph_lengths': []
            }
        
        lengths = [len(paragraph) for paragraph in paragraphs]
        
        return {
            'total_paragraphs': len(paragraphs),
            'average_length': sum(lengths) / len(lengths),
            'longest_paragraph': max(paragraphs, key=len)[:100] + '...' if max(paragraphs, key=len) else '',
            'shortest_paragraph': min(paragraphs, key=len),
            'paragraph_lengths': lengths,
            'max_length': max(lengths),
            'min_length': min(lengths)
        }