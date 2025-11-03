/**
 * 测试分析API的脚本
 * @author AI Agent
 * @version 1.0.0
 * @created 2025-11-02
 */

// 测试创建分析任务的API
async function testCreateAnalysis() {
    const url = 'http://localhost:8080/api/analysis/create';
    const requestData = {
        projectId: "1",
        analysisType: "WORD_FREQUENCY",
        description: "词频分析测试任务"
    };

    try {
        console.log('🚀 开始测试创建分析任务API...');
        console.log('请求URL:', url);
        console.log('请求数据:', JSON.stringify(requestData, null, 2));

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData)
        });

        console.log('响应状态:', response.status);
        console.log('响应状态文本:', response.statusText);

        const responseData = await response.json();
        console.log('响应数据:', JSON.stringify(responseData, null, 2));

        if (response.ok) {
            console.log('✅ API调用成功!');
            return responseData;
        } else {
            console.log('❌ API调用失败!');
            return null;
        }
    } catch (error) {
        console.error('❌ 网络错误:', error);
        return null;
    }
}

// 执行测试
testCreateAnalysis();