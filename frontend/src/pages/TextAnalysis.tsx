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
  EyeOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  BookOutlined
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

  // 辅助函数
  const getAnalysisTypeName = (type: AnalysisType): string => {
    const typeNames = {
      WORD_FREQUENCY: '词频分析',
      TIMELINE: '时间轴分析',
      GEOGRAPHY: '地理位置分析',
      TEXT_SUMMARY: '文本摘要',
      MULTIDIMENSIONAL: '多维度分析'
    }
    return typeNames[type] || type
  }

  const getStatusText = (status: string): string => {
    const statusTexts = {
      PENDING: '等待中',
      PROCESSING: '处理中',
      COMPLETED: '已完成',
      FAILED: '失败'
    }
    return statusTexts[status] || status
  }

  const getStatusColor = (status: string): string => {
    const statusColors = {
      PENDING: '#faad14',
      PROCESSING: '#1890ff',
      COMPLETED: '#52c41a',
      FAILED: '#ff4d4f'
    }
    return statusColors[status] || '#666'
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
      // 由于后端当前返回的是JSON响应而不是Blob，我们需要先获取分析结果数据
      const analysisResult = await analysisService.getAnalysisById(analysisId)
      
      // 根据格式生成相应的文件内容
      let content: string
      let mimeType: string
      let fileExtension: string

      switch (format) {
        case 'JSON':
          content = JSON.stringify(analysisResult, null, 2)
          mimeType = 'application/json'
          fileExtension = 'json'
          break
        case 'CSV':
          content = convertToCSV(analysisResult)
          mimeType = 'text/csv'
          fileExtension = 'csv'
          break
        case 'EXCEL':
          // 对于Excel格式，我们暂时使用CSV格式
          content = convertToCSV(analysisResult)
          mimeType = 'text/csv'
          fileExtension = 'csv'
          break
        case 'PDF':
          // 对于PDF格式，我们暂时使用JSON格式
          content = JSON.stringify(analysisResult, null, 2)
          mimeType = 'application/json'
          fileExtension = 'json'
          break
        default:
          throw new Error(`不支持的导出格式: ${format}`)
      }

      // 创建Blob对象
      const blob = new Blob([content], { type: mimeType })
      
      // 验证Blob对象是否有效
      if (!(blob instanceof Blob)) {
        throw new Error('创建Blob对象失败')
      }

      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `analysis_${analysisId}.${fileExtension}`
      document.body.appendChild(a)
      a.click()
      
      // 清理资源
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      
      message.success('导出成功')
    } catch (error) {
      console.error('导出失败:', error)
      message.error(`导出失败: ${error instanceof Error ? error.message : '未知错误'}`)
    }
  }

  // 将分析结果转换为CSV格式
  const convertToCSV = (analysisResult: any): string => {
    try {
      const lines: string[] = []
      
      // 添加基本信息
      lines.push('分析结果导出')
      lines.push(`分析ID,${analysisResult.id}`)
      lines.push(`分析类型,${analysisResult.analysisType}`)
      lines.push(`状态,${analysisResult.status}`)
      lines.push(`创建时间,${analysisResult.createdAt}`)
      lines.push(`更新时间,${analysisResult.updatedAt}`)
      lines.push('')

      // 如果有结果数据，尝试解析并添加
      if (analysisResult.resultData) {
        let resultData
        try {
          resultData = typeof analysisResult.resultData === 'string' 
            ? JSON.parse(analysisResult.resultData) 
            : analysisResult.resultData
        } catch {
          resultData = analysisResult.resultData
        }

        // 添加统计信息
        if (resultData.statistics) {
          lines.push('统计信息')
          Object.entries(resultData.statistics).forEach(([key, value]) => {
            lines.push(`${key},${value}`)
          })
          lines.push('')
        }

        // 添加词频数据（如果是词频分析）
        if (resultData.word_categories) {
          lines.push('词频数据')
          lines.push('词语,频次,类别')
          
          Object.entries(resultData.word_categories).forEach(([category, words]: [string, any]) => {
            if (Array.isArray(words)) {
              words.forEach((word: any) => {
                lines.push(`${word.word || word.text || word},${word.count || word.frequency || 1},${category}`)
              })
            }
          })
        }
      }

      return lines.join('\n')
    } catch (error) {
      console.error('CSV转换失败:', error)
      return `分析结果导出失败: ${error instanceof Error ? error.message : '未知错误'}`
    }
  }

  // 文件表格列定义
  const fileColumns: TableColumnsType<FileInfo> = [
    {
      title: '文件名',
      dataIndex: 'originalFileName',
      key: 'originalFileName',
      render: (text: string) => (
        <Space>
          <FileTextOutlined />
          {text}
        </Space>
      )
    },
    {
      title: '文件大小',
      dataIndex: 'formattedSize',
      key: 'formattedSize',
      render: (size: string) => size
    },
    {
      title: '上传时间',
      dataIndex: 'uploadTime',
      key: 'uploadTime',
      render: (date: string) => new Date(date).toLocaleString()
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'PROCESSED' ? 'green' : status === 'PROCESSING' ? 'blue' : status === 'UPLOADED' ? 'orange' : 'red'}>
          {status === 'PROCESSED' ? '已处理' : status === 'PROCESSING' ? '处理中' : status === 'UPLOADED' ? '已上传' : '失败'}
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

  // 渲染分析结果详情
  const renderAnalysisDetail = () => {
    if (!currentAnalysis) {
      return <div>暂无分析结果</div>
    }

    // 显示分析任务基本信息
    const taskInfo = (
      <div style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={8}>
            <Statistic title="分析类型" value={getAnalysisTypeName(currentAnalysis.analysisType)} />
          </Col>
          <Col span={8}>
            <Statistic title="创建时间" value={new Date(currentAnalysis.createdAt).toLocaleString()} />
          </Col>
          <Col span={8}>
            <Statistic 
              title="状态" 
              value={getStatusText(currentAnalysis.status)} 
              valueStyle={{ color: getStatusColor(currentAnalysis.status) }}
            />
          </Col>
        </Row>
        {currentAnalysis.progress !== undefined && (
          <div style={{ marginTop: 16 }}>
            <Progress 
              percent={currentAnalysis.progress} 
              status={currentAnalysis.status === 'FAILED' ? 'exception' : 'active'}
            />
          </div>
        )}
      </div>
    )

    // 如果分析未完成，只显示基本信息
    if (currentAnalysis.status !== 'COMPLETED') {
      return (
        <div>
          {taskInfo}
          <div style={{ textAlign: 'center', color: '#666', marginTop: 32 }}>
            {currentAnalysis.status === 'PENDING' && '分析任务等待中...'}
            {currentAnalysis.status === 'PROCESSING' && '分析任务进行中...'}
            {currentAnalysis.status === 'FAILED' && '分析任务失败，请重试'}
          </div>
        </div>
      )
    }

    // 分析完成，显示详细结果
    const { analysisType, resultData } = currentAnalysis

    return (
      <div>
        {taskInfo}
        <Divider />
        {(() => {
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
        })()}
      </div>
    )
  }

  // 渲染词频分析结果
  const renderWordFrequencyResult = (result: any) => {
    // 解析 JSON 字符串（如果需要）
    let parsedResult = result
    if (typeof result === 'string') {
      try {
        parsedResult = JSON.parse(result)
      } catch (error) {
        console.error('解析词频分析结果失败:', error)
        return <Alert message="数据格式错误" type="error" />
      }
    }

    // 提取统计信息，提供默认值
    const statistics = parsedResult?.statistics || {}
    const totalWords = statistics.total_words || statistics.totalWords || 0
    const uniqueWords = statistics.unique_words || statistics.uniqueWords || 0
    const lexicalDiversity = statistics.lexical_diversity || statistics.lexicalDiversity || 0
    const averageFrequency = statistics.average_frequency || statistics.averageFrequency || 0

    // 处理词频数据
    let wordData: any[] = []
    
    if (parsedResult?.word_list) {
      // 后端格式：word_list 数组
      wordData = parsedResult.word_list.map((item: any, index: number) => ({
        key: index,
        word: item.word || '',
        frequency: item.count || item.frequency || 0,
        category: '词汇',
        weight: (item.frequency || item.count || 0) / Math.max(totalWords, 1),
        rank: index + 1
      }))
    } else if (parsedResult?.word_categories) {
      // 后端实际格式：word_categories 对象
      const categories = parsedResult.word_categories
      let rank = 1
      
      // 高频词
      if (categories.high_frequency) {
        categories.high_frequency.forEach((word: string) => {
          wordData.push({
            key: wordData.length,
            word,
            frequency: 4, // 假设高频词频次为4
            category: '高频词',
            weight: 4 / Math.max(totalWords, 1),
            rank: rank++
          })
        })
      }
      
      // 中频词
      if (categories.medium_frequency) {
        categories.medium_frequency.forEach((word: string) => {
          wordData.push({
            key: wordData.length,
            word,
            frequency: 2, // 假设中频词频次为2
            category: '中频词',
            weight: 2 / Math.max(totalWords, 1),
            rank: rank++
          })
        })
      }
      
      // 低频词（只显示前10个）
      if (categories.low_frequency) {
        categories.low_frequency.slice(0, 10).forEach((word: string) => {
          wordData.push({
            key: wordData.length,
            word,
            frequency: 1, // 假设低频词频次为1
            category: '低频词',
            weight: 1 / Math.max(totalWords, 1),
            rank: rank++
          })
        })
      }
    } else if (parsedResult?.topWords) {
      // 前端期望格式：topWords 数组
      wordData = parsedResult.topWords.map((item: any, index: number) => ({
        key: index,
        word: item.word || '',
        frequency: item.frequency || 0,
        category: item.category || '词汇',
        weight: item.weight || 0,
        rank: item.rank || index + 1
      }))
    }

    return (
      <div>
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Statistic title="总词数" value={totalWords} />
          </Col>
          <Col span={6}>
            <Statistic title="唯一词数" value={uniqueWords} />
          </Col>
          <Col span={6}>
            <Statistic title="词汇多样性" value={lexicalDiversity} precision={3} />
          </Col>
          <Col span={6}>
            <Statistic title="平均频率" value={averageFrequency} precision={3} />
          </Col>
        </Row>
        
        {wordData.length > 0 ? (
          <Table
            dataSource={wordData}
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
        ) : (
          <Alert message="暂无词频数据" type="info" />
        )}
      </div>
    )
  }

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