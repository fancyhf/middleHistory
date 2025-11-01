/**
 * 完整的UI自动化测试脚本
 * @author AI Agent
 * @version 1.0.0
 * @created 2025-10-30
 */

const puppeteer = require('puppeteer');
const path = require('path');
const fs = require('fs');

class UITestSuite {
    constructor() {
        this.browser = null;
        this.page = null;
        this.testResults = [];
        this.screenshotDir = 'H:\\ui-test-screenshots';
        
        // 确保截图目录存在
        if (!fs.existsSync(this.screenshotDir)) {
            fs.mkdirSync(this.screenshotDir, { recursive: true });
        }
    }

    async setup() {
        console.log('🚀 启动UI测试套件...');
        
        this.browser = await puppeteer.launch({
            executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
            headless: false,
            args: ['--no-sandbox', '--disable-setuid-sandbox'],
            defaultViewport: { width: 1280, height: 720 }
        });
        
        this.page = await this.browser.newPage();
        console.log('✅ 浏览器和页面初始化完成');
    }

    async teardown() {
        if (this.browser) {
            await this.browser.close();
            console.log('✅ 浏览器已关闭');
        }
    }

    async takeScreenshot(testName) {
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
        const filename = `${testName}-${timestamp}.png`;
        const filepath = path.join(this.screenshotDir, filename);
        
        await this.page.screenshot({ path: filepath, fullPage: true });
        console.log(`📸 截图已保存: ${filepath}`);
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
            await this.takeScreenshot(`${testName}-success`);
            
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
            await this.takeScreenshot(`${testName}-failed`);
        }
    }

    async testPageLoad() {
        const url = 'http://localhost:3000/ui-test-simple.html';
        await this.page.goto(url, { waitUntil: 'networkidle2' });
        
        const title = await this.page.title();
        if (!title.includes('NLP UI')) {
            throw new Error(`页面标题不正确: ${title}`);
        }
        
        console.log(`页面标题: ${title}`);
    }

    async testButtonInteraction() {
        // 等待按钮出现
        await this.page.waitForSelector('button', { timeout: 5000 });
        
        // 检查按钮数量
        const buttons = await this.page.$$('button');
        console.log(`检测到 ${buttons.length} 个按钮`);
        
        if (buttons.length === 0) {
            throw new Error('页面中没有找到按钮');
        }
        
        // 点击第一个按钮
        await this.page.click('button');
        console.log('✅ 按钮点击成功');
        
        // 等待可能的响应
        await this.page.waitForTimeout(1000);
    }

    async testTextInput() {
        // 查找文本输入框
        const textInputs = await this.page.$$('input[type="text"], textarea');
        
        if (textInputs.length > 0) {
            console.log(`检测到 ${textInputs.length} 个文本输入框`);
            
            // 在第一个输入框中输入测试文本
            await textInputs[0].type('这是Puppeteer自动化测试文本');
            console.log('✅ 文本输入成功');
            
            // 验证输入的文本
            const inputValue = await textInputs[0].evaluate(el => el.value);
            if (!inputValue.includes('Puppeteer')) {
                throw new Error('文本输入验证失败');
            }
        } else {
            console.log('⚠️  页面中没有找到文本输入框');
        }
    }

    async testPageElements() {
        // 检查页面基本元素
        const elements = {
            'h1': await this.page.$$('h1'),
            'h2': await this.page.$$('h2'),
            'p': await this.page.$$('p'),
            'div': await this.page.$$('div'),
            'button': await this.page.$$('button'),
            'input': await this.page.$$('input')
        };
        
        console.log('页面元素统计:');
        for (const [tag, nodeList] of Object.entries(elements)) {
            console.log(`  ${tag}: ${nodeList.length} 个`);
        }
        
        // 验证页面不为空
        const bodyText = await this.page.$eval('body', el => el.textContent.trim());
        if (bodyText.length === 0) {
            throw new Error('页面内容为空');
        }
        
        console.log(`页面文本长度: ${bodyText.length} 字符`);
    }

    async testResponsiveness() {
        // 测试不同屏幕尺寸
        const viewports = [
            { width: 1920, height: 1080, name: '桌面' },
            { width: 768, height: 1024, name: '平板' },
            { width: 375, height: 667, name: '手机' }
        ];
        
        for (const viewport of viewports) {
            await this.page.setViewport({ width: viewport.width, height: viewport.height });
            await this.page.waitForTimeout(500); // 等待布局调整
            
            console.log(`✅ ${viewport.name}视图 (${viewport.width}x${viewport.height}) 测试完成`);
            await this.takeScreenshot(`responsive-${viewport.name}`);
        }
    }

    async generateReport() {
        const report = {
            testSuite: 'UI自动化测试',
            timestamp: new Date().toISOString(),
            totalTests: this.testResults.length,
            passed: this.testResults.filter(r => r.status === 'PASSED').length,
            failed: this.testResults.filter(r => r.status === 'FAILED').length,
            results: this.testResults
        };
        
        const reportPath = path.join(this.screenshotDir, 'test-report.json');
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        
        console.log('\n📊 测试报告:');
        console.log(`总测试数: ${report.totalTests}`);
        console.log(`通过: ${report.passed}`);
        console.log(`失败: ${report.failed}`);
        console.log(`成功率: ${((report.passed / report.totalTests) * 100).toFixed(1)}%`);
        console.log(`报告已保存到: ${reportPath}`);
        
        return report;
    }

    async runAllTests() {
        try {
            await this.setup();
            
            await this.runTest('页面加载测试', () => this.testPageLoad());
            await this.runTest('页面元素测试', () => this.testPageElements());
            await this.runTest('按钮交互测试', () => this.testButtonInteraction());
            await this.runTest('文本输入测试', () => this.testTextInput());
            await this.runTest('响应式设计测试', () => this.testResponsiveness());
            
            const report = await this.generateReport();
            
            if (report.failed === 0) {
                console.log('\n🎉 所有测试通过！Puppeteer安装和配置完全成功！');
                return true;
            } else {
                console.log('\n⚠️  部分测试失败，请检查详细报告');
                return false;
            }
            
        } finally {
            await this.teardown();
        }
    }
}

// 运行测试套件
async function main() {
    const testSuite = new UITestSuite();
    
    try {
        const success = await testSuite.runAllTests();
        process.exit(success ? 0 : 1);
    } catch (error) {
        console.error('💥 测试套件运行失败:', error);
        process.exit(1);
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    main();
}

module.exports = UITestSuite;