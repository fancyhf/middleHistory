"""
词频统计分析服务
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 17:35:00

功能：
- 词频统计
- 词频分类
- 关键词提取
- 词云数据生成
- 词频趋势分析
"""

import jieba
import jieba.analyse
import re
import logging
from typing import Dict, List, Optional, Any, Tuple
from collections import Counter, defaultdict
import math

logger = logging.getLogger(__name__)

class WordFrequencyAnalyzer:
    """词频统计分析器"""
    
    def __init__(self):
        """初始化词频分析器"""
        logger.info("初始化词频分析器")
        
        # 加载自定义词典
        self._load_custom_dict()
        
        # 停用词列表
        self.stop_words = self._load_stop_words()
        
        # 词性过滤列表
        self.valid_pos = {'n', 'nr', 'ns', 'nt', 'nz', 'v', 'vd', 'vn', 'a', 'ad', 'an'}
        
        # 历史相关词汇分类
        self.historical_categories = self._load_historical_categories()
    
    def _load_custom_dict(self):
        """加载自定义词典"""
        try:
            # 历史相关词汇
            historical_words = [
                '春秋战国', '秦始皇', '汉武帝', '唐太宗', '宋太祖', '明太祖', '康熙',
                '辛亥革命', '五四运动', '抗日战争', '解放战争', '改革开放',
                '丝绸之路', '长城', '故宫', '天安门', '兵马俑', '紫禁城',
                '万里长城', '敦煌莫高窟', '泰山', '黄山', '长江', '黄河',
                '孔子', '老子', '庄子', '孟子', '荀子', '韩非子', '墨子',
                '诸葛亮', '刘备', '关羽', '张飞', '曹操', '孙权', '周瑜',
                '三国演义', '水浒传', '西游记', '红楼梦', '史记', '资治通鉴'
            ]
            
            for word in historical_words:
                jieba.add_word(word)
            
            logger.info(f"加载自定义词典完成，词汇数量: {len(historical_words)}")
            
        except Exception as e:
            logger.error(f"加载自定义词典失败: {str(e)}")
    
    def _load_stop_words(self) -> set:
        """加载停用词"""
        try:
            # 基础停用词
            stop_words = {
                '的', '了', '在', '是', '我', '有', '和', '就', '不', '人', '都', '一', '一个',
                '上', '也', '很', '到', '说', '要', '去', '你', '会', '着', '没有', '看', '好',
                '自己', '这', '那', '里', '就是', '还', '把', '来', '时', '对', '生', '可以',
                '但是', '这个', '中', '能', '为', '已经', '并', '最', '再', '么', '什么',
                '只', '当', '把', '还是', '因为', '如果', '所以', '但', '或者', '虽然',
                '然而', '因此', '由于', '尽管', '无论', '不管', '除了', '除非', '直到',
                '他', '她', '它', '我们', '你们', '他们', '她们', '它们', '这些', '那些',
                '每', '各', '某', '另', '其他', '别的', '如此', '这样', '那样', '年', '月', '日'
            }
            
            logger.info(f"加载停用词完成，停用词数量: {len(stop_words)}")
            return stop_words
            
        except Exception as e:
            logger.error(f"加载停用词失败: {str(e)}")
            return set()
    
    def _load_historical_categories(self) -> Dict[str, List[str]]:
        """加载历史词汇分类"""
        try:
            categories = {
                '朝代': [
                    '夏朝', '商朝', '周朝', '春秋', '战国', '秦朝', '汉朝', '三国',
                    '晋朝', '南北朝', '隋朝', '唐朝', '宋朝', '元朝', '明朝', '清朝'
                ],
                '皇帝': [
                    '秦始皇', '汉武帝', '唐太宗', '宋太祖', '明太祖', '康熙', '乾隆',
                    '雍正', '嘉靖', '万历', '光绪', '宣统'
                ],
                '历史人物': [
                    '孔子', '老子', '庄子', '孟子', '荀子', '韩非子', '墨子',
                    '诸葛亮', '刘备', '关羽', '张飞', '曹操', '孙权', '周瑜',
                    '岳飞', '文天祥', '郑和', '李白', '杜甫', '苏轼', '辛弃疾'
                ],
                '历史事件': [
                    '辛亥革命', '五四运动', '抗日战争', '解放战争', '改革开放',
                    '文化大革命', '洋务运动', '戊戌变法', '新文化运动'
                ],
                '地理位置': [
                    '长城', '故宫', '天安门', '兵马俑', '紫禁城', '万里长城',
                    '敦煌莫高窟', '泰山', '黄山', '长江', '黄河', '丝绸之路'
                ],
                '文化典籍': [
                    '三国演义', '水浒传', '西游记', '红楼梦', '史记', '资治通鉴',
                    '论语', '道德经', '孙子兵法', '诗经', '楚辞'
                ]
            }
            
            logger.info(f"加载历史词汇分类完成，分类数量: {len(categories)}")
            return categories
            
        except Exception as e:
            logger.error(f"加载历史词汇分类失败: {str(e)}")
            return {}
    
    def extract_words(self, text: str, options: Dict = None) -> List[str]:
        """
        提取文本中的词汇
        
        Args:
            text: 待分析文本
            options: 提取选项
        
        Returns:
            词汇列表
        """
        if not text:
            return []
        
        options = options or {}
        
        # 文本预处理
        text = re.sub(r'\s+', ' ', text.strip())
        
        # 分词
        if options.get('use_hmm', True):
            words = jieba.lcut(text, HMM=True)
        else:
            words = jieba.lcut(text, HMM=False)
        
        # 过滤词汇
        filtered_words = []
        min_length = options.get('min_word_length', 2)
        remove_stopwords = options.get('remove_stopwords', True)
        filter_pos = options.get('filter_pos', True)
        
        # 如果需要词性过滤，使用词性标注
        if filter_pos:
            import jieba.posseg as pseg
            words_with_pos = pseg.lcut(text)
            
            for word, pos in words_with_pos:
                word = word.strip()
                
                # 长度过滤
                if len(word) < min_length:
                    continue
                
                # 停用词过滤
                if remove_stopwords and word in self.stop_words:
                    continue
                
                # 词性过滤
                if pos not in self.valid_pos:
                    continue
                
                # 数字和标点过滤
                if word.isdigit() or not word.isalnum():
                    continue
                
                filtered_words.append(word)
        else:
            for word in words:
                word = word.strip()
                
                # 长度过滤
                if len(word) < min_length:
                    continue
                
                # 停用词过滤
                if remove_stopwords and word in self.stop_words:
                    continue
                
                # 数字和标点过滤
                if word.isdigit() or not word.isalnum():
                    continue
                
                filtered_words.append(word)
        
        return filtered_words
    
    def calculate_frequency(self, words: List[str], options: Dict = None) -> List[Dict]:
        """
        计算词频
        
        Args:
            words: 词汇列表
            options: 计算选项
        
        Returns:
            词频统计结果
        """
        if not words:
            return []
        
        options = options or {}
        
        # 统计词频
        word_counter = Counter(words)
        total_words = len(words)
        
        # 计算词频和相对频率
        frequency_results = []
        for word, count in word_counter.items():
            relative_freq = count / total_words
            
            frequency_results.append({
                'word': word,
                'count': count,
                'frequency': relative_freq,
                'percentage': round(relative_freq * 100, 2)
            })
        
        # 排序
        sort_by = options.get('sort_by', 'count')
        reverse = options.get('reverse', True)
        
        if sort_by == 'count':
            frequency_results.sort(key=lambda x: x['count'], reverse=reverse)
        elif sort_by == 'frequency':
            frequency_results.sort(key=lambda x: x['frequency'], reverse=reverse)
        elif sort_by == 'word':
            frequency_results.sort(key=lambda x: x['word'], reverse=reverse)
        
        # 限制返回数量
        limit = options.get('limit', None)
        if limit:
            frequency_results = frequency_results[:limit]
        
        return frequency_results
    
    def categorize_words(self, words: List[str], options: Dict = None) -> Dict[str, List[Dict]]:
        """
        词汇分类
        
        Args:
            words: 词汇列表
            options: 分类选项
        
        Returns:
            分类结果
        """
        if not words:
            return {}
        
        options = options or {}
        
        # 统计词频
        word_counter = Counter(words)
        
        # 分类结果
        categorized_words = defaultdict(list)
        uncategorized_words = []
        
        # 按历史分类进行分类
        for word, count in word_counter.items():
            categorized = False
            
            for category, category_words in self.historical_categories.items():
                if word in category_words:
                    categorized_words[category].append({
                        'word': word,
                        'count': count,
                        'frequency': count / len(words)
                    })
                    categorized = True
                    break
            
            if not categorized:
                uncategorized_words.append({
                    'word': word,
                    'count': count,
                    'frequency': count / len(words)
                })
        
        # 添加未分类词汇
        if uncategorized_words:
            categorized_words['其他'] = uncategorized_words
        
        # 对每个分类内的词汇按频次排序
        for category in categorized_words:
            categorized_words[category].sort(key=lambda x: x['count'], reverse=True)
            
            # 限制每个分类的词汇数量
            limit = options.get('category_limit', 20)
            categorized_words[category] = categorized_words[category][:limit]
        
        return dict(categorized_words)
    
    def extract_keywords(self, text: str, options: Dict = None) -> List[Dict]:
        """
        关键词提取
        
        Args:
            text: 待分析文本
            options: 提取选项
        
        Returns:
            关键词列表
        """
        if not text:
            return []
        
        options = options or {}
        
        # 使用TF-IDF提取关键词
        topk = options.get('topk', 20)
        withWeight = options.get('with_weight', True)
        allowPOS = options.get('allow_pos', ('n', 'nr', 'ns', 'nt', 'nz', 'v', 'vd', 'vn', 'a'))
        
        try:
            # 使用jieba的TF-IDF关键词提取
            keywords_with_weight = jieba.analyse.extract_tags(
                text, 
                topK=topk, 
                withWeight=withWeight,
                allowPOS=allowPOS
            )
            
            keywords = []
            for i, item in enumerate(keywords_with_weight):
                if withWeight:
                    word, weight = item
                    keywords.append({
                        'word': word,
                        'weight': round(weight, 4),
                        'rank': i + 1
                    })
                else:
                    keywords.append({
                        'word': item,
                        'rank': i + 1
                    })
            
            return keywords
            
        except Exception as e:
            logger.error(f"关键词提取异常: {str(e)}")
            return []
    
    def generate_wordcloud_data(self, words: List[str], options: Dict = None) -> List[Dict]:
        """
        生成词云数据
        
        Args:
            words: 词汇列表
            options: 生成选项
        
        Returns:
            词云数据
        """
        if not words:
            return []
        
        options = options or {}
        
        # 计算词频
        word_counter = Counter(words)
        max_count = max(word_counter.values()) if word_counter else 1
        
        # 生成词云数据
        wordcloud_data = []
        limit = options.get('limit', 100)
        min_font_size = options.get('min_font_size', 12)
        max_font_size = options.get('max_font_size', 60)
        
        for word, count in word_counter.most_common(limit):
            # 计算字体大小
            font_size = min_font_size + (max_font_size - min_font_size) * (count / max_count)
            
            wordcloud_data.append({
                'text': word,
                'value': count,
                'size': round(font_size, 1)
            })
        
        return wordcloud_data
    
    def analyze_frequency_trend(self, text_segments: List[str], options: Dict = None) -> Dict:
        """
        分析词频趋势
        
        Args:
            text_segments: 文本片段列表（按时间顺序）
            options: 分析选项
        
        Returns:
            词频趋势分析结果
        """
        if not text_segments:
            return {}
        
        options = options or {}
        
        # 提取每个片段的词汇
        segment_words = []
        for segment in text_segments:
            words = self.extract_words(segment, options)
            segment_words.append(words)
        
        # 统计所有词汇
        all_words = []
        for words in segment_words:
            all_words.extend(words)
        
        # 获取高频词汇
        word_counter = Counter(all_words)
        top_words = [word for word, count in word_counter.most_common(options.get('top_words', 20))]
        
        # 计算每个片段中高频词的出现情况
        trend_data = {}
        for word in top_words:
            trend_data[word] = []
            for i, words in enumerate(segment_words):
                word_count = words.count(word)
                trend_data[word].append({
                    'segment': i + 1,
                    'count': word_count,
                    'frequency': word_count / len(words) if words else 0
                })
        
        return {
            'top_words': top_words,
            'trend_data': trend_data,
            'total_segments': len(text_segments)
        }
    
    def analyze(self, text: str, options: Dict = None) -> Dict:
        """
        综合词频分析
        
        Args:
            text: 待分析文本
            options: 分析选项
        
        Returns:
            综合分析结果
        """
        if not text:
            return {}
        
        options = options or {}
        
        try:
            logger.info(f"开始词频分析，文本长度: {len(text)}")
            
            # 提取词汇
            words = self.extract_words(text, options.get('word_extraction', {}))
            
            if not words:
                return {'error': '未能提取到有效词汇'}
            
            # 计算词频
            frequency_results = self.calculate_frequency(
                words, 
                options.get('frequency_calculation', {})
            )
            
            # 词汇分类
            categorized_words = self.categorize_words(
                words, 
                options.get('word_categorization', {})
            )
            
            # 关键词提取
            keywords = self.extract_keywords(
                text, 
                options.get('keyword_extraction', {})
            )
            
            # 生成词云数据
            wordcloud_data = self.generate_wordcloud_data(
                words, 
                options.get('wordcloud_generation', {})
            )
            
            # 基础统计
            total_words = len(words)
            unique_words = len(set(words))
            
            result = {
                'basic_stats': {
                    'total_words': total_words,
                    'unique_words': unique_words,
                    'diversity_ratio': round(unique_words / total_words, 4) if total_words > 0 else 0
                },
                'word_frequency': frequency_results,
                'categorized_words': categorized_words,
                'keywords': keywords,
                'wordcloud_data': wordcloud_data
            }
            
            logger.info(f"词频分析完成，提取词汇: {total_words}, 唯一词汇: {unique_words}")
            
            return result
            
        except Exception as e:
            logger.error(f"词频分析异常: {str(e)}")
            raise