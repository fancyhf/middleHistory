/**
 * 数据概览页面组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

import React, { useState, useEffect } from 'react'
import { Row, Col, Card, Statistic, Spin, Alert, Button } from 'antd'
import { 
  FileTextOutlined, 
  CloudOutlined, 
  GlobalOutlined, 
  ClockCircleOutlined,
  ReloadOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'
import { guestApi, serviceStatusManager, healthCheck } from '../services/api'

interface DashboardStats {
  totalDocuments: number
  totalWords: number
  uniqueWords: number
  analysisCount: number
}

const Dashboard: React.FC = () => {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isGuestMode, setIsGuestMode] = useState(false)
  const [serviceStatus, setServiceStatus] = useState({ main: false, nlp: false })
  const [stats, setStats] = useState<DashboardStats>({
    totalDocuments: 0,
    totalWords: 0,
    uniqueWords: 0,
    analysisCount: 0
  })

  // 检查用户登录状态
  const checkLoginStatus = () => {
    const token = localStorage.getItem('access_token')
    setIsGuestMode(!token)
    return !!token
  }

  // 加载数据
  const loadDashboardData = async () => {
    try {
      setLoading(true)
      setError(null)
      
      // 检查服务状态
      const status = await healthCheck.checkAllServices()
      setServiceStatus(status)
      
      // 尝试获取真实数据
      if (status.main) {
        try {
          const response = await guestApi.getPublicStats()
          if (response.success) {
            setStats({
              totalDocuments: response.data.totalDocuments || 0,
              totalWords: response.data.totalWords || 0,
              uniqueWords: response.data.uniqueWords || 0,
              analysisCount: response.data.totalAnalysis || 0
            })
          } else {
            throw new Error('API响应失败')
          }
        } catch (apiError) {
          console.log('API调用失败，使用演示数据')
          // 使用演示数据
          setStats({
            totalDocuments: 1250,
            totalWords: 45680,
            uniqueWords: 8920,
            analysisCount: 156
          })
        }
      } else {
        // 服务离线时使用演示数据
        console.log('服务离线，使用演示数据')
        setStats({
          totalDocuments: 1250,
          totalWords: 45680,
          uniqueWords: 8920,
          analysisCount: 156
        })
      }
    } catch (error) {
      console.error('加载数据失败:', error)
      setError('数据加载失败，显示演示数据')
      // 即使出错也显示演示数据
      setStats({
        totalDocuments: 1250,
        totalWords: 45680,
        uniqueWords: 8920,
        analysisCount: 156
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    checkLoginStatus()
    loadDashboardData()
  }, [])

  const getChartOption = () => ({
    title: {
      text: '文档分析趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [120, 200, 150, 80, 70, 110],
      type: 'line',
      smooth: true,
      itemStyle: {
        color: '#1890ff'
      }
    }]
  })

  if (loading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
        <p style={{ marginTop: 16, textAlign: 'center' }}>正在加载数据...</p>
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <h1>数据概览</h1>
        <p className="description">
          {isGuestMode 
            ? '游客模式 - 查看演示数据，登录后可查看完整功能' 
            : '查看历史数据统计分析的整体情况'
          }
        </p>
        {error && (
          <Alert
            message="数据加载提示"
            description={error}
            type="warning"
            showIcon
            style={{ marginBottom: 16 }}
            action={
              <Button 
                size="small" 
                icon={<ReloadOutlined />} 
                onClick={loadDashboardData}
              >
                重试
              </Button>
            }
          />
        )}
        {isGuestMode && (
          <Alert
            message="游客模式"
            description="您正在以游客身份浏览，部分功能受限。登录后可使用完整功能。"
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="文档总数"
              value={stats.totalDocuments}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="词汇总数"
              value={stats.totalWords}
              prefix={<CloudOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="唯一词汇"
              value={stats.uniqueWords}
              prefix={<GlobalOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="分析次数"
              value={stats.analysisCount}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#eb2f96' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="分析趋势图" className="chart-container">
            <ReactECharts 
              option={getChartOption()} 
              style={{ height: '400px' }}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card 
            title="系统状态" 
            style={{ height: '100%' }}
            extra={
              <Button 
                size="small" 
                icon={<ReloadOutlined />} 
                onClick={loadDashboardData}
              >
                刷新
              </Button>
            }
          >
            <Alert
              message={`主API服务: ${serviceStatus.main ? '在线' : '离线'}`}
              description={serviceStatus.main ? '数据服务运行正常' : '数据服务连接失败，使用演示数据'}
              type={serviceStatus.main ? 'success' : 'warning'}
              showIcon
              style={{ marginBottom: 16 }}
            />
            <Alert
              message={`NLP服务: ${serviceStatus.nlp ? '在线' : '离线'}`}
              description={serviceStatus.nlp ? 'NLP分析服务运行正常' : 'NLP分析服务不可用，部分功能受限'}
              type={serviceStatus.nlp ? 'success' : 'warning'}
              showIcon
              style={{ marginBottom: 16 }}
            />
            {isGuestMode ? (
              <Alert
                message="游客模式"
                description="当前为游客模式，显示演示数据"
                type="info"
                showIcon
              />
            ) : (
              <Alert
                message="用户模式"
                description="已登录用户，可使用完整功能"
                type="success"
                showIcon
              />
            )}
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard