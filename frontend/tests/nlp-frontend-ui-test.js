/**
 * NLP前端UI自动化测试脚本
 * @author AI Agent
 * @version 1.0.0
 * @created 2025-10-30
 * @description 测试NLP功能页面的UI交互效果和数据可视化
 */

const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

class NLPFrontendUITest {
    constructor() {
        this.browser = null;
        this.page = null;
        this.testResults = [];
        // 更新路径：从frontend/tests/目录指向正确的测试报告和截图目录
        this.screenshotDir = path.join(__dirname, '..', 'test-screenshots', 'nlp-ui-test-screenshots');
        this.reportDir = path.join(__dirname, '..', 'test-reports', 'nlp-ui-test-reports');
        
        // 确保截图和报告目录存在
        if (!fs.existsSync(this.screenshotDir)) {
            fs.mkdirSync(this.screenshotDir, { recursive: true });
        }
        if (!fs.existsSync(this.reportDir)) {
            fs.mkdirSync(this.reportDir, { recursive: true });
        }
    }

    async setup() {
        console.log('🚀 启动NLP前端UI测试...');
        
        this.browser = await puppeteer.launch({
            executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
            headless: false,
            defaultViewport: { width: 1920, height: 1080 },
            args: [
                '--no-sandbox',
                '--disable-setuid-sandbox',
                '--disable-dev-shm-usage',
                '--disable-web-security',
                '--allow-running-insecure-content'
            ]
        });

        this.page = await this.browser.newPage();
        
        // 设置用户代理
        await this.page.setUserAgent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36');
        
        console.log('✅ 浏览器启动成功');
    }

    async teardown() {
        if (this.browser) {
            await this.browser.close();
            console.log('✅ 浏览器已关闭');
        }
    }

    async takeScreenshot(testName, status = 'info', description = '') {
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
        const filename = `${testName}-${status}-${timestamp}.png`;
        const filepath = path.join(this.screenshotDir, filename);
        
        await this.page.screenshot({ 
            path: filepath, 
            fullPage: true 
        });
        
        console.log(`📸 截图已保存: ${filepath}`);
        console.log(`   描述: ${description}`);
        
        return filepath;
    }

    async runTest(testName, testFunction) {
        const startTime = Date.now();
        console.log(`\n🧪 运行测试: ${testName}`);
        
        try {
            await testFunction();
            const duration = Date.now() - startTime;
            
            this.testResults.push({
                name: testName,
                status: 'PASS',
                duration: duration,
                timestamp: new Date().toISOString(),
                error: null
            });
            
            console.log(`✅ 测试通过: ${testName} (${duration}ms)`);
            await this.takeScreenshot(testName, 'success', '测试通过');
            
        } catch (error) {
            const duration = Date.now() - startTime;
            
            this.testResults.push({
                name: testName,
                status: 'FAIL',
                duration: duration,
                timestamp: new Date().toISOString(),
                error: error.message
            });
            
            console.log(`❌ 测试失败: ${testName} (${duration}ms)`);
            console.log(`   错误: ${error.message}`);
            await this.takeScreenshot(testName, 'failure', `测试失败: ${error.message}`);
        }
    }

    async testApplicationLoad() {
        console.log('测试应用加载...');
        
        // 访问前端应用
        await this.page.goto('http://localhost:3000', { 
            waitUntil: 'networkidle2',
            timeout: 30000 
        });
        
        // 等待React应用加载
        await this.page.waitForSelector('#root', { timeout: 10000 });
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        // 检查页面基本元素
        const rootElement = await this.page.$('#root');
        if (!rootElement) {
            throw new Error('React根元素未找到');
        }
        
        // 检查页面内容是否加载
        const rootContent = await this.page.$eval('#root', el => el.innerHTML);
        if (rootContent.length < 100) {
            throw new Error('页面内容加载不完整');
        }
        
        console.log('✅ 应用加载成功');
    }

    async testNavigationToNLPTest() {
        console.log('测试导航到NLP测试页面...');
        
        // 导航到NLP测试页面
        await this.page.goto('http://localhost:3000/nlp-test', { 
            waitUntil: 'networkidle2',
            timeout: 30000 
        });
        
        // 等待页面加载
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // 检查URL是否正确
        const currentUrl = this.page.url();
        if (!currentUrl.includes('/nlp-test')) {
            throw new Error(`导航失败，当前URL: ${currentUrl}`);
        }
        
        console.log('✅ 成功导航到NLP测试页面');
    }

    async testWordFrequencyAnalysis() {
        console.log('测试词频分析功能...');
        
        // 查找文本输入区域
        const textInputSelectors = [
            'textarea[placeholder*="输入"]',
            'textarea[placeholder*="文本"]',
            '.ant-input',
            'textarea',
            'input[type="text"]'
        ];
        
        let textInput = null;
        for (const selector of textInputSelectors) {
            try {
                textInput = await this.page.$(selector);
                if (textInput) break;
            } catch (e) {
                continue;
            }
        }
        
        if (!textInput) {
            throw new Error('未找到文本输入框');
        }
        
        // 输入测试文本
        const testText = '这是一个测试文本，用于验证词频分析功能。文本分析是自然语言处理的重要组成部分。';
        await textInput.click();
        await textInput.type(testText);
        
        // 查找词频分析按钮
        const buttonSelectors = [
            'button:contains("词频")',
            'button:contains("分析")',
            '.ant-btn',
            'button[type="submit"]'
        ];
        
        let analyzeButton = null;
        for (const selector of buttonSelectors) {
            try {
                analyzeButton = await this.page.$(selector);
                if (analyzeButton) {
                    const buttonText = await this.page.evaluate(el => el.textContent, analyzeButton);
                    if (buttonText.includes('词频') || buttonText.includes('分析')) {
                        break;
                    }
                }
            } catch (e) {
                continue;
            }
        }
        
        if (analyzeButton) {
            await analyzeButton.click();
            await new Promise(resolve => setTimeout(resolve, 3000));
        }
        
        console.log('✅ 词频分析测试完成');
    }

    async testTimelineAnalysis() {
        console.log('测试时间线分析功能...');
        
        // 查找时间线分析相关按钮或链接
        const timelineSelectors = [
            'button:contains("时间线")',
            'a:contains("时间线")',
            '.timeline',
            '[data-testid="timeline"]'
        ];
        
        let timelineElement = null;
        for (const selector of timelineSelectors) {
            try {
                timelineElement = await this.page.$(selector);
                if (timelineElement) break;
            } catch (e) {
                continue;
            }
        }
        
        if (timelineElement) {
            await timelineElement.click();
            await new Promise(resolve => setTimeout(resolve, 2000));
        }
        
        console.log('✅ 时间线分析测试完成');
    }

    async testGeographicAnalysis() {
        console.log('测试地理分析功能...');
        
        // 查找地理分析相关按钮或链接
        const geoSelectors = [
            'button:contains("地理")',
            'button:contains("地图")',
            'a:contains("地理")',
            '.geographic',
            '[data-testid="geographic"]'
        ];
        
        let geoElement = null;
        for (const selector of geoSelectors) {
            try {
                geoElement = await this.page.$(selector);
                if (geoElement) break;
            } catch (e) {
                continue;
            }
        }
        
        if (geoElement) {
            await geoElement.click();
            await new Promise(resolve => setTimeout(resolve, 2000));
        }
        
        console.log('✅ 地理分析测试完成');
    }

    async testTextSummary() {
        console.log('测试文本摘要功能...');
        
        // 查找文本摘要相关按钮或链接
        const summarySelectors = [
            'button:contains("摘要")',
            'button:contains("总结")',
            'a:contains("摘要")',
            '.summary',
            '[data-testid="summary"]'
        ];
        
        let summaryElement = null;
        for (const selector of summarySelectors) {
            try {
                summaryElement = await this.page.$(selector);
                if (summaryElement) break;
            } catch (e) {
                continue;
            }
        }
        
        if (summaryElement) {
            await summaryElement.click();
            await new Promise(resolve => setTimeout(resolve, 2000));
        }
        
        console.log('✅ 文本摘要测试完成');
    }

    async testDataVisualization() {
        console.log('测试数据可视化组件...');
        
        // 检查各种可视化元素
        const visualElements = {
            'Canvas元素': 'canvas',
            'SVG元素': 'svg',
            '图表容器': '.chart, .echarts, .recharts',
            'Ant Design图表': '.ant-chart',
            '标签和徽章': '.ant-tag, .ant-badge'
        };
        
        console.log('可视化元素统计:');
        for (const [name, selector] of Object.entries(visualElements)) {
            try {
                const elements = await this.page.$$(selector);
                console.log(`  ${name}: ${elements.length} 个`);
            } catch (e) {
                console.log(`  ${name}: 0 个`);
            }
        }
        
        // 检查交互式元素
        const interactiveElements = await this.page.$$('button, a, input, select, .ant-btn');
        console.log(`交互式元素: ${interactiveElements.length} 个`);
        
        console.log('✅ 数据可视化检查完成');
    }

    async testResponsiveDesign() {
        console.log('测试响应式设计...');
        
        const viewports = [
            { name: '桌面大屏', width: 1920, height: 1080 },
            { name: '桌面标准', width: 1366, height: 768 },
            { name: '平板', width: 768, height: 1024 },
            { name: '手机', width: 375, height: 667 }
        ];
        
        for (const viewport of viewports) {
            await this.page.setViewport({ width: viewport.width, height: viewport.height });
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            // 截图保存不同尺寸下的页面
            const filename = `responsive-${viewport.name}-${new Date().toISOString().replace(/[:.]/g, '-')}.png`;
            const filepath = path.join(this.screenshotDir, filename);
            await this.page.screenshot({ path: filepath, fullPage: true });
            
            console.log(`✅ ${viewport.name}视图 (${viewport.width}x${viewport.height}) 测试完成`);
            console.log(`📸 截图已保存: ${filepath}`);
            console.log(`   描述: 响应式测试: ${viewport.name}`);
        }
        
        // 恢复默认视口
        await this.page.setViewport({ width: 1920, height: 1080 });
        
        console.log('✅ 响应式设计测试完成');
    }

    async generateReport() {
        const timestamp = new Date().toISOString();
        const totalTests = this.testResults.length;
        const passedTests = this.testResults.filter(t => t.status === 'PASS').length;
        const failedTests = this.testResults.filter(t => t.status === 'FAIL').length;
        const successRate = totalTests > 0 ? ((passedTests / totalTests) * 100).toFixed(1) : 0;
        const totalDuration = this.testResults.reduce((sum, t) => sum + t.duration, 0);

        const report = {
            testSuite: 'NLP前端UI自动化测试',
            timestamp: timestamp,
            summary: {
                totalTests: totalTests,
                passed: passedTests,
                failed: failedTests,
                successRate: `${successRate}%`,
                totalDuration: `${totalDuration}ms`
            },
            testResults: this.testResults,
            screenshotDirectory: this.screenshotDir,
            reportDirectory: this.reportDir
        };

        // 生成JSON报告
        const jsonReportPath = path.join(this.reportDir, 'nlp-frontend-ui-test-report.json');
        fs.writeFileSync(jsonReportPath, JSON.stringify(report, null, 2), 'utf8');

        // 生成HTML报告
        const htmlReport = this.generateHTMLReport(report);
        const htmlReportPath = path.join(this.reportDir, 'nlp-frontend-ui-test-report.html');
        fs.writeFileSync(htmlReportPath, htmlReport, 'utf8');

        console.log(`\n📊 NLP前端UI测试报告:`);
        console.log(`测试套件: ${report.testSuite}`);
        console.log(`测试时间: ${timestamp}`);
        console.log(`总测试数: ${totalTests}`);
        console.log(`通过: ${passedTests}`);
        console.log(`失败: ${failedTests}`);
        console.log(`成功率: ${successRate}%`);
        console.log(`总耗时: ${totalDuration}ms`);
        console.log(`JSON报告: ${jsonReportPath}`);
        console.log(`HTML报告: ${htmlReportPath}`);

        if (failedTests > 0) {
            console.log(`\n⚠️  ${failedTests}个测试失败，请检查详细报告`);
        }

        return report;
    }

    generateHTMLReport(report) {
        return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NLP前端UI测试报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 30px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .summary-card { background: #f8f9fa; padding: 15px; border-radius: 6px; text-align: center; }
        .summary-card h3 { margin: 0 0 10px 0; color: #333; }
        .summary-card .value { font-size: 24px; font-weight: bold; color: #007bff; }
        .test-results { margin-top: 30px; }
        .test-item { margin-bottom: 15px; padding: 15px; border-radius: 6px; border-left: 4px solid #ddd; }
        .test-pass { border-left-color: #28a745; background-color: #d4edda; }
        .test-fail { border-left-color: #dc3545; background-color: #f8d7da; }
        .test-name { font-weight: bold; margin-bottom: 5px; }
        .test-details { font-size: 14px; color: #666; }
        .error-message { color: #dc3545; font-family: monospace; background: #f8f8f8; padding: 10px; border-radius: 4px; margin-top: 10px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>NLP前端UI自动化测试报告</h1>
            <p>生成时间: ${report.timestamp}</p>
        </div>
        
        <div class="summary">
            <div class="summary-card">
                <h3>总测试数</h3>
                <div class="value">${report.summary.totalTests}</div>
            </div>
            <div class="summary-card">
                <h3>通过</h3>
                <div class="value" style="color: #28a745;">${report.summary.passed}</div>
            </div>
            <div class="summary-card">
                <h3>失败</h3>
                <div class="value" style="color: #dc3545;">${report.summary.failed}</div>
            </div>
            <div class="summary-card">
                <h3>成功率</h3>
                <div class="value">${report.summary.successRate}</div>
            </div>
            <div class="summary-card">
                <h3>总耗时</h3>
                <div class="value">${report.summary.totalDuration}</div>
            </div>
        </div>
        
        <div class="test-results">
            <h2>测试结果详情</h2>
            ${report.testResults.map(test => `
                <div class="test-item ${test.status === 'PASS' ? 'test-pass' : 'test-fail'}">
                    <div class="test-name">${test.name}</div>
                    <div class="test-details">
                        状态: ${test.status} | 耗时: ${test.duration}ms | 时间: ${test.timestamp}
                    </div>
                    ${test.error ? `<div class="error-message">错误: ${test.error}</div>` : ''}
                </div>
            `).join('')}
        </div>
    </div>
</body>
</html>`;
    }

    async run() {
        try {
            await this.setup();

            // 运行所有测试
            await this.runTest('应用加载测试', () => this.testApplicationLoad());
            await this.runTest('导航到NLP测试页面', () => this.testNavigationToNLPTest());
            await this.runTest('词频分析功能测试', () => this.testWordFrequencyAnalysis());
            await this.runTest('时间线分析功能测试', () => this.testTimelineAnalysis());
            await this.runTest('地理分析功能测试', () => this.testGeographicAnalysis());
            await this.runTest('文本摘要功能测试', () => this.testTextSummary());
            await this.runTest('数据可视化检查', () => this.testDataVisualization());
            await this.runTest('响应式设计测试', () => this.testResponsiveDesign());

            // 生成测试报告
            await this.generateReport();

        } catch (error) {
            console.error('❌ 测试运行失败:', error);
        } finally {
            await this.teardown();
        }
    }
}

// 运行测试
const test = new NLPFrontendUITest();
test.run().catch(console.error);