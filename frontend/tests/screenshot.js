/**
 * 前端UI截图脚本
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function takeScreenshots() {
  // 创建截图目录
  const screenshotDir = path.join(__dirname, 'screenshots');
  if (!fs.existsSync(screenshotDir)) {
    fs.mkdirSync(screenshotDir);
  }

  // 启动浏览器
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1920, height: 1080 }
  });
  const page = await context.newPage();

  try {
    console.log('开始截图...');
    
    // 等待页面加载
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle' });
    await page.waitForTimeout(2000); // 等待2秒确保页面完全加载

    // 页面路由配置
    const pages = [
      { name: 'dashboard', path: '/', title: '仪表板' },
      { name: 'text-analysis', path: '/text-analysis', title: '文本分析' },
      { name: 'word-cloud', path: '/word-cloud', title: '词云分析' },
      { name: 'geographical-analysis', path: '/geographical-analysis', title: '地理分析' },
      { name: 'timeline-analysis', path: '/timeline-analysis', title: '时间轴分析' }
    ];

    // 为每个页面截图
    for (const pageInfo of pages) {
      console.log(`正在截图: ${pageInfo.title} (${pageInfo.path})`);
      
      // 导航到页面
      await page.goto(`http://localhost:3000${pageInfo.path}`, { waitUntil: 'networkidle' });
      await page.waitForTimeout(3000); // 等待页面内容加载完成
      
      // 等待可能的异步数据加载
      try {
        await page.waitForSelector('.ant-spin', { state: 'detached', timeout: 5000 });
      } catch (e) {
        // 如果没有loading状态，继续执行
      }
      
      // 截图
      const screenshotPath = path.join(screenshotDir, `${pageInfo.name}.png`);
      await page.screenshot({ 
        path: screenshotPath, 
        fullPage: true
      });
      
      console.log(`截图完成: ${screenshotPath}`);
    }

    console.log('所有页面截图完成！');
    
  } catch (error) {
    console.error('截图过程中发生错误:', error);
  } finally {
    await browser.close();
  }
}

// 执行截图
takeScreenshots().catch(console.error);