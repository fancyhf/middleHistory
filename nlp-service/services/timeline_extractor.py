"""
时间轴事件提取服务
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 17:35:00

功能：
- 时间表达式识别
- 历史事件提取
- 时间轴构建
- 事件关联分析
- 时间序列分析
"""

import re
import jieba
import logging
from typing import Dict, List, Optional, Any, Tuple
from datetime import datetime, timedelta
from collections import defaultdict
import json

logger = logging.getLogger(__name__)

class TimelineExtractor:
    """时间轴事件提取器"""
    
    def __init__(self):
        """初始化时间轴提取器"""
        logger.info("初始化时间轴提取器")
        
        # 时间表达式模式
        self.time_patterns = self._init_time_patterns()
        
        # 历史朝代时间映射
        self.dynasty_periods = self._init_dynasty_periods()
        
        # 历史事件关键词
        self.event_keywords = self._init_event_keywords()
        
        # 时间修饰词
        self.time_modifiers = self._init_time_modifiers()
    
    def _init_time_patterns(self) -> List[Dict]:
        """初始化时间表达式模式"""
        patterns = [
            # 具体年份
            {
                'pattern': r'(\d{1,4})年',
                'type': 'year',
                'priority': 1
            },
            # 朝代年号
            {
                'pattern': r'(春秋|战国|秦|汉|唐|宋|元|明|清)(?:朝)?(?:代)?',
                'type': 'dynasty',
                'priority': 2
            },
            # 皇帝年号
            {
                'pattern': r'(康熙|乾隆|雍正|嘉靖|万历|光绪|宣统|贞观|开元|天宝)(?:年间|时期|朝|代)?',
                'type': 'emperor_era',
                'priority': 2
            },
            # 世纪表达
            {
                'pattern': r'(?:第)?(\d{1,2})世纪',
                'type': 'century',
                'priority': 2
            },
            # 相对时间
            {
                'pattern': r'(古代|近代|现代|当代|上古|中古|近世)',
                'type': 'relative_period',
                'priority': 3
            },
            # 月日表达
            {
                'pattern': r'(\d{1,2})月(\d{1,2})日',
                'type': 'month_day',
                'priority': 1
            },
            # 季节表达
            {
                'pattern': r'(春|夏|秋|冬)(?:季|天)',
                'type': 'season',
                'priority': 3
            },
            # 历史时期
            {
                'pattern': r'(春秋战国|魏晋南北朝|隋唐|宋元|明清|民国|新中国)(?:时期|时代)?',
                'type': 'historical_period',
                'priority': 2
            }
        ]
        
        # 编译正则表达式
        for pattern in patterns:
            pattern['regex'] = re.compile(pattern['pattern'])
        
        logger.info(f"初始化时间表达式模式完成，模式数量: {len(patterns)}")
        return patterns
    
    def _init_dynasty_periods(self) -> Dict[str, Dict]:
        """初始化朝代时间映射"""
        periods = {
            '夏朝': {'start': -2070, 'end': -1600, 'description': '中国第一个世袭制王朝'},
            '商朝': {'start': -1600, 'end': -1046, 'description': '青铜文明鼎盛时期'},
            '周朝': {'start': -1046, 'end': -256, 'description': '中国历史上最长的朝代'},
            '春秋': {'start': -770, 'end': -476, 'description': '春秋时期'},
            '战国': {'start': -475, 'end': -221, 'description': '战国时期'},
            '秦朝': {'start': -221, 'end': -206, 'description': '中国第一个统一的封建王朝'},
            '汉朝': {'start': -206, 'end': 220, 'description': '西汉和东汉'},
            '三国': {'start': 220, 'end': 280, 'description': '魏蜀吴三国鼎立'},
            '晋朝': {'start': 265, 'end': 420, 'description': '西晋和东晋'},
            '南北朝': {'start': 420, 'end': 589, 'description': '南北朝对峙时期'},
            '隋朝': {'start': 581, 'end': 618, 'description': '短暂的统一王朝'},
            '唐朝': {'start': 618, 'end': 907, 'description': '盛唐时期'},
            '宋朝': {'start': 960, 'end': 1279, 'description': '北宋和南宋'},
            '元朝': {'start': 1271, 'end': 1368, 'description': '蒙古族建立的王朝'},
            '明朝': {'start': 1368, 'end': 1644, 'description': '汉族复兴的王朝'},
            '清朝': {'start': 1644, 'end': 1912, 'description': '中国最后一个封建王朝'},
            '民国': {'start': 1912, 'end': 1949, 'description': '中华民国时期'},
            '新中国': {'start': 1949, 'end': 2024, 'description': '中华人民共和国'}
        }
        
        logger.info(f"初始化朝代时间映射完成，朝代数量: {len(periods)}")
        return periods
    
    def _init_event_keywords(self) -> Dict[str, List[str]]:
        """初始化历史事件关键词"""
        keywords = {
            '战争': [
                '战争', '战役', '战斗', '征战', '攻打', '围攻', '进攻', '防守', '抵抗',
                '起义', '叛乱', '革命', '政变', '兵变', '民变', '农民起义'
            ],
            '政治': [
                '建立', '统一', '分裂', '灭亡', '覆灭', '建国', '立国', '称帝', '登基',
                '退位', '禅让', '篡位', '政变', '改革', '变法', '新政', '政策'
            ],
            '文化': [
                '发明', '创造', '著作', '编写', '撰写', '创作', '发现', '开创',
                '传播', '兴起', '发展', '繁荣', '衰落', '复兴', '改进', '完善'
            ],
            '经济': [
                '贸易', '商业', '农业', '手工业', '货币', '税收', '赋税', '经济',
                '繁荣', '发展', '衰退', '改革', '开放', '封闭', '垄断', '自由'
            ],
            '社会': [
                '迁移', '迁都', '人口', '民族', '宗教', '信仰', '习俗', '制度',
                '等级', '阶层', '社会', '民生', '生活', '教育', '科举', '学校'
            ],
            '自然': [
                '地震', '洪水', '干旱', '饥荒', '瘟疫', '灾害', '天灾', '自然',
                '气候', '环境', '生态', '资源', '开发', '保护', '破坏', '恢复'
            ]
        }
        
        logger.info(f"初始化历史事件关键词完成，分类数量: {len(keywords)}")
        return keywords
    
    def _init_time_modifiers(self) -> List[str]:
        """初始化时间修饰词"""
        modifiers = [
            '初', '中', '末', '早', '晚', '前', '后', '上', '下',
            '初期', '中期', '末期', '早期', '晚期', '前期', '后期',
            '开始', '结束', '期间', '时期', '时代', '年间', '左右',
            '大约', '约', '近', '将近', '不到', '超过', '以上', '以下'
        ]
        
        logger.info(f"初始化时间修饰词完成，修饰词数量: {len(modifiers)}")
        return modifiers
    
    def extract_time_expressions(self, text: str, options: Dict = None) -> List[Dict]:
        """
        提取时间表达式
        
        Args:
            text: 待分析文本
            options: 提取选项
        
        Returns:
            时间表达式列表
        """
        if not text:
            return []
        
        options = options or {}
        
        time_expressions = []
        
        # 按优先级处理时间模式
        sorted_patterns = sorted(self.time_patterns, key=lambda x: x['priority'])
        
        for pattern_info in sorted_patterns:
            matches = pattern_info['regex'].finditer(text)
            
            for match in matches:
                time_expr = {
                    'text': match.group(0),
                    'type': pattern_info['type'],
                    'start': match.start(),
                    'end': match.end(),
                    'groups': match.groups(),
                    'priority': pattern_info['priority']
                }
                
                # 解析具体时间信息
                parsed_time = self._parse_time_expression(time_expr)
                if parsed_time:
                    time_expr.update(parsed_time)
                
                time_expressions.append(time_expr)
        
        # 去重和排序
        time_expressions = self._deduplicate_time_expressions(time_expressions)
        time_expressions.sort(key=lambda x: x['start'])
        
        return time_expressions
    
    def _parse_time_expression(self, time_expr: Dict) -> Optional[Dict]:
        """解析时间表达式"""
        expr_type = time_expr['type']
        text = time_expr['text']
        groups = time_expr['groups']
        
        parsed = {}
        
        try:
            if expr_type == 'year':
                year = int(groups[0])
                parsed.update({
                    'year': year,
                    'normalized_year': year,
                    'precision': 'year'
                })
            
            elif expr_type == 'dynasty':
                dynasty = groups[0]
                if dynasty in self.dynasty_periods:
                    period = self.dynasty_periods[dynasty]
                    parsed.update({
                        'dynasty': dynasty,
                        'start_year': period['start'],
                        'end_year': period['end'],
                        'description': period['description'],
                        'precision': 'dynasty'
                    })
            
            elif expr_type == 'century':
                century = int(groups[0])
                start_year = (century - 1) * 100 + 1
                end_year = century * 100
                parsed.update({
                    'century': century,
                    'start_year': start_year,
                    'end_year': end_year,
                    'precision': 'century'
                })
            
            elif expr_type == 'month_day':
                month = int(groups[0])
                day = int(groups[1])
                parsed.update({
                    'month': month,
                    'day': day,
                    'precision': 'day'
                })
            
            elif expr_type == 'historical_period':
                period = groups[0]
                if period in self.dynasty_periods:
                    period_info = self.dynasty_periods[period]
                    parsed.update({
                        'period': period,
                        'start_year': period_info['start'],
                        'end_year': period_info['end'],
                        'precision': 'period'
                    })
            
            return parsed
            
        except (ValueError, IndexError) as e:
            logger.warning(f"解析时间表达式失败: {text}, 错误: {str(e)}")
            return None
    
    def _deduplicate_time_expressions(self, time_expressions: List[Dict]) -> List[Dict]:
        """去重时间表达式"""
        if not time_expressions:
            return []
        
        # 按位置和优先级去重
        unique_expressions = []
        seen_positions = set()
        
        # 按优先级排序
        sorted_expressions = sorted(time_expressions, key=lambda x: (x['start'], x['priority']))
        
        for expr in sorted_expressions:
            position_key = (expr['start'], expr['end'])
            
            if position_key not in seen_positions:
                unique_expressions.append(expr)
                seen_positions.add(position_key)
        
        return unique_expressions
    
    def extract_events(self, text: str, time_expressions: List[Dict] = None, options: Dict = None) -> List[Dict]:
        """
        提取历史事件
        
        Args:
            text: 待分析文本
            time_expressions: 时间表达式列表
            options: 提取选项
        
        Returns:
            历史事件列表
        """
        if not text:
            return []
        
        options = options or {}
        
        # 如果没有提供时间表达式，先提取
        if time_expressions is None:
            time_expressions = self.extract_time_expressions(text, options)
        
        events = []
        
        # 分句处理
        sentences = self._split_sentences(text)
        
        for i, sentence in enumerate(sentences):
            # 查找句子中的时间表达式
            sentence_times = []
            for time_expr in time_expressions:
                if self._is_time_in_sentence(time_expr, sentence, text):
                    sentence_times.append(time_expr)
            
            # 如果句子包含时间表达式，尝试提取事件
            if sentence_times:
                event = self._extract_event_from_sentence(sentence, sentence_times, i)
                if event:
                    events.append(event)
        
        # 事件后处理
        events = self._post_process_events(events, options)
        
        return events
    
    def _split_sentences(self, text: str) -> List[str]:
        """分句"""
        # 使用标点符号分句
        sentence_pattern = r'[。！？；\n]+'
        sentences = re.split(sentence_pattern, text)
        
        # 过滤空句子
        sentences = [s.strip() for s in sentences if s.strip()]
        
        return sentences
    
    def _is_time_in_sentence(self, time_expr: Dict, sentence: str, full_text: str) -> bool:
        """判断时间表达式是否在句子中"""
        # 简单的位置判断
        time_text = time_expr['text']
        return time_text in sentence
    
    def _extract_event_from_sentence(self, sentence: str, time_expressions: List[Dict], sentence_index: int) -> Optional[Dict]:
        """从句子中提取事件"""
        if not sentence or not time_expressions:
            return None
        
        # 分词
        words = jieba.lcut(sentence)
        
        # 识别事件类型
        event_type = self._classify_event_type(words)
        
        # 提取关键实体
        entities = self._extract_entities(words)
        
        # 构建事件
        event = {
            'sentence': sentence,
            'sentence_index': sentence_index,
            'time_expressions': time_expressions,
            'event_type': event_type,
            'entities': entities,
            'keywords': self._extract_event_keywords(words),
            'confidence': self._calculate_event_confidence(sentence, time_expressions, event_type)
        }
        
        # 标准化时间
        normalized_time = self._normalize_event_time(time_expressions)
        if normalized_time:
            event.update(normalized_time)
        
        return event
    
    def _classify_event_type(self, words: List[str]) -> str:
        """分类事件类型"""
        word_set = set(words)
        
        # 计算每个类型的匹配度
        type_scores = {}
        
        for event_type, keywords in self.event_keywords.items():
            score = len(word_set.intersection(set(keywords)))
            if score > 0:
                type_scores[event_type] = score
        
        # 返回得分最高的类型
        if type_scores:
            return max(type_scores, key=type_scores.get)
        else:
            return '其他'
    
    def _extract_entities(self, words: List[str]) -> List[Dict]:
        """提取实体"""
        entities = []
        
        # 简单的实体识别（基于词性和词典）
        import jieba.posseg as pseg
        
        # 重新进行词性标注
        text = ''.join(words)
        words_with_pos = pseg.lcut(text)
        
        for word, pos in words_with_pos:
            entity_type = None
            
            # 人名
            if pos == 'nr':
                entity_type = '人物'
            # 地名
            elif pos == 'ns':
                entity_type = '地点'
            # 机构名
            elif pos == 'nt':
                entity_type = '机构'
            # 其他名词
            elif pos in ['n', 'nz'] and len(word) >= 2:
                entity_type = '概念'
            
            if entity_type:
                entities.append({
                    'text': word,
                    'type': entity_type,
                    'pos': pos
                })
        
        return entities
    
    def _extract_event_keywords(self, words: List[str]) -> List[str]:
        """提取事件关键词"""
        keywords = []
        
        # 过滤停用词和短词
        stop_words = {'的', '了', '在', '是', '有', '和', '就', '不', '人', '都', '一'}
        
        for word in words:
            if len(word) >= 2 and word not in stop_words:
                keywords.append(word)
        
        return keywords[:10]  # 限制关键词数量
    
    def _calculate_event_confidence(self, sentence: str, time_expressions: List[Dict], event_type: str) -> float:
        """计算事件置信度"""
        confidence = 0.0
        
        # 基础分数
        confidence += 0.3
        
        # 时间表达式数量加分
        confidence += min(len(time_expressions) * 0.2, 0.4)
        
        # 句子长度加分
        sentence_length = len(sentence)
        if 10 <= sentence_length <= 100:
            confidence += 0.2
        
        # 事件类型加分
        if event_type != '其他':
            confidence += 0.1
        
        return min(confidence, 1.0)
    
    def _normalize_event_time(self, time_expressions: List[Dict]) -> Optional[Dict]:
        """标准化事件时间"""
        if not time_expressions:
            return None
        
        # 选择最精确的时间表达式
        best_time = None
        best_precision = 0
        
        precision_map = {
            'year': 4,
            'month_day': 3,
            'dynasty': 2,
            'century': 2,
            'period': 1,
            'relative_period': 0
        }
        
        for time_expr in time_expressions:
            precision = precision_map.get(time_expr.get('precision', ''), 0)
            if precision > best_precision:
                best_precision = precision
                best_time = time_expr
        
        if best_time:
            normalized = {
                'primary_time': best_time,
                'time_precision': best_time.get('precision', 'unknown')
            }
            
            # 提取标准化年份
            if 'year' in best_time:
                normalized['normalized_year'] = best_time['year']
            elif 'start_year' in best_time:
                normalized['normalized_year'] = best_time['start_year']
                normalized['year_range'] = (best_time['start_year'], best_time['end_year'])
            
            return normalized
        
        return None
    
    def _post_process_events(self, events: List[Dict], options: Dict) -> List[Dict]:
        """事件后处理"""
        if not events:
            return []
        
        # 过滤低置信度事件
        min_confidence = options.get('min_confidence', 0.3)
        filtered_events = [e for e in events if e.get('confidence', 0) >= min_confidence]
        
        # 按时间排序
        def get_sort_key(event):
            if 'normalized_year' in event:
                return event['normalized_year']
            elif event.get('time_expressions'):
                # 使用第一个时间表达式的开始位置
                return event['time_expressions'][0].get('start', 0)
            else:
                return event.get('sentence_index', 0)
        
        filtered_events.sort(key=get_sort_key)
        
        # 限制事件数量
        max_events = options.get('max_events', 50)
        if len(filtered_events) > max_events:
            filtered_events = filtered_events[:max_events]
        
        return filtered_events
    
    def build_timeline(self, events: List[Dict], options: Dict = None) -> Dict:
        """
        构建时间轴
        
        Args:
            events: 事件列表
            options: 构建选项
        
        Returns:
            时间轴数据
        """
        if not events:
            return {}
        
        options = options or {}
        
        # 按时间分组
        timeline_groups = defaultdict(list)
        
        for event in events:
            # 确定时间分组键
            group_key = self._get_timeline_group_key(event, options)
            if group_key:
                timeline_groups[group_key].append(event)
        
        # 构建时间轴节点
        timeline_nodes = []
        for group_key, group_events in timeline_groups.items():
            node = self._create_timeline_node(group_key, group_events, options)
            if node:
                timeline_nodes.append(node)
        
        # 排序时间轴节点
        timeline_nodes.sort(key=lambda x: x.get('sort_key', 0))
        
        # 构建时间轴统计
        timeline_stats = self._calculate_timeline_stats(timeline_nodes, events)
        
        return {
            'timeline': timeline_nodes,
            'statistics': timeline_stats,
            'total_events': len(events),
            'total_periods': len(timeline_nodes)
        }
    
    def _get_timeline_group_key(self, event: Dict, options: Dict) -> Optional[str]:
        """获取时间轴分组键"""
        group_by = options.get('group_by', 'century')
        
        if 'normalized_year' in event:
            year = event['normalized_year']
            
            if group_by == 'century':
                if year > 0:
                    century = (year - 1) // 100 + 1
                    return f"{century}世纪"
                else:
                    century = abs(year) // 100 + 1
                    return f"公元前{century}世纪"
            
            elif group_by == 'dynasty':
                # 根据年份确定朝代
                for dynasty, period in self.dynasty_periods.items():
                    if period['start'] <= year <= period['end']:
                        return dynasty
                return '未知时期'
            
            elif group_by == 'year':
                return str(year)
        
        # 使用事件中的朝代信息
        for time_expr in event.get('time_expressions', []):
            if time_expr.get('type') == 'dynasty':
                return time_expr.get('dynasty', '未知朝代')
        
        return None
    
    def _create_timeline_node(self, group_key: str, events: List[Dict], options: Dict) -> Optional[Dict]:
        """创建时间轴节点"""
        if not events:
            return None
        
        # 计算节点的时间范围
        years = []
        for event in events:
            if 'normalized_year' in event:
                years.append(event['normalized_year'])
            elif 'year_range' in event:
                years.extend(event['year_range'])
        
        # 统计事件类型
        event_types = defaultdict(int)
        for event in events:
            event_type = event.get('event_type', '其他')
            event_types[event_type] += 1
        
        # 提取关键实体
        all_entities = []
        for event in events:
            all_entities.extend(event.get('entities', []))
        
        # 实体去重和统计
        entity_counter = defaultdict(int)
        for entity in all_entities:
            entity_counter[entity['text']] += 1
        
        top_entities = sorted(entity_counter.items(), key=lambda x: x[1], reverse=True)[:10]
        
        node = {
            'period': group_key,
            'event_count': len(events),
            'events': events[:options.get('max_events_per_node', 10)],
            'event_types': dict(event_types),
            'top_entities': [{'name': name, 'count': count} for name, count in top_entities],
            'time_range': {
                'start': min(years) if years else None,
                'end': max(years) if years else None
            },
            'sort_key': min(years) if years else 0
        }
        
        return node
    
    def _calculate_timeline_stats(self, timeline_nodes: List[Dict], events: List[Dict]) -> Dict:
        """计算时间轴统计"""
        stats = {
            'total_periods': len(timeline_nodes),
            'total_events': len(events),
            'event_distribution': {},
            'entity_distribution': {},
            'time_span': {}
        }
        
        # 事件类型分布
        event_type_counter = defaultdict(int)
        for event in events:
            event_type = event.get('event_type', '其他')
            event_type_counter[event_type] += 1
        
        stats['event_distribution'] = dict(event_type_counter)
        
        # 实体分布
        entity_counter = defaultdict(int)
        for event in events:
            for entity in event.get('entities', []):
                entity_counter[entity['text']] += 1
        
        top_entities = sorted(entity_counter.items(), key=lambda x: x[1], reverse=True)[:20]
        stats['entity_distribution'] = dict(top_entities)
        
        # 时间跨度
        all_years = []
        for event in events:
            if 'normalized_year' in event:
                all_years.append(event['normalized_year'])
        
        if all_years:
            stats['time_span'] = {
                'earliest': min(all_years),
                'latest': max(all_years),
                'span_years': max(all_years) - min(all_years)
            }
        
        return stats
    
    def analyze(self, text: str, options: Dict = None) -> Dict:
        """
        综合时间轴分析
        
        Args:
            text: 待分析文本
            options: 分析选项
        
        Returns:
            时间轴分析结果
        """
        if not text:
            return {}
        
        options = options or {}
        
        try:
            logger.info(f"开始时间轴分析，文本长度: {len(text)}")
            
            # 提取时间表达式
            time_expressions = self.extract_time_expressions(
                text, 
                options.get('time_extraction', {})
            )
            
            # 提取历史事件
            events = self.extract_events(
                text, 
                time_expressions, 
                options.get('event_extraction', {})
            )
            
            # 构建时间轴
            timeline = self.build_timeline(
                events, 
                options.get('timeline_building', {})
            )
            
            result = {
                'time_expressions': time_expressions,
                'events': events,
                'timeline': timeline,
                'summary': {
                    'time_expressions_count': len(time_expressions),
                    'events_count': len(events),
                    'timeline_periods': timeline.get('total_periods', 0)
                }
            }
            
            logger.info(f"时间轴分析完成，提取时间表达式: {len(time_expressions)}, 事件: {len(events)}")
            
            return result
            
        except Exception as e:
            logger.error(f"时间轴分析异常: {str(e)}")
            raise