"""
文本摘要服务
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 17:35:00

功能：
- 自动文本摘要生成
- 关键句提取
- 主题摘要
- 多层次摘要
- 摘要质量评估
"""

import re
import jieba
import jieba.posseg as pseg
import logging
from typing import Dict, List, Optional, Any, Tuple
from collections import defaultdict, Counter
import math
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

logger = logging.getLogger(__name__)

class TextSummarizer:
    """文本摘要器"""
    
    def __init__(self):
        """初始化文本摘要器"""
        logger.info("初始化文本摘要器")
        
        # 停用词
        self.stop_words = self._init_stop_words()
        
        # 重要词汇
        self.important_words = self._init_important_words()
        
        # 句子分割模式
        self.sentence_patterns = self._init_sentence_patterns()
        
        # 摘要评估指标
        self.evaluation_metrics = self._init_evaluation_metrics()
    
    def _init_stop_words(self) -> set:
        """初始化停用词"""
        stop_words = {
            # 常见停用词
            '的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个',
            '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好',
            '自己', '这', '那', '里', '就是', '还是', '为了', '都是', '可以', '这个',
            '来', '他', '她', '它', '们', '这些', '那些', '什么', '怎么', '为什么',
            '因为', '所以', '但是', '然后', '如果', '虽然', '虽说', '不过', '而且',
            '或者', '还有', '以及', '以后', '以前', '现在', '当时', '那时', '这时',
            
            # 标点符号
            '，', '。', '！', '？', '；', '：', '"', '"', ''', ''', '（', '）',
            '【', '】', '《', '》', '、', '…', '——', '—', '·', '～',
            
            # 数字和字母
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        }
        
        logger.info(f"初始化停用词完成，停用词数量: {len(stop_words)}")
        return stop_words
    
    def _init_important_words(self) -> Dict[str, float]:
        """初始化重要词汇及其权重"""
        important_words = {
            # 历史相关词汇
            '历史': 2.0, '朝代': 2.0, '皇帝': 2.0, '王朝': 2.0, '帝国': 2.0,
            '战争': 1.8, '革命': 1.8, '改革': 1.8, '变法': 1.8, '起义': 1.8,
            '文化': 1.5, '政治': 1.5, '经济': 1.5, '社会': 1.5, '军事': 1.5,
            '制度': 1.5, '法律': 1.5, '宗教': 1.5, '哲学': 1.5, '艺术': 1.5,
            
            # 时间词汇
            '年': 1.3, '月': 1.3, '日': 1.3, '世纪': 1.5, '年代': 1.5, '时期': 1.5, '时代': 1.5, '朝': 1.3, '代': 1.3,
            '初': 1.2, '中': 1.2, '末': 1.2, '前': 1.2, '后': 1.2, '早': 1.2, '晚': 1.2, '古': 1.3, '近': 1.3, '现代': 1.3,
            
            # 地理词汇
            '中国': 1.8, '华夏': 1.8, '中华': 1.8, '汉': 1.5, '唐': 1.5, '宋': 1.5, '元': 1.5, '明': 1.5, '清': 1.5,
            '北京': 1.4, '南京': 1.4, '西安': 1.4, '洛阳': 1.4, '开封': 1.4, '杭州': 1.4, '成都': 1.4, '广州': 1.4,
            
            # 人物词汇
            '皇帝': 1.8, '皇后': 1.6, '太子': 1.6, '王': 1.5, '侯': 1.4, '公': 1.4, '将军': 1.6, '大臣': 1.5, '官员': 1.4,
            '学者': 1.5, '文人': 1.5, '诗人': 1.5, '画家': 1.5, '思想家': 1.6, '哲学家': 1.6, '史学家': 1.6,
            
            # 事件词汇
            '建立': 1.6, '统一': 1.7, '分裂': 1.6, '灭亡': 1.6, '兴起': 1.5, '衰落': 1.5, '发展': 1.4, '变化': 1.4,
            '影响': 1.5, '作用': 1.4, '意义': 1.5, '价值': 1.4, '贡献': 1.5, '成就': 1.5, '失败': 1.4, '教训': 1.4
        }
        
        logger.info(f"初始化重要词汇完成，词汇数量: {len(important_words)}")
        return important_words
    
    def _init_sentence_patterns(self) -> List[re.Pattern]:
        """初始化句子分割模式"""
        patterns = [
            re.compile(r'[。！？；]'),  # 中文句号、感叹号、问号、分号
            re.compile(r'[.!?;]'),     # 英文句号、感叹号、问号、分号
            re.compile(r'\n+'),        # 换行符
        ]
        
        logger.info(f"初始化句子分割模式完成，模式数量: {len(patterns)}")
        return patterns
    
    def _init_evaluation_metrics(self) -> Dict[str, float]:
        """初始化摘要评估指标权重"""
        metrics = {
            'coverage': 0.3,      # 覆盖度
            'coherence': 0.25,    # 连贯性
            'conciseness': 0.2,   # 简洁性
            'informativeness': 0.15,  # 信息量
            'readability': 0.1    # 可读性
        }
        
        logger.info(f"初始化评估指标完成，指标数量: {len(metrics)}")
        return metrics
    
    def split_sentences(self, text: str) -> List[str]:
        """
        分割句子
        
        Args:
            text: 输入文本
        
        Returns:
            句子列表
        """
        if not text:
            return []
        
        sentences = []
        current_sentence = ""
        
        for char in text:
            current_sentence += char
            
            # 检查是否是句子结束符
            if char in '。！？；.!?;':
                sentence = current_sentence.strip()
                if sentence and len(sentence) > 3:  # 过滤过短的句子
                    sentences.append(sentence)
                current_sentence = ""
            elif char == '\n':
                sentence = current_sentence.strip()
                if sentence and len(sentence) > 3:
                    sentences.append(sentence)
                current_sentence = ""
        
        # 处理最后一个句子
        if current_sentence.strip() and len(current_sentence.strip()) > 3:
            sentences.append(current_sentence.strip())
        
        return sentences
    
    def calculate_sentence_scores(self, sentences: List[str], method: str = 'tfidf') -> List[float]:
        """
        计算句子得分
        
        Args:
            sentences: 句子列表
            method: 计算方法 ('tfidf', 'frequency', 'position', 'combined')
        
        Returns:
            句子得分列表
        """
        if not sentences:
            return []
        
        if method == 'tfidf':
            return self._calculate_tfidf_scores(sentences)
        elif method == 'frequency':
            return self._calculate_frequency_scores(sentences)
        elif method == 'position':
            return self._calculate_position_scores(sentences)
        elif method == 'combined':
            return self._calculate_combined_scores(sentences)
        else:
            return [1.0] * len(sentences)
    
    def _calculate_tfidf_scores(self, sentences: List[str]) -> List[float]:
        """使用TF-IDF计算句子得分"""
        try:
            # 预处理句子
            processed_sentences = []
            for sentence in sentences:
                words = jieba.lcut(sentence)
                filtered_words = [w for w in words if w not in self.stop_words and len(w) > 1]
                processed_sentences.append(' '.join(filtered_words))
            
            if not any(processed_sentences):
                return [1.0] * len(sentences)
            
            # 计算TF-IDF
            vectorizer = TfidfVectorizer(max_features=1000, ngram_range=(1, 2))
            tfidf_matrix = vectorizer.fit_transform(processed_sentences)
            
            # 计算每个句子的TF-IDF得分（向量的L2范数）
            scores = []
            for i in range(tfidf_matrix.shape[0]):
                score = np.linalg.norm(tfidf_matrix[i].toarray())
                scores.append(score)
            
            return scores
            
        except Exception as e:
            logger.warning(f"TF-IDF计算失败: {e}")
            return [1.0] * len(sentences)
    
    def _calculate_frequency_scores(self, sentences: List[str]) -> List[float]:
        """使用词频计算句子得分"""
        # 统计所有词的频率
        word_freq = Counter()
        
        for sentence in sentences:
            words = jieba.lcut(sentence)
            for word in words:
                if word not in self.stop_words and len(word) > 1:
                    word_freq[word] += 1
        
        if not word_freq:
            return [1.0] * len(sentences)
        
        # 计算每个句子的得分
        scores = []
        for sentence in sentences:
            words = jieba.lcut(sentence)
            sentence_score = 0
            word_count = 0
            
            for word in words:
                if word not in self.stop_words and len(word) > 1:
                    # 基础频率得分
                    freq_score = word_freq[word] / max(word_freq.values())
                    
                    # 重要词汇加权
                    importance_weight = self.important_words.get(word, 1.0)
                    
                    sentence_score += freq_score * importance_weight
                    word_count += 1
            
            # 平均得分
            if word_count > 0:
                scores.append(sentence_score / word_count)
            else:
                scores.append(0.0)
        
        return scores
    
    def _calculate_position_scores(self, sentences: List[str]) -> List[float]:
        """使用位置信息计算句子得分"""
        scores = []
        total_sentences = len(sentences)
        
        for i, sentence in enumerate(sentences):
            # 位置权重：开头和结尾的句子权重较高
            if i < total_sentences * 0.1:  # 前10%
                position_score = 1.0
            elif i > total_sentences * 0.9:  # 后10%
                position_score = 0.8
            else:  # 中间部分
                position_score = 0.6
            
            # 句子长度权重：适中长度的句子权重较高
            length_score = min(len(sentence) / 100, 1.0)  # 标准化到0-1
            if length_score < 0.2:  # 过短
                length_score *= 0.5
            elif length_score > 0.8:  # 过长
                length_score *= 0.8
            
            scores.append(position_score * length_score)
        
        return scores
    
    def _calculate_combined_scores(self, sentences: List[str]) -> List[float]:
        """综合多种方法计算句子得分"""
        tfidf_scores = self._calculate_tfidf_scores(sentences)
        freq_scores = self._calculate_frequency_scores(sentences)
        pos_scores = self._calculate_position_scores(sentences)
        
        # 标准化得分
        def normalize_scores(scores):
            if not scores or max(scores) == 0:
                return scores
            max_score = max(scores)
            return [s / max_score for s in scores]
        
        tfidf_scores = normalize_scores(tfidf_scores)
        freq_scores = normalize_scores(freq_scores)
        pos_scores = normalize_scores(pos_scores)
        
        # 加权组合
        combined_scores = []
        for i in range(len(sentences)):
            combined_score = (
                0.4 * tfidf_scores[i] +
                0.4 * freq_scores[i] +
                0.2 * pos_scores[i]
            )
            combined_scores.append(combined_score)
        
        return combined_scores
    
    def extract_key_sentences(self, sentences: List[str], scores: List[float], 
                            num_sentences: int = None, threshold: float = None) -> List[Dict]:
        """
        提取关键句子
        
        Args:
            sentences: 句子列表
            scores: 句子得分列表
            num_sentences: 提取句子数量
            threshold: 得分阈值
        
        Returns:
            关键句子信息列表
        """
        if not sentences or not scores:
            return []
        
        # 创建句子信息列表
        sentence_info = []
        for i, (sentence, score) in enumerate(zip(sentences, scores)):
            sentence_info.append({
                'index': i,
                'sentence': sentence,
                'score': score,
                'length': len(sentence),
                'word_count': len(jieba.lcut(sentence))
            })
        
        # 按得分排序
        sentence_info.sort(key=lambda x: x['score'], reverse=True)
        
        # 选择关键句子
        selected_sentences = []
        
        if num_sentences:
            # 按数量选择
            selected_sentences = sentence_info[:num_sentences]
        elif threshold:
            # 按阈值选择
            selected_sentences = [s for s in sentence_info if s['score'] >= threshold]
        else:
            # 默认选择前20%的句子
            num_to_select = max(1, len(sentences) // 5)
            selected_sentences = sentence_info[:num_to_select]
        
        # 按原始顺序排序
        selected_sentences.sort(key=lambda x: x['index'])
        
        return selected_sentences
    
    def generate_summary(self, text: str, options: Dict = None) -> Dict:
        """
        生成文本摘要
        
        Args:
            text: 输入文本
            options: 摘要选项
        
        Returns:
            摘要结果
        """
        if not text:
            return {}
        
        options = options or {}
        
        # 分割句子
        sentences = self.split_sentences(text)
        
        if not sentences:
            return {'summary': '', 'key_sentences': [], 'statistics': {}}
        
        # 计算句子得分
        scoring_method = options.get('method', 'combined')
        scores = self.calculate_sentence_scores(sentences, scoring_method)
        
        # 提取关键句子
        num_sentences = options.get('num_sentences')
        threshold = options.get('threshold')
        summary_ratio = options.get('summary_ratio', 0.3)
        
        if not num_sentences and not threshold:
            num_sentences = max(1, int(len(sentences) * summary_ratio))
        
        key_sentences = self.extract_key_sentences(
            sentences, scores, num_sentences, threshold
        )
        
        # 生成摘要文本
        summary_text = ''.join([s['sentence'] for s in key_sentences])
        
        # 生成统计信息
        statistics = self._generate_summary_statistics(
            text, summary_text, sentences, key_sentences, scores
        )
        
        return {
            'summary': summary_text,
            'key_sentences': key_sentences,
            'statistics': statistics,
            'method': scoring_method,
            'original_sentence_count': len(sentences),
            'summary_sentence_count': len(key_sentences)
        }
    
    def _generate_summary_statistics(self, original_text: str, summary_text: str,
                                   sentences: List[str], key_sentences: List[Dict],
                                   scores: List[float]) -> Dict:
        """生成摘要统计信息"""
        statistics = {
            'original_length': len(original_text),
            'summary_length': len(summary_text),
            'compression_ratio': len(summary_text) / len(original_text) if original_text else 0,
            'original_sentences': len(sentences),
            'summary_sentences': len(key_sentences),
            'sentence_compression_ratio': len(key_sentences) / len(sentences) if sentences else 0,
            'average_sentence_score': sum(scores) / len(scores) if scores else 0,
            'max_sentence_score': max(scores) if scores else 0,
            'min_sentence_score': min(scores) if scores else 0
        }
        
        # 词汇统计
        original_words = jieba.lcut(original_text)
        summary_words = jieba.lcut(summary_text)
        
        statistics.update({
            'original_word_count': len(original_words),
            'summary_word_count': len(summary_words),
            'word_compression_ratio': len(summary_words) / len(original_words) if original_words else 0,
            'unique_words_original': len(set(original_words)),
            'unique_words_summary': len(set(summary_words)),
            'vocabulary_retention': len(set(summary_words) & set(original_words)) / len(set(original_words)) if original_words else 0
        })
        
        return statistics
    
    def generate_multi_level_summary(self, text: str, levels: List[float] = None) -> Dict:
        """
        生成多层次摘要
        
        Args:
            text: 输入文本
            levels: 摘要比例列表，如 [0.1, 0.3, 0.5]
        
        Returns:
            多层次摘要结果
        """
        if not text:
            return {}
        
        levels = levels or [0.1, 0.3, 0.5]
        
        summaries = {}
        
        for level in levels:
            level_name = f"level_{int(level * 100)}"
            summary_result = self.generate_summary(text, {
                'summary_ratio': level,
                'method': 'combined'
            })
            summaries[level_name] = summary_result
        
        return {
            'multi_level_summaries': summaries,
            'levels': levels,
            'original_text_length': len(text)
        }
    
    def extract_key_topics(self, text: str, num_topics: int = 5) -> List[Dict]:
        """
        提取关键主题
        
        Args:
            text: 输入文本
            num_topics: 主题数量
        
        Returns:
            主题列表
        """
        if not text:
            return []
        
        # 分词和词频统计
        words = jieba.lcut(text)
        word_freq = Counter()
        
        for word in words:
            if word not in self.stop_words and len(word) > 1:
                word_freq[word] += 1
        
        if not word_freq:
            return []
        
        # 提取高频词作为主题关键词
        top_words = word_freq.most_common(num_topics * 3)
        
        # 按重要性分组主题
        topics = []
        used_words = set()
        
        for i in range(num_topics):
            topic_words = []
            topic_score = 0
            
            for word, freq in top_words:
                if word not in used_words and len(topic_words) < 5:
                    importance = self.important_words.get(word, 1.0)
                    topic_words.append({
                        'word': word,
                        'frequency': freq,
                        'importance': importance,
                        'score': freq * importance
                    })
                    topic_score += freq * importance
                    used_words.add(word)
            
            if topic_words:
                topics.append({
                    'topic_id': i + 1,
                    'keywords': topic_words,
                    'total_score': topic_score,
                    'description': self._generate_topic_description(topic_words)
                })
        
        # 按得分排序
        topics.sort(key=lambda x: x['total_score'], reverse=True)
        
        return topics
    
    def _generate_topic_description(self, keywords: List[Dict]) -> str:
        """生成主题描述"""
        if not keywords:
            return "未知主题"
        
        # 取前3个关键词生成描述
        top_keywords = sorted(keywords, key=lambda x: x['score'], reverse=True)[:3]
        keyword_names = [kw['word'] for kw in top_keywords]
        
        return f"关于{' '.join(keyword_names)}的主题"
    
    def evaluate_summary_quality(self, original_text: str, summary_text: str) -> Dict:
        """
        评估摘要质量
        
        Args:
            original_text: 原始文本
            summary_text: 摘要文本
        
        Returns:
            质量评估结果
        """
        if not original_text or not summary_text:
            return {}
        
        evaluation = {}
        
        # 覆盖度评估
        coverage_score = self._evaluate_coverage(original_text, summary_text)
        evaluation['coverage'] = coverage_score
        
        # 连贯性评估
        coherence_score = self._evaluate_coherence(summary_text)
        evaluation['coherence'] = coherence_score
        
        # 简洁性评估
        conciseness_score = self._evaluate_conciseness(original_text, summary_text)
        evaluation['conciseness'] = conciseness_score
        
        # 信息量评估
        informativeness_score = self._evaluate_informativeness(summary_text)
        evaluation['informativeness'] = informativeness_score
        
        # 可读性评估
        readability_score = self._evaluate_readability(summary_text)
        evaluation['readability'] = readability_score
        
        # 综合得分
        overall_score = sum(
            evaluation[metric] * weight 
            for metric, weight in self.evaluation_metrics.items()
            if metric in evaluation
        )
        evaluation['overall_score'] = overall_score
        
        return evaluation
    
    def _evaluate_coverage(self, original_text: str, summary_text: str) -> float:
        """评估覆盖度"""
        original_words = set(jieba.lcut(original_text))
        summary_words = set(jieba.lcut(summary_text))
        
        # 过滤停用词
        original_words = {w for w in original_words if w not in self.stop_words and len(w) > 1}
        summary_words = {w for w in summary_words if w not in self.stop_words and len(w) > 1}
        
        if not original_words:
            return 0.0
        
        coverage = len(summary_words & original_words) / len(original_words)
        return min(coverage, 1.0)
    
    def _evaluate_coherence(self, summary_text: str) -> float:
        """评估连贯性"""
        sentences = self.split_sentences(summary_text)
        
        if len(sentences) < 2:
            return 1.0
        
        # 简单的连贯性评估：相邻句子的词汇重叠度
        coherence_scores = []
        
        for i in range(len(sentences) - 1):
            words1 = set(jieba.lcut(sentences[i]))
            words2 = set(jieba.lcut(sentences[i + 1]))
            
            # 过滤停用词
            words1 = {w for w in words1 if w not in self.stop_words and len(w) > 1}
            words2 = {w for w in words2 if w not in self.stop_words and len(w) > 1}
            
            if words1 and words2:
                overlap = len(words1 & words2) / len(words1 | words2)
                coherence_scores.append(overlap)
        
        return sum(coherence_scores) / len(coherence_scores) if coherence_scores else 0.0
    
    def _evaluate_conciseness(self, original_text: str, summary_text: str) -> float:
        """评估简洁性"""
        compression_ratio = len(summary_text) / len(original_text) if original_text else 0
        
        # 理想的压缩比在0.1-0.3之间
        if 0.1 <= compression_ratio <= 0.3:
            return 1.0
        elif compression_ratio < 0.1:
            return compression_ratio / 0.1  # 过于简洁
        else:
            return max(0, 1 - (compression_ratio - 0.3) / 0.7)  # 不够简洁
    
    def _evaluate_informativeness(self, summary_text: str) -> float:
        """评估信息量"""
        words = jieba.lcut(summary_text)
        
        # 计算重要词汇的比例
        important_word_count = 0
        total_word_count = 0
        
        for word in words:
            if word not in self.stop_words and len(word) > 1:
                total_word_count += 1
                if word in self.important_words:
                    important_word_count += 1
        
        if total_word_count == 0:
            return 0.0
        
        return important_word_count / total_word_count
    
    def _evaluate_readability(self, summary_text: str) -> float:
        """评估可读性"""
        sentences = self.split_sentences(summary_text)
        
        if not sentences:
            return 0.0
        
        # 平均句子长度
        avg_sentence_length = sum(len(s) for s in sentences) / len(sentences)
        
        # 理想句子长度在20-50字符之间
        if 20 <= avg_sentence_length <= 50:
            length_score = 1.0
        elif avg_sentence_length < 20:
            length_score = avg_sentence_length / 20
        else:
            length_score = max(0, 1 - (avg_sentence_length - 50) / 50)
        
        return length_score
    
    def analyze(self, text: str, options: Dict = None) -> Dict:
        """
        综合文本摘要分析
        
        Args:
            text: 待分析文本
            options: 分析选项
        
        Returns:
            摘要分析结果
        """
        if not text:
            return {}
        
        options = options or {}
        
        try:
            logger.info(f"开始文本摘要分析，文本长度: {len(text)}")
            
            # 生成基础摘要
            summary_result = self.generate_summary(text, options.get('summary', {}))
            
            # 生成多层次摘要
            multi_level_result = self.generate_multi_level_summary(
                text, options.get('levels', [0.1, 0.3, 0.5])
            )
            
            # 提取关键主题
            topics = self.extract_key_topics(text, options.get('num_topics', 5))
            
            # 评估摘要质量
            quality_evaluation = self.evaluate_summary_quality(
                text, summary_result.get('summary', '')
            )
            
            result = {
                'summary': summary_result,
                'multi_level_summaries': multi_level_result,
                'key_topics': topics,
                'quality_evaluation': quality_evaluation,
                'text_statistics': {
                    'original_length': len(text),
                    'original_sentences': len(self.split_sentences(text)),
                    'original_words': len(jieba.lcut(text))
                }
            }
            
            logger.info(f"文本摘要分析完成，生成摘要长度: {len(summary_result.get('summary', ''))}")
            
            return result
            
        except Exception as e:
            logger.error(f"文本摘要分析异常: {str(e)}")
            raise