'use client';

import { useState, useEffect } from 'react';
import { useWeb3Modal } from '@web3modal/wagmi/react';
import { useAccount, useBalance } from 'wagmi';
import { Button, Card, Row, Col, Statistic, Typography, Space, Spin } from 'antd';
import { Bitcoin, Coins, TrendingUp, Users, Shield, Zap } from 'lucide-react';
import { MarketDataProvider } from '@/providers/MarketDataProvider';
import { useAuth } from '@/hooks/useAuth';
import { useMarketData } from '@/hooks/useMarketData';
import { useUserAssets } from '@/hooks/useUserAssets';
import { SimpleChart } from '@/components/SimpleChart';
import { MarketOverview } from '@/components/MarketOverview';
import { TradeForm } from '@/components/TradeForm';
import { AssetOverview } from '@/components/AssetOverview';
import { RecentTrades } from '@/components/RecentTrades';

const { Title, Text } = Typography;

export default function Home() {
  const { open } = useWeb3Modal();
  const { isConnected, address } = useAccount();
  const { isAuthenticated, user } = useAuth();
  const { data: ethBalance } = useBalance({ address });

  const { marketData, loading: marketLoading } = useMarketData();
  const { assets, loading: assetsLoading } = useUserAssets();

  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  if (!isClient) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <MarketDataProvider>
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-900 dark:to-gray-800">
        {/* Header */}
        <header className="bg-white dark:bg-gray-800 shadow-sm border-b">
          <div className="container mx-auto px-4 py-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <div className="flex items-center space-x-2">
                  <Bitcoin className="h-8 w-8 text-blue-600" />
                  <Title level={3} className="!mb-0">CEX</Title>
                </div>
                <nav className="hidden md:flex space-x-6">
                  <a href="#" className="text-gray-600 hover:text-gray-900 dark:text-gray-300 dark:hover:text-white">
                    交易
                  </a>
                  <a href="#" className="text-gray-600 hover:text-gray-900 dark:text-gray-300 dark:hover:text-white">
                    市场
                  </a>
                  <a href="#" className="text-gray-600 hover:text-gray-900 dark:text-gray-300 dark:hover:text-white">
                    资产
                  </a>
                  <a href="#" className="text-gray-600 hover:text-gray-900 dark:text-gray-300 dark:hover:text-white">
                    财务
                  </a>
                </nav>
              </div>

              <div className="flex items-center space-x-4">
                {isConnected && ethBalance && (
                  <div className="hidden sm:block">
                    <Text type="secondary">
                      {ethBalance.formatted.slice(0, 6)} ETH
                    </Text>
                  </div>
                )}

                {isAuthenticated ? (
                  <div className="flex items-center space-x-2">
                    <Text>{user?.username}</Text>
                    <Button type="primary" size="small">
                      个人中心
                    </Button>
                  </div>
                ) : (
                  <Space>
                    <Button onClick={() => open()}>
                      {isConnected ? '断开MetaMask' : '连接MetaMask'}
                    </Button>
                    <Button type="primary">
                      登录
                    </Button>
                  </Space>
                )}
              </div>
            </div>
          </div>
        </header>

        {/* Hero Section */}
        <section className="py-12">
          <div className="container mx-auto px-4">
            <div className="text-center mb-12">
              <Title level={1} className="text-4xl md:text-6xl font-bold mb-4">
                安全可靠的加密货币交易平台
              </Title>
              <Text className="text-xl text-gray-600 dark:text-gray-400">
                专业的数字资产交易服务，支持多种加密货币交易
              </Text>
            </div>

            {/* Stats */}
            <Row gutter={[16, 16]} className="mb-8">
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="24小时交易量"
                    value={marketLoading ? '-' : '¥12.5B'}
                    prefix={<TrendingUp className="h-4 w-4" />}
                    valueStyle={{ color: '#3f8600' }}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="注册用户"
                    value={marketLoading ? '-' : '2.5M'}
                    prefix={<Users className="h-4 w-4" />}
                    valueStyle={{ color: '#1890ff' }}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="支持币种"
                    value={marketLoading ? '-' : '150+'}
                    prefix={<Bitcoin className="h-4 w-4" />}
                    valueStyle={{ color: '#722ed1' }}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="安全性"
                    value="99.99%"
                    prefix={<Shield className="h-4 w-4" />}
                    valueStyle={{ color: '#cf1322' }}
                  />
                </Card>
              </Col>
            </Row>
          </div>
        </section>

        {/* Main Content */}
        <div className="container mx-auto px-4 pb-12">
          <Row gutter={[16, 16]}>
            {/* Trading Chart */}
            <Col xs={24} lg={16}>
              <Card title="BTC/USDT" className="h-96">
                <SimpleChart symbol="BTC/USDT" />
              </Card>
            </Col>

            {/* Trading Form */}
            <Col xs={24} lg={8}>
              <Card title="快速交易" className="h-96">
                <TradeForm />
              </Card>
            </Col>

            {/* Market Overview */}
            <Col xs={24}>
              <Card title="市场概览">
                <MarketOverview />
              </Card>
            </Col>

            {/* User Assets */}
            <Col xs={24} lg={12}>
              <Card title="我的资产">
                <AssetOverview assets={assets} loading={assetsLoading} />
              </Card>
            </Col>

            {/* Recent Trades */}
            <Col xs={24} lg={12}>
              <Card title="最近交易">
                <RecentTrades />
              </Card>
            </Col>
          </Row>
        </div>

        {/* Features */}
        <section className="py-16 bg-white dark:bg-gray-800">
          <div className="container mx-auto px-4">
            <div className="text-center mb-12">
              <Title level={2} className="text-3xl font-bold mb-4">
                为什么选择我们的平台？
              </Title>
            </div>

            <Row gutter={[32, 32]}>
              <Col xs={24} md={8}>
                <div className="text-center">
                  <Shield className="h-12 w-12 text-blue-600 mx-auto mb-4" />
                  <Title level={3} className="mb-2">安全可靠</Title>
                  <Text className="text-gray-600 dark:text-gray-400">
                    采用银行级安全标准，多重签名冷钱包存储，资金安全有保障
                  </Text>
                </div>
              </Col>
              <Col xs={24} md={8}>
                <div className="text-center">
                  <Zap className="h-12 w-12 text-green-600 mx-auto mb-4" />
                  <Title level={3} className="mb-2">极速交易</Title>
                  <Text className="text-gray-600 dark:text-gray-400">
                    高性能撮合引擎，微秒级订单处理，支持高频交易策略
                  </Text>
                </div>
              </Col>
              <Col xs={24} md={8}>
                <div className="text-center">
                  <Users className="h-12 w-12 text-purple-600 mx-auto mb-4" />
                  <Title level={3} className="mb-2">专业服务</Title>
                  <Text className="text-gray-600 dark:text-gray-400">
                    7x24小时客服支持，专业分析师团队，为您的投资保驾护航
                  </Text>
                </div>
              </Col>
            </Row>
          </div>
        </section>

        {/* Footer */}
        <footer className="bg-gray-900 text-white py-8">
          <div className="container mx-auto px-4">
            <div className="text-center">
              <Text className="text-gray-400">
                © 2024 CEX. All rights reserved. | 安全第一，服务至上
              </Text>
            </div>
          </div>
        </footer>
      </div>
    </MarketDataProvider>
  );
}