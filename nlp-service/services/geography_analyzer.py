"""
地理位置识别服务
Author: AI Agent
Version: 1.0.0
Created: 2024-12-29 17:35:00

功能：
- 地名识别和提取
- 地理位置标准化
- 地理关系分析
- 地理分布统计
- 地理可视化数据生成
"""

import re
import jieba
import jieba.posseg as pseg
import logging
from typing import Dict, List, Optional, Any, Tuple
from collections import defaultdict, Counter
import json

logger = logging.getLogger(__name__)

class GeographyAnalyzer:
    """地理位置分析器"""
    
    def __init__(self):
        """初始化地理位置分析器"""
        logger.info("初始化地理位置分析器")
        
        # 地名词典
        self.place_dict = self._init_place_dict()
        
        # 地理层级关系
        self.geo_hierarchy = self._init_geo_hierarchy()
        
        # 地理关键词
        self.geo_keywords = self._init_geo_keywords()
        
        # 地理位置坐标映射
        self.coordinates = self._init_coordinates()
        
        # 历史地名映射
        self.historical_names = self._init_historical_names()
    
    def _init_place_dict(self) -> Dict[str, Dict]:
        """初始化地名词典"""
        places = {
            # 省级行政区
            '北京': {'type': '直辖市', 'level': 'province', 'region': '华北', 'coordinates': [116.4074, 39.9042]},
            '上海': {'type': '直辖市', 'level': 'province', 'region': '华东', 'coordinates': [121.4737, 31.2304]},
            '天津': {'type': '直辖市', 'level': 'province', 'region': '华北', 'coordinates': [117.1901, 39.1084]},
            '重庆': {'type': '直辖市', 'level': 'province', 'region': '西南', 'coordinates': [106.5516, 29.5630]},
            '河北': {'type': '省', 'level': 'province', 'region': '华北', 'coordinates': [114.5149, 38.0428]},
            '山西': {'type': '省', 'level': 'province', 'region': '华北', 'coordinates': [112.5489, 37.8570]},
            '内蒙古': {'type': '自治区', 'level': 'province', 'region': '华北', 'coordinates': [111.7656, 40.8175]},
            '辽宁': {'type': '省', 'level': 'province', 'region': '东北', 'coordinates': [123.4315, 41.8057]},
            '吉林': {'type': '省', 'level': 'province', 'region': '东北', 'coordinates': [125.3245, 43.8868]},
            '黑龙江': {'type': '省', 'level': 'province', 'region': '东北', 'coordinates': [126.6420, 45.7570]},
            '江苏': {'type': '省', 'level': 'province', 'region': '华东', 'coordinates': [118.7674, 32.0415]},
            '浙江': {'type': '省', 'level': 'province', 'region': '华东', 'coordinates': [120.1536, 30.2650]},
            '安徽': {'type': '省', 'level': 'province', 'region': '华东', 'coordinates': [117.2830, 31.8612]},
            '福建': {'type': '省', 'level': 'province', 'region': '华东', 'coordinates': [119.3063, 26.0745]},
            '江西': {'type': '省', 'level': 'province', 'region': '华东', 'coordinates': [115.8921, 28.6765]},
            '山东': {'type': '省', 'level': 'province', 'region': '华东', 'coordinates': [117.0009, 36.6758]},
            '河南': {'type': '省', 'level': 'province', 'region': '华中', 'coordinates': [113.6540, 34.7566]},
            '湖北': {'type': '省', 'level': 'province', 'region': '华中', 'coordinates': [114.2986, 30.5928]},
            '湖南': {'type': '省', 'level': 'province', 'region': '华中', 'coordinates': [112.9823, 28.1949]},
            '广东': {'type': '省', 'level': 'province', 'region': '华南', 'coordinates': [113.2804, 23.1291]},
            '广西': {'type': '自治区', 'level': 'province', 'region': '华南', 'coordinates': [108.3201, 22.8240]},
            '海南': {'type': '省', 'level': 'province', 'region': '华南', 'coordinates': [110.3312, 20.0311]},
            '四川': {'type': '省', 'level': 'province', 'region': '西南', 'coordinates': [104.0665, 30.5723]},
            '贵州': {'type': '省', 'level': 'province', 'region': '西南', 'coordinates': [106.7135, 26.5783]},
            '云南': {'type': '省', 'level': 'province', 'region': '西南', 'coordinates': [102.7123, 25.0406]},
            '西藏': {'type': '自治区', 'level': 'province', 'region': '西南', 'coordinates': [91.1174, 29.6466]},
            '陕西': {'type': '省', 'level': 'province', 'region': '西北', 'coordinates': [108.9486, 34.2631]},
            '甘肃': {'type': '省', 'level': 'province', 'region': '西北', 'coordinates': [103.8236, 36.0581]},
            '青海': {'type': '省', 'level': 'province', 'region': '西北', 'coordinates': [101.7782, 36.6171]},
            '宁夏': {'type': '自治区', 'level': 'province', 'region': '西北', 'coordinates': [106.2586, 38.4717]},
            '新疆': {'type': '自治区', 'level': 'province', 'region': '西北', 'coordinates': [87.6177, 43.7928]},
            
            # 重要城市
            '西安': {'type': '市', 'level': 'city', 'province': '陕西', 'coordinates': [108.9398, 34.3416]},
            '南京': {'type': '市', 'level': 'city', 'province': '江苏', 'coordinates': [118.7969, 32.0603]},
            '杭州': {'type': '市', 'level': 'city', 'province': '浙江', 'coordinates': [120.1551, 30.2741]},
            '成都': {'type': '市', 'level': 'city', 'province': '四川', 'coordinates': [104.0668, 30.5728]},
            '武汉': {'type': '市', 'level': 'city', 'province': '湖北', 'coordinates': [114.3054, 30.5931]},
            '广州': {'type': '市', 'level': 'city', 'province': '广东', 'coordinates': [113.2644, 23.1291]},
            '深圳': {'type': '市', 'level': 'city', 'province': '广东', 'coordinates': [114.0579, 22.5431]},
            '青岛': {'type': '市', 'level': 'city', 'province': '山东', 'coordinates': [120.3826, 36.0671]},
            '大连': {'type': '市', 'level': 'city', 'province': '辽宁', 'coordinates': [121.6147, 38.9140]},
            '厦门': {'type': '市', 'level': 'city', 'province': '福建', 'coordinates': [118.1689, 24.4797]},
            
            # 历史地名
            '长安': {'type': '古都', 'level': 'city', 'modern_name': '西安', 'coordinates': [108.9398, 34.3416]},
            '洛阳': {'type': '古都', 'level': 'city', 'province': '河南', 'coordinates': [112.4540, 34.6197]},
            '开封': {'type': '古都', 'level': 'city', 'province': '河南', 'coordinates': [114.3075, 34.7975]},
            '金陵': {'type': '古都', 'level': 'city', 'modern_name': '南京', 'coordinates': [118.7969, 32.0603]},
            '临安': {'type': '古都', 'level': 'city', 'modern_name': '杭州', 'coordinates': [120.1551, 30.2741]},
            '大都': {'type': '古都', 'level': 'city', 'modern_name': '北京', 'coordinates': [116.4074, 39.9042]},
            '燕京': {'type': '古都', 'level': 'city', 'modern_name': '北京', 'coordinates': [116.4074, 39.9042]},
            
            # 重要地理特征
            '长江': {'type': '河流', 'level': 'feature', 'coordinates': [121.4737, 31.2304]},
            '黄河': {'type': '河流', 'level': 'feature', 'coordinates': [117.0009, 36.6758]},
            '珠江': {'type': '河流', 'level': 'feature', 'coordinates': [113.2644, 23.1291]},
            '长城': {'type': '建筑', 'level': 'feature', 'coordinates': [116.4074, 40.4319]},
            '泰山': {'type': '山脉', 'level': 'feature', 'coordinates': [117.1264, 36.2530]},
            '华山': {'type': '山脉', 'level': 'feature', 'coordinates': [110.0865, 34.4754]},
            '黄山': {'type': '山脉', 'level': 'feature', 'coordinates': [118.1670, 30.1327]},
            '峨眉山': {'type': '山脉', 'level': 'feature', 'coordinates': [103.4844, 29.6016]},
            '西湖': {'type': '湖泊', 'level': 'feature', 'coordinates': [120.1445, 30.2524]},
            '洞庭湖': {'type': '湖泊', 'level': 'feature', 'coordinates': [113.0823, 29.0598]},
            '鄱阳湖': {'type': '湖泊', 'level': 'feature', 'coordinates': [116.1739, 29.0120]}
        }
        
        logger.info(f"初始化地名词典完成，地名数量: {len(places)}")
        return places
    
    def _init_geo_hierarchy(self) -> Dict[str, List[str]]:
        """初始化地理层级关系"""
        hierarchy = {
            '华北': ['北京', '天津', '河北', '山西', '内蒙古'],
            '东北': ['辽宁', '吉林', '黑龙江'],
            '华东': ['上海', '江苏', '浙江', '安徽', '福建', '江西', '山东'],
            '华中': ['河南', '湖北', '湖南'],
            '华南': ['广东', '广西', '海南'],
            '西南': ['重庆', '四川', '贵州', '云南', '西藏'],
            '西北': ['陕西', '甘肃', '青海', '宁夏', '新疆']
        }
        
        logger.info(f"初始化地理层级关系完成，区域数量: {len(hierarchy)}")
        return hierarchy
    
    def _init_geo_keywords(self) -> List[str]:
        """初始化地理关键词"""
        keywords = [
            # 方位词
            '东', '南', '西', '北', '中', '上', '下', '左', '右',
            '东部', '南部', '西部', '北部', '中部', '东南', '西南', '东北', '西北',
            '内', '外', '前', '后', '边', '境', '界', '沿', '近', '远',
            
            # 地形词
            '山', '河', '江', '湖', '海', '岛', '半岛', '平原', '高原', '盆地',
            '峡谷', '沙漠', '草原', '森林', '丘陵', '山脉', '山峰', '山谷',
            '流域', '三角洲', '海湾', '海峡', '港口', '码头',
            
            # 行政词
            '省', '市', '县', '区', '镇', '乡', '村', '街道', '路', '街',
            '州', '府', '郡', '道', '路', '关', '城', '都', '京', '邑',
            
            # 建筑词
            '宫', '殿', '庙', '寺', '塔', '楼', '阁', '亭', '桥', '门',
            '城墙', '城门', '皇宫', '故宫', '天坛', '地坛'
        ]
        
        logger.info(f"初始化地理关键词完成，关键词数量: {len(keywords)}")
        return keywords
    
    def _init_coordinates(self) -> Dict[str, Tuple[float, float]]:
        """初始化地理位置坐标"""
        coordinates = {}
        
        for place, info in self.place_dict.items():
            if 'coordinates' in info:
                coordinates[place] = tuple(info['coordinates'])
        
        logger.info(f"初始化地理坐标完成，坐标数量: {len(coordinates)}")
        return coordinates
    
    def _init_historical_names(self) -> Dict[str, str]:
        """初始化历史地名映射"""
        mappings = {
            '长安': '西安',
            '金陵': '南京',
            '临安': '杭州',
            '大都': '北京',
            '燕京': '北京',
            '汴京': '开封',
            '汴梁': '开封',
            '应天府': '南京',
            '顺天府': '北京',
            '江宁': '南京',
            '建康': '南京',
            '中山': '南京',
            '石头城': '南京'
        }
        
        logger.info(f"初始化历史地名映射完成，映射数量: {len(mappings)}")
        return mappings
    
    def extract_places(self, text: str, options: Dict = None) -> List[Dict]:
        """
        提取地名
        
        Args:
            text: 待分析文本
            options: 提取选项
        
        Returns:
            地名列表
        """
        if not text:
            return []
        
        options = options or {}
        
        places = []
        
        # 使用词性标注提取地名
        words_with_pos = pseg.lcut(text)
        
        for word, pos in words_with_pos:
            if pos == 'ns':  # 地名词性
                place_info = self._analyze_place(word, text)
                if place_info:
                    places.append(place_info)
        
        # 使用地名词典匹配
        for place_name, place_data in self.place_dict.items():
            if place_name in text:
                # 计算出现位置
                positions = []
                start = 0
                while True:
                    pos = text.find(place_name, start)
                    if pos == -1:
                        break
                    positions.append(pos)
                    start = pos + 1
                
                for pos in positions:
                    place_info = {
                        'name': place_name,
                        'position': pos,
                        'length': len(place_name),
                        'confidence': 0.9,  # 词典匹配置信度较高
                        'source': 'dictionary'
                    }
                    place_info.update(place_data)
                    places.append(place_info)
        
        # 去重和排序
        places = self._deduplicate_places(places)
        places.sort(key=lambda x: x['position'])
        
        # 后处理
        places = self._post_process_places(places, options)
        
        return places
    
    def _analyze_place(self, place_name: str, context: str) -> Optional[Dict]:
        """分析地名信息"""
        if not place_name or len(place_name) < 2:
            return None
        
        place_info = {
            'name': place_name,
            'position': context.find(place_name),
            'length': len(place_name),
            'confidence': 0.7,  # 词性标注置信度
            'source': 'pos_tagging'
        }
        
        # 检查是否在词典中
        if place_name in self.place_dict:
            place_info.update(self.place_dict[place_name])
            place_info['confidence'] = 0.9
        else:
            # 尝试推断地名类型
            place_type = self._infer_place_type(place_name)
            if place_type:
                place_info['type'] = place_type
                place_info['level'] = self._infer_place_level(place_name, place_type)
        
        return place_info
    
    def _infer_place_type(self, place_name: str) -> Optional[str]:
        """推断地名类型"""
        # 基于后缀推断
        if place_name.endswith(('省', '市', '县', '区', '镇', '乡', '村')):
            return '行政区'
        elif place_name.endswith(('山', '峰', '岭', '岗')):
            return '山脉'
        elif place_name.endswith(('江', '河', '溪', '川')):
            return '河流'
        elif place_name.endswith(('湖', '海', '池', '潭')):
            return '水体'
        elif place_name.endswith(('岛', '屿', '礁')):
            return '岛屿'
        elif place_name.endswith(('关', '口', '门')):
            return '关隘'
        elif place_name.endswith(('寺', '庙', '观', '宫', '殿')):
            return '建筑'
        
        return None
    
    def _infer_place_level(self, place_name: str, place_type: str) -> str:
        """推断地名级别"""
        if place_type == '行政区':
            if place_name.endswith('省'):
                return 'province'
            elif place_name.endswith('市'):
                return 'city'
            elif place_name.endswith('县'):
                return 'county'
            elif place_name.endswith(('区', '镇', '乡')):
                return 'district'
            elif place_name.endswith('村'):
                return 'village'
        
        return 'feature'
    
    def _deduplicate_places(self, places: List[Dict]) -> List[Dict]:
        """去重地名"""
        if not places:
            return []
        
        # 按名称和位置去重
        unique_places = []
        seen_keys = set()
        
        for place in places:
            key = (place['name'], place['position'])
            if key not in seen_keys:
                unique_places.append(place)
                seen_keys.add(key)
        
        return unique_places
    
    def _post_process_places(self, places: List[Dict], options: Dict) -> List[Dict]:
        """地名后处理"""
        if not places:
            return []
        
        # 过滤低置信度地名
        min_confidence = options.get('min_confidence', 0.5)
        filtered_places = [p for p in places if p.get('confidence', 0) >= min_confidence]
        
        # 标准化历史地名
        for place in filtered_places:
            if place['name'] in self.historical_names:
                place['modern_name'] = self.historical_names[place['name']]
                place['is_historical'] = True
            else:
                place['is_historical'] = False
        
        # 添加地理关系
        for place in filtered_places:
            place['geo_relations'] = self._get_geo_relations(place)
        
        # 限制数量
        max_places = options.get('max_places', 100)
        if len(filtered_places) > max_places:
            # 按置信度排序，取前N个
            filtered_places.sort(key=lambda x: x.get('confidence', 0), reverse=True)
            filtered_places = filtered_places[:max_places]
        
        return filtered_places
    
    def _get_geo_relations(self, place: Dict) -> Dict:
        """获取地理关系"""
        relations = {
            'region': None,
            'province': None,
            'parent': None,
            'children': []
        }
        
        place_name = place['name']
        
        # 查找所属区域
        for region, provinces in self.geo_hierarchy.items():
            if place_name in provinces:
                relations['region'] = region
                break
            
            # 检查是否是该区域内的城市
            if place.get('province') in provinces:
                relations['region'] = region
                relations['province'] = place.get('province')
                break
        
        return relations
    
    def analyze_distribution(self, places: List[Dict], options: Dict = None) -> Dict:
        """
        分析地理分布
        
        Args:
            places: 地名列表
            options: 分析选项
        
        Returns:
            地理分布分析结果
        """
        if not places:
            return {}
        
        options = options or {}
        
        # 按类型统计
        type_distribution = defaultdict(int)
        for place in places:
            place_type = place.get('type', '未知')
            type_distribution[place_type] += 1
        
        # 按级别统计
        level_distribution = defaultdict(int)
        for place in places:
            level = place.get('level', '未知')
            level_distribution[level] += 1
        
        # 按区域统计
        region_distribution = defaultdict(int)
        for place in places:
            region = place.get('geo_relations', {}).get('region', '未知')
            region_distribution[region] += 1
        
        # 按省份统计
        province_distribution = defaultdict(int)
        for place in places:
            province = place.get('province') or place.get('geo_relations', {}).get('province', '未知')
            province_distribution[province] += 1
        
        # 历史地名统计
        historical_count = sum(1 for place in places if place.get('is_historical', False))
        modern_count = len(places) - historical_count
        
        # 生成热力图数据
        heatmap_data = self._generate_heatmap_data(places)
        
        # 地理中心计算
        geo_center = self._calculate_geo_center(places)
        
        return {
            'total_places': len(places),
            'type_distribution': dict(type_distribution),
            'level_distribution': dict(level_distribution),
            'region_distribution': dict(region_distribution),
            'province_distribution': dict(province_distribution),
            'historical_modern_ratio': {
                'historical': historical_count,
                'modern': modern_count,
                'ratio': historical_count / len(places) if places else 0
            },
            'heatmap_data': heatmap_data,
            'geo_center': geo_center,
            'coverage_analysis': self._analyze_coverage(places)
        }
    
    def _generate_heatmap_data(self, places: List[Dict]) -> List[Dict]:
        """生成热力图数据"""
        heatmap_data = []
        
        # 统计每个地点的出现频次
        place_counter = Counter()
        for place in places:
            place_counter[place['name']] += 1
        
        # 生成热力图点
        for place_name, count in place_counter.items():
            if place_name in self.coordinates:
                lng, lat = self.coordinates[place_name]
                heatmap_data.append({
                    'name': place_name,
                    'coordinates': [lng, lat],
                    'value': count,
                    'weight': min(count / max(place_counter.values()) * 100, 100)
                })
        
        return heatmap_data
    
    def _calculate_geo_center(self, places: List[Dict]) -> Optional[Dict]:
        """计算地理中心"""
        valid_coords = []
        
        for place in places:
            place_name = place['name']
            if place_name in self.coordinates:
                lng, lat = self.coordinates[place_name]
                valid_coords.append((lng, lat))
        
        if not valid_coords:
            return None
        
        # 计算平均坐标
        avg_lng = sum(coord[0] for coord in valid_coords) / len(valid_coords)
        avg_lat = sum(coord[1] for coord in valid_coords) / len(valid_coords)
        
        return {
            'longitude': avg_lng,
            'latitude': avg_lat,
            'coordinates': [avg_lng, avg_lat]
        }
    
    def _analyze_coverage(self, places: List[Dict]) -> Dict:
        """分析地理覆盖范围"""
        coverage = {
            'regions_covered': set(),
            'provinces_covered': set(),
            'coordinate_bounds': None
        }
        
        coordinates = []
        
        for place in places:
            # 统计覆盖的区域和省份
            geo_relations = place.get('geo_relations', {})
            if geo_relations.get('region'):
                coverage['regions_covered'].add(geo_relations['region'])
            if geo_relations.get('province'):
                coverage['provinces_covered'].add(geo_relations['province'])
            
            # 收集坐标
            place_name = place['name']
            if place_name in self.coordinates:
                coordinates.append(self.coordinates[place_name])
        
        # 计算坐标边界
        if coordinates:
            lngs = [coord[0] for coord in coordinates]
            lats = [coord[1] for coord in coordinates]
            
            coverage['coordinate_bounds'] = {
                'min_lng': min(lngs),
                'max_lng': max(lngs),
                'min_lat': min(lats),
                'max_lat': max(lats),
                'center': [(min(lngs) + max(lngs)) / 2, (min(lats) + max(lats)) / 2]
            }
        
        # 转换集合为列表
        coverage['regions_covered'] = list(coverage['regions_covered'])
        coverage['provinces_covered'] = list(coverage['provinces_covered'])
        
        return coverage
    
    def generate_map_data(self, places: List[Dict], options: Dict = None) -> Dict:
        """
        生成地图可视化数据
        
        Args:
            places: 地名列表
            options: 生成选项
        
        Returns:
            地图数据
        """
        if not places:
            return {}
        
        options = options or {}
        
        # 生成标记点数据
        markers = []
        for place in places:
            place_name = place['name']
            if place_name in self.coordinates:
                lng, lat = self.coordinates[place_name]
                
                marker = {
                    'name': place_name,
                    'coordinates': [lng, lat],
                    'type': place.get('type', '未知'),
                    'level': place.get('level', 'feature'),
                    'confidence': place.get('confidence', 0),
                    'is_historical': place.get('is_historical', False),
                    'modern_name': place.get('modern_name'),
                    'description': self._generate_place_description(place)
                }
                
                markers.append(marker)
        
        # 生成连线数据（地理关系）
        connections = self._generate_connections(places)
        
        # 生成区域统计数据
        region_stats = self._generate_region_stats(places)
        
        return {
            'markers': markers,
            'connections': connections,
            'region_stats': region_stats,
            'map_center': self._calculate_geo_center(places),
            'zoom_level': self._calculate_zoom_level(places)
        }
    
    def _generate_place_description(self, place: Dict) -> str:
        """生成地点描述"""
        name = place['name']
        place_type = place.get('type', '地点')
        level = place.get('level', '')
        
        description = f"{name}（{place_type}"
        
        if place.get('is_historical'):
            modern_name = place.get('modern_name')
            if modern_name:
                description += f"，今{modern_name}"
        
        if place.get('province'):
            description += f"，{place['province']}"
        
        description += "）"
        
        return description
    
    def _generate_connections(self, places: List[Dict]) -> List[Dict]:
        """生成地理连线数据"""
        connections = []
        
        # 简单的连线逻辑：连接同一省份的地点
        province_places = defaultdict(list)
        
        for place in places:
            province = place.get('province') or place.get('geo_relations', {}).get('province')
            if province and place['name'] in self.coordinates:
                province_places[province].append(place)
        
        # 为每个省份内的地点生成连线
        for province, province_place_list in province_places.items():
            if len(province_place_list) > 1:
                for i in range(len(province_place_list) - 1):
                    place1 = province_place_list[i]
                    place2 = province_place_list[i + 1]
                    
                    connection = {
                        'from': place1['name'],
                        'to': place2['name'],
                        'from_coords': self.coordinates[place1['name']],
                        'to_coords': self.coordinates[place2['name']],
                        'type': 'province_relation',
                        'province': province
                    }
                    
                    connections.append(connection)
        
        return connections
    
    def _generate_region_stats(self, places: List[Dict]) -> Dict:
        """生成区域统计数据"""
        region_stats = {}
        
        for region, provinces in self.geo_hierarchy.items():
            region_places = []
            
            for place in places:
                place_province = place.get('province') or place.get('geo_relations', {}).get('province')
                if place_province in provinces or place['name'] in provinces:
                    region_places.append(place)
            
            if region_places:
                region_stats[region] = {
                    'count': len(region_places),
                    'places': [p['name'] for p in region_places[:10]],  # 限制显示数量
                    'types': list(set(p.get('type', '未知') for p in region_places))
                }
        
        return region_stats
    
    def _calculate_zoom_level(self, places: List[Dict]) -> int:
        """计算地图缩放级别"""
        if not places:
            return 5
        
        # 根据地点分布范围计算缩放级别
        coordinates = []
        for place in places:
            if place['name'] in self.coordinates:
                coordinates.append(self.coordinates[place['name']])
        
        if not coordinates:
            return 5
        
        if len(coordinates) == 1:
            return 10
        
        # 计算坐标范围
        lngs = [coord[0] for coord in coordinates]
        lats = [coord[1] for coord in coordinates]
        
        lng_range = max(lngs) - min(lngs)
        lat_range = max(lats) - min(lats)
        
        max_range = max(lng_range, lat_range)
        
        # 根据范围确定缩放级别
        if max_range > 30:
            return 3
        elif max_range > 15:
            return 4
        elif max_range > 8:
            return 5
        elif max_range > 4:
            return 6
        elif max_range > 2:
            return 7
        elif max_range > 1:
            return 8
        else:
            return 9
    
    def analyze(self, text: str, options: Dict = None) -> Dict:
        """
        综合地理分析
        
        Args:
            text: 待分析文本
            options: 分析选项
        
        Returns:
            地理分析结果
        """
        if not text:
            return {}
        
        options = options or {}
        
        try:
            logger.info(f"开始地理分析，文本长度: {len(text)}")
            
            # 提取地名
            places = self.extract_places(text, options.get('extraction', {}))
            
            # 分析地理分布
            distribution = self.analyze_distribution(places, options.get('distribution', {}))
            
            # 生成地图数据
            map_data = self.generate_map_data(places, options.get('map', {}))
            
            result = {
                'places': places,
                'distribution': distribution,
                'map_data': map_data,
                'summary': {
                    'total_places': len(places),
                    'unique_places': len(set(p['name'] for p in places)),
                    'historical_places': sum(1 for p in places if p.get('is_historical')),
                    'regions_covered': len(distribution.get('regions_covered', [])),
                    'provinces_covered': len(distribution.get('provinces_covered', []))
                }
            }
            
            logger.info(f"地理分析完成，识别地名: {len(places)}")
            
            return result
            
        except Exception as e:
            logger.error(f"地理分析异常: {str(e)}")
            raise