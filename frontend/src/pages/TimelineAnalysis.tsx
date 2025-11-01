/**
 * 时间轴分析页面组件 - 历史事件时间轴展示
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-19T10:30:00Z
 * @description 展示中国历史和世界历史重要事件的时间轴分析
 */

import React, { useState, useEffect } from 'react'
import { Card, Timeline, Select, Space, Tag, Spin, InputNumber } from 'antd'
import { ClockCircleOutlined, HistoryOutlined } from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
const { Option } = Select

interface HistoricalEvent {
  id: string
  year: number
  era: string
  title: string
  description: string
  category: '中国历史' | '世界历史' | '科技发明' | '文化艺术' | '政治军事'
  importance: 'high' | 'medium' | 'low'
  dynasty?: string
  region?: string
}

interface TimeSeriesData {
  period: string
  eventCount: number
  category: string
}

const TimelineAnalysis: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [historicalEvents, setHistoricalEvents] = useState<HistoricalEvent[]>([])
  const [timeSeriesData, setTimeSeriesData] = useState<TimeSeriesData[]>([])
  const [yearRange, setYearRange] = useState<[number, number]>([-2000, 2000])
  const [selectedCategory, setSelectedCategory] = useState<string>('all')

  useEffect(() => {
    loadHistoricalData()
  }, [yearRange, selectedCategory])

  const loadHistoricalData = async () => {
    setLoading(true)
    try {
      // 模拟数据加载
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      const historicalEvents: HistoricalEvent[] = [
        // 中国古代历史
        {
          id: '1',
          year: -221,
          era: '公元前221年',
          title: '秦始皇统一中国',
          description: '秦始皇嬴政统一六国，建立中国历史上第一个统一的封建王朝',
          category: '中国历史',
          importance: 'high',
          dynasty: '秦朝',
          region: '中国'
        },
        {
          id: '2',
          year: -206,
          era: '公元前206年',
          title: '汉朝建立',
          description: '刘邦建立汉朝，开创了中国历史上最重要的朝代之一',
          category: '中国历史',
          importance: 'high',
          dynasty: '汉朝',
          region: '中国'
        },
        {
          id: '3',
          year: 618,
          era: '公元618年',
          title: '唐朝建立',
          description: '李渊建立唐朝，开创了中国历史上的盛世',
          category: '中国历史',
          importance: 'high',
          dynasty: '唐朝',
          region: '中国'
        },
        {
          id: '4',
          year: 960,
          era: '公元960年',
          title: '宋朝建立',
          description: '赵匡胤建立宋朝，中国进入经济文化高度发展时期',
          category: '中国历史',
          importance: 'medium',
          dynasty: '宋朝',
          region: '中国'
        },
        {
          id: '5',
          year: 1368,
          era: '公元1368年',
          title: '明朝建立',
          description: '朱元璋建立明朝，推翻元朝统治',
          category: '中国历史',
          importance: 'high',
          dynasty: '明朝',
          region: '中国'
        },
        {
          id: '6',
          year: 1644,
          era: '公元1644年',
          title: '清朝建立',
          description: '满族建立清朝，成为中国最后一个封建王朝',
          category: '中国历史',
          importance: 'high',
          dynasty: '清朝',
          region: '中国'
        },
        
        // 世界历史重要事件
        {
          id: '7',
          year: -753,
          era: '公元前753年',
          title: '罗马建城',
          description: '传说中罗马城的建立，标志着罗马文明的开始',
          category: '世界历史',
          importance: 'high',
          region: '意大利'
        },
        {
          id: '8',
          year: -776,
          era: '公元前776年',
          title: '第一届奥林匹克运动会',
          description: '古希腊举办第一届奥林匹克运动会',
          category: '文化艺术',
          importance: 'medium',
          region: '希腊'
        },
        {
          id: '9',
          year: 476,
          era: '公元476年',
          title: '西罗马帝国灭亡',
          description: '西罗马帝国灭亡，标志着古代历史的结束',
          category: '世界历史',
          importance: 'high',
          region: '欧洲'
        },
        {
          id: '10',
          year: 1453,
          era: '公元1453年',
          title: '拜占庭帝国灭亡',
          description: '奥斯曼帝国攻陷君士坦丁堡，拜占庭帝国灭亡',
          category: '世界历史',
          importance: 'high',
          region: '土耳其'
        },
        
        // 科技发明
        {
          id: '11',
          year: 105,
          era: '公元105年',
          title: '蔡伦改进造纸术',
          description: '东汉蔡伦改进造纸术，促进了文化传播',
          category: '科技发明',
          importance: 'high',
          dynasty: '汉朝',
          region: '中国'
        },
        {
          id: '12',
          year: 1440,
          era: '公元1440年',
          title: '古腾堡发明印刷术',
          description: '德国古腾堡发明活字印刷术，推动了文艺复兴',
          category: '科技发明',
          importance: 'high',
          region: '德国'
        },
        
        // 近现代重要事件
        {
          id: '13',
          year: 1492,
          era: '公元1492年',
          title: '哥伦布发现美洲',
          description: '哥伦布航海发现美洲大陆，开启了大航海时代',
          category: '世界历史',
          importance: 'high',
          region: '美洲'
        },
        {
          id: '14',
          year: 1789,
          era: '公元1789年',
          title: '法国大革命',
          description: '法国大革命爆发，推翻了封建专制制度',
          category: '政治军事',
          importance: 'high',
          region: '法国'
        },
        {
          id: '15',
          year: 1840,
          era: '公元1840年',
          title: '鸦片战争',
          description: '第一次鸦片战争爆发，中国开始沦为半殖民地半封建社会',
          category: '中国历史',
          importance: 'high',
          dynasty: '清朝',
          region: '中国'
        }
      ]

      const timeSeriesData: TimeSeriesData[] = [
        { period: '古代早期(前3000-前500)', eventCount: 15, category: '政治军事' },
        { period: '古代晚期(前500-500)', eventCount: 25, category: '政治军事' },
        { period: '中世纪(500-1500)', eventCount: 30, category: '政治军事' },
        { period: '近世(1500-1800)', eventCount: 40, category: '政治军事' },
        { period: '近现代(1800-2000)', eventCount: 60, category: '政治军事' },
        { period: '古代早期(前3000-前500)', eventCount: 8, category: '科技发明' },
        { period: '古代晚期(前500-500)', eventCount: 12, category: '科技发明' },
        { period: '中世纪(500-1500)', eventCount: 20, category: '科技发明' },
        { period: '近世(1500-1800)', eventCount: 35, category: '科技发明' },
        { period: '近现代(1800-2000)', eventCount: 80, category: '科技发明' },
        { period: '古代早期(前3000-前500)', eventCount: 10, category: '文化艺术' },
        { period: '古代晚期(前500-500)', eventCount: 18, category: '文化艺术' },
        { period: '中世纪(500-1500)', eventCount: 25, category: '文化艺术' },
        { period: '近世(1500-1800)', eventCount: 45, category: '文化艺术' },
        { period: '近现代(1800-2000)', eventCount: 70, category: '文化艺术' }
      ]
      
      setHistoricalEvents(historicalEvents)
      setTimeSeriesData(timeSeriesData)
    } catch (error) {
      console.error('加载历史数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const getTimeSeriesOption = () => {
    const categories = [...new Set(timeSeriesData.map(item => item.category))]
    const periods = [...new Set(timeSeriesData.map(item => item.period))]
    
    const series = categories.map(category => ({
      name: category,
      type: 'line',
      smooth: true,
      data: periods.map(period => {
        const item = timeSeriesData.find(d => d.period === period && d.category === category)
        return item ? item.eventCount : 0
      })
    }))

    return {
      title: {
        text: '历史事件时间序列分析',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: categories,
        top: 30
      },
      xAxis: {
        type: 'category',
        name: '历史时期',
        data: periods,
        axisLabel: {
          rotate: 45,
          interval: 0
        }
      },
      yAxis: {
        type: 'value',
        name: '事件数量'
      },
      series
    }
  }

  const getImportanceColor = (importance: string) => {
    switch (importance) {
      case 'high': return 'red'
      case 'medium': return 'orange'
      case 'low': return 'green'
      default: return 'blue'
    }
  }

  const getCategoryColor = (category: string) => {
    const colors: { [key: string]: string } = {
      '中国历史': 'purple',
      '世界历史': 'blue',
      '政治军事': 'cyan',
      '科技发明': 'green',
      '文化艺术': 'orange'
    }
    return colors[category] || 'default'
  }

  return (
    <div>
      <div className="page-header">
        <h1>时间轴分析</h1>
        <p className="description">基于时间维度的数据变化趋势分析</p>
      </div>

      <Card 
        title="历史事件筛选" 
        style={{ marginBottom: 24 }}
        extra={<HistoryOutlined />}
      >
        <Space size="large" wrap>
          <div>
            <label style={{ marginRight: 8 }}>年份范围:</label>
            <InputNumber
              value={yearRange[0]}
              onChange={(value) => setYearRange([value || -3000, yearRange[1]])}
              placeholder="起始年份"
              style={{ width: 120, marginRight: 8 }}
            />
            <span style={{ margin: '0 8px' }}>至</span>
            <InputNumber
              value={yearRange[1]}
              onChange={(value) => setYearRange([yearRange[0], value || 2000])}
              placeholder="结束年份"
              style={{ width: 120 }}
            />
          </div>
          <div>
            <label style={{ marginRight: 8 }}>分类筛选:</label>
            <Select
              value={selectedCategory}
              onChange={setSelectedCategory}
              style={{ width: 150 }}
            >
              <Option value="all">全部分类</Option>
              <Option value="中国历史">中国历史</Option>
              <Option value="世界历史">世界历史</Option>
              <Option value="科技发明">科技发明</Option>
              <Option value="文化艺术">文化艺术</Option>
              <Option value="政治军事">政治军事</Option>
            </Select>
          </div>
        </Space>
      </Card>

      <Card title="时间序列图表" style={{ marginBottom: 24 }}>
        {loading ? (
          <div className="loading-container">
            <Spin size="large" />
          </div>
        ) : (
          <ReactECharts 
            option={getTimeSeriesOption()} 
            style={{ height: '400px' }}
          />
        )}
      </Card>

      <Card title="历史事件时间轴" loading={loading}>
        <Timeline mode="left">
          {historicalEvents
            .filter(event => 
              selectedCategory === 'all' || event.category === selectedCategory
            )
            .filter(event => 
              event.year >= yearRange[0] && event.year <= yearRange[1]
            )
            .sort((a, b) => a.year - b.year)
            .map(event => (
              <Timeline.Item
                key={event.id}
                color={getImportanceColor(event.importance)}
                dot={<ClockCircleOutlined />}
              >
                <div>
                  <h4 style={{ margin: 0, color: '#1890ff' }}>{event.title}</h4>
                  <p style={{ margin: '4px 0', color: '#666', fontWeight: 'bold' }}>
                    {event.era} ({event.year > 0 ? `公元${event.year}年` : `公元前${Math.abs(event.year)}年`})
                  </p>
                  <p style={{ margin: '4px 0' }}>{event.description}</p>
                  <Space>
                    <Tag color={getCategoryColor(event.category)}>{event.category}</Tag>
                    <Tag color={getImportanceColor(event.importance)}>
                      {event.importance === 'high' ? '重要' : 
                       event.importance === 'medium' ? '一般' : '普通'}
                    </Tag>
                    {event.dynasty && <Tag color="gold">{event.dynasty}</Tag>}
                    {event.region && <Tag color="cyan">{event.region}</Tag>}
                  </Space>
                </div>
              </Timeline.Item>
            ))}
        </Timeline>
      </Card>
    </div>
  )
}

export default TimelineAnalysis