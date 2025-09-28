'use client';

import { useState } from 'react';
import { Table, Typography, Tag, Space, Button, Card, Row, Col, Progress } from 'antd';
import { TrendingUp, TrendingDown, DollarSign, Bitcoin, Eye } from 'lucide-react';
import { Asset } from '@/types';

const { Text, Title } = Typography;

interface AssetOverviewProps {
  assets: Asset[];
  loading: boolean;
}

export function AssetOverview({ assets, loading }: AssetOverviewProps) {
  const [showHidden, setShowHidden] = useState(false);

  const formatNumber = (num: string, decimals = 8) => {
    const value = parseFloat(num);
    if (isNaN(value)) return '0';
    return value.toFixed(decimals);
  };

  const formatCurrency = (num: string) => {
    const value = parseFloat(num);
    if (isNaN(value)) return '$0.00';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value);
  };

  const formatLargeNumber = (num: string) => {
    const value = parseFloat(num);
    if (isNaN(value)) return '0';

    if (value >= 1e9) {
      return `${(value / 1e9).toFixed(2)}B`;
    } else if (value >= 1e6) {
      return `${(value / 1e6).toFixed(2)}M`;
    } else if (value >= 1e3) {
      return `${(value / 1e3).toFixed(2)}K`;
    }
    return value.toFixed(2);
  };

  const getAssetIcon = (currency: string) => {
    if (!currency) {
      return <div className="h-6 w-6 bg-gray-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
        ?
      </div>;
    }
    switch (currency.toLowerCase()) {
      case 'btc':
        return <Bitcoin className="h-6 w-6 text-orange-500" />;
      case 'eth':
        return <div className="h-6 w-6 bg-purple-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
          Ξ
        </div>;
      case 'usdt':
        return <div className="h-6 w-6 bg-green-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
          ₮
        </div>;
      default:
        return <div className="h-6 w-6 bg-gray-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
          {currency?.charAt(0)?.toUpperCase() || '?'}
        </div>;
    }
  };

  const getAssetColor = (currency: string) => {
    if (!currency) return 'gray';
    switch (currency.toLowerCase()) {
      case 'btc':
        return 'orange';
      case 'eth':
        return 'purple';
      case 'usdt':
        return 'green';
      default:
        return 'blue';
    }
  };

  const calculateTotalValue = () => {
    return assets.reduce((sum, asset) => sum + asset.usdValue, 0);
  };

  const calculateBtcValue = () => {
    return assets.reduce((sum, asset) => sum + asset.btcValue, 0);
  };

  const columns = [
    {
      title: '资产',
      key: 'currency',
      render: (record: Asset) => (
        <div className="flex items-center space-x-3">
          {getAssetIcon(record.symbol)}
          <div>
            <div className="font-medium">{record.symbol}</div>
            <div className="text-xs text-gray-500">{record.name}</div>
          </div>
        </div>
      ),
    },
    {
      title: '可用',
      dataIndex: 'availableBalance',
      key: 'availableBalance',
      render: (availableBalance: number, record: Asset) => (
        <div>
          <div className="font-medium">{formatNumber(availableBalance.toString())}</div>
          <div className="text-xs text-gray-500">
            {formatCurrency(record.usdValue.toString())}
          </div>
        </div>
      ),
    },
    {
      title: '冻结',
      dataIndex: 'frozenBalance',
      key: 'frozenBalance',
      render: (frozenBalance: number) => (
        <Text type="secondary">{formatNumber(frozenBalance.toString())}</Text>
      ),
    },
    {
      title: '总价值',
      dataIndex: 'totalBalance',
      key: 'totalBalance',
      render: (totalBalance: number, record: Asset) => (
        <div>
          <div className="font-medium">{formatNumber(totalBalance.toString())}</div>
          <div className="text-sm text-gray-600">
            {formatCurrency(record.usdValue.toString())}
          </div>
        </div>
      ),
    },
    {
      title: '占比',
      key: 'percentage',
      render: (record: Asset) => {
        const totalValue = calculateTotalValue();
        const percentage = totalValue > 0 ? (record.usdValue / totalValue) * 100 : 0;
        return (
          <div className="flex items-center space-x-2">
            <div className="w-16">
              <Progress
                percent={percentage}
                size="small"
                strokeColor={getAssetColor(record.symbol)}
                showInfo={false}
              />
            </div>
            <Text className="text-sm">{percentage.toFixed(2)}%</Text>
          </div>
        );
      },
    },
    {
      title: '操作',
      key: 'action',
      render: (record: Asset) => (
        <Space size="small">
          <Button type="link" size="small">
            充值
          </Button>
          <Button type="link" size="small">
            提现
          </Button>
          <Button type="link" size="small">
            交易
          </Button>
        </Space>
      ),
    },
  ];

  const totalValue = calculateTotalValue();
  const totalBtcValue = calculateBtcValue();

  return (
    <div className="space-y-6">
      {/* Total Assets Summary */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-6 rounded-lg">
        <div className="flex justify-between items-start mb-4">
          <div>
            <Title level={4} className="text-white mb-2">总资产估值</Title>
            <div className="text-3xl font-bold mb-1">
              {formatCurrency(totalValue.toString())}
            </div>
            <div className="text-blue-100">
              ≈ {formatNumber(totalBtcValue.toString(), 8)} BTC
            </div>
          </div>
          <Button
            type="default"
            icon={<Eye className="h-4 w-4" />}
            onClick={() => setShowHidden(!showHidden)}
          >
            {showHidden ? '隐藏' : '显示'}隐藏资产
          </Button>
        </div>

        <Row gutter={[16, 16]}>
          <Col xs={12} sm={6}>
            <div className="text-center">
              <div className="text-blue-200 text-sm">总资产折合</div>
              <div className="text-lg font-semibold">
                {formatCurrency(totalValue.toString())}
              </div>
            </div>
          </Col>
          <Col xs={12} sm={6}>
            <div className="text-center">
              <div className="text-blue-200 text-sm">24h涨跌</div>
              <div className="text-lg font-semibold text-green-300">+2.45%</div>
            </div>
          </Col>
          <Col xs={12} sm={6}>
            <div className="text-center">
              <div className="text-blue-200 text-sm">资产种类</div>
              <div className="text-lg font-semibold">{assets.length}</div>
            </div>
          </Col>
          <Col xs={12} sm={6}>
            <div className="text-center">
              <div className="text-blue-200 text-sm">今日收益</div>
              <div className="text-lg font-semibold text-green-300">+$123.45</div>
            </div>
          </Col>
        </Row>
      </div>

      {/* Quick Actions */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card className="text-center hover:shadow-lg transition-shadow cursor-pointer">
            <div className="text-green-600 mb-2">
              <TrendingUp className="h-8 w-8 mx-auto" />
            </div>
            <Title level={5} className="mb-1">充值</Title>
            <Text type="secondary">快速充值数字资产</Text>
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card className="text-center hover:shadow-lg transition-shadow cursor-pointer">
            <div className="text-red-600 mb-2">
              <TrendingDown className="h-8 w-8 mx-auto" />
            </div>
            <Title level={5} className="mb-1">提现</Title>
            <Text type="secondary">安全快速提取资产</Text>
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card className="text-center hover:shadow-lg transition-shadow cursor-pointer">
            <div className="text-blue-600 mb-2">
              <DollarSign className="h-8 w-8 mx-auto" />
            </div>
            <Title level={5} className="mb-1">交易</Title>
            <Text type="secondary">开始您的交易之旅</Text>
          </Card>
        </Col>
      </Row>

      {/* Asset Distribution */}
      <Card title="资产分布">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {assets.slice(0, 6).map((asset) => {
            const totalValue = calculateTotalValue();
            const percentage = totalValue > 0 ? (asset.usdValue / totalValue) * 100 : 0;
            return (
              <div key={asset.symbol} className="flex items-center space-x-3">
                {getAssetIcon(asset.symbol)}
                <div className="flex-1">
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-medium">{asset.symbol}</span>
                    <span className="text-sm text-gray-600">{percentage.toFixed(2)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </Card>

      {/* Asset List */}
      <Card title="资产列表">
        <Table
          columns={columns}
          dataSource={assets}
          loading={loading ? true : undefined}
          rowKey="symbol"
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `${range[0]}-${range[1]} 共 ${total} 条`,
          }}
          scroll={{ x: 800 }}
          className="asset-table"
        />
      </Card>
    </div>
  );
}