/**
 * Puppeteer测试脚本
 * @author AI Agent
 * @version 1.0.0
 * @created 2025-10-30
 */

const puppeteer = require('puppeteer');
const path = require('path');

async function testPuppeteer() {
    console.log('开始测试Puppeteer...');
    
    let browser;
    try {
        // 使用系统已安装的Chrome浏览器
        browser = await puppeteer.launch({
            executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
            headless: false, // 设置为false以便看到浏览器窗口
            args: ['--no-sandbox', '--disable-setuid-sandbox']
        });
        
        console.log('✅ 浏览器启动成功');
        
        const page = await browser.newPage();
        console.log('✅ 新页面创建成功');
        
        // 测试访问本地页面
        const localUrl = 'http://localhost:3000/ui-test-simple.html';
        console.log(`正在访问: ${localUrl}`);
        
        await page.goto(localUrl, { waitUntil: 'networkidle2' });
        console.log('✅ 页面加载成功');
        
        // 获取页面标题
        const title = await page.title();
        console.log(`页面标题: ${title}`);
        
        // 截图保存到H盘
        const screenshotPath = path.join('H:', 'puppeteer-test-screenshot.png');
        await page.screenshot({ path: screenshotPath });
        console.log(`✅ 截图已保存到: ${screenshotPath}`);
        
        // 测试页面交互
        const buttonExists = await page.$('button') !== null;
        if (buttonExists) {
            console.log('✅ 检测到页面中的按钮元素');
        }
        
        console.log('🎉 Puppeteer测试完成，所有功能正常！');
        
    } catch (error) {
        console.error('❌ 测试失败:', error.message);
        throw error;
    } finally {
        if (browser) {
            await browser.close();
            console.log('✅ 浏览器已关闭');
        }
    }
}

// 运行测试
testPuppeteer()
    .then(() => {
        console.log('\n🎯 测试结果: 成功');
        console.log('Puppeteer已正确安装并配置，可以正常使用！');
        process.exit(0);
    })
    .catch((error) => {
        console.error('\n💥 测试结果: 失败');
        console.error('错误详情:', error);
        process.exit(1);
    });