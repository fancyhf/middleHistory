/**
 * NLP功能测试页面
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-01-15
 */

import React, { useState } from 'react';
import { 
  Card, 
  Input, 
  Button, 
  Space, 
  Typography, 
  Alert, 
  Spin, 
  Tabs, 
  Table, 
  Tag,
  Row,
  Col,
  Statistic,
  Timeline,
  message
} from 'antd';
import { 
  ThunderboltOutlined, 
  FileTextOutlined, 
  BarChartOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;
const { TextArea } = Input;

interface WordFrequencyResult {
  word: string;
  frequency: number;
  percentage: number;
}

interface TimelineEvent {
  time: string;
  event: string;
  confidence: number;
}

interface GeographicLocation {
  location: string;
  frequency: number;
  coordinates?: [number, number];
}

interface AnalysisResult {
  wordFrequency?: WordFrequencyResult[];
  timeline?: TimelineEvent[];
  geographic?: GeographicLocation[];
  summary?: string;
  textStats?: {
    totalWords: number;
    uniqueWords: number;
    sentences: number;
  };
}

const NlpTest: React.FC = () => {
  const [inputText, setInputText] = useState<string>(
    '明朝永乐年间，郑和率领庞大的船队七次下西洋，访问了东南亚、印度洋、阿拉伯海、红海等地区的30多个国家和地区。这些航海活动不仅展示了中国古代的航海技术和造船工艺，也促进了中外文化交流和贸易往来。'
  );
  const [loading, setLoading] = useState<boolean>(false);
  const [results, setResults] = useState<AnalysisResult>({});
  const [activeTab, setActiveTab] = useState<string>('wordFreq');

  const NLP_BASE_URL = 'http://localhost:5001/api';

  const handleAnalysis = async (analysisType: string) => {
    if (!inputText.trim()) {
      message.error('请输入要分析的文本');
      return;
    }

    setLoading(true);
    try {
      let endpoint = '';
      let requestBody: any = { text: inputText };

      switch (analysisType) {
        case 'wordFreq':
          endpoint = '/analyze/word-frequency';
          requestBody = { ...requestBody, max_results: 20, min_length: 2 };
          break;
        case 'timeline':
          endpoint = '/analyze/timeline';
          break;
        case 'geographic':
          endpoint = '/analyze/geographic';
          break;
        case 'summary':
          endpoint = '/analyze/summary';
          requestBody = { ...requestBody, summary_type: 'extractive', max_sentences: 3 };
          break;
        default:
          return;
      }

      const response = await fetch(`${NLP_BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      
      if (data.success) {
        setResults(prev => ({
          ...prev,
          [analysisType]: data.data
        }));
        message.success(`${getAnalysisTypeName(analysisType)}分析完成`);
      } else {
        throw new Error(data.message || '分析失败');
      }
    } catch (error) {
      console.error('分析错误:', error);
      message.error(`分析失败: ${error instanceof Error ? error.message : '未知错误'}`);
    } finally {
      setLoading(false);
    }
  };

  const getAnalysisTypeName = (type: string): string => {
    const names: Record<string, string> = {
      wordFreq: '词频',
      timeline: '时间轴',
      geographic: '地理',
      summary: '摘要'
    };
    return names[type] || type;
  };

  const renderWordFrequency = () => {
    if (!results.wordFrequency) return null;

    const columns = [
      {
        title: '词语',
        dataIndex: 'word',
        key: 'word',
        render: (text: string) => <Tag color="blue">{text}</Tag>
      },
      {
        title: '频次',
        dataIndex: 'frequency',
        key: 'frequency',
        sorter: (a: WordFrequencyResult, b: WordFrequencyResult) => b.frequency - a.frequency
      },
      {
        title: '占比',
        dataIndex: 'percentage',
        key: 'percentage',
        render: (value: number) => `${(value * 100).toFixed(2)}%`
      }
    ];

    return (
      <Card title="词频分析结果" size="small">
        <Table
          dataSource={results.wordFrequency}
          columns={columns}
          rowKey="word"
          size="small"
          pagination={{ pageSize: 10 }}
        />
      </Card>
    );
  };

  const renderTimeline = () => {
    if (!results.timeline) return null;

    const timelineItems = results.timeline.map((event, index) => ({
      children: (
        <div>
          <Text strong>{event.time}</Text>
          <br />
          <Text>{event.event}</Text>
          <br />
          <Text type="secondary">置信度: {(event.confidence * 100).toFixed(1)}%</Text>
        </div>
      ),
      color: event.confidence > 0.8 ? 'green' : event.confidence > 0.5 ? 'orange' : 'red'
    }));

    return (
      <Card title="时间轴分析结果" size="small">
        <Timeline items={timelineItems} />
      </Card>
    );
  };

  const renderGeographic = () => {
    if (!results.geographic) return null;

    const columns = [
      {
        title: '地点',
        dataIndex: 'location',
        key: 'location',
        render: (text: string) => <Tag color="green">{text}</Tag>
      },
      {
        title: '提及次数',
        dataIndex: 'frequency',
        key: 'frequency',
        sorter: (a: GeographicLocation, b: GeographicLocation) => b.frequency - a.frequency
      }
    ];

    return (
      <Card title="地理分析结果" size="small">
        <Table
          dataSource={results.geographic}
          columns={columns}
          rowKey="location"
          size="small"
          pagination={{ pageSize: 10 }}
        />
      </Card>
    );
  };

  const renderSummary = () => {
    if (!results.summary) return null;

    return (
      <Card title="文本摘要结果" size="small">
        <Alert
          message="摘要内容"
          description={results.summary}
          type="info"
          showIcon
        />
      </Card>
    );
  };

  const tabItems = [
    {
      key: 'wordFreq',
      label: (
        <span>
          <BarChartOutlined />
          词频分析
        </span>
      ),
      children: renderWordFrequency()
    },
    {
      key: 'timeline',
      label: (
        <span>
          <ClockCircleOutlined />
          时间轴分析
        </span>
      ),
      children: renderTimeline()
    },
    {
      key: 'geographic',
      label: (
        <span>
          <EnvironmentOutlined />
          地理分析
        </span>
      ),
      children: renderGeographic()
    },
    {
      key: 'summary',
      label: (
        <span>
          <FileTextOutlined />
          文本摘要
        </span>
      ),
      children: renderSummary()
    }
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Title level={2}>
        <ThunderboltOutlined style={{ marginRight: 8 }} />
        NLP功能测试
      </Title>

      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card title="文本输入" size="small">
            <TextArea
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="请输入要分析的文本..."
              rows={6}
              showCount
              maxLength={5000}
            />
            <div style={{ marginTop: 16 }}>
              <Space>
                <Button
                  type="primary"
                  icon={<BarChartOutlined />}
                  loading={loading}
                  onClick={() => handleAnalysis('wordFreq')}
                >
                  词频分析
                </Button>
                <Button
                  icon={<ClockCircleOutlined />}
                  loading={loading}
                  onClick={() => handleAnalysis('timeline')}
                >
                  时间轴分析
                </Button>
                <Button
                  icon={<EnvironmentOutlined />}
                  loading={loading}
                  onClick={() => handleAnalysis('geographic')}
                >
                  地理分析
                </Button>
                <Button
                  icon={<FileTextOutlined />}
                  loading={loading}
                  onClick={() => handleAnalysis('summary')}
                >
                  文本摘要
                </Button>
              </Space>
            </div>
          </Card>
        </Col>

        <Col span={24}>
          <Spin spinning={loading}>
            <Tabs
              activeKey={activeTab}
              onChange={setActiveTab}
              items={tabItems}
            />
          </Spin>
        </Col>
      </Row>
    </div>
  );
};

export default NlpTest;