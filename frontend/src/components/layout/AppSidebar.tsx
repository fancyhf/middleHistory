/**
 * 应用程序侧边栏组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

import React from 'react'
import { Layout, Menu, MenuProps } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  DashboardOutlined,
  FileTextOutlined,
  CloudOutlined,
  EnvironmentOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
  SettingOutlined,
  UserOutlined,
  ThunderboltOutlined,
  GlobalOutlined,
} from '@ant-design/icons'

const { Sider } = Layout

type MenuItem = Required<MenuProps>['items'][number]

interface AppSidebarProps {
  className?: string
  collapsed?: boolean
  onCollapse?: (collapsed: boolean) => void
}

const AppSidebar: React.FC<AppSidebarProps> = ({ 
  className, 
  collapsed = false, 
  onCollapse 
}) => {
  const navigate = useNavigate()
  const location = useLocation()

  const menuItems: MenuItem[] = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: '数据概览',
    },
    {
          key: '/text-analysis',
          icon: <FileTextOutlined />,
          label: '文本分析',
        },
        {
          key: '/nlp-test',
          icon: <ThunderboltOutlined />,
          label: 'NLP功能测试',
        },

    {
      key: '/word-cloud',
      icon: <CloudOutlined />,
      label: '词云分析',
    },
    {
      key: '/geographic',
      icon: <GlobalOutlined />,
      label: '地理分析',
    },
    {
      key: '/timeline',
      icon: <ClockCircleOutlined />,
      label: '时间轴分析',
    },
  ]

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key)
  }

  return (
    <Sider 
      className={className}
      collapsible 
      collapsed={collapsed} 
      onCollapse={onCollapse}
      width={200}
      collapsedWidth={80}
      style={{
        background: '#fff',
        boxShadow: '2px 0 8px rgba(0, 0, 0, 0.15)',
      }}
    >
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        items={menuItems}
        onClick={handleMenuClick}
        style={{
          height: '100%',
          borderRight: 'none',
          paddingTop: '16px',
        }}
      />
    </Sider>
  )
}

export default AppSidebar