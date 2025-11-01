/**
 * 应用程序头部组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

import React from 'react'
import { Layout, Typography, Space, Avatar, Dropdown, MenuProps } from 'antd'
import { UserOutlined, SettingOutlined, LogoutOutlined } from '@ant-design/icons'

const { Header } = Layout
const { Title } = Typography

interface AppHeaderProps {
  className?: string
}

const AppHeader: React.FC<AppHeaderProps> = ({ className }) => {
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人资料',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '系统设置',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
    },
  ]

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    switch (key) {
      case 'profile':
        console.log('打开个人资料')
        break
      case 'settings':
        console.log('打开系统设置')
        break
      case 'logout':
        console.log('退出登录')
        break
      default:
        break
    }
  }

  return (
    <Header 
      className={className}
      style={{ 
        background: '#001529', 
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <div className="logo" style={{
          height: '32px',
          width: '32px',
          background: 'rgba(255, 255, 255, 0.3)',
          borderRadius: '6px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginRight: '16px'
        }}>
          📊
        </div>
        <Title level={4} style={{ 
          color: '#fff', 
          margin: 0,
          fontSize: '18px',
          fontWeight: 600
        }}>
          历史数据统计分析工具
        </Title>
      </div>
      
      <Space>
        <Dropdown 
          menu={{ items: userMenuItems, onClick: handleMenuClick }}
          placement="bottomRight"
        >
          <Avatar 
            style={{ 
              backgroundColor: '#1890ff',
              cursor: 'pointer'
            }} 
            icon={<UserOutlined />} 
          />
        </Dropdown>
      </Space>
    </Header>
  )
}

export default AppHeader