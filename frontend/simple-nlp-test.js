/**
 * 简化的前端NLP集成测试
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 21:00:00
 */

import fetch from 'node-fetch';

// 测试配置
const NLP_BASE_URL = 'http://localhost:5001';
const TEST_TEXT = '明朝永乐年间，郑和率领庞大的船队七次下西洋，访问了东南亚、印度洋、阿拉伯海、红海等地区的30多个国家和地区。这些航海活动不仅展示了中国古代的航海技术和造船工艺，也促进了中外文化交流和贸易往来。';

// 测试结果
let testResults = {
  total: 0,
  passed: 0,
  failed: 0,
  details: []
};

// 工具函数
function log(message, type = 'info') {
  const timestamp = new Date().toLocaleString();
  const prefix = type === 'error' ? '❌' : type === 'success' ? '✅' : 'ℹ️';
  console.log(`[${timestamp}] ${prefix} ${message}`);
}

async function makeRequest(endpoint, data = null) {
  try {
    const options = {
      method: data ? 'POST' : 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    };
    
    if (data) {
      options.body = JSON.stringify(data);
    }
    
    const response = await fetch(`${NLP_BASE_URL}${endpoint}`, options);
    const result = await response.json();
    
    return {
      success: response.ok && result.success !== false,
      status: response.status,
      data: result,
      error: result.message || result.error
    };
  } catch (error) {
    return {
      success: false,
      error: error.message
    };
  }
}

// 测试函数
async function testHealthCheck() {
  log('测试NLP服务健康检查...');
  testResults.total++;
  
  try {
    const response = await makeRequest('/api/health');
    
    if (response.success && response.data.status === 'healthy') {
      log('健康检查通过', 'success');
      testResults.passed++;
      testResults.details.push({
        test: '健康检查',
        status: 'PASSED',
        response: response.data
      });
    } else {
      throw new Error(`健康检查失败: ${response.error || '状态异常'}`);
    }
  } catch (error) {
    log(`健康检查失败: ${error.message}`, 'error');
    testResults.failed++;
    testResults.details.push({
      test: '健康检查',
      status: 'FAILED',
      error: error.message
    });
  }
}

async function testTextStructure() {
  log('测试文本结构分析...');
  testResults.total++;
  
  try {
    const response = await makeRequest('/api/analyze/text', { 
      text: TEST_TEXT,
      type: 'structure'
    });
    
    if (response.success && response.data) {
      const data = response.data;
      log(`文本结构分析成功 - 返回数据: ${JSON.stringify(data).substring(0, 100)}...`, 'success');
      testResults.passed++;
      testResults.details.push({
        test: '文本结构分析',
        status: 'PASSED',
        metrics: {
          dataKeys: Object.keys(data),
          responseSize: JSON.stringify(data).length
        }
      });
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`文本结构分析失败: ${error.message}`, 'error');
    testResults.failed++;
    testResults.details.push({
      test: '文本结构分析',
      status: 'FAILED',
      error: error.message
    });
  }
}

async function testWordFrequency() {
  log('测试词频分析...');
  testResults.total++;
  
  try {
    const response = await makeRequest('/api/analyze/word-frequency', { 
      text: TEST_TEXT,
      max_results: 10,
      min_length: 2
    });
    
    if (response.success && response.data && response.data.data && response.data.data.word_frequency) {
      const wordFreq = response.data.data.word_frequency;
      log(`词频分析成功 - 发现 ${wordFreq.length} 个高频词`, 'success');
      testResults.passed++;
      testResults.details.push({
        test: '词频分析',
        status: 'PASSED',
        metrics: {
          totalWords: wordFreq.length,
          topWords: wordFreq.slice(0, 3).map(w => `${w.word}(${w.frequency})`)
        }
      });
    } else {
      throw new Error(`分析失败: ${response.error || '数据格式不正确'}`);
    }
  } catch (error) {
    log(`词频分析失败: ${error.message}`, 'error');
    testResults.failed++;
    testResults.details.push({
      test: '词频分析',
      status: 'FAILED',
      error: error.message
    });
  }
}

async function testTimeline() {
  log('测试时间轴分析...');
  testResults.total++;
  
  try {
    const response = await makeRequest('/api/analyze/timeline', { text: TEST_TEXT });
    
    if (response.success) {
      const events = response.data.timeline_events || [];
      log(`时间轴分析成功 - 发现 ${events.length} 个时间事件`, 'success');
      testResults.passed++;
      testResults.details.push({
        test: '时间轴分析',
        status: 'PASSED',
        metrics: {
          totalEvents: events.length,
          events: events.slice(0, 2).map(e => e.event_text || e.text)
        }
      });
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`时间轴分析失败: ${error.message}`, 'error');
    testResults.failed++;
    testResults.details.push({
      test: '时间轴分析',
      status: 'FAILED',
      error: error.message
    });
  }
}

async function testGeography() {
  log('测试地理位置分析...');
  testResults.total++;
  
  try {
    const response = await makeRequest('/api/analyze/geographic', { text: TEST_TEXT });
    
    if (response.success) {
      const locations = response.data.locations || [];
      log(`地理位置分析成功 - 发现 ${locations.length} 个地理位置`, 'success');
      testResults.passed++;
      testResults.details.push({
        test: '地理位置分析',
        status: 'PASSED',
        metrics: {
          totalLocations: locations.length,
          locations: locations.slice(0, 3).map(l => l.location || l.name || l)
        }
      });
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`地理位置分析失败: ${error.message}`, 'error');
    testResults.failed++;
    testResults.details.push({
      test: '地理位置分析',
      status: 'FAILED',
      error: error.message
    });
  }
}

async function testTextSummary() {
  log('测试文本摘要生成...');
  testResults.total++;
  
  try {
    const response = await makeRequest('/api/analyze/summary', { 
      text: TEST_TEXT,
      summary_type: 'extractive',
      max_sentences: 2
    });
    
    if (response.success && response.data && response.data.data && response.data.data.summary) {
      const summary = response.data.data.summary;
      const summaryStr = typeof summary === 'string' ? summary : JSON.stringify(summary);
      log(`文本摘要生成成功 - 摘要长度: ${summaryStr.length} 字符`, 'success');
      testResults.passed++;
      testResults.details.push({
        test: '文本摘要生成',
        status: 'PASSED',
        metrics: {
          summaryLength: summaryStr.length,
          originalLength: TEST_TEXT.length,
          summary: summaryStr.substring(0, 50) + '...'
        }
      });
    } else {
      throw new Error(`分析失败: ${response.error || '数据格式不正确'}`);
    }
  } catch (error) {
    log(`文本摘要生成失败: ${error.message}`, 'error');
    testResults.failed++;
    testResults.details.push({
      test: '文本摘要生成',
      status: 'FAILED',
      error: error.message
    });
  }
}

async function testComprehensive() {
  log('测试综合分析...');
  testResults.total++;
  
  try {
    const response = await makeRequest('/api/analyze/comprehensive', { text: TEST_TEXT });
    
    if (response.success) {
      const data = response.data;
      const modules = Object.keys(data).filter(key => data[key] !== null && data[key] !== undefined);
      log(`综合分析成功 - 包含 ${modules.length} 个分析模块`, 'success');
      testResults.passed++;
      testResults.details.push({
        test: '综合分析',
        status: 'PASSED',
        metrics: {
          totalModules: modules.length,
          modules: modules
        }
      });
    } else {
      throw new Error(`分析失败: ${response.error || '未知错误'}`);
    }
  } catch (error) {
    log(`综合分析失败: ${error.message}`, 'error');
    testResults.failed++;
    testResults.details.push({
      test: '综合分析',
      status: 'FAILED',
      error: error.message
    });
  }
}

// 主测试函数
async function runTests() {
  console.log('开始前端NLP集成测试...\n');
  
  const tests = [
    testHealthCheck,
    testTextStructure,
    testWordFrequency,
    testTimeline,
    testGeography,
    testTextSummary,
    testComprehensive
  ];
  
  for (const test of tests) {
    await test();
    await new Promise(resolve => setTimeout(resolve, 500)); // 短暂延迟
  }
  
  // 生成测试报告
  generateReport();
}

function generateReport() {
  const successRate = ((testResults.passed / testResults.total) * 100).toFixed(1);
  
  console.log('\n' + '='.repeat(60));
  console.log('前端NLP集成测试报告');
  console.log('='.repeat(60));
  console.log(`总测试数: ${testResults.total}`);
  console.log(`通过测试: ${testResults.passed}`);
  console.log(`失败测试: ${testResults.failed}`);
  console.log(`成功率: ${successRate}%`);
  console.log('\n详细结果:');
  console.log('-'.repeat(60));
  
  testResults.details.forEach((result, index) => {
    const status = result.status === 'PASSED' ? '✅ 通过' : '❌ 失败';
    console.log(`${index + 1}. ${result.test}: ${status}`);
    
    if (result.metrics) {
      console.log(`   指标: ${JSON.stringify(result.metrics, null, 2)}`);
    }
    
    if (result.error) {
      console.log(`   错误: ${result.error}`);
    }
    console.log('');
  });
  
  console.log('='.repeat(60));
  console.log(`测试完成! 成功率: ${successRate}%`);
  console.log('='.repeat(60));
}

// 运行测试
runTests().catch(console.error);