/**
 * 地理分析页面组件
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

import React, { useState, useEffect } from 'react'
import { Card, Select, Spin, Statistic, Row, Col } from 'antd'
import { GlobalOutlined } from '@ant-design/icons'
import { MapContainer, TileLayer, Marker, Popup, CircleMarker } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const { Option } = Select

// 修复 Leaflet 默认图标问题
delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
})

interface GeographicData {
  id: string
  name: string
  latitude: number
  longitude: number
  count: number
  description: string
}

const GeographicAnalysis: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [mapData, setMapData] = useState<GeographicData[]>([])
  const [selectedRegion, setSelectedRegion] = useState<string>('all')
  const [totalLocations, setTotalLocations] = useState(0)
  const [totalMentions, setTotalMentions] = useState(0)

  useEffect(() => {
    loadGeographicData()
  }, [selectedRegion])

  const loadGeographicData = async () => {
    setLoading(true)
    try {
      // 模拟数据加载
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      const mockData: GeographicData[] = [
        {
          id: '1',
          name: '北京',
          latitude: 39.9042,
          longitude: 116.4074,
          count: 156,
          description: '首都，政治文化中心'
        },
        {
          id: '2',
          name: '上海',
          latitude: 31.2304,
          longitude: 121.4737,
          count: 142,
          description: '经济中心，国际大都市'
        },
        {
          id: '3',
          name: '广州',
          latitude: 23.1291,
          longitude: 113.2644,
          count: 98,
          description: '华南地区中心城市'
        },
        {
          id: '4',
          name: '深圳',
          latitude: 22.5431,
          longitude: 114.0579,
          count: 87,
          description: '改革开放前沿城市'
        },
        {
          id: '5',
          name: '杭州',
          latitude: 30.2741,
          longitude: 120.1551,
          count: 76,
          description: '历史文化名城'
        },
        {
          id: '6',
          name: '南京',
          latitude: 32.0603,
          longitude: 118.7969,
          count: 65,
          description: '六朝古都'
        },
        {
          id: '7',
          name: '西安',
          latitude: 34.3416,
          longitude: 108.9398,
          count: 54,
          description: '十三朝古都'
        },
        {
          id: '8',
          name: '成都',
          latitude: 30.5728,
          longitude: 104.0668,
          count: 43,
          description: '天府之国'
        }
      ]
      
      setMapData(mockData)
      setTotalLocations(mockData.length)
      setTotalMentions(mockData.reduce((sum, item) => sum + item.count, 0))
    } catch (error) {
      console.error('加载地理数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const getMarkerSize = (count: number) => {
    const maxCount = Math.max(...mapData.map(item => item.count))
    const minSize = 10
    const maxSize = 30
    return minSize + (count / maxCount) * (maxSize - minSize)
  }

  const getMarkerColor = (count: number) => {
    const maxCount = Math.max(...mapData.map(item => item.count))
    const ratio = count / maxCount
    if (ratio > 0.8) return '#ff4d4f'
    if (ratio > 0.6) return '#fa8c16'
    if (ratio > 0.4) return '#fadb14'
    if (ratio > 0.2) return '#52c41a'
    return '#1890ff'
  }

  return (
    <div>
      <div className="page-header">
        <h1>地理分析</h1>
        <p className="description">基于地理位置的数据分布分析</p>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="地理位置总数"
              value={totalLocations}
              prefix={<GlobalOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="提及总次数"
              value={totalMentions}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="平均提及次数"
              value={totalLocations > 0 ? Math.round(totalMentions / totalLocations) : 0}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Card 
        title="地理分布图" 
        className="chart-container"
        extra={
          <Select
            value={selectedRegion}
            onChange={setSelectedRegion}
            style={{ width: 120 }}
          >
            <Option value="all">全部地区</Option>
            <Option value="north">华北地区</Option>
            <Option value="south">华南地区</Option>
            <Option value="east">华东地区</Option>
            <Option value="west">西部地区</Option>
          </Select>
        }
      >
        {loading ? (
          <div className="loading-container">
            <Spin size="large" />
          </div>
        ) : (
          <div style={{ height: '600px', width: '100%' }}>
            <MapContainer
              center={[35.0, 110.0]}
              zoom={5}
              style={{ height: '100%', width: '100%' }}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {mapData.map((location) => (
                <CircleMarker
                  key={location.id}
                  center={[location.latitude, location.longitude]}
                  radius={getMarkerSize(location.count)}
                  fillColor={getMarkerColor(location.count)}
                  color="#fff"
                  weight={2}
                  opacity={1}
                  fillOpacity={0.8}
                >
                  <Popup>
                    <div>
                      <h4>{location.name}</h4>
                      <p>提及次数: {location.count}</p>
                      <p>{location.description}</p>
                    </div>
                  </Popup>
                </CircleMarker>
              ))}
            </MapContainer>
          </div>
        )}
      </Card>
    </div>
  )
}

export default GeographicAnalysis