/**
 * 词云分析页面组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

import React, { useState, useEffect } from 'react'
import { Card, Button, Slider, Select, Space, Spin } from 'antd'
import { CloudOutlined, DownloadOutlined, ReloadOutlined } from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'

const { Option } = Select

interface WordCloudData {
  name: string
  value: number
}

const WordCloud: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [wordCloudData, setWordCloudData] = useState<WordCloudData[]>([])
  const [maxWords, setMaxWords] = useState(100)
  const [colorScheme, setColorScheme] = useState('default')

  useEffect(() => {
    loadWordCloudData()
  }, [])

  const loadWordCloudData = async () => {
    setLoading(true)
    try {
      // 模拟数据加载
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      const mockData: WordCloudData[] = [
        { name: '历史', value: 156 },
        { name: '数据', value: 142 },
        { name: '分析', value: 128 },
        { name: '统计', value: 98 },
        { name: '研究', value: 87 },
        { name: '方法', value: 76 },
        { name: '结果', value: 65 },
        { name: '文献', value: 54 },
        { name: '理论', value: 43 },
        { name: '模型', value: 32 },
        { name: '算法', value: 28 },
        { name: '技术', value: 25 },
        { name: '系统', value: 22 },
        { name: '应用', value: 19 },
        { name: '发展', value: 16 },
        { name: '创新', value: 14 },
        { name: '优化', value: 12 },
        { name: '实验', value: 10 },
        { name: '评估', value: 8 },
        { name: '改进', value: 6 },
      ]
      
      setWordCloudData(mockData)
    } catch (error) {
      console.error('加载词云数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const getWordCloudOption = () => {
    const colors = {
      default: ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc'],
      blue: ['#1890ff', '#40a9ff', '#69c0ff', '#91d5ff', '#bae7ff'],
      green: ['#52c41a', '#73d13d', '#95de64', '#b7eb8f', '#d9f7be'],
      purple: ['#722ed1', '#9254de', '#b37feb', '#d3adf7', '#efdbff']
    }

    return {
      tooltip: {
        show: true,
        formatter: (params: any) => {
          return `${params.name}: ${params.value}`
        }
      },
      series: [{
        type: 'wordCloud',
        gridSize: 2,
        sizeRange: [12, 60],
        rotationRange: [-90, 90],
        shape: 'pentagon',
        width: '100%',
        height: '100%',
        drawOutOfBound: false,
        textStyle: {
          fontFamily: 'sans-serif',
          fontWeight: 'bold',
          color: function () {
            const colorArray = colors[colorScheme as keyof typeof colors] || colors.default
            return colorArray[Math.round(Math.random() * (colorArray.length - 1))]
          }
        },
        emphasis: {
          textStyle: {
            shadowBlur: 10,
            shadowColor: '#333'
          }
        },
        data: wordCloudData.slice(0, maxWords)
      }]
    }
  }

  const handleRefresh = () => {
    loadWordCloudData()
  }

  const handleDownload = () => {
    // 这里应该实现下载功能
    console.log('下载词云图片')
  }

  return (
    <div>
      <div className="page-header">
        <h1>词云分析</h1>
        <p className="description">以词云形式展示文本中的高频词汇</p>
      </div>

      <Card 
        title="词云配置" 
        style={{ marginBottom: 24 }}
        extra={
          <Space>
            <Button 
              icon={<ReloadOutlined />} 
              onClick={handleRefresh}
              loading={loading}
            >
              刷新
            </Button>
            <Button 
              type="primary" 
              icon={<DownloadOutlined />}
              onClick={handleDownload}
            >
              下载
            </Button>
          </Space>
        }
      >
        <Space size="large" wrap>
          <div>
            <label style={{ marginRight: 8 }}>显示词汇数量:</label>
            <Slider
              min={20}
              max={200}
              value={maxWords}
              onChange={setMaxWords}
              style={{ width: 200 }}
              tooltip={{ formatter: (value) => `${value} 个词` }}
            />
          </div>
          <div>
            <label style={{ marginRight: 8 }}>配色方案:</label>
            <Select
              value={colorScheme}
              onChange={setColorScheme}
              style={{ width: 120 }}
            >
              <Option value="default">默认</Option>
              <Option value="blue">蓝色系</Option>
              <Option value="green">绿色系</Option>
              <Option value="purple">紫色系</Option>
            </Select>
          </div>
        </Space>
      </Card>

      <Card title="词云图" className="chart-container">
        {loading ? (
          <div className="loading-container">
            <Spin size="large" />
          </div>
        ) : wordCloudData.length > 0 ? (
          <ReactECharts 
            option={getWordCloudOption()} 
            style={{ height: '600px' }}
          />
        ) : (
          <div style={{ 
            textAlign: 'center', 
            padding: '100px 24px',
            color: '#8c8c8c'
          }}>
            <CloudOutlined style={{ fontSize: '48px', marginBottom: '16px' }} />
            <p>暂无词云数据</p>
          </div>
        )}
      </Card>
    </div>
  )
}

export default WordCloud