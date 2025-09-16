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

  useEffect(() => {
    // Initialize WebSocket connection for real-time market data
    const wsUrl = `${process.env.NEXT_PUBLIC_WS_URL}/market`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      setIsConnected(true);
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
          // Update individual market data
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
      setIsConnected(false);
      console.log('Market data WebSocket disconnected');
    };

    ws.onerror = (error) => {
      console.error('Market data WebSocket error:', error);
      setIsConnected(false);
    };

    return () => {
      ws.close();
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