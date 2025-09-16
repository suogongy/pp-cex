'use client';

import { useState } from 'react';
import { Card, Form, Input, Select, Button, Radio, Space, Typography, Alert, Divider } from 'antd';
import { ArrowUp, ArrowDown, Info } from 'lucide-react';
import { useUserAssets } from '@/hooks/useUserAssets';
import { useMarketData } from '@/hooks/useMarketData';

const { Text, Title } = Typography;
const { Option } = Select;

interface TradeFormData {
  symbol: string;
  type: 'buy' | 'sell';
  orderType: 'limit' | 'market';
  price: string;
  amount: string;
  total: string;
}

export function TradeForm() {
  const [form] = Form.useForm<TradeFormData>();
  const [tradeType, setTradeType] = useState<'buy' | 'sell'>('buy');
  const [orderType, setOrderType] = useState<'limit' | 'market'>('limit');
  const [activeField, setActiveField] = useState<'price' | 'amount' | 'total'>('amount');

  const { assets } = useUserAssets();
  const { marketData } = useMarketData();

  const currentPrice = marketData.find(data => data.symbol === 'BTCUSDT')?.price || '0';

  const handleTypeChange = (e: any) => {
    setTradeType(e.target.value);
  };

  const handleOrderTypeChange = (value: 'limit' | 'market') => {
    setOrderType(value);
  };

  const handleFieldChange = (changedFields: any, allFields: any) => {
    const values = form.getFieldsValue();

    // Auto-calculate based on active field
    if (activeField === 'amount' && values.price && values.amount) {
      const total = parseFloat(values.price) * parseFloat(values.amount);
      form.setFieldValue('total', total.toFixed(8));
    } else if (activeField === 'total' && values.price && values.total) {
      const amount = parseFloat(values.total) / parseFloat(values.price);
      form.setFieldValue('amount', amount.toFixed(8));
    } else if (activeField === 'price' && values.amount && values.total) {
      const price = parseFloat(values.total) / parseFloat(values.amount);
      form.setFieldValue('price', price.toFixed(8));
    }
  };

  const handleSubmit = async (values: TradeFormData) => {
    try {
      console.log('Trade submitted:', values);
      // TODO: Implement trade logic
    } catch (error) {
      console.error('Trade error:', error);
    }
  };

  const getAvailableBalance = () => {
    if (tradeType === 'buy') {
      const usdtAsset = assets.find(asset => asset.currency === 'USDT');
      return usdtAsset ? usdtAsset.available : '0';
    } else {
      const btcAsset = assets.find(asset => asset.currency === 'BTC');
      return btcAsset ? btcAsset.available : '0';
    }
  };

  const getMinAmount = () => {
    return '0.0001';
  };

  const getMaxAmount = () => {
    const balance = parseFloat(getAvailableBalance());
    if (tradeType === 'buy') {
      return (balance / parseFloat(currentPrice)).toFixed(8);
    }
    return balance.toFixed(8);
  };

  const estimateFee = (amount: string) => {
    const value = parseFloat(amount) || 0;
    return (value * 0.001).toFixed(8); // 0.1% fee
  };

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleSubmit}
      onFieldsChange={handleFieldChange}
      initialValues={{
        symbol: 'BTCUSDT',
        type: 'buy',
        orderType: 'limit',
        price: currentPrice,
        amount: '',
        total: '',
      }}
    >
      {/* Trade Type Selection */}
      <div className="mb-4">
        <Radio.Group
          value={tradeType}
          onChange={handleTypeChange}
          className="w-full"
          buttonStyle="solid"
        >
          <Radio.Button value="buy" className="flex-1 text-center">
            <div className="flex items-center justify-center space-x-2">
              <ArrowUp className="h-4 w-4" />
              <span>买入</span>
            </div>
          </Radio.Button>
          <Radio.Button value="sell" className="flex-1 text-center">
            <div className="flex items-center justify-center space-x-2">
              <ArrowDown className="h-4 w-4" />
              <span>卖出</span>
            </div>
          </Radio.Button>
        </Radio.Group>
      </div>

      {/* Order Type Selection */}
      <Form.Item name="orderType" className="mb-4">
        <Radio.Group
          value={orderType}
          onChange={(e) => handleOrderTypeChange(e.target.value)}
          className="w-full"
        >
          <Radio value="limit">限价委托</Radio>
          <Radio value="market">市价委托</Radio>
        </Radio.Group>
      </Form.Item>

      {/* Price Input */}
      {orderType === 'limit' && (
        <Form.Item
          name="price"
          label="价格"
          rules={[
            { required: true, message: '请输入价格' },
            { type: 'string', pattern: /^[0-9]*\.?[0-9]+$/, message: '请输入有效价格' },
          ]}
        >
          <Input
            placeholder="0.00"
            suffix="USDT"
            onFocus={() => setActiveField('price')}
          />
        </Form.Item>
      )}

      {/* Market Price Info */}
      {orderType === 'market' && (
        <Alert
          message="市价委托"
          description={`当前市场价格: ${currentPrice} USDT`}
          type="info"
          showIcon
          className="mb-4"
        />
      )}

      {/* Amount Input */}
      <Form.Item
        name="amount"
        label="数量"
        rules={[
          { required: true, message: '请输入数量' },
          { type: 'string', pattern: /^[0-9]*\.?[0-9]+$/, message: '请输入有效数量' },
        ]}
        extra={
          <div className="flex justify-between mt-1">
            <Text type="secondary">
              可用: {getAvailableBalance()} {tradeType === 'buy' ? 'USDT' : 'BTC'}
            </Text>
            <Space>
              <Button
                type="link"
                size="small"
                onClick={() => {
                  form.setFieldValue('amount', getMaxAmount());
                  setActiveField('amount');
                }}
              >
                全部
              </Button>
              <Button
                type="link"
                size="small"
                onClick={() => {
                  form.setFieldValue('amount', (parseFloat(getMaxAmount()) * 0.5).toFixed(8));
                  setActiveField('amount');
                }}
              >
                50%
              </Button>
              <Button
                type="link"
                size="small"
                onClick={() => {
                  form.setFieldValue('amount', (parseFloat(getMaxAmount()) * 0.25).toFixed(8));
                  setActiveField('amount');
                }}
              >
                25%
              </Button>
            </Space>
          </div>
        }
      >
        <Input
          placeholder="0.00000000"
          suffix="BTC"
          onFocus={() => setActiveField('amount')}
        />
      </Form.Item>

      {/* Total Input */}
      <Form.Item
        name="total"
        label="总额"
        rules={[
          { required: true, message: '请输入总额' },
          { type: 'string', pattern: /^[0-9]*\.?[0-9]+$/, message: '请输入有效总额' },
        ]}
      >
        <Input
          placeholder="0.00"
          suffix="USDT"
          onFocus={() => setActiveField('total')}
        />
      </Form.Item>

      {/* Fee Info */}
      <div className="mb-4 p-3 bg-gray-50 dark:bg-gray-800 rounded-lg">
        <div className="flex justify-between items-center mb-2">
          <Text type="secondary">手续费</Text>
          <Text>{estimateFee(form.getFieldValue('amount') || '0')} USDT</Text>
        </div>
        <div className="flex justify-between items-center">
          <Text type="secondary">费率</Text>
          <Text>0.1%</Text>
        </div>
      </div>

      {/* Available Balance */}
      <Alert
        message={
          <div className="flex justify-between items-center">
            <span>可用余额</span>
            <span className="font-medium">
              {getAvailableBalance()} {tradeType === 'buy' ? 'USDT' : 'BTC'}
            </span>
          </div>
        }
        type="info"
        showIcon
        className="mb-4"
      />

      {/* Submit Button */}
      <Form.Item>
        <Button
          type="primary"
          htmlType="submit"
          size="large"
          className="w-full"
          style={{
            backgroundColor: tradeType === 'buy' ? '#10b981' : '#ef4444',
            borderColor: tradeType === 'buy' ? '#10b981' : '#ef4444',
          }}
        >
          {tradeType === 'buy' ? '买入 BTC' : '卖出 BTC'}
        </Button>
      </Form.Item>

      {/* Trading Tips */}
      <Divider />
      <div className="space-y-2">
        <Text type="secondary" className="text-sm">
          <Info className="h-4 w-4 inline mr-1" />
          最小交易量: {getMinAmount()} BTC
        </Text>
        <br />
        <Text type="secondary" className="text-sm">
          <Info className="h-4 w-4 inline mr-1" />
          交易时间: 7x24小时
        </Text>
      </div>
    </Form>
  );
}