'use client';

import { useState, useEffect } from 'react';
import { Table, Typography, Tag, Space, Avatar, Tooltip } from 'antd';
import { Clock, ArrowUp, ArrowDown, Minus } from 'lucide-react';

const { Text } = Typography;

interface TradeRecord {
  id: string;
  symbol: string;
  price: string;
  amount: string;
  total: string;
  time: string;
  type: 'buy' | 'sell';
}

// Mock trade data - in real implementation, this would come from WebSocket
const mockTrades: TradeRecord[] = [
  {
    id: '1',
    symbol: 'BTCUSDT',
    price: '43500.00',
    amount: '0.125',
    total: '5437.50',
    time: new Date(Date.now() - 1000).toISOString(),
    type: 'buy',
  },
  {
    id: '2',
    symbol: 'BTCUSDT',
    price: '43500.50',
    amount: '0.089',
    total: '3871.54',
    time: new Date(Date.now() - 2000).toISOString(),
    type: 'sell',
  },
  {
    id: '3',
    symbol: 'BTCUSDT',
    price: '43499.75',
    amount: '0.234',
    total: '10178.92',
    time: new Date(Date.now() - 3000).toISOString(),
    type: 'buy',
  },
  {
    id: '4',
    symbol: 'BTCUSDT',
    price: '43498.25',
    amount: '0.156',
    total: '6785.73',
    time: new Date(Date.now() - 4000).toISOString(),
    type: 'sell',
  },
  {
    id: '5',
    symbol: 'BTCUSDT',
    price: '43501.00',
    amount: '0.067',
    total: '2914.57',
    time: new Date(Date.now() - 5000).toISOString(),
    type: 'buy',
  },
  {
    id: '6',
    symbol: 'BTCUSDT',
    price: '43500.25',
    amount: '0.123',
    total: '5350.53',
    time: new Date(Date.now() - 6000).toISOString(),
    type: 'sell',
  },
  {
    id: '7',
    symbol: 'BTCUSDT',
    price: '43499.50',
    amount: '0.089',
    total: '3871.46',
    time: new Date(Date.now() - 7000).toISOString(),
    type: 'buy',
  },
  {
    id: '8',
    symbol: 'BTCUSDT',
    price: '43500.75',
    amount: '0.145',
    total: '6307.61',
    time: new Date(Date.now() - 8000).toISOString(),
    type: 'sell',
  },
];

export function RecentTrades() {
  const [trades, setTrades] = useState<TradeRecord[]>(mockTrades);
  const [lastPrice, setLastPrice] = useState<string>(mockTrades[0]?.price || '0');

  // Simulate real-time trade updates
  useEffect(() => {
    const interval = setInterval(() => {
      // Generate random trade
      const newTrade: TradeRecord = {
        id: Date.now().toString(),
        symbol: 'BTCUSDT',
        price: (parseFloat(lastPrice) + (Math.random() - 0.5) * 10).toFixed(2),
        amount: (Math.random() * 0.5).toFixed(6),
        total: '0',
        time: new Date().toISOString(),
        type: Math.random() > 0.5 ? 'buy' : 'sell',
      };

      newTrade.total = (parseFloat(newTrade.price) * parseFloat(newTrade.amount)).toFixed(2);

      setTrades(prev => [newTrade, ...prev].slice(0, 20));
      setLastPrice(newTrade.price);
    }, 3000);

    return () => clearInterval(interval);
  }, [lastPrice]);

  const formatTime = (timeString: string) => {
    const date = new Date(timeString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    if (diff < 60000) {
      return `${Math.floor(diff / 1000)}秒前`;
    } else if (diff < 3600000) {
      return `${Math.floor(diff / 60000)}分钟前`;
    } else {
      return date.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    }
  };

  const formatNumber = (num: string, decimals: number) => {
    const value = parseFloat(num);
    if (isNaN(value)) return '0';
    return value.toFixed(decimals);
  };

  const getPriceColor = (currentPrice: string, previousPrice?: string) => {
    if (!previousPrice) return 'text-gray-600';
    const current = parseFloat(currentPrice);
    const previous = parseFloat(previousPrice);
    if (current > previous) return 'text-green-600';
    if (current < previous) return 'text-red-600';
    return 'text-gray-600';
  };

  const getPriceIcon = (type: 'buy' | 'sell') => {
    if (type === 'buy') {
      return <ArrowUp className="h-3 w-3 text-green-600" />;
    }
    return <ArrowDown className="h-3 w-3 text-red-600" />;
  };

  const columns = [
    {
      title: '时间',
      dataIndex: 'time',
      key: 'time',
      width: 80,
      render: (time: string) => (
        <Tooltip title={new Date(time).toLocaleString('zh-CN')}>
          <div className="flex items-center space-x-1">
            <Clock className="h-3 w-3 text-gray-400" />
            <Text className="text-xs text-gray-500">{formatTime(time)}</Text>
          </div>
        </Tooltip>
      ),
    },
    {
      title: '方向',
      dataIndex: 'type',
      key: 'type',
      width: 60,
      render: (type: 'buy' | 'sell') => (
        <div className="flex items-center space-x-1">
          {getPriceIcon(type)}
          <Text className={type === 'buy' ? 'text-green-600' : 'text-red-600'}>
            {type === 'buy' ? '买' : '卖'}
          </Text>
        </div>
      ),
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      render: (price: string, record: TradeRecord, index: number) => {
        const previousTrade = trades[index + 1];
        return (
          <Text className={getPriceColor(price, previousTrade?.price)}>
            {formatNumber(price, 2)}
          </Text>
        );
      },
      sorter: (a: TradeRecord, b: TradeRecord) =>
        parseFloat(a.price) - parseFloat(b.price),
      defaultSortOrder: 'desc' as const,
    },
    {
      title: '数量',
      dataIndex: 'amount',
      key: 'amount',
      width: 100,
      render: (amount: string) => (
        <Text>{formatNumber(amount, 6)}</Text>
      ),
      align: 'right' as const,
    },
    {
      title: '总额',
      dataIndex: 'total',
      key: 'total',
      width: 120,
      render: (total: string) => (
        <Text>{formatNumber(total, 2)}</Text>
      ),
      align: 'right' as const,
    },
  ];

  return (
    <div className="space-y-4">
      {/* Trade Statistics */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-gray-50 dark:bg-gray-800 p-3 rounded-lg">
          <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">最新价格</div>
          <div className="text-lg font-semibold text-green-600">
            ${formatNumber(lastPrice, 2)}
          </div>
        </div>
        <div className="bg-gray-50 dark:bg-gray-800 p-3 rounded-lg">
          <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">24h最高</div>
          <div className="text-lg font-semibold">$44,250.00</div>
        </div>
        <div className="bg-gray-50 dark:bg-gray-800 p-3 rounded-lg">
          <div className="text-sm text-gray-600 dark:text-gray-400 mb-1">24h最低</div>
          <div className="text-lg font-semibold">$42,150.00</div>
        </div>
      </div>

      {/* Recent Trades Table */}
      <div className="bg-white dark:bg-gray-800 rounded-lg overflow-hidden">
        <div className="p-4 border-b border-gray-200 dark:border-gray-700">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold">最近交易</h3>
            <Space>
              <Tag color="blue">BTC/USDT</Tag>
              <Tag color="orange">实时更新</Tag>
            </Space>
          </div>
        </div>

        <div className="overflow-x-auto">
          <Table
            columns={columns}
            dataSource={trades}
            rowKey="id"
            pagination={false}
            scroll={{ y: 400 }}
            size="small"
            className="trades-table"
            rowClassName={(record, index) => {
              const previousTrade = trades[index + 1];
              if (!previousTrade) return '';
              const currentPrice = parseFloat(record.price);
              const previousPrice = parseFloat(previousTrade.price);

              if (currentPrice > previousPrice) {
                return 'hover:bg-green-50 dark:hover:bg-green-900/10';
              } else if (currentPrice < previousPrice) {
                return 'hover:bg-red-50 dark:hover:bg-red-900/10';
              }
              return '';
            }}
            showHeader={false}
            components={{
              body: {
                cell: ({ children, ...props }) => (
                  <td {...props} className="py-2 px-3 text-sm">
                    {children}
                  </td>
                ),
              },
            }}
          />
        </div>
      </div>

      {/* Trade Legend */}
      <div className="flex items-center justify-center space-x-6 text-sm text-gray-600 dark:text-gray-400">
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 bg-green-500 rounded-full"></div>
          <span>买入</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 bg-red-500 rounded-full"></div>
          <span>卖出</span>
        </div>
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 bg-gray-500 rounded-full"></div>
          <span>价格变化</span>
        </div>
      </div>
    </div>
  );
}