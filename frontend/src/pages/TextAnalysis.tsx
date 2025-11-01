/**
 * 文本分析页面
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 18:00:00
 */

import React, { useState, useCallback } from 'react'
import {
  Card,
  Upload,
  Button,
  Table,
  Progress,
  Alert,
  Modal,
  Form,
  Select,
  InputNumber,
  Switch,
  Space,
  Tag,
  Statistic,
  Row,
  Col,
  Tabs,
  message,
  Typography,
  Divider,
  Tooltip
} from 'antd'
import {
  UploadOutlined,
  FileTextOutlined,
  BarChartOutlined,
  DeleteOutlined,
  ReloadOutlined,
  DownloadOutlined,
  EyeOutlined
} from '@ant-design/icons'
import fileService, { FileInfo } from '../services/fileService'
import analysisService, { AnalysisResult, AnalysisType, AnalysisParameters } from '../services/analysisService'

const { Option } = Select
const { Text } = Typography

interface AnalysisFormData {
  analysisType: AnalysisType
  parameters: AnalysisParameters
}

const TextAnalysis: React.FC = () => {
  // 状态管理
  const [uploadedFiles, setUploadedFiles] = useState<FileInfo[]>([])
  const [uploading, setUploading] = useState(false)
  const [analysisResults, setAnalysisResults] = useState<AnalysisResult[]>([])
  const [currentAnalysis, setCurrentAnalysis] = useState<AnalysisResult | null>(null)
  const [analyzing, setAnalyzing] = useState(false)
  const [analysisModalVisible, setAnalysisModalVisible] = useState(false)
  const [selectedFiles, setSelectedFiles] = useState<number[]>([])
  const [activeTab, setActiveTab] = useState('upload')

  const [form] = Form.useForm<AnalysisFormData>()

  // 文件上传配置
  const uploadProps: UploadProps = {
    name: 'files',
    multiple: true,
    accept: '.txt,.doc,.docx,.pdf',
    beforeUpload: (file) => {
      const isValidType = ['text/plain', 'application/msword', 
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
        'application/pdf'].includes(file.type)
      
      if (!isValidType) {
        message.error('只支持 TXT、DOC、DOCX、PDF 格式的文件！')
        return false
      }

      const isValidSize = file.size / 1024 / 1024 < 10
      if (!isValidSize) {
        message.error('文件大小不能超过 10MB！')
        return false
      }

      return false // 阻止自动上传，手动处理
    },
    onChange: async (info) => {
      if (info.fileList.length > 0) {
        await handleFileUpload(info.fileList.map(f => f.originFileObj!).filter(Boolean))
      }
    }
  }

  // 处理文件上传
  const handleFileUpload = async (files: File[]) => {
    if (files.length === 0) return

    setUploading(true)
    try {
      const uploadPromises = files.map(file => 
        fileService.uploadFile({
          projectId: 1, // 假设项目ID为1
          file: file
        })
      )
      const results = await Promise.all(uploadPromises)
      
      setUploadedFiles(prev => [...prev, ...results])
      message.success(`成功上传 ${results.length} 个文件`)
    } catch (error) {
      console.error('文件上传失败:', error)
      message.error('文件上传失败，请重试')
    } finally {
      setUploading(false)
    }
  }

  // 开始分析
  const handleStartAnalysis = () => {
    if (selectedFiles.length === 0) {
      message.warning('请先选择要分析的文件')
      return
    }
    setAnalysisModalVisible(true)
  }

  // 提交分析任务
  const handleAnalysisSubmit = async (values: AnalysisFormData) => {
    setAnalyzing(true)
    try {
      const result = await analysisService.createAnalysis({
        projectId: 1, // 假设项目ID为1
        analysisType: values.analysisType,
        fileIds: selectedFiles,
        parameters: values.parameters
      })

      setAnalysisResults(prev => [result, ...prev])
      setCurrentAnalysis(result)
      setAnalysisModalVisible(false)
      setActiveTab('results')
      message.success('分析任务已创建，正在处理中...')

      // 轮询检查分析进度
      pollAnalysisProgress(result.id)
    } catch (error) {
      console.error('创建分析任务失败:', error)
      message.error('创建分析任务失败，请重试')
    } finally {
      setAnalyzing(false)
    }
  }

  // 轮询分析进度
  const pollAnalysisProgress = async (analysisId: number) => {
    const poll = async () => {
      try {
        const progress = await analysisService.getAnalysisProgress(analysisId)
        
        // 更新分析结果状态
        setAnalysisResults(prev => 
          prev.map(result => 
            result.id === analysisId 
              ? { ...result, status: progress.status, progress: progress.progress }
              : result
          )
        )

        if (progress.status === 'COMPLETED' || progress.status === 'FAILED') {
          // 获取完整的分析结果
          const fullResult = await analysisService.getAnalysisById(analysisId)
          setAnalysisResults(prev => 
            prev.map(result => 
              result.id === analysisId ? fullResult : result
            )
          )
          
          if (currentAnalysis?.id === analysisId) {
            setCurrentAnalysis(fullResult)
          }

          if (progress.status === 'COMPLETED') {
            message.success('分析完成！')
          } else {
            message.error('分析失败：' + progress.message)
          }
        } else {
          // 继续轮询
          setTimeout(poll, 2000)
        }
      } catch (error) {
        console.error('获取分析进度失败:', error)
      }
    }

    poll()
  }

  // 删除分析结果
  const handleDeleteAnalysis = async (analysisId: number) => {
    try {
      await analysisService.deleteAnalysis(analysisId)
      setAnalysisResults(prev => prev.filter(result => result.id !== analysisId))
      if (currentAnalysis?.id === analysisId) {
        setCurrentAnalysis(null)
      }
      message.success('分析结果已删除')
    } catch (error) {
      console.error('删除分析结果失败:', error)
      message.error('删除分析结果失败')
    }
  }

  // 重新运行分析
  const handleRerunAnalysis = async (analysisId: number) => {
    try {
      const result = await analysisService.rerunAnalysis(analysisId)
      setAnalysisResults(prev => 
        prev.map(r => r.id === analysisId ? result : r)
      )
      message.success('分析任务已重新启动')
      pollAnalysisProgress(result.id)
    } catch (error) {
      console.error('重新运行分析失败:', error)
      message.error('重新运行分析失败')
    }
  }

  // 导出分析结果
  const handleExportAnalysis = async (analysisId: number, format: 'JSON' | 'CSV' | 'EXCEL' | 'PDF') => {
    try {
      const blob = await analysisService.exportAnalysis(analysisId, format)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `analysis_${analysisId}.${format.toLowerCase()}`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      message.success('导出成功')
    } catch (error) {
      console.error('导出失败:', error)
      message.error('导出失败')
    }
  }

  // 文件表格列定义
  const fileColumns: TableColumnsType<FileInfo> = [
    {
      title: '文件名',
      dataIndex: 'originalName',
      key: 'originalName',
      render: (text: string) => (
        <Space>
          <FileTextOutlined />
          {text}
        </Space>
      )
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      render: (size: number) => fileService.formatFileSize(size)
    },
    {
      title: '上传时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleString()
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'PROCESSED' ? 'green' : status === 'PROCESSING' ? 'blue' : 'red'}>
          {status === 'PROCESSED' ? '已处理' : status === 'PROCESSING' ? '处理中' : '失败'}
        </Tag>
      )
    }
  ]

  // 分析结果表格列定义
  const analysisColumns: TableColumnsType<AnalysisResult> = [
    {
      title: '分析类型',
      dataIndex: 'analysisType',
      key: 'analysisType',
      render: (type: AnalysisType) => (
        <Tag icon={getAnalysisIcon(type)} color={getAnalysisColor(type)}>
          {analysisService.getAnalysisTypeName(type)}
        </Tag>
      )
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string, record: AnalysisResult) => (
        <Space direction="vertical" size="small">
          <Tag color={getStatusColor(status)}>
            {analysisService.getAnalysisStatusName(status as any)}
          </Tag>
          {status === 'RUNNING' && (
            <Progress percent={record.progress} size="small" />
          )}
        </Space>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleString()
    },
    {
      title: '执行时间',
      dataIndex: 'executionTime',
      key: 'executionTime',
      render: (time: number) => time ? `${(time / 1000).toFixed(2)}s` : '-'
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record: AnalysisResult) => (
        <Space>
          <Button
            type="link"
            size="small"
            onClick={() => setCurrentAnalysis(record)}
          >
            查看
          </Button>
          {record.status === 'FAILED' && (
            <Button
              type="link"
              size="small"
              icon={<ReloadOutlined />}
              onClick={() => handleRerunAnalysis(record.id)}
            >
              重试
            </Button>
          )}
          {record.status === 'COMPLETED' && (
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => handleExportAnalysis(record.id, 'JSON')}
            >
              导出
            </Button>
          )}
          <Button
            type="link"
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteAnalysis(record.id)}
          >
            删除
          </Button>
        </Space>
      )
    }
  ]

  // 获取分析类型图标
  const getAnalysisIcon = (type: AnalysisType) => {
    const icons = {
      WORD_FREQUENCY: <BarChartOutlined />,
      TIMELINE: <ClockCircleOutlined />,
      GEOGRAPHY: <EnvironmentOutlined />,
      TEXT_SUMMARY: <BookOutlined />,
      MULTIDIMENSIONAL: <FileTextOutlined />
    }
    return icons[type]
  }

  // 获取分析类型颜色
  const getAnalysisColor = (type: AnalysisType) => {
    const colors = {
      WORD_FREQUENCY: 'blue',
      TIMELINE: 'green',
      GEOGRAPHY: 'orange',
      TEXT_SUMMARY: 'purple',
      MULTIDIMENSIONAL: 'red'
    }
    return colors[type]
  }

  // 获取状态颜色
  const getStatusColor = (status: string) => {
    const colors = {
      PENDING: 'default',
      RUNNING: 'processing',
      COMPLETED: 'success',
      FAILED: 'error',
      CANCELLED: 'warning'
    }
    return colors[status as keyof typeof colors] || 'default'
  }

  // 渲染分析结果详情
  const renderAnalysisDetail = () => {
    if (!currentAnalysis || currentAnalysis.status !== 'COMPLETED') {
      return <div>暂无分析结果</div>
    }

    const { analysisType, resultData } = currentAnalysis

    switch (analysisType) {
      case 'WORD_FREQUENCY':
        return renderWordFrequencyResult(resultData as WordFrequencyResult)
      case 'TIMELINE':
        return renderTimelineResult(resultData as TimelineResult)
      case 'GEOGRAPHY':
        return renderGeographyResult(resultData as GeographyResult)
      case 'TEXT_SUMMARY':
        return renderTextSummaryResult(resultData as TextSummaryResult)
      case 'MULTIDIMENSIONAL':
        return renderMultidimensionalResult(resultData as MultidimensionalResult)
      default:
        return <div>未知的分析类型</div>
    }
  }

  // 渲染词频分析结果
  const renderWordFrequencyResult = (result: WordFrequencyResult) => (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Statistic title="总词数" value={result.statistics.totalWords} />
        </Col>
        <Col span={6}>
          <Statistic title="唯一词数" value={result.statistics.uniqueWords} />
        </Col>
        <Col span={6}>
          <Statistic title="词汇多样性" value={result.statistics.lexicalDiversity} precision={3} />
        </Col>
        <Col span={6}>
          <Statistic title="平均词长" value={result.statistics.averageWordLength} precision={2} />
        </Col>
      </Row>
      
      <Table
        dataSource={result.topWords}
        columns={[
          { title: '词语', dataIndex: 'word', key: 'word' },
          { title: '频次', dataIndex: 'frequency', key: 'frequency' },
          { title: '类别', dataIndex: 'category', key: 'category', render: (cat: string) => <Tag>{cat}</Tag> },
          { title: '权重', dataIndex: 'weight', key: 'weight', render: (w: number) => w.toFixed(3) },
          { title: '排名', dataIndex: 'rank', key: 'rank' }
        ]}
        pagination={{ pageSize: 10 }}
        size="small"
      />
    </div>
  )

  // 渲染时间轴分析结果
  const renderTimelineResult = (result: TimelineResult) => (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Statistic title="事件总数" value={result.statistics.totalEvents} />
        </Col>
        <Col span={8}>
          <Statistic title="时间跨度" value={`${result.statistics.timeSpan.start} - ${result.statistics.timeSpan.end}`} />
        </Col>
        <Col span={8}>
          <Statistic title="平均置信度" value={result.statistics.averageConfidence} precision={3} />
        </Col>
      </Row>
      
      <Table
        dataSource={result.events}
        columns={[
          { title: '事件', dataIndex: 'eventText', key: 'eventText' },
          { title: '时间', dataIndex: 'timeExpression', key: 'timeExpression' },
          { title: '朝代', dataIndex: 'dynasty', key: 'dynasty', render: (d: string) => d ? <Tag>{d}</Tag> : '-' },
          { title: '置信度', dataIndex: 'confidence', key: 'confidence', render: (c: number) => (c * 100).toFixed(1) + '%' },
          { title: '重要性', dataIndex: 'importanceScore', key: 'importanceScore', render: (s: number) => s.toFixed(2) }
        ]}
        pagination={{ pageSize: 10 }}
        size="small"
      />
    </div>
  )

  // 渲染地理分析结果
  const renderGeographyResult = (result: GeographyResult) => (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={8}>
          <Statistic title="地点总数" value={result.statistics.totalLocations} />
        </Col>
        <Col span={8}>
          <Statistic title="平均置信度" value={result.statistics.averageConfidence} precision={3} />
        </Col>
        <Col span={8}>
          <Statistic title="省份分布" value={Object.keys(result.statistics.provinceDistribution).length} />
        </Col>
      </Row>
      
      <Table
        dataSource={result.locations}
        columns={[
          { title: '地点名称', dataIndex: 'originalName', key: 'originalName' },
          { title: '标准名称', dataIndex: 'standardName', key: 'standardName' },
          { title: '类型', dataIndex: 'locationType', key: 'locationType', render: (t: string) => <Tag>{t}</Tag> },
          { title: '省份', dataIndex: 'province', key: 'province' },
          { title: '出现次数', dataIndex: 'occurrenceCount', key: 'occurrenceCount' },
          { title: '置信度', dataIndex: 'confidence', key: 'confidence', render: (c: number) => (c * 100).toFixed(1) + '%' }
        ]}
        pagination={{ pageSize: 10 }}
        size="small"
      />
    </div>
  )

  // 渲染文本摘要结果
  const renderTextSummaryResult = (result: TextSummaryResult) => (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Statistic title="原文长度" value={result.statistics.originalLength} />
        </Col>
        <Col span={6}>
          <Statistic title="摘要长度" value={result.statistics.summaryLength} />
        </Col>
        <Col span={6}>
          <Statistic title="压缩比" value={result.statistics.compressionRatio} precision={2} suffix="%" />
        </Col>
        <Col span={6}>
          <Statistic title="关键词数" value={result.statistics.keywordCount} />
        </Col>
      </Row>
      
      <Card title="摘要内容" style={{ marginBottom: 16 }}>
        <p>{result.summary}</p>
      </Card>
      
      <Card title="关键主题">
        <Table
          dataSource={result.keyTopics}
          columns={[
            { title: '主题', dataIndex: 'topic', key: 'topic' },
            { title: '类别', dataIndex: 'category', key: 'category', render: (c: string) => <Tag>{c}</Tag> },
            { title: '得分', dataIndex: 'score', key: 'score', render: (s: number) => s.toFixed(3) },
            { title: '关键词', dataIndex: 'keywords', key: 'keywords', render: (kws: string[]) => kws.join(', ') }
          ]}
          pagination={false}
          size="small"
        />
      </Card>
    </div>
  )

  // 渲染多维分析结果
  const renderMultidimensionalResult = (result: MultidimensionalResult) => (
    <Tabs 
      defaultActiveKey="overview"
      items={[
        {
          key: 'overview',
          label: '概览',
          children: (
            <Row gutter={16}>
              <Col span={12}>
                <Card title="词频分析" size="small">
                  {result.wordFrequency?.statistics && (
                    <Statistic title="总词数" value={result.wordFrequency.statistics.totalWords} />
                  )}
                </Card>
              </Col>
              <Col span={12}>
                <Card title="时间轴分析" size="small">
                  {result.timeline?.statistics && (
                    <Statistic title="事件数" value={result.timeline.statistics.totalEvents} />
                  )}
                </Card>
              </Col>
            </Row>
          )
        },
        {
          key: 'correlations',
          label: '关联分析',
          children: (
            <Card title="洞察发现">
              {result.insights.map((insight, index) => (
                <Alert key={index} message={insight} type="info" style={{ marginBottom: 8 }} />
              ))}
            </Card>
          )
        }
      ]}
    />
  )

  return (
    <div style={{ padding: '24px' }}>
      <Card title="文本分析" extra={
        <Button 
          type="primary" 
          icon={<BarChartOutlined />}
          onClick={handleStartAnalysis}
          disabled={selectedFiles.length === 0}
        >
          开始分析
        </Button>
      }>
        <Tabs 
          activeKey={activeTab} 
          onChange={setActiveTab}
          items={[
            {
              key: 'upload',
              label: '文件上传',
              children: (
                <Card>
                  <Upload.Dragger {...uploadProps} style={{ marginBottom: 16 }}>
                    <p className="ant-upload-drag-icon">
                      <UploadOutlined />
                    </p>
                    <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
                    <p className="ant-upload-hint">
                      支持 TXT、DOC、DOCX、PDF 格式，单个文件不超过 10MB
                    </p>
                  </Upload.Dragger>

                  {uploadedFiles.length > 0 && (
                    <Table
                      dataSource={uploadedFiles}
                      columns={fileColumns}
                      rowKey="id"
                      loading={uploading}
                      rowSelection={{
                        selectedRowKeys: selectedFiles,
                        onChange: (keys) => setSelectedFiles(keys as number[])
                      }}
                      pagination={{ pageSize: 5 }}
                    />
                  )}
                </Card>
              )
            },
            {
              key: 'results',
              label: '分析结果',
              children: (
                <Row gutter={16}>
                  <Col span={12}>
                    <Card title="分析任务列表">
                      <Table
                        dataSource={analysisResults}
                        columns={analysisColumns}
                        rowKey="id"
                        pagination={{ pageSize: 5 }}
                        size="small"
                      />
                    </Card>
                  </Col>
                  <Col span={12}>
                    <Card title="分析详情">
                      {renderAnalysisDetail()}
                    </Card>
                  </Col>
                </Row>
              )
            }
          ]}
        />
      </Card>

      {/* 分析配置模态框 */}
      <Modal
        title="配置分析参数"
        open={analysisModalVisible}
        onCancel={() => setAnalysisModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleAnalysisSubmit}
          initialValues={{
            analysisType: 'WORD_FREQUENCY',
            parameters: analysisService.getDefaultParameters('WORD_FREQUENCY')
          }}
        >
          <Form.Item
            name="analysisType"
            label="分析类型"
            rules={[{ required: true, message: '请选择分析类型' }]}
          >
            <Select
              onChange={(type: AnalysisType) => {
                form.setFieldsValue({
                  parameters: analysisService.getDefaultParameters(type)
                })
              }}
            >
              <Option value="WORD_FREQUENCY">词频分析</Option>
              <Option value="TIMELINE">时间轴分析</Option>
              <Option value="GEOGRAPHY">地理分析</Option>
              <Option value="TEXT_SUMMARY">文本摘要</Option>
              <Option value="MULTIDIMENSIONAL">多维分析</Option>
            </Select>
          </Form.Item>

          <Form.Item shouldUpdate={(prev, curr) => prev.analysisType !== curr.analysisType}>
            {({ getFieldValue }) => {
              const analysisType = getFieldValue('analysisType') as AnalysisType
              return renderParameterForm(analysisType)
            }}
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={analyzing}>
                开始分析
              </Button>
              <Button onClick={() => setAnalysisModalVisible(false)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )

  // 渲染参数配置表单
  function renderParameterForm(analysisType: AnalysisType) {
    switch (analysisType) {
      case 'WORD_FREQUENCY':
        return (
          <>
            <Form.Item name={['parameters', 'wordFrequency', 'maxWords']} label="最大词数">
              <InputNumber min={10} max={200} />
            </Form.Item>
            <Form.Item name={['parameters', 'wordFrequency', 'minFrequency']} label="最小频次">
              <InputNumber min={1} max={10} />
            </Form.Item>
            <Form.Item name={['parameters', 'wordFrequency', 'filterStopwords']} label="过滤停用词" valuePropName="checked">
              <Switch />
            </Form.Item>
          </>
        )
      case 'TIMELINE':
        return (
          <>
            <Form.Item name={['parameters', 'timeline', 'enableDynastyMapping']} label="启用朝代映射" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item name={['parameters', 'timeline', 'minConfidence']} label="最小置信度">
              <InputNumber min={0} max={1} step={0.1} />
            </Form.Item>
            <Form.Item name={['parameters', 'timeline', 'includeRelativeTime']} label="包含相对时间" valuePropName="checked">
              <Switch />
            </Form.Item>
          </>
        )
      case 'GEOGRAPHY':
        return (
          <>
            <Form.Item name={['parameters', 'geography', 'enableCoordinates']} label="启用坐标" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item name={['parameters', 'geography', 'minConfidence']} label="最小置信度">
              <InputNumber min={0} max={1} step={0.1} />
            </Form.Item>
            <Form.Item name={['parameters', 'geography', 'includeHistoricalNames']} label="包含历史地名" valuePropName="checked">
              <Switch />
            </Form.Item>
          </>
        )
      case 'TEXT_SUMMARY':
        return (
          <>
            <Form.Item name={['parameters', 'textSummary', 'maxSentences']} label="最大句数">
              <InputNumber min={3} max={20} />
            </Form.Item>
            <Form.Item name={['parameters', 'textSummary', 'maxLength']} label="最大长度">
              <InputNumber min={100} max={2000} />
            </Form.Item>
            <Form.Item name={['parameters', 'textSummary', 'summaryType']} label="摘要类型">
              <Select>
                <Option value="extractive">抽取式</Option>
                <Option value="abstractive">生成式</Option>
              </Select>
            </Form.Item>
          </>
        )
      case 'MULTIDIMENSIONAL':
        return (
          <>
            <Form.Item name={['parameters', 'multidimensional', 'includeAllTypes']} label="包含所有类型" valuePropName="checked">
              <Switch />
            </Form.Item>
            <Form.Item name={['parameters', 'multidimensional', 'enableCorrelations']} label="启用关联分析" valuePropName="checked">
              <Switch />
            </Form.Item>
          </>
        )
      default:
        return null
    }
  }
}

export default TextAnalysis