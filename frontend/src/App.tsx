/**
 * 主应用组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

import React, { useState } from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Layout } from 'antd'
import AppHeader from './components/layout/AppHeader'
import AppSidebar from './components/layout/AppSidebar'
import ServiceStatus from './components/ServiceStatus'
import Dashboard from './pages/Dashboard'
import TextAnalysis from './pages/TextAnalysis'
import NlpTest from './pages/NlpTest'
import WordCloud from './pages/WordCloud'
import GeographicAnalysis from './pages/GeographicAnalysis'
import TimelineAnalysis from './pages/TimelineAnalysis'

import './App.css'

const { Content } = Layout

const App: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false)

  return (
    <Router>
      <Layout className="app-layout">
        <AppHeader className="app-header" />
        <Layout className="app-content-layout">
          <AppSidebar 
            className="app-sider"
            collapsed={collapsed}
            onCollapse={setCollapsed}
          />
          <Layout className={`app-main-content ${collapsed ? 'collapsed' : ''}`}>
            <Content
              style={{
                padding: '24px',
                margin: 0,
                minHeight: 'calc(100vh - 112px)',
                background: '#f0f2f5',
              }}
            >
              <div style={{ 
                background: '#fff', 
                padding: '24px', 
                borderRadius: '8px',
                minHeight: 'calc(100vh - 160px)'
              }}>
                <Routes>
                  <Route path="/" element={<Dashboard />} />
                  <Route path="/text-analysis" element={<TextAnalysis />} />
            <Route path="/nlp-test" element={<NlpTest />} />
            <Route path="/word-cloud" element={<WordCloud />} />
                  <Route path="/geographic" element={<GeographicAnalysis />} />
                  <Route path="/timeline" element={<TimelineAnalysis />} />
      
                </Routes>
              </div>
            </Content>
          </Layout>
        </Layout>
        
        {/* 开发环境服务状态监控 */}
        <ServiceStatus showInDevelopment={true} />
      </Layout>
    </Router>
  )
}

export default App