'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { MarketData } from '@/types';

interface MarketDataContextType {
  marketData: MarketData[];
  isConnected: boolean;
  updateMarketData: (data: MarketData) => void;
}

const MarketDataContext = createContext<MarketDataContextType | undefined>(undefined);

export function MarketDataProvider({ children }: { children: ReactNode }) {
  const [marketData, setMarketData] = useState<MarketData[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [useMockData, setUseMockData] = useState(false);

  useEffect(() => {
    const wsUrl = `${process.env.NEXT_PUBLIC_WS_URL}/market`;

    // 模拟市场数据
    const mockData: MarketData[] = [
      {
        id: 'bitcoin',
        name: 'Bitcoin',
        symbol: 'BTC',
        price: 45230.5,
        change24h: 2.45,
        volume24h: 28500000000,
        marketCap: 870000000000,
      },
      {
        id: 'ethereum',
        name: 'Ethereum',
        symbol: 'ETH',
        price: 3120.8,
        change24h: -1.2,
        volume24h: 15800000000,
        marketCap: 380000000000,
      },
      {
        id: 'tether',
        name: 'Tether',
        symbol: 'USDT',
        price: 1.0,
        change24h: 0.01,
        volume24h: 45200000000,
        marketCap: 95000000000,
      },
    ];

    // Try WebSocket connection first
    const ws = new WebSocket(wsUrl);
    let connectionTimeout: NodeJS.Timeout;

    // Set timeout for WebSocket connection
    connectionTimeout = setTimeout(() => {
      console.log('WebSocket connection timeout, using mock data');
      ws.close();
      setUseMockData(true);
      setMarketData(mockData);
    }, 3000); // 3 seconds timeout

    ws.onopen = () => {
      clearTimeout(connectionTimeout);
      setIsConnected(true);
      setUseMockData(false);
      console.log('Market data WebSocket connected');

      // Subscribe to all market data updates
      ws.send(JSON.stringify({
        method: 'SUBSCRIBE',
        params: ['market.overview'],
        id: 1,
      }));
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);

        if (data.method === 'market.overview') {
          setMarketData(data.params);
        } else if (data.method === 'market.update') {
          setMarketData(prev =>
            prev.map(item =>
              item.symbol === data.params.symbol ? data.params : item
            )
          );
        }
      } catch (err) {
        console.error('Market data WebSocket message parse error:', err);
      }
    };

    ws.onclose = () => {
      clearTimeout(connectionTimeout);
      setIsConnected(false);
      if (!useMockData) {
        console.log('WebSocket disconnected, falling back to mock data');
        setUseMockData(true);
        setMarketData(mockData);
      }
    };

    ws.onerror = (error) => {
      clearTimeout(connectionTimeout);
      console.log('WebSocket connection failed, using mock data');
      setIsConnected(false);
      setUseMockData(true);
      setMarketData(mockData);
    };

    return () => {
      clearTimeout(connectionTimeout);
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, []);

  const updateMarketData = (data: MarketData) => {
    setMarketData(prev =>
      prev.map(item =>
        item.symbol === data.symbol ? data : item
      )
    );
  };

  const value: MarketDataContextType = {
    marketData,
    isConnected,
    updateMarketData,
  };

  return (
    <MarketDataContext.Provider value={value}>
      {children}
    </MarketDataContext.Provider>
  );
}

export function useMarketDataContext() {
  const context = useContext(MarketDataContext);
  if (context === undefined) {
    throw new Error('useMarketDataContext must be used within a MarketDataProvider');
  }
  return context;
}