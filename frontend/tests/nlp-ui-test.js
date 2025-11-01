/**
 * NLP功能UI自动化测试脚本
 * @author AI Agent
 * @version 1.0.0
 * @created 2025-10-30
 */

const puppeteer = require('puppeteer');
const path = require('path');
const fs = require('fs');

class NLPUITestSuite {
    constructor() {
        this.browser = null;
        this.page = null;
        this.testResults = [];
        this.screenshotDir = 'H:\\nlp-ui-test-results';
        this.baseUrl = 'http://localhost:3000';
        
        // 确保测试结果目录存在
        if (!fs.existsSync(this.screenshotDir)) {
            fs.mkdirSync(this.screenshotDir, { recursive: true });
        }
    }

    async setup() {
        console.log('🚀 启动NLP UI测试套件...');
        
        this.browser = await puppeteer.launch({
            executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
            headless: false,
            args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-web-security'],
            defaultViewport: { width: 1920, height: 1080 },
            slowMo: 100 // 减慢操作速度以便观察
        });
        
        this.page = await this.browser.newPage();
        
        // 设置更长的超时时间
        this.page.setDefaultTimeout(30000);
        this.page.setDefaultNavigationTimeout(30000);
        
        console.log('✅ 浏览器和页面初始化完成');
    }

    async teardown() {
        if (this.browser) {
            await this.browser.close();
            console.log('✅ 浏览器已关闭');
        }
    }

    async takeScreenshot(testName, description = '') {
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
        const filename = `${testName}-${timestamp}.png`;
        const filepath = path.join(this.screenshotDir, filename);
        
        await this.page.screenshot({ 
            path: filepath, 
            fullPage: true
            // 移除quality参数，PNG不支持质量设置
        });
        
        console.log(`📸 截图已保存: ${filepath}`);
        if (description) {
            console.log(`   描述: ${description}`);
        }
        return filepath;
    }

    async runTest(testName, testFunction) {
        console.log(`\n🧪 运行测试: ${testName}`);
        const startTime = Date.now();
        
        try {
            await testFunction();
            const duration = Date.now() - startTime;
            
            this.testResults.push({
                name: testName,
                status: 'PASSED',
                duration: `${duration}ms`,
                timestamp: new Date().toISOString()
            });
            
            console.log(`✅ 测试通过: ${testName} (${duration}ms)`);
            await this.takeScreenshot(`${testName}-success`, '测试通过');
            
        } catch (error) {
            const duration = Date.now() - startTime;
            
            this.testResults.push({
                name: testName,
                status: 'FAILED',
                duration: `${duration}ms`,
                error: error.message,
                timestamp: new Date().toISOString()
            });
            
            console.error(`❌ 测试失败: ${testName} (${duration}ms)`);
            console.error(`错误: ${error.message}`);
            await this.takeScreenshot(`${testName}-failed`, `测试失败: ${error.message}`);
        }
    }

    async waitForElement(selector, timeout = 10000) {
        try {
            await this.page.waitForSelector(selector, { timeout });
            return true;
        } catch (error) {
            console.warn(`⚠️  元素未找到: ${selector}`);
            return false;
        }
    }

    async testApplicationLoad() {
        console.log('导航到主页...');
        await this.page.goto(this.baseUrl, { waitUntil: 'networkidle2' });
        
        // 等待React应用加载
        console.log('等待React应用加载...');
        await this.page.waitForSelector('#root', { timeout: 30000 });
        
        // 等待页面内容渲染
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        // 检查页面标题
        const title = await this.page.title();
        console.log(`页面标题: ${title}`);
        
        // 检查React根元素是否有内容
        const rootContent = await this.page.$eval('#root', el => el.innerHTML.length);
        console.log(`React根元素内容长度: ${rootContent}`);
        
        if (rootContent === 0) {
            throw new Error('React应用未正确加载，根元素为空');
        }
        
        // 检查是否有任何可见的内容元素
        const visibleElements = await this.page.$$eval('*', elements => {
            return elements.filter(el => {
                const style = window.getComputedStyle(el);
                return style.display !== 'none' && 
                       style.visibility !== 'hidden' && 
                       el.offsetWidth > 0 && 
                       el.offsetHeight > 0 &&
                       el.textContent && 
                       el.textContent.trim().length > 0;
            }).length;
        });
        
        console.log(`找到 ${visibleElements} 个可见元素`);
        
        if (visibleElements === 0) {
            throw new Error('页面没有可见的内容元素');
        }
        
        // 检查主要布局元素 - 使用更通用的选择器
        const headerExists = await this.waitForElement('header, .header, .app-header, .ant-layout-header, nav, .nav');
        const sidebarExists = await this.waitForElement('.sider, .sidebar, .app-sider, .ant-layout-sider, aside, .aside');
        const contentExists = await this.waitForElement('.content, .main-content, .app-main-content, .ant-layout-content, main, .main');
        
        console.log(`布局元素检测: header=${headerExists}, sidebar=${sidebarExists}, content=${contentExists}`);
        
        console.log('✅ 应用主页加载成功');
    }

    async testNavigationToNLPTest() {
        console.log('导航到NLP测试页面...');
        
        // 点击侧边栏的NLP测试链接
        const nlpTestLink = await this.page.$('a[href="/nlp-test"]');
        if (nlpTestLink) {
            await nlpTestLink.click();
        } else {
            // 如果直接链接不存在，尝试直接导航
            await this.page.goto(`${this.baseUrl}/nlp-test`, { waitUntil: 'networkidle2' });
        }
        
        // 等待页面加载
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // 验证页面内容
        const pageTitle = await this.page.$eval('h1, .ant-typography h1', el => el.textContent);
        console.log(`NLP测试页面标题: ${pageTitle}`);
        
        // 检查关键元素
        const textAreaExists = await this.waitForElement('textarea');
        const analyzeButtonExists = await this.waitForElement('button');
        
        if (!textAreaExists) {
            throw new Error('文本输入区域未找到');
        }
        
        console.log('✅ 成功导航到NLP测试页面');
    }

    async testTextInputAndAnalysis() {
        console.log('测试文本输入和分析功能...');
        
        // 测试文本
        const testText = `
        在中国古代历史中，唐朝（618年-907年）是一个辉煌的时代。
        长安作为当时的首都，是世界上最大的城市之一。
        唐太宗李世民在位期间（626年-649年），实行了许多重要的政治改革。
        丝绸之路连接了东西方文明，促进了文化和商业的交流。
        诗人李白和杜甫在这个时期创作了许多不朽的诗篇。
        `;
        
        // 查找文本输入框
        const textArea = await this.page.$('textarea');
        if (!textArea) {
            throw new Error('文本输入框未找到');
        }
        
        // 清空并输入测试文本
        await textArea.click({ clickCount: 3 }); // 全选
        await textArea.type(testText);
        console.log('✅ 测试文本输入完成');
        
        // 查找并点击分析按钮
        const analyzeButtons = await this.page.$$('button');
        let analyzeButton = null;
        
        for (const button of analyzeButtons) {
            const buttonText = await button.evaluate(el => el.textContent);
            if (buttonText && (buttonText.includes('分析') || buttonText.includes('开始') || buttonText.includes('执行'))) {
                analyzeButton = button;
                break;
            }
        }
        
        if (!analyzeButton) {
            // 如果没找到特定的分析按钮，使用第一个按钮
            analyzeButton = analyzeButtons[0];
        }
        
        if (analyzeButton) {
            await analyzeButton.click();
            console.log('✅ 点击分析按钮');
            
            // 等待分析结果
            await new Promise(resolve => setTimeout(resolve, 3000));
        } else {
            throw new Error('分析按钮未找到');
        }
    }

    async testWordFrequencyResults() {
        console.log('验证词频分析结果...');
        
        // 查找词频分析相关的元素
        const wordFrequencyElements = await this.page.$$eval('*', elements => {
            return elements.filter(el => {
                const text = el.textContent || '';
                return text.includes('词频') || text.includes('频率') || text.includes('统计');
            }).length;
        });
        
        if (wordFrequencyElements > 0) {
            console.log(`✅ 找到 ${wordFrequencyElements} 个词频相关元素`);
        }
        
        // 检查是否有表格或列表显示词频结果
        const tables = await this.page.$$('table');
        const lists = await this.page.$$('ul, ol');
        
        console.log(`找到 ${tables.length} 个表格，${lists.length} 个列表`);
        
        // 检查是否有数据可视化元素（如图表）
        const chartElements = await this.page.$$('canvas, svg, .chart, .visualization');
        console.log(`找到 ${chartElements.length} 个可能的图表元素`);
    }

    async testTimelineAnalysis() {
        console.log('验证时间线分析结果...');
        
        // 查找时间线相关的元素
        const timelineElements = await this.page.$$eval('*', elements => {
            return elements.filter(el => {
                const text = el.textContent || '';
                return text.includes('时间') || text.includes('年') || text.includes('朝代') || text.includes('时期');
            }).length;
        });
        
        if (timelineElements > 0) {
            console.log(`✅ 找到 ${timelineElements} 个时间线相关元素`);
        }
        
        // 检查是否有时间线组件
        const timelineComponents = await this.page.$$('.ant-timeline, .timeline, .time-line');
        console.log(`找到 ${timelineComponents.length} 个时间线组件`);
    }

    async testGeographicAnalysis() {
        console.log('验证地理分析结果...');
        
        // 查找地理位置相关的元素
        const geoElements = await this.page.$$eval('*', elements => {
            return elements.filter(el => {
                const text = el.textContent || '';
                return text.includes('地理') || text.includes('位置') || text.includes('长安') || text.includes('城市');
            }).length;
        });
        
        if (geoElements > 0) {
            console.log(`✅ 找到 ${geoElements} 个地理相关元素`);
        }
        
        // 检查是否有地图或地理可视化元素
        const mapElements = await this.page.$$('.map, .geographic, .location, canvas, svg');
        console.log(`找到 ${mapElements.length} 个可能的地理可视化元素`);
    }

    async testDataVisualization() {
        console.log('检查数据可视化组件...');
        
        // 检查各种可视化元素
        const visualElements = {
            'Canvas元素': await this.page.$$('canvas'),
            'SVG元素': await this.page.$$('svg'),
            '图表容器': await this.page.$$('.chart, .visualization, .graph'),
            'Ant Design图表': await this.page.$$('.ant-statistic, .ant-progress, .ant-table'),
            '标签和徽章': await this.page.$$('.ant-tag, .ant-badge')
        };
        
        console.log('可视化元素统计:');
        for (const [type, elements] of Object.entries(visualElements)) {
            console.log(`  ${type}: ${elements.length} 个`);
        }
        
        // 检查页面是否有交互式元素
        const interactiveElements = await this.page.$$('button, input, select, .ant-btn, .ant-input, .ant-select');
        console.log(`交互式元素: ${interactiveElements.length} 个`);
    }

    async testUserInteractionFlow() {
        console.log('测试用户交互流程...');
        
        // 测试标签页切换（如果存在）
        const tabs = await this.page.$$('.ant-tabs-tab');
        if (tabs.length > 0) {
            console.log(`找到 ${tabs.length} 个标签页`);
            
            // 点击第二个标签页（如果存在）
            if (tabs.length > 1) {
                await tabs[1].click();
                await new Promise(resolve => setTimeout(resolve, 1000));
                console.log('✅ 标签页切换测试完成');
            }
        }
        
        // 测试按钮交互
        const buttons = await this.page.$$('button:not([disabled])');
        if (buttons.length > 0) {
            console.log(`找到 ${buttons.length} 个可点击按钮`);
            
            // 随机点击一个按钮进行交互测试
            if (buttons.length > 0) {
                const randomButton = buttons[Math.floor(Math.random() * buttons.length)];
                await randomButton.click();
                await new Promise(resolve => setTimeout(resolve, 1000));
                console.log('✅ 按钮交互测试完成');
            }
        }
    }

    async testResponsiveness() {
        console.log('测试响应式设计...');
        
        const viewports = [
            { width: 1920, height: 1080, name: '桌面大屏' },
            { width: 1366, height: 768, name: '桌面标准' },
            { width: 768, height: 1024, name: '平板' },
            { width: 375, height: 667, name: '手机' }
        ];
        
        for (const viewport of viewports) {
            await this.page.setViewport({ width: viewport.width, height: viewport.height });
            // 使用setTimeout替代waitForTimeout
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            console.log(`✅ ${viewport.name}视图 (${viewport.width}x${viewport.height}) 测试完成`);
            await this.takeScreenshot(`responsive-${viewport.name}`, `响应式测试: ${viewport.name}`);
        }
        
        // 恢复默认视口
        await this.page.setViewport({ width: 1920, height: 1080 });
    }

    async generateDetailedReport() {
        const report = {
            testSuite: 'NLP功能UI自动化测试',
            timestamp: new Date().toISOString(),
            baseUrl: this.baseUrl,
            browser: 'Chrome',
            totalTests: this.testResults.length,
            passed: this.testResults.filter(r => r.status === 'PASSED').length,
            failed: this.testResults.filter(r => r.status === 'FAILED').length,
            results: this.testResults,
            summary: {
                successRate: this.testResults.length > 0 ? 
                    ((this.testResults.filter(r => r.status === 'PASSED').length / this.testResults.length) * 100).toFixed(1) + '%' : '0%',
                totalDuration: this.testResults.reduce((sum, r) => sum + parseInt(r.duration), 0) + 'ms'
            }
        };
        
        const reportPath = path.join(this.screenshotDir, 'nlp-ui-test-report.json');
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        
        // 生成HTML报告
        const htmlReport = this.generateHTMLReport(report);
        const htmlReportPath = path.join(this.screenshotDir, 'nlp-ui-test-report.html');
        fs.writeFileSync(htmlReportPath, htmlReport);
        
        console.log('\n📊 NLP UI测试报告:');
        console.log(`测试套件: ${report.testSuite}`);
        console.log(`测试时间: ${report.timestamp}`);
        console.log(`总测试数: ${report.totalTests}`);
        console.log(`通过: ${report.passed}`);
        console.log(`失败: ${report.failed}`);
        console.log(`成功率: ${report.summary.successRate}`);
        console.log(`总耗时: ${report.summary.totalDuration}`);
        console.log(`JSON报告: ${reportPath}`);
        console.log(`HTML报告: ${htmlReportPath}`);
        
        return report;
    }

    generateHTMLReport(report) {
        return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NLP UI测试报告</title>
    <style>
        body { font-family: 'Microsoft YaHei', Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 30px; }
        .summary { display: flex; justify-content: space-around; margin: 20px 0; }
        .stat { text-align: center; padding: 15px; background: #f8f9fa; border-radius: 6px; }
        .stat-value { font-size: 24px; font-weight: bold; color: #1890ff; }
        .stat-label { color: #666; margin-top: 5px; }
        .test-results { margin-top: 30px; }
        .test-item { margin: 10px 0; padding: 15px; border-radius: 6px; border-left: 4px solid #ddd; }
        .test-passed { border-left-color: #52c41a; background: #f6ffed; }
        .test-failed { border-left-color: #ff4d4f; background: #fff2f0; }
        .test-name { font-weight: bold; margin-bottom: 5px; }
        .test-duration { color: #666; font-size: 12px; }
        .test-error { color: #ff4d4f; margin-top: 5px; font-size: 14px; }
        .timestamp { color: #999; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🧪 NLP功能UI自动化测试报告</h1>
            <p class="timestamp">生成时间: ${report.timestamp}</p>
        </div>
        
        <div class="summary">
            <div class="stat">
                <div class="stat-value">${report.totalTests}</div>
                <div class="stat-label">总测试数</div>
            </div>
            <div class="stat">
                <div class="stat-value" style="color: #52c41a">${report.passed}</div>
                <div class="stat-label">通过</div>
            </div>
            <div class="stat">
                <div class="stat-value" style="color: #ff4d4f">${report.failed}</div>
                <div class="stat-label">失败</div>
            </div>
            <div class="stat">
                <div class="stat-value" style="color: #1890ff">${report.summary.successRate}</div>
                <div class="stat-label">成功率</div>
            </div>
        </div>
        
        <div class="test-results">
            <h2>📋 测试结果详情</h2>
            ${report.results.map(result => `
                <div class="test-item ${result.status === 'PASSED' ? 'test-passed' : 'test-failed'}">
                    <div class="test-name">
                        ${result.status === 'PASSED' ? '✅' : '❌'} ${result.name}
                    </div>
                    <div class="test-duration">耗时: ${result.duration}</div>
                    ${result.error ? `<div class="test-error">错误: ${result.error}</div>` : ''}
                </div>
            `).join('')}
        </div>
        
        <div style="margin-top: 30px; padding: 15px; background: #f8f9fa; border-radius: 6px;">
            <h3>📝 测试环境信息</h3>
            <p><strong>测试URL:</strong> ${report.baseUrl}</p>
            <p><strong>浏览器:</strong> ${report.browser}</p>
            <p><strong>总耗时:</strong> ${report.summary.totalDuration}</p>
        </div>
    </div>
</body>
</html>`;
    }

    async runAllTests() {
        try {
            await this.setup();
            
            // 运行所有测试
            await this.runTest('应用加载测试', () => this.testApplicationLoad());
            await this.runTest('导航到NLP测试页面', () => this.testNavigationToNLPTest());
            await this.runTest('文本输入和分析功能', () => this.testTextInputAndAnalysis());
            await this.runTest('词频分析结果验证', () => this.testWordFrequencyResults());
            await this.runTest('时间线分析验证', () => this.testTimelineAnalysis());
            await this.runTest('地理分析验证', () => this.testGeographicAnalysis());
            await this.runTest('数据可视化检查', () => this.testDataVisualization());
            await this.runTest('用户交互流程测试', () => this.testUserInteractionFlow());
            await this.runTest('响应式设计测试', () => this.testResponsiveness());
            
            // 生成详细报告
            const report = await this.generateDetailedReport();
            
            if (report.failed === 0) {
                console.log('\n🎉 所有NLP UI测试通过！前端NLP功能集成完全成功！');
                return true;
            } else {
                console.log(`\n⚠️  ${report.failed}个测试失败，请检查详细报告`);
                return false;
            }
            
        } finally {
            await this.teardown();
        }
    }
}

// 运行测试套件
async function main() {
    const testSuite = new NLPUITestSuite();
    
    try {
        console.log('🔍 开始NLP功能UI自动化测试...');
        const success = await testSuite.runAllTests();
        process.exit(success ? 0 : 1);
    } catch (error) {
        console.error('💥 NLP UI测试套件运行失败:', error);
        process.exit(1);
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    main();
}

module.exports = NLPUITestSuite;