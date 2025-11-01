/**
 * 服务状态监控组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */

import React, { useState, useEffect } from 'react'
import { Card, Badge, Button, Space, Typography, Divider, FloatButton } from 'antd'
import { ReloadOutlined, CheckCircleOutlined, CloseCircleOutlined, CloseOutlined, MonitorOutlined } from '@ant-design/icons'
import { healthCheck } from '../services/api'

const { Text, Title } = Typography

interface ServiceStatusProps {
  showInDevelopment?: boolean
}

interface ServiceHealth {
  main: boolean
  nlp: boolean
  lastCheck: Date
}

const ServiceStatus: React.FC<ServiceStatusProps> = ({ 
  showInDevelopment = true 
}) => {
  const [serviceHealth, setServiceHealth] = useState<ServiceHealth>({
    main: false,
    nlp: false,
    lastCheck: new Date()
  })
  const [checking, setChecking] = useState(false)
  const [visible, setVisible] = useState(() => {
    // 从localStorage读取用户的显示偏好，默认显示
    const saved = localStorage.getItem('serviceStatus_visible')
    return saved !== null ? JSON.parse(saved) : true
  })

  // 检查服务状态
  const checkServices = async () => {
    setChecking(true)
    try {
      const health = await healthCheck.checkAllServices()
      setServiceHealth({
        main: health.main,
        nlp: health.nlp,
        lastCheck: new Date()
      })
    } catch (error) {
      console.error('服务状态检查失败:', error)
      setServiceHealth({
        main: false,
        nlp: false,
        lastCheck: new Date()
      })
    } finally {
      setChecking(false)
    }
  }

  // 关闭状态面板
  const handleClose = () => {
    setVisible(false)
    localStorage.setItem('serviceStatus_visible', 'false')
  }

  // 重新显示状态面板
  const handleShow = () => {
    setVisible(true)
    localStorage.setItem('serviceStatus_visible', 'true')
  }

  // 组件挂载时检查服务状态
  useEffect(() => {
    checkServices()
    
    // 定期检查服务状态（每30秒）
    const interval = setInterval(checkServices, 30000)
    
    return () => clearInterval(interval)
  }, [])

  // 开发环境才显示
  if (!showInDevelopment && import.meta.env.VITE_DEV_MODE !== 'true') {
    return null
  }

  const getStatusBadge = (status: boolean) => {
    return status ? (
      <Badge 
        status="success" 
        text={
          <Space>
            <CheckCircleOutlined style={{ color: '#52c41a' }} />
            <Text type="success">正常</Text>
          </Space>
        } 
      />
    ) : (
      <Badge 
        status="error" 
        text={
          <Space>
            <CloseCircleOutlined style={{ color: '#ff4d4f' }} />
            <Text type="danger">离线</Text>
          </Space>
        } 
      />
    )
  }

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('zh-CN', {
      hour12: false,
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }

  return (
    <>
      {/* 主状态面板 */}
      {visible && (
        <Card
          title={
            <Space>
              <Title level={5} style={{ margin: 0 }}>服务状态监控</Title>
              <Text type="secondary">(开发环境)</Text>
            </Space>
          }
          size="small"
          extra={
            <Space>
              <Button
                type="text"
                icon={<ReloadOutlined />}
                loading={checking}
                onClick={checkServices}
                size="small"
                title="刷新状态"
              >
                刷新
              </Button>
              <Button
                type="text"
                icon={<CloseOutlined />}
                onClick={handleClose}
                size="small"
                title="关闭状态面板"
                style={{ color: '#999' }}
              />
            </Space>
          }
          style={{ 
            position: 'fixed', 
            top: 16, 
            right: 16, 
            width: 300,
            zIndex: 1000,
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
          }}
        >
          <Space direction="vertical" style={{ width: '100%' }}>
            {/* 主API服务状态 */}
            <div>
              <Text strong>主API服务</Text>
              <div style={{ marginTop: 4 }}>
                {getStatusBadge(serviceHealth.main)}
              </div>
              <Text type="secondary" style={{ fontSize: '12px' }}>
                {import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}
              </Text>
            </div>

            <Divider style={{ margin: '8px 0' }} />

            {/* NLP服务状态 */}
            <div>
              <Text strong>NLP服务</Text>
              <div style={{ marginTop: 4 }}>
                {getStatusBadge(serviceHealth.nlp)}
              </div>
              <Text type="secondary" style={{ fontSize: '12px' }}>
                {import.meta.env.VITE_NLP_BASE_URL || 'http://localhost:5001/api'}
              </Text>
            </div>

            <Divider style={{ margin: '8px 0' }} />

            {/* 最后检查时间 */}
            <div style={{ textAlign: 'center' }}>
              <Text type="secondary" style={{ fontSize: '12px' }}>
                最后检查: {formatTime(serviceHealth.lastCheck)}
              </Text>
            </div>

            {/* 服务启动提示 */}
            {(!serviceHealth.main || !serviceHealth.nlp) && (
              <div style={{ 
                background: '#fff7e6', 
                border: '1px solid #ffd591',
                borderRadius: '4px',
                padding: '8px',
                marginTop: '8px'
              }}>
                <Text style={{ fontSize: '12px', color: '#d46b08' }}>
                  💡 启动服务提示:
                </Text>
                {!serviceHealth.main && (
                  <div style={{ fontSize: '12px', color: '#d46b08' }}>
                    • 运行 backend/start-backend.bat
                  </div>
                )}
                {!serviceHealth.nlp && (
                  <div style={{ fontSize: '12px', color: '#d46b08' }}>
                    • 运行 nlp-service/start-nlp.bat
                  </div>
                )}
              </div>
            )}
          </Space>
        </Card>
      )}

      {/* 重新显示的悬浮按钮 */}
      {!visible && (
        <FloatButton
          icon={<MonitorOutlined />}
          tooltip="显示服务状态"
          onClick={handleShow}
          style={{
            right: 24,
            bottom: 24,
          }}
        />
      )}
    </>
  )
}

export default ServiceStatus