/**
 * 简单的NLP功能测试脚本
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

// 使用内置fetch API (Node.js 18+)
// const fetch = require('node-fetch'); // 移除这行

class SimpleNLPTester {
    constructor() {
        this.baseUrl = 'http://localhost:5001/api';
        this.testData = {
            sampleText: '明朝永乐年间，郑和率领庞大的船队七次下西洋，访问了东南亚、印度洋、阿拉伯海、红海等地区的30多个国家和地区。这些航海活动不仅展示了中国古代的航海技术和造船工艺，也促进了中外文化交流和贸易往来。'
        };
        this.testResults = [];
    }

    log(message) {
        const timestamp = new Date().toLocaleTimeString();
        console.log(`[${timestamp}] ${message}`);
    }

    async testAPI(endpoint, data, testName) {
        this.log(`测试 ${testName}...`);
        
        try {
            const fullUrl = `${this.baseUrl}${endpoint}`;
            this.log(`   请求URL: ${fullUrl}`);
            
            const response = await fetch(fullUrl, {
                method: endpoint === '/health' ? 'GET' : 'POST',
                headers: endpoint === '/health' ? {} : {
                    'Content-Type': 'application/json'
                },
                body: endpoint === '/health' ? undefined : JSON.stringify(data)
            });

            const isSuccess = response.ok;
            const responseData = isSuccess ? await response.json() : null;
            
            this.testResults.push({
                testName,
                success: isSuccess,
                endpoint,
                status: response.status,
                data: responseData,
                timestamp: new Date().toISOString()
            });

            if (isSuccess) {
                this.log(`✅ ${testName} 成功`);
                if (responseData) {
                    this.logResponseDetails(testName, responseData);
                }
            } else {
                this.log(`❌ ${testName} 失败 - 状态码: ${response.status}`);
            }

            return isSuccess;
        } catch (error) {
            this.log(`❌ ${testName} 异常: ${error.message}`);
            this.testResults.push({
                testName,
                success: false,
                endpoint,
                error: error.message,
                timestamp: new Date().toISOString()
            });
            return false;
        }
    }

    logResponseDetails(testName, data) {
        switch (testName) {
            case 'NLP服务健康检查':
                this.log(`   状态: ${data.status || 'OK'}`);
                break;
            case '词频分析API':
                this.log(`   词频结果数量: ${data.word_frequency?.length || 0}`);
                if (data.word_frequency && data.word_frequency.length > 0) {
                    this.log(`   前3个高频词: ${data.word_frequency.slice(0, 3).map(w => `${w.word}(${w.frequency})`).join(', ')}`);
                }
                break;
            case '时间轴分析API':
                this.log(`   时间点数量: ${data.timeline?.length || 0}`);
                if (data.timeline && data.timeline.length > 0) {
                    this.log(`   时间范围: ${data.timeline.map(t => t.time).join(', ')}`);
                }
                break;
            case '地理分析API':
                this.log(`   地理位置数量: ${data.locations?.length || 0}`);
                if (data.locations && data.locations.length > 0) {
                    this.log(`   地点: ${data.locations.map(l => l.name).join(', ')}`);
                }
                break;
            case '文本摘要API':
                this.log(`   摘要长度: ${data.summary?.length || 0} 字符`);
                if (data.summary) {
                    this.log(`   摘要预览: ${data.summary.substring(0, 50)}...`);
                }
                break;
        }
    }

    async runAllTests() {
        this.log('🎯 开始NLP功能测试...');
        
        // 测试健康检查
        await this.testAPI('/health', null, 'NLP服务健康检查');
        await this.delay(500);

        // 测试词频分析
        await this.testAPI('/analyze/word-frequency', { text: this.testData.sampleText }, '词频分析API');
        await this.delay(500);

        // 测试时间轴分析
        await this.testAPI('/analyze/timeline', { text: this.testData.sampleText }, '时间轴分析API');
        await this.delay(500);

        // 测试地理分析
        await this.testAPI('/analyze/geographic', { text: this.testData.sampleText }, '地理分析API');
        await this.delay(500);

        // 测试文本摘要
        await this.testAPI('/analyze/summary', { text: this.testData.sampleText }, '文本摘要API');

        this.generateSummary();
    }

    async delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    generateSummary() {
        const successCount = this.testResults.filter(r => r.success).length;
        const totalCount = this.testResults.length;
        const successRate = ((successCount / totalCount) * 100).toFixed(1);

        this.log('\n' + '='.repeat(50));
        this.log('📊 NLP功能测试总结');
        this.log('='.repeat(50));
        this.log(`总测试数: ${totalCount}`);
        this.log(`成功测试: ${successCount}`);
        this.log(`失败测试: ${totalCount - successCount}`);
        this.log(`成功率: ${successRate}%`);
        
        if (successCount === totalCount) {
            this.log('🎉 所有NLP功能测试通过！');
        } else {
            this.log('⚠️ 部分测试失败，需要检查相关功能');
            const failedTests = this.testResults.filter(r => !r.success);
            failedTests.forEach(test => {
                this.log(`   ❌ ${test.testName}: ${test.error || `状态码 ${test.status}`}`);
            });
        }
        this.log('='.repeat(50));

        // 保存详细报告
        this.saveReport();
    }

    saveReport() {
        const report = {
            summary: {
                timestamp: new Date().toISOString(),
                totalTests: this.testResults.length,
                successfulTests: this.testResults.filter(r => r.success).length,
                failedTests: this.testResults.filter(r => !r.success).length,
                successRate: ((this.testResults.filter(r => r.success).length / this.testResults.length) * 100).toFixed(1) + '%'
            },
            testResults: this.testResults
        };

        const fs = require('fs');
        const reportPath = `nlp-test-report-${new Date().toISOString().split('T')[0]}.json`;
        
        try {
            fs.writeFileSync(reportPath, JSON.stringify(report, null, 2), 'utf8');
            this.log(`📄 详细测试报告已保存到: ${reportPath}`);
        } catch (error) {
            this.log(`❌ 保存报告失败: ${error.message}`);
        }
    }

    async testNLPAPIs() {
        this.log('开始测试NLP功能API...\n');
        
        const testText = "明朝永乐年间，郑和率领庞大的船队七次下西洋，访问了东南亚、印度洋、阿拉伯海和非洲东海岸的许多国家和地区。";
        
        const tests = [
            {
                name: 'NLP服务健康检查',
                endpoint: '/health',
                method: 'GET',
                data: null
            },
            {
                name: '词频分析API',
                endpoint: '/analyze/word-frequency',
                method: 'POST',
                data: { text: testText, max_results: 10, min_length: 2 }
            },
            {
                name: '时间轴分析API',
                endpoint: '/analyze/timeline',
                method: 'POST',
                data: { text: testText }
            },
            {
                name: '地理分析API',
                endpoint: '/analyze/geographic',
                method: 'POST',
                data: { text: testText }
            },
            {
                name: '文本摘要API',
                endpoint: '/analyze/summary',
                method: 'POST',
                data: { text: testText, summary_type: 'extractive', max_sentences: 2 }
            }
        ];

        let successCount = 0;
        for (const test of tests) {
            const success = await this.testAPI(test.endpoint, test.data, test.name);
            if (success) successCount++;
            this.log(''); // 空行分隔
        }

        this.log(`\n测试完成！成功: ${successCount}/${tests.length}`);
        this.log(`成功率: ${(successCount / tests.length * 100).toFixed(1)}%`);
        
        return {
            total: tests.length,
            success: successCount,
            rate: (successCount / tests.length * 100).toFixed(1) + '%'
        };
    }
}

// 主执行函数
async function main() {
    const tester = new SimpleNLPTester();
    
    try {
        await tester.runAllTests();
        process.exit(0);
    } catch (error) {
        console.error('💥 测试执行失败:', error.message);
        process.exit(1);
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    main();
}

module.exports = SimpleNLPTester;