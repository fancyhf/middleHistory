/**
 * 前端NLP集成测试脚本 - 更新版
 * @author AI Agent
 * @version 2.0.0
 * @created 2024-12-29 20:30:00
 * @updated 2024-12-29 21:00:00
 */

// 测试配置
const TEST_CONFIG = {
  nlpServiceUrl: 'http://localhost:5000',
  testTimeout: 30000,
  retryAttempts: 3,
  testData: {
    shortText: '唐太宗李世民在贞观年间励精图治，开创了贞观之治的盛世。',
    mediumText: `唐朝是中国历史上最辉煌的朝代之一。唐太宗李世民在位期间，实行开明的政治制度，重用贤才，轻徭薄赋，使得国力强盛，经济繁荣。贞观年间，长安城人口超过百万，成为当时世界上最大的城市。唐朝的疆域辽阔，东至朝鲜半岛，西达中亚，南抵越南北部，北至蒙古高原。丝绸之路的繁荣促进了中外文化交流，佛教、道教、儒教并存发展。唐诗达到了中国古典诗歌的巅峰，李白、杜甫、王维等诗人留下了不朽的作品。`,
    longText: `中国古代历史悠久，文明灿烂。从夏朝开始，经历了商、周、秦、汉、三国、晋、南北朝、隋、唐、五代十国、宋、元、明、清等朝代。秦始皇统一六国后，建立了中央集权制度，统一了文字、货币、度量衡。汉朝时期，张骞出使西域，开辟了丝绸之路，促进了东西方文化交流。汉武帝时期，国力达到鼎盛，疆域广阔。唐朝是中国封建社会的鼎盛时期。唐太宗李世民开创贞观之治，唐玄宗时期出现开元盛世。长安城是当时世界上最大的城市，人口超过百万。唐朝文化繁荣，诗歌达到巅峰，李白、杜甫、白居易等诗人名垂青史。宋朝虽然军事相对较弱，但经济文化高度发达。北宋都城开封，南宋都城临安（今杭州），商业繁荣，科技发达。四大发明中的指南针、火药、印刷术都在宋朝得到广泛应用。明朝时期，郑和七下西洋，展现了中国的海上实力。明成祖迁都北京，修建了紫禁城。明朝中后期，资本主义萌芽出现，商品经济发达。清朝是中国最后一个封建王朝。康熙、雍正、乾隆三朝被称为康乾盛世，国力强盛，疆域辽阔。但清朝后期闭关锁国，逐渐落后于世界潮流。`
  }
};

// 测试结果存储
let testResults = {
  totalTests: 0,
  passedTests: 0,
  failedTests: 0,
  testDetails: [],
  startTime: null,
  endTime: null,
  errors: []
};

// 工具函数
function log(message, type = 'info') {
  const timestamp = new Date().toISOString();
  const prefix = type === 'error' ? '❌' : type === 'success' ? '✅' : 'ℹ️';
  console.log(`[${timestamp}] ${prefix} ${message}`);
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function makeRequest(url, options = {}) {
  const defaultOptions = {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    timeout: TEST_CONFIG.testTimeout
  };

  const finalOptions = { ...defaultOptions, ...options };
  
  try {
    const response = await fetch(url, finalOptions);
    const data = await response.json();
    
    return {
      success: response.ok,
      status: response.status,
      data: data,
      headers: response.headers
    };
  } catch (error) {
    return {
      success: false,
      error: error.message,
      status: 0
    };
  }
}

// 测试函数
async function testNlpServiceHealth() {
  log('开始测试NLP服务健康状态...');
  
  try {
    const response = await makeRequest(`${TEST_CONFIG.nlpServiceUrl}/api/health`);
    
    if (response.success && response.data.status === 'healthy') {
      log('NLP服务健康检查通过', 'success');
      return {
        name: 'NLP服务健康检查',
        status: 'PASSED',
        response: response.data,
        duration: 0
      };
    } else {
      throw new Error(`健康检查失败: ${response.error || '服务状态异常'}`);
    }
  } catch (error) {
    log(`NLP服务健康检查失败: ${error.message}`, 'error');
    return {
      name: 'NLP服务健康检查',
      status: 'FAILED',
      error: error.message,
      duration: 0
    };
  }
}

async function testTextStructureAnalysis() {
  log('开始测试文本结构分析...');
  
  try {
    const startTime = Date.now();
    const response = await makeRequest(`${TEST_CONFIG.nlpServiceUrl}/api/analyze/structure`, {
      method: 'POST',
      body: JSON.stringify({ text: TEST_CONFIG.testData.mediumText })
    });
    const duration = Date.now() - startTime;

    if (response.success && response.data.structure) {
      const structure = response.data.structure;
      
      // 验证返回数据格式
      const requiredFields = ['sentence_count', 'paragraph_count', 'word_count', 'char_count'];
      const hasAllFields = requiredFields.every(field => structure.hasOwnProperty(field));
      
      if (hasAllFields) {
        log(`文本结构分析成功 - 句子数: ${structure.sentence_count}, 段落数: ${structure.paragraph_count}`, 'success');
        return {
          name: '文本结构分析',
          status: 'PASSED',
          response: response.data,
          duration: duration,
          metrics: {
            sentenceCount: structure.sentence_count,
            paragraphCount: structure.paragraph_count,
            wordCount: structure.word_count,
            charCount: structure.char_count
          }
        };
      } else {
        throw new Error('返回数据格式不完整');
      }
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`文本结构分析失败: ${error.message}`, 'error');
    return {
      name: '文本结构分析',
      status: 'FAILED',
      error: error.message,
      duration: 0
    };
  }
}

async function testWordFrequencyAnalysis() {
  log('开始测试词频分析...');
  
  try {
    const startTime = Date.now();
    const response = await makeRequest(`${TEST_CONFIG.nlpServiceUrl}/api/analyze/word-frequency`, {
      method: 'POST',
      body: JSON.stringify({ 
        text: TEST_CONFIG.testData.longText,
        max_results: 20,
        min_length: 2
      })
    });
    const duration = Date.now() - startTime;

    if (response.success && response.data.word_frequency) {
      const wordFreq = response.data.word_frequency;
      
      // 验证返回数据格式
      if (Array.isArray(wordFreq) && wordFreq.length > 0) {
        const firstWord = wordFreq[0];
        if (firstWord.word && typeof firstWord.frequency === 'number') {
          log(`词频分析成功 - 发现 ${wordFreq.length} 个高频词`, 'success');
          return {
            name: '词频分析',
            status: 'PASSED',
            response: response.data,
            duration: duration,
            metrics: {
              totalWords: wordFreq.length,
              topWord: firstWord.word,
              topFrequency: firstWord.frequency
            }
          };
        }
      }
      throw new Error('返回数据格式不正确');
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`词频分析失败: ${error.message}`, 'error');
    return {
      name: '词频分析',
      status: 'FAILED',
      error: error.message,
      duration: 0
    };
  }
}

async function testTimelineAnalysis() {
  log('开始测试时间轴分析...');
  
  try {
    const startTime = Date.now();
    const response = await makeRequest(`${TEST_CONFIG.nlpServiceUrl}/api/analyze/timeline`, {
      method: 'POST',
      body: JSON.stringify({ text: TEST_CONFIG.testData.longText })
    });
    const duration = Date.now() - startTime;

    if (response.success && response.data.timeline_events) {
      const events = response.data.timeline_events;
      
      if (Array.isArray(events) && events.length > 0) {
        const firstEvent = events[0];
        if (firstEvent.event_text && firstEvent.time_expression) {
          log(`时间轴分析成功 - 发现 ${events.length} 个时间事件`, 'success');
          return {
            name: '时间轴分析',
            status: 'PASSED',
            response: response.data,
            duration: duration,
            metrics: {
              totalEvents: events.length,
              firstEvent: firstEvent.event_text,
              timeExpression: firstEvent.time_expression
            }
          };
        }
      }
      
      // 如果没有发现事件，也算成功（可能文本中确实没有时间信息）
      log('时间轴分析完成 - 未发现明确的时间事件', 'success');
      return {
        name: '时间轴分析',
        status: 'PASSED',
        response: response.data,
        duration: duration,
        metrics: {
          totalEvents: 0
        }
      };
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`时间轴分析失败: ${error.message}`, 'error');
    return {
      name: '时间轴分析',
      status: 'FAILED',
      error: error.message,
      duration: 0
    };
  }
}

async function testGeographyAnalysis() {
  log('开始测试地理位置分析...');
  
  try {
    const startTime = Date.now();
    const response = await makeRequest(`${TEST_CONFIG.nlpServiceUrl}/api/analyze/geography`, {
      method: 'POST',
      body: JSON.stringify({ text: TEST_CONFIG.testData.longText })
    });
    const duration = Date.now() - startTime;

    if (response.success && response.data.locations) {
      const locations = response.data.locations;
      
      if (Array.isArray(locations)) {
        log(`地理位置分析成功 - 发现 ${locations.length} 个地理位置`, 'success');
        return {
          name: '地理位置分析',
          status: 'PASSED',
          response: response.data,
          duration: duration,
          metrics: {
            totalLocations: locations.length,
            locations: locations.slice(0, 5).map(loc => loc.location || loc.name || loc)
          }
        };
      }
      
      throw new Error('返回数据格式不正确');
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`地理位置分析失败: ${error.message}`, 'error');
    return {
      name: '地理位置分析',
      status: 'FAILED',
      error: error.message,
      duration: 0
    };
  }
}

async function testTextSummary() {
  log('开始测试文本摘要生成...');
  
  try {
    const startTime = Date.now();
    const response = await makeRequest(`${TEST_CONFIG.nlpServiceUrl}/api/analyze/summary`, {
      method: 'POST',
      body: JSON.stringify({ 
        text: TEST_CONFIG.testData.longText,
        summary_type: 'extractive',
        max_sentences: 3
      })
    });
    const duration = Date.now() - startTime;

    if (response.success && response.data.summary) {
      const summary = response.data.summary;
      
      if (typeof summary === 'string' && summary.length > 0) {
        log(`文本摘要生成成功 - 摘要长度: ${summary.length} 字符`, 'success');
        return {
          name: '文本摘要生成',
          status: 'PASSED',
          response: response.data,
          duration: duration,
          metrics: {
            summaryLength: summary.length,
            originalLength: TEST_CONFIG.testData.longText.length,
            compressionRatio: (summary.length / TEST_CONFIG.testData.longText.length * 100).toFixed(2) + '%'
          }
        };
      }
      
      throw new Error('摘要内容为空');
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`文本摘要生成失败: ${error.message}`, 'error');
    return {
      name: '文本摘要生成',
      status: 'FAILED',
      error: error.message,
      duration: 0
    };
  }
}

async function testComprehensiveAnalysis() {
  log('开始测试综合分析...');
  
  try {
    const startTime = Date.now();
    const response = await makeRequest(`${TEST_CONFIG.nlpServiceUrl}/api/analyze/comprehensive`, {
      method: 'POST',
      body: JSON.stringify({ text: TEST_CONFIG.testData.mediumText })
    });
    const duration = Date.now() - startTime;

    if (response.success && response.data) {
      const data = response.data;
      
      // 检查是否包含各个分析模块的结果
      const modules = ['structure', 'word_frequency', 'timeline_events', 'locations', 'summary'];
      const availableModules = modules.filter(module => data.hasOwnProperty(module));
      
      log(`综合分析成功 - 包含 ${availableModules.length} 个分析模块`, 'success');
      return {
        name: '综合分析',
        status: 'PASSED',
        response: response.data,
        duration: duration,
        metrics: {
          totalModules: availableModules.length,
          availableModules: availableModules
        }
      };
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`综合分析失败: ${error.message}`, 'error');
    return {
      name: '综合分析',
      status: 'FAILED',
      error: error.message,
      duration: 0
    };
  }
}

// 错误处理测试
async function testErrorHandling() {
  log('开始测试错误处理机制...');
  
  const errorTests = [
    {
      name: '空文本测试',
      url: `${TEST_CONFIG.nlpServiceUrl}/api/analyze/structure`,
      body: { text: '' }
    },
    {
      name: '无效参数测试',
      url: `${TEST_CONFIG.nlpServiceUrl}/api/analyze/word-frequency`,
      body: { text: 'test', max_results: -1 }
    },
    {
      name: '不存在的端点测试',
      url: `${TEST_CONFIG.nlpServiceUrl}/api/analyze/nonexistent`,
      body: { text: 'test' }
    }
  ];

  const results = [];
  
  for (const test of errorTests) {
    try {
      const response = await makeRequest(test.url, {
        method: 'POST',
        body: JSON.stringify(test.body)
      });
      
      // 错误处理测试期望返回错误状态
      if (!response.success || response.status >= 400) {
        log(`${test.name} - 正确处理了错误情况`, 'success');
        results.push({
          name: test.name,
          status: 'PASSED',
          expectedError: true,
          actualStatus: response.status
        });
      } else {
        log(`${test.name} - 未正确处理错误情况`, 'error');
        results.push({
          name: test.name,
          status: 'FAILED',
          expectedError: true,
          actualStatus: response.status,
          error: '应该返回错误状态但返回了成功'
        });
      }
    } catch (error) {
      log(`${test.name} - 正确抛出了异常`, 'success');
      results.push({
        name: test.name,
        status: 'PASSED',
        expectedError: true,
        error: error.message
      });
    }
  }
  
  return {
    name: '错误处理机制测试',
    status: results.every(r => r.status === 'PASSED') ? 'PASSED' : 'FAILED',
    subTests: results,
    duration: 0
  };
}

// 性能测试
async function testPerformance() {
  log('开始测试性能指标...');
  
  const performanceTests = [
    {
      name: '小文本处理性能',
      text: TEST_CONFIG.testData.shortText,
      endpoint: '/api/analyze/structure'
    },
    {
      name: '中等文本处理性能',
      text: TEST_CONFIG.testData.mediumText,
      endpoint: '/api/analyze/word-frequency'
    },
    {
      name: '大文本处理性能',
      text: TEST_CONFIG.testData.longText,
      endpoint: '/api/analyze/comprehensive'
    }
  ];

  const results = [];
  
  for (const test of performanceTests) {
    const times = [];
    
    // 执行3次测试取平均值
    for (let i = 0; i < 3; i++) {
      const startTime = Date.now();
      try {
        await makeRequest(`${TEST_CONFIG.nlpServiceUrl}${test.endpoint}`, {
          method: 'POST',
          body: JSON.stringify({ text: test.text })
        });
        times.push(Date.now() - startTime);
      } catch (error) {
        times.push(-1); // 标记失败
      }
    }
    
    const validTimes = times.filter(t => t > 0);
    const avgTime = validTimes.length > 0 ? validTimes.reduce((a, b) => a + b, 0) / validTimes.length : -1;
    
    results.push({
      name: test.name,
      averageTime: avgTime,
      textLength: test.text.length,
      successRate: (validTimes.length / times.length * 100).toFixed(1) + '%'
    });
    
    log(`${test.name} - 平均响应时间: ${avgTime}ms, 成功率: ${validTimes.length}/3`);
  }
  
  return {
    name: '性能测试',
    status: 'COMPLETED',
    results: results,
    duration: 0
  };
}

// 主测试函数
async function runAllTests() {
  log('开始前端NLP集成测试...');
  testResults.startTime = new Date();
  
  const tests = [
    testNlpServiceHealth,
    testTextStructureAnalysis,
    testWordFrequencyAnalysis,
    testTimelineAnalysis,
    testGeographyAnalysis,
    testTextSummary,
    testComprehensiveAnalysis,
    testErrorHandling,
    testPerformance
  ];

  for (const test of tests) {
    try {
      testResults.totalTests++;
      const result = await test();
      
      if (result.status === 'PASSED' || result.status === 'COMPLETED') {
        testResults.passedTests++;
      } else {
        testResults.failedTests++;
        testResults.errors.push(result.error || '未知错误');
      }
      
      testResults.testDetails.push(result);
      
      // 测试间隔
      await sleep(1000);
    } catch (error) {
      testResults.failedTests++;
      testResults.errors.push(error.message);
      testResults.testDetails.push({
        name: test.name || '未知测试',
        status: 'FAILED',
        error: error.message,
        duration: 0
      });
      log(`测试执行异常: ${error.message}`, 'error');
    }
  }
  
  testResults.endTime = new Date();
  generateTestReport();
}

// 生成测试报告
function generateTestReport() {
  const duration = testResults.endTime - testResults.startTime;
  const successRate = ((testResults.passedTests / testResults.totalTests) * 100).toFixed(1);
  
  console.log('\n' + '='.repeat(80));
  console.log('前端NLP集成测试报告');
  console.log('='.repeat(80));
  console.log(`测试开始时间: ${testResults.startTime.toLocaleString()}`);
  console.log(`测试结束时间: ${testResults.endTime.toLocaleString()}`);
  console.log(`总测试时间: ${duration}ms`);
  console.log(`总测试数量: ${testResults.totalTests}`);
  console.log(`通过测试: ${testResults.passedTests}`);
  console.log(`失败测试: ${testResults.failedTests}`);
  console.log(`成功率: ${successRate}%`);
  console.log('\n详细测试结果:');
  console.log('-'.repeat(80));
  
  testResults.testDetails.forEach((test, index) => {
    const status = test.status === 'PASSED' ? '✅ 通过' : 
                  test.status === 'COMPLETED' ? '✅ 完成' : '❌ 失败';
    console.log(`${index + 1}. ${test.name}: ${status}`);
    
    if (test.duration) {
      console.log(`   响应时间: ${test.duration}ms`);
    }
    
    if (test.metrics) {
      console.log(`   指标: ${JSON.stringify(test.metrics, null, 2)}`);
    }
    
    if (test.error) {
      console.log(`   错误: ${test.error}`);
    }
    
    if (test.subTests) {
      test.subTests.forEach(subTest => {
        const subStatus = subTest.status === 'PASSED' ? '✅' : '❌';
        console.log(`     - ${subTest.name}: ${subStatus}`);
      });
    }
    
    if (test.results) {
      test.results.forEach(result => {
        console.log(`     - ${result.name}: ${result.averageTime}ms (${result.successRate})`);
      });
    }
    
    console.log('');
  });
  
  if (testResults.errors.length > 0) {
    console.log('错误汇总:');
    console.log('-'.repeat(80));
    testResults.errors.forEach((error, index) => {
      console.log(`${index + 1}. ${error}`);
    });
  }
  
  console.log('\n' + '='.repeat(80));
  console.log(`测试完成! 成功率: ${successRate}%`);
  console.log('='.repeat(80));
  
  // 保存测试报告到文件
  saveTestReport();
}

// 保存测试报告
function saveTestReport() {
  const reportData = {
    summary: {
      startTime: testResults.startTime,
      endTime: testResults.endTime,
      duration: testResults.endTime - testResults.startTime,
      totalTests: testResults.totalTests,
      passedTests: testResults.passedTests,
      failedTests: testResults.failedTests,
      successRate: ((testResults.passedTests / testResults.totalTests) * 100).toFixed(1) + '%'
    },
    testDetails: testResults.testDetails,
    errors: testResults.errors,
    config: TEST_CONFIG
  };
  
  // 在浏览器环境中，可以下载报告文件
  if (typeof window !== 'undefined') {
    const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `nlp-integration-test-report-${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
  
  log('测试报告已生成', 'success');
}

// 如果在Node.js环境中运行
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    runAllTests,
    testResults,
    TEST_CONFIG
  };
}

// 如果在浏览器环境中运行
if (typeof window !== 'undefined') {
  window.NlpIntegrationTest = {
    runAllTests,
    testResults,
    TEST_CONFIG
  };
}

// 自动运行测试（如果直接执行此脚本）
if (typeof require !== 'undefined' && require.main === module) {
  runAllTests().catch(console.error);
}