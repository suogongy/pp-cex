'use client';

import { useState } from 'react';
import { Table, Typography, Tag, Space, Input, Select, Button, Tooltip } from 'antd';
import { Search, TrendingUp, TrendingDown, Minus } from 'lucide-react';
import { useMarketData } from '@/hooks/useMarketData';
import { useMarketSearch } from '@/hooks/useMarketData';
import { MarketData } from '@/types';

const { Text, Link } = Typography;
const { Option } = Select;

export function MarketOverview() {
  const { marketData, loading } = useMarketData();
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<'symbol' | 'priceChangePercent' | 'volume24h'>('volume24h');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  const { results: searchResults, loading: searchLoading } = useMarketSearch(searchQuery);

  // Filter and sort market data
  const getDisplayData = () => {
    let data = searchQuery ? searchResults : marketData;

    // Sort data
    data = [...data].sort((a, b) => {
      let aValue: number, bValue: number;

      switch (sortBy) {
        case 'symbol':
          aValue = a.symbol.localeCompare(b.symbol);
          bValue = 0;
          break;
        case 'priceChangePercent':
          aValue = parseFloat(a.priceChangePercent);
          bValue = parseFloat(b.priceChangePercent);
          break;
        case 'volume24h':
          aValue = parseFloat(a.volume24h);
          bValue = parseFloat(b.volume24h);
          break;
        default:
          return 0;
      }

      if (sortOrder === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    return data;
  };

  const handleSort = (field: typeof sortBy) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('desc');
    }
  };

  const formatNumber = (num: string, decimals = 8) => {
    const value = parseFloat(num);
    if (isNaN(value)) return '0';
    return value.toFixed(decimals);
  };

  const formatVolume = (volume: string) => {
    const value = parseFloat(volume);
    if (isNaN(value)) return '0';

    if (value >= 1e9) {
      return `$${(value / 1e9).toFixed(2)}B`;
    } else if (value >= 1e6) {
      return `$${(value / 1e6).toFixed(2)}M`;
    } else if (value >= 1e3) {
      return `$${(value / 1e3).toFixed(2)}K`;
    }
    return `$${value.toFixed(2)}`;
  };

  const getPriceChangeColor = (percent: string) => {
    const value = parseFloat(percent);
    if (value > 0) return 'text-green-600';
    if (value < 0) return 'text-red-600';
    return 'text-gray-600';
  };

  const getPriceChangeIcon = (percent: string) => {
    const value = parseFloat(percent);
    if (value > 0) return <TrendingUp className="h-4 w-4 text-green-600" />;
    if (value < 0) return <TrendingDown className="h-4 w-4 text-red-600" />;
    return <Minus className="h-4 w-4 text-gray-600" />;
  };

  const columns = [
    {
      title: '交易对',
      dataIndex: 'symbol',
      key: 'symbol',
      render: (symbol: string, record: MarketData) => (
        <div className="flex items-center space-x-2">
          <Text strong>{symbol}</Text>
          <Text type="secondary" className="text-xs">
            {symbol.replace('USDT', '')}
          </Text>
        </div>
      ),
      sorter: true,
      sortOrder: sortBy === 'symbol' ? sortOrder : null,
      onHeaderCell: () => ({
        onClick: () => handleSort('symbol'),
        style: { cursor: 'pointer' },
      }),
    },
    {
      title: '最新价格',
      dataIndex: 'price',
      key: 'price',
      render: (price: string) => (
        <Text strong>
          ${formatNumber(price, 4)}
        </Text>
      ),
    },
    {
      title: '24h涨跌',
      dataIndex: 'priceChangePercent',
      key: 'priceChangePercent',
      render: (percent: string) => (
        <div className="flex items-center space-x-1">
          {getPriceChangeIcon(percent)}
          <Text className={getPriceChangeColor(percent)}>
            {percent}%
          </Text>
        </div>
      ),
      sorter: true,
      sortOrder: sortBy === 'priceChangePercent' ? sortOrder : null,
      onHeaderCell: () => ({
        onClick: () => handleSort('priceChangePercent'),
        style: { cursor: 'pointer' },
      }),
    },
    {
      title: '24h最高',
      dataIndex: 'high24h',
      key: 'high24h',
      render: (high: string) => (
        <Text type="secondary">
          ${formatNumber(high, 4)}
        </Text>
      ),
    },
    {
      title: '24h最低',
      dataIndex: 'low24h',
      key: 'low24h',
      render: (low: string) => (
        <Text type="secondary">
          ${formatNumber(low, 4)}
        </Text>
      ),
    },
    {
      title: '24h成交量',
      dataIndex: 'volume24h',
      key: 'volume24h',
      render: (volume: string) => (
        <Text>
          {formatVolume(volume)}
        </Text>
      ),
      sorter: true,
      sortOrder: sortBy === 'volume24h' ? sortOrder : null,
      onHeaderCell: () => ({
        onClick: () => handleSort('volume24h'),
        style: { cursor: 'pointer' },
      }),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record: MarketData) => (
        <Space size="small">
          <Button type="link" size="small">
            交易
          </Button>
          <Button type="link" size="small">
            详情
          </Button>
        </Space>
      ),
    },
  ];

  const displayData = getDisplayData();

  return (
    <div className="space-y-4">
      {/* Search and Filter */}
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div className="relative flex-1 max-w-md">
          <Search
            placeholder="搜索交易对..."
            allowClear
            enterButton={<Search className="h-4 w-4" />}
            size="large"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            loading={searchLoading}
          />
        </div>

        <Space wrap>
          <Select
            defaultValue="all"
            style={{ width: 120 }}
            size="large"
            onChange={(value) => {
              // Filter by category
              console.log('Filter by category:', value);
            }}
          >
            <Option value="all">全部</Option>
            <Option value="hot">热门</Option>
            <Option value="new">新币</Option>
            <Option value="defi">DeFi</Option>
          </Select>

          <Button size="large">
            刷新
          </Button>
        </Space>
      </div>

      {/* Market Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-gradient-to-r from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 p-4 rounded-lg">
          <Text type="secondary">24h涨幅</Text>
          <div className="text-2xl font-bold text-green-600">+2.45%</div>
        </div>
        <div className="bg-gradient-to-r from-red-50 to-red-100 dark:from-red-900/20 dark:to-red-800/20 p-4 rounded-lg">
          <Text type="secondary">24h跌幅</Text>
          <div className="text-2xl font-bold text-red-600">-1.23%</div>
        </div>
        <div className="bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 p-4 rounded-lg">
          <Text type="secondary">总市值</Text>
          <div className="text-2xl font-bold text-blue-600">$1.23T</div>
        </div>
        <div className="bg-gradient-to-r from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 p-4 rounded-lg">
          <Text type="secondary">24h成交量</Text>
          <div className="text-2xl font-bold text-purple-600">$45.6B</div>
        </div>
      </div>

      {/* Market Table */}
      <Table
        columns={columns}
        dataSource={displayData}
        loading={loading || searchLoading}
        rowKey="symbol"
        pagination={{
          pageSize: 20,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) => `${range[0]}-${range[1]} 共 ${total} 条`,
        }}
        scroll={{ x: 800 }}
        className="market-table"
        rowClassName={(record) => {
          const percent = parseFloat(record.priceChangePercent);
          return percent > 0 ? 'hover:bg-green-50 dark:hover:bg-green-900/10' :
                 percent < 0 ? 'hover:bg-red-50 dark:hover:bg-red-900/10' : '';
        }}
      />
    </div>
  );
}