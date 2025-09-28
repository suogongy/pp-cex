'use client';

import { useState, useEffect } from 'react';
import { MarketData } from '@/types';

export function useMarketData() {
  const [marketData, setMarketData] = useState<MarketData[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 模拟数据加载
    const mockData: MarketData[] = [
      {
        id: 'bitcoin',
        name: 'Bitcoin',
        symbol: 'BTC',
        price: 45000,
        change24h: 2.5,
        volume24h: 28000000000,
        marketCap: 850000000000,
      },
      {
        id: 'ethereum',
        name: 'Ethereum',
        symbol: 'ETH',
        price: 3200,
        change24h: -1.2,
        volume24h: 15000000000,
        marketCap: 380000000000,
      },
    ];

    const timer = setTimeout(() => {
      setMarketData(mockData);
      setLoading(false);
    }, 1000);

    return () => clearTimeout(timer);
  }, []);

  return { marketData, loading };
}

export function useMarketSearch(query: string) {
  const [results, setResults] = useState<MarketData[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      setLoading(false);
      return;
    }

    setLoading(true);

    // 模拟搜索延迟
    const timer = setTimeout(() => {
      // 模拟搜索结果
      const mockResults: MarketData[] = [
        {
          id: 'bitcoin',
          name: 'Bitcoin',
          symbol: 'BTC',
          price: 45000,
          change24h: 2.5,
          volume24h: 28000000000,
          marketCap: 850000000000,
        },
        {
          id: 'ethereum',
          name: 'Ethereum',
          symbol: 'ETH',
          price: 3200,
          change24h: -1.2,
          volume24h: 15000000000,
          marketCap: 380000000000,
        },
      ].filter(item =>
        item.name.toLowerCase().includes(query.toLowerCase()) ||
        item.symbol.toLowerCase().includes(query.toLowerCase())
      );

      setResults(mockResults);
      setLoading(false);
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  return { results, loading };
}