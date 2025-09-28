'use client';

import { Card } from 'antd';
import { TrendingUp } from 'lucide-react';

interface SimpleChartProps {
  symbol?: string;
  height?: number;
}

export function SimpleChart({ symbol = 'BTC/USDT', height = 400 }: SimpleChartProps) {
  return (
    <Card
      title={`${symbol} 价格图表`}
      className="h-full"
      styles={{
        body: { height: height, padding: 0 }
      }}
    >
      <div className="flex items-center justify-center h-full bg-gray-50">
        <div className="text-center">
          <TrendingUp className="w-16 h-16 text-blue-500 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-700 mb-2">图表加载中</h3>
          <p className="text-gray-500">交易图表功能正在开发中</p>
        </div>
      </div>
    </Card>
  );
}