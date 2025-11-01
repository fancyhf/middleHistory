-- 历史数据统计分析工具测试数据插入脚本
-- @author AI Agent
-- @version 1.0.0
-- @created 2024-12-29 18:10:00

USE history_analysis;

-- 插入测试用户
INSERT INTO users (username, email, password_hash, full_name, status) VALUES
('admin', 'admin@historyanalysis.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYdB9W', '系统管理员', 'ACTIVE'),
('historian1', 'historian1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYdB9W', '历史学者张三', 'ACTIVE'),
('researcher1', 'researcher1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYdB9W', '研究员李四', 'ACTIVE');

-- 插入测试项目
INSERT INTO projects (name, description, user_id, tags, settings, file_count, analysis_count) VALUES
('明清史料分析', '明清时期历史文献的文本分析项目，重点关注政治、经济、社会变迁', 2, 
 JSON_ARRAY('明朝', '清朝', '政治史', '社会史'), 
 JSON_OBJECT('auto_analysis', true, 'language', 'zh-CN', 'analysis_depth', 'detailed'),
 3, 5),
('古代地理研究', '中国古代地理文献的地名识别与分析', 2,
 JSON_ARRAY('古代地理', '地名学', '历史地理'),
 JSON_OBJECT('geo_analysis', true, 'timeline_analysis', false),
 2, 3),
('史记文本研究', '《史记》文本的词频分析和内容挖掘', 3,
 JSON_ARRAY('史记', '司马迁', '文本挖掘'),
 JSON_OBJECT('word_frequency', true, 'sentiment_analysis', true),
 1, 2);

-- 插入测试文件信息
INSERT INTO file_info (project_id, original_name, stored_name, file_path, file_type, file_size, md5_hash, content_preview, status) VALUES
(1, '明史纪事本末.txt', 'file_001_20241229.txt', '/uploads/2024/12/29/file_001_20241229.txt', 'text/plain', 1024000, 'a1b2c3d4e5f6789012345678901234ab', '明史纪事本末，记录明朝重要历史事件...', 'PROCESSED'),
(1, '清史稿选段.txt', 'file_002_20241229.txt', '/uploads/2024/12/29/file_002_20241229.txt', 'text/plain', 2048000, 'b2c3d4e5f6789012345678901234abc1', '清史稿中关于康熙朝政治制度的记述...', 'PROCESSED'),
(1, '明清经济史料.docx', 'file_003_20241229.docx', '/uploads/2024/12/29/file_003_20241229.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 3072000, 'c3d4e5f6789012345678901234abc12d', '明清时期商业发展与货币制度变迁...', 'UPLOADED'),
(2, '水经注地名考.txt', 'file_004_20241229.txt', '/uploads/2024/12/29/file_004_20241229.txt', 'text/plain', 1536000, 'd4e5f6789012345678901234abc12de3', '水经注中的古代地名及其现代对应关系...', 'PROCESSED'),
(2, '禹贡地理志.txt', 'file_005_20241229.txt', '/uploads/2024/12/29/file_005_20241229.txt', 'text/plain', 1280000, 'e5f6789012345678901234abc12de34f', '禹贡九州的地理范围和行政区划...', 'UPLOADED'),
(3, '史记选段.txt', 'file_006_20241229.txt', '/uploads/2024/12/29/file_006_20241229.txt', 'text/plain', 2560000, 'f6789012345678901234abc12de34f56', '史记中关于秦汉政治制度的记述...', 'PROCESSED');

-- 插入测试分析任务
INSERT INTO analysis_tasks (project_id, file_id, task_type, task_name, parameters, status, progress, started_at, completed_at, execution_time) VALUES
(1, 1, 'TEXT_ANALYSIS', '明史纪事本末文本分析', JSON_OBJECT('include_sentiment', true, 'include_entities', true), 'COMPLETED', 100, '2024-12-29 10:00:00', '2024-12-29 10:05:30', 330000),
(1, 1, 'WORD_FREQUENCY', '明史纪事词频统计', JSON_OBJECT('min_word_length', 2, 'max_results', 100), 'COMPLETED', 100, '2024-12-29 10:10:00', '2024-12-29 10:12:15', 135000),
(1, 2, 'TIMELINE_EXTRACTION', '清史稿时间轴提取', JSON_OBJECT('time_granularity', 'year', 'include_events', true), 'COMPLETED', 100, '2024-12-29 11:00:00', '2024-12-29 11:08:45', 525000),
(2, 4, 'GEOGRAPHY_ANALYSIS', '水经注地理分析', JSON_OBJECT('include_coordinates', true, 'modern_mapping', true), 'COMPLETED', 100, '2024-12-29 14:00:00', '2024-12-29 14:06:20', 380000),
(1, NULL, 'TEXT_SUMMARY', '明清史料综合摘要', JSON_OBJECT('summary_length', 'medium', 'key_topics', 5), 'RUNNING', 75, '2024-12-29 16:00:00', NULL, NULL),
(3, 6, 'WORD_FREQUENCY', '史记词频分析', JSON_OBJECT('category_analysis', true, 'historical_terms', true), 'PENDING', 0, NULL, NULL, NULL);

-- 插入测试分析结果
INSERT INTO analysis_results (task_id, result_type, result_data, metadata, cache_key) VALUES
(1, 'text_analysis', 
 JSON_OBJECT(
   'word_count', 15420,
   'sentence_count', 892,
   'paragraph_count', 156,
   'sentiment_score', 0.65,
   'entities', JSON_ARRAY(
     JSON_OBJECT('text', '朱元璋', 'type', 'PERSON', 'confidence', 0.95),
     JSON_OBJECT('text', '南京', 'type', 'LOCATION', 'confidence', 0.88),
     JSON_OBJECT('text', '洪武', 'type', 'TIME_PERIOD', 'confidence', 0.92)
   )
 ),
 JSON_OBJECT('processing_time', 330, 'model_version', '1.0.0'),
 'text_analysis_task_1_20241229'),

(2, 'word_frequency',
 JSON_OBJECT(
   'total_words', 8756,
   'unique_words', 2341,
   'top_words', JSON_ARRAY(
     JSON_OBJECT('word', '皇帝', 'count', 156, 'frequency', 0.0178),
     JSON_OBJECT('word', '朝廷', 'count', 134, 'frequency', 0.0153),
     JSON_OBJECT('word', '官员', 'count', 98, 'frequency', 0.0112),
     JSON_OBJECT('word', '制度', 'count', 87, 'frequency', 0.0099),
     JSON_OBJECT('word', '政治', 'count', 76, 'frequency', 0.0087)
   ),
   'categories', JSON_OBJECT(
     '政治词汇', 45.2,
     '人物称谓', 23.8,
     '地理名词', 18.5,
     '时间概念', 12.5
   )
 ),
 JSON_OBJECT('algorithm', 'jieba_tf', 'stop_words_removed', 1245),
 'word_freq_task_2_20241229'),

(3, 'timeline_extraction',
 JSON_OBJECT(
   'events_count', 67,
   'time_span', JSON_OBJECT('start', '1644年', 'end', '1722年'),
   'events', JSON_ARRAY(
     JSON_OBJECT('time', '1661年', 'event', '康熙帝即位', 'confidence', 0.95, 'type', '政治事件'),
     JSON_OBJECT('time', '1673年', 'event', '三藩之乱爆发', 'confidence', 0.88, 'type', '军事事件'),
     JSON_OBJECT('time', '1681年', 'event', '平定三藩', 'confidence', 0.92, 'type', '军事事件')
   ),
   'timeline_stats', JSON_OBJECT(
     'political_events', 34,
     'military_events', 18,
     'economic_events', 9,
     'cultural_events', 6
   )
 ),
 JSON_OBJECT('extraction_method', 'regex_nlp', 'confidence_threshold', 0.8),
 'timeline_task_3_20241229'),

(4, 'geography_analysis',
 JSON_OBJECT(
   'locations_count', 89,
   'provinces_mentioned', 15,
   'cities_mentioned', 34,
   'rivers_mentioned', 23,
   'mountains_mentioned', 17,
   'top_locations', JSON_ARRAY(
     JSON_OBJECT('name', '长江', 'type', '河流', 'mentions', 45, 'modern_name', '长江'),
     JSON_OBJECT('name', '洛阳', 'type', '城市', 'mentions', 32, 'modern_name', '洛阳市'),
     JSON_OBJECT('name', '泰山', 'type', '山脉', 'mentions', 28, 'modern_name', '泰山')
   ),
   'geographical_distribution', JSON_OBJECT(
     '华北地区', 35.2,
     '华中地区', 28.7,
     '华南地区', 18.9,
     '西北地区', 10.1,
     '东北地区', 4.5,
     '西南地区', 2.6
   )
 ),
 JSON_OBJECT('coordinate_system', 'WGS84', 'mapping_accuracy', 0.87),
 'geo_analysis_task_4_20241229');

-- 插入系统配置
INSERT INTO system_config (config_key, config_value, config_type, description, is_public) VALUES
('system.name', '历史数据统计分析工具', 'STRING', '系统名称', TRUE),
('system.version', '1.0.0', 'STRING', '系统版本', TRUE),
('file.max_size', '104857600', 'INTEGER', '文件最大上传大小（字节）', FALSE),
('file.allowed_types', '["txt", "doc", "docx", "pdf"]', 'JSON', '允许上传的文件类型', FALSE),
('analysis.max_concurrent_tasks', '5', 'INTEGER', '最大并发分析任务数', FALSE),
('cache.default_ttl', '3600', 'INTEGER', '默认缓存过期时间（秒）', FALSE),
('nlp.service_timeout', '30000', 'INTEGER', 'NLP服务超时时间（毫秒）', FALSE),
('ui.theme', 'light', 'STRING', '默认UI主题', TRUE),
('ui.language', 'zh-CN', 'STRING', '默认语言', TRUE),
('maintenance.mode', 'false', 'BOOLEAN', '维护模式开关', FALSE);