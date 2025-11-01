-- 历史数据统计分析工具示例数据
-- Author: AI Agent
-- Version: 1.0.0
-- Created: 2024-12-29 18:00:00

USE history_analysis;

-- 插入示例用户
INSERT INTO users (username, email, password_hash, full_name, role, status, last_login_at) VALUES
('zhangsan', 'zhangsan@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYON.y', '张三', 'USER', 'ACTIVE', NOW()),
('lisi', 'lisi@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYON.y', '李四', 'USER', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('wangwu', 'wangwu@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYON.y', '王五', 'USER', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('researcher', 'researcher@university.edu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGGrAHKtYON.y', '历史研究员', 'USER', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- 插入示例项目
INSERT INTO projects (name, description, user_id, status, settings, created_at) VALUES
('唐朝历史研究', '研究唐朝政治、经济、文化发展历程的综合性项目', 2, 'ACTIVE', 
 '{"analysis_types": ["word_frequency", "timeline", "geography"], "language": "zh", "auto_analysis": true}', 
 DATE_SUB(NOW(), INTERVAL 10 DAY)),

('宋代文学分析', '分析宋代诗词文学作品的主题、风格和历史背景', 3, 'ACTIVE',
 '{"analysis_types": ["word_frequency", "text_summary"], "focus_period": "宋代", "include_poetry": true}',
 DATE_SUB(NOW(), INTERVAL 7 DAY)),

('明清政治制度对比', '对比分析明朝和清朝的政治制度差异与演变', 4, 'ACTIVE',
 '{"analysis_types": ["timeline", "geography", "multidimensional"], "compare_dynasties": ["明朝", "清朝"]}',
 DATE_SUB(NOW(), INTERVAL 5 DAY)),

('春秋战国史料整理', '整理和分析春秋战国时期的历史文献资料', 2, 'ACTIVE',
 '{"analysis_types": ["word_frequency", "timeline", "geography"], "period": "春秋战国", "include_archaeology": false}',
 DATE_SUB(NOW(), INTERVAL 3 DAY)),

('中国古代地理变迁', '研究中国古代地理环境变化对历史发展的影响', 3, 'ARCHIVED',
 '{"analysis_types": ["geography", "timeline"], "focus": "地理变迁", "time_span": "古代"}',
 DATE_SUB(NOW(), INTERVAL 15 DAY));

-- 插入示例文件
INSERT INTO files (filename, original_filename, file_path, file_size, file_type, mime_type, project_id, user_id, 
                  content_hash, text_content, text_length, status, created_at) VALUES

-- 唐朝历史研究项目的文件
('tang_dynasty_overview.txt', '唐朝概述.txt', '/uploads/tang_dynasty_overview.txt', 15420, 'txt', 'text/plain', 1, 2,
 'a1b2c3d4e5f6', '唐朝（618年-907年）是中国历史上的一个重要朝代，由李渊建立。唐朝是中国古代最强盛的朝代之一，政治开明，经济繁荣，文化昌盛，对外交流频繁。唐太宗李世民开创了"贞观之治"，唐玄宗时期达到"开元盛世"的顶峰。唐朝的都城长安是当时世界上最大的城市，人口超过百万。唐朝实行科举制度，选拔人才，促进了社会流动。唐诗是中国文学的瑰宝，李白、杜甫等诗人留下了不朽的作品。', 
 245, 'PROCESSED', DATE_SUB(NOW(), INTERVAL 9 DAY)),

('tang_politics.txt', '唐朝政治制度.txt', '/uploads/tang_politics.txt', 22800, 'txt', 'text/plain', 1, 2,
 'b2c3d4e5f6a1', '唐朝政治制度以三省六部制为核心。三省指中书省、门下省、尚书省，分别负责决策、审议、执行。六部包括吏部、户部、礼部、兵部、刑部、工部，各司其职。皇帝拥有最高权力，但需要通过三省六部系统来治理国家。唐朝还设立了监察制度，御史台负责监督官员。地方行政实行道、州、县三级制。唐朝的政治制度相对开明，重视人才选拔，科举制度得到完善和发展。',
 198, 'PROCESSED', DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- 宋代文学分析项目的文件  
('song_poetry.txt', '宋代诗词选集.txt', '/uploads/song_poetry.txt', 31200, 'txt', 'text/plain', 2, 3,
 'c3d4e5f6a1b2', '宋代文学以诗词著称，苏轼、李清照、辛弃疾等文学家创作了大量优秀作品。宋词分为豪放派和婉约派两大流派。豪放派以苏轼、辛弃疾为代表，作品气势磅礴，内容广阔；婉约派以柳永、李清照为代表，作品细腻委婉，情感真挚。宋代诗歌继承唐诗传统，又有所创新，注重理趣，体现了宋代理学思想的影响。宋代文学反映了当时的社会生活和思想观念，具有重要的历史价值和文学价值。',
 187, 'PROCESSED', DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- 明清政治制度对比项目的文件
('ming_qing_comparison.txt', '明清制度对比.txt', '/uploads/ming_qing_comparison.txt', 28900, 'txt', 'text/plain', 3, 4,
 'd4e5f6a1b2c3', '明朝和清朝都是中国历史上的重要朝代，在政治制度上既有继承又有发展。明朝废除了丞相制度，皇帝直接统领六部，加强了中央集权。清朝在明朝制度基础上，设立了军机处，进一步强化了皇权。明朝实行卫所制，清朝则建立了八旗制度。在科举制度方面，两朝都很重视，但清朝对汉族士人的限制更多。明朝的监察制度相对完善，清朝则更加严密。两朝在地方行政、法律制度等方面也有不同特点。',
 201, 'PROCESSED', DATE_SUB(NOW(), INTERVAL 4 DAY)),

-- 春秋战国史料整理项目的文件
('chunqiu_zhanguo.txt', '春秋战国史料.txt', '/uploads/chunqiu_zhanguo.txt', 41500, 'txt', 'text/plain', 4, 2,
 'e5f6a1b2c3d4', '春秋战国时期（公元前770年-公元前221年）是中国历史上的大变革时代。春秋时期，周王室衰微，诸侯争霸，出现了齐桓公、晋文公、楚庄王、吴王阖闾、越王勾践等霸主。战国时期，七雄并立，分别是齐、楚、燕、韩、赵、魏、秦。这一时期思想活跃，出现了儒家、道家、法家、墨家等诸子百家。政治上，各国进行变法改革，如商鞅变法、吴起变法等。军事上，战争规模扩大，战术战略不断发展。最终秦国统一六国，结束了分裂局面。',
 234, 'PROCESSED', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 插入示例分析结果
INSERT INTO analysis_results (project_id, user_id, analysis_type, file_ids, parameters, result_data, 
                             status, progress, execution_time, created_at) VALUES

-- 词频分析结果
(1, 2, 'WORD_FREQUENCY', '[1, 2]', 
 '{"max_words": 50, "min_frequency": 2, "filter_stopwords": true}',
 '{"total_words": 1245, "unique_words": 456, "top_words": [{"word": "唐朝", "frequency": 15, "category": "朝代"}, {"word": "政治", "frequency": 12, "category": "制度"}, {"word": "皇帝", "frequency": 8, "category": "人物"}]}',
 'COMPLETED', 100, 2340, DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- 时间轴分析结果  
(1, 2, 'TIMELINE', '[1]',
 '{"enable_dynasty_mapping": true, "min_confidence": 0.5}',
 '{"events": [{"event": "李渊建立唐朝", "time": "618年", "type": "政治", "confidence": 0.95}, {"event": "贞观之治", "time": "627-649年", "type": "政治", "confidence": 0.90}]}',
 'COMPLETED', 100, 3120, DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- 地理分析结果
(1, 2, 'GEOGRAPHY', '[1, 2]',
 '{"enable_coordinates": true, "min_confidence": 0.3}',
 '{"locations": [{"name": "长安", "type": "都城", "coordinates": [34.3416, 108.9398], "count": 5}, {"name": "洛阳", "type": "城市", "coordinates": [34.6197, 112.4540], "count": 3}]}',
 'COMPLETED', 100, 1890, DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- 文本摘要结果
(2, 3, 'TEXT_SUMMARY', '[3]',
 '{"max_sentences": 3, "max_length": 200}',
 '{"summary": "宋代文学以诗词著称，苏轼、李清照、辛弃疾等文学家创作了大量优秀作品。宋词分为豪放派和婉约派两大流派。宋代文学反映了当时的社会生活和思想观念，具有重要的历史价值和文学价值。", "compression_ratio": 0.35}',
 'COMPLETED', 100, 1560, DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- 多维分析结果
(3, 4, 'MULTIDIMENSIONAL', '[4]',
 '{"include_all_types": true, "focus_keywords": ["明朝", "清朝"]}',
 '{"word_frequency": {"明朝": 8, "清朝": 7, "制度": 12}, "timeline": [{"event": "明朝建立", "time": "1368年"}], "geography": [{"name": "北京", "count": 6}]}',
 'COMPLETED', 100, 4230, DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 插入词频分析详细结果
INSERT INTO word_frequency_results (analysis_id, word, frequency, category, weight, rank_position) VALUES
(1, '唐朝', 15, '朝代', 2.5, 1),
(1, '政治', 12, '制度', 1.8, 2),
(1, '皇帝', 8, '人物', 2.0, 3),
(1, '长安', 7, '地理', 1.5, 4),
(1, '制度', 6, '制度', 1.5, 5),
(1, '文化', 5, '文化', 1.3, 6),
(1, '经济', 4, '经济', 1.2, 7),
(1, '科举', 4, '制度', 1.8, 8),
(1, '诗歌', 3, '文化', 1.5, 9),
(1, '贞观', 3, '历史事件', 2.0, 10);

-- 插入时间轴事件详细结果
INSERT INTO timeline_events (analysis_id, event_text, event_type, time_expression, parsed_time, 
                           year_start, year_end, dynasty, confidence, importance_score, 
                           entities, location) VALUES
(2, '李渊建立唐朝', '政治', '618年', '618年', 618, 618, '唐朝', 0.95, 0.90, 
 '{"persons": ["李渊"], "organizations": ["唐朝"]}', '长安'),
(2, '贞观之治开始', '政治', '627年', '627年', 627, 649, '唐朝', 0.90, 0.85,
 '{"persons": ["李世民"], "events": ["贞观之治"]}', '长安'),
(2, '开元盛世', '政治', '713-741年', '713-741年', 713, 741, '唐朝', 0.88, 0.82,
 '{"persons": ["李隆基"], "events": ["开元盛世"]}', '长安'),
(2, '安史之乱', '军事', '755-763年', '755-763年', 755, 763, '唐朝', 0.92, 0.88,
 '{"persons": ["安禄山", "史思明"], "events": ["安史之乱"]}', '洛阳');

-- 插入地理位置详细结果
INSERT INTO geo_locations (analysis_id, original_name, standard_name, location_type, level, 
                          province, latitude, longitude, confidence, occurrence_count,
                          aliases, historical_names, modern_name) VALUES
(3, '长安', '西安', 'historical_city', 'city', '陕西省', 34.3416, 108.9398, 0.95, 5,
 '["西安", "咸阳"]', '["镐京", "长安城"]', '西安'),
(3, '洛阳', '洛阳', 'city', 'city', '河南省', 34.6197, 112.4540, 0.90, 3,
 '["洛阳市"]', '["雒阳", "神都"]', '洛阳'),
(3, '江南', '江南', 'region', 'region', '多省', 30.0000, 120.0000, 0.75, 2,
 '["江南地区"]', '["江东"]', '江南地区'),
(3, '关中', '关中', 'region', 'region', '陕西省', 34.3000, 108.9000, 0.80, 4,
 '["关中平原"]', '["三秦"]', '关中地区');

-- 插入操作日志示例
INSERT INTO operation_logs (user_id, operation_type, resource_type, resource_id, operation_desc,
                           request_params, ip_address, user_agent, execution_time, status, created_at) VALUES
(2, 'CREATE', 'PROJECT', 1, '创建项目：唐朝历史研究', 
 '{"name": "唐朝历史研究", "description": "研究唐朝政治、经济、文化发展历程"}',
 '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', 
 120, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 10 DAY)),

(2, 'UPLOAD', 'FILE', 1, '上传文件：唐朝概述.txt',
 '{"filename": "唐朝概述.txt", "size": 15420}',
 '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
 890, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 9 DAY)),

(2, 'ANALYZE', 'ANALYSIS', 1, '执行词频分析',
 '{"type": "WORD_FREQUENCY", "files": [1, 2]}',
 '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
 2340, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 8 DAY)),

(3, 'LOGIN', 'USER', 3, '用户登录',
 '{"username": "wangwu"}',
 '192.168.1.101', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
 45, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 6 DAY)),

(4, 'CREATE', 'PROJECT', 3, '创建项目：明清政治制度对比',
 '{"name": "明清政治制度对比", "description": "对比分析明朝和清朝的政治制度差异"}',
 '192.168.1.102', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36',
 156, 'SUCCESS', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 更新项目统计数据
UPDATE projects SET 
    file_count = (SELECT COUNT(*) FROM files WHERE project_id = projects.id),
    analysis_count = (SELECT COUNT(*) FROM analysis_results WHERE project_id = projects.id);

-- 插入更多系统配置
INSERT INTO system_config (config_key, config_value, config_type, description, is_public) VALUES
('nlp.service_url', 'http://localhost:5001', 'STRING', 'NLP服务地址', FALSE),
('nlp.timeout', '30', 'INTEGER', 'NLP服务超时时间（秒）', FALSE),
('nlp.retry_count', '3', 'INTEGER', 'NLP服务重试次数', FALSE),
('storage.upload_path', '/uploads', 'STRING', '文件上传路径', FALSE),
('storage.temp_path', '/temp', 'STRING', '临时文件路径', FALSE),
('email.smtp_host', 'smtp.example.com', 'STRING', 'SMTP服务器地址', FALSE),
('email.smtp_port', '587', 'INTEGER', 'SMTP服务器端口', FALSE),
('security.jwt_secret', 'your-secret-key-here', 'STRING', 'JWT密钥', FALSE),
('security.jwt_expiration', '86400', 'INTEGER', 'JWT过期时间（秒）', FALSE),
('ui.theme', 'light', 'STRING', '默认主题', TRUE),
('ui.language', 'zh-CN', 'STRING', '默认语言', TRUE);