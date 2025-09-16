'use client';

import { useState, useEffect, useCallback } from 'react';
import { api, endpoints } from '@/lib/api';
import { MarketData, TradePair, KlineData } from '@/types';

interface MarketDataHook {
  marketData: MarketData[];
  tradePairs: TradePair[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
  getTicker: (symbol: string) => MarketData | null;
  getKline: (symbol: string, interval: string, limit?: number) => Promise<KlineData[]>;
  getDepth: (symbol: string) => Promise<any>;
  getTrades: (symbol: string, limit?: number) => Promise<any>;
}

export function useMarketData(): MarketDataHook {
  const [marketData, setMarketData] = useState<MarketData[]>([]);
  const [tradePairs, setTradePairs] = useState<TradePair[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch market overview
      const overviewData = await api.get<MarketData[]>(endpoints.market.overview);
      setMarketData(overviewData);

      // Fetch trade pairs
      const pairsData = await api.get<TradePair[]>(endpoints.market.pairs);
      setTradePairs(pairsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取市场数据失败');
      console.error('Market data fetch error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const getTicker = useCallback((symbol: string): MarketData | null => {
    return marketData.find(data => data.symbol === symbol) || null;
  }, [marketData]);

  const getKline = useCallback(async (symbol: string, interval: string, limit = 100): Promise<KlineData[]> => {
    try {
      const response = await api.get<KlineData[]>(endpoints.market.kline.replace(':symbol', symbol), {
        params: { interval, limit },
      });
      return response;
    } catch (err) {
      console.error('Kline data fetch error:', err);
      throw err;
    }
  }, []);

  const getDepth = useCallback(async (symbol: string) => {
    try {
      const response = await api.get(endpoints.market.depth.replace(':symbol', symbol));
      return response;
    } catch (err) {
      console.error('Depth data fetch error:', err);
      throw err;
    }
  }, []);

  const getTrades = useCallback(async (symbol: string, limit = 50) => {
    try {
      const response = await api.get(endpoints.market.trades.replace(':symbol', symbol), {
        params: { limit },
      });
      return response;
    } catch (err) {
      console.error('Trades data fetch error:', err);
      throw err;
    }
  }, []);

  return {
    marketData,
    tradePairs,
    loading,
    error,
    refresh: fetchData,
    getTicker,
    getKline,
    getDepth,
    getTrades,
  };
}

// Hook for real-time market data
export function useRealTimeMarketData(symbol: string) {
  const [price, setPrice] = useState<string>('0');
  const [priceChange, setPriceChange] = useState<string>('0');
  const [priceChangePercent, setPriceChangePercent] = useState<string>('0');
  const [volume, setVolume] = useState<string>('0');
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    if (!symbol) return;

    // Initialize WebSocket connection
    const wsUrl = `${process.env.NEXT_PUBLIC_WS_URL}/market/${symbol}`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      setIsConnected(true);
      console.log('Market data WebSocket connected');

      // Subscribe to ticker updates
      ws.send(JSON.stringify({
        method: 'SUBSCRIBE',
        params: [`ticker.${symbol}`],
        id: 1,
      }));
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);

        if (data.method === 'ticker') {
          const { c: price, h: high24h, l: low24h, v: volume24h } = data.params;
          setPrice(price);
          setVolume(volume24h);

          // Calculate price change
          if (high24h && low24h) {
            const change = parseFloat(price) - parseFloat(low24h);
            const changePercent = (change / parseFloat(low24h)) * 100;
            setPriceChange(change.toFixed(8));
            setPriceChangePercent(changePercent.toFixed(2));
          }
        }
      } catch (err) {
        console.error('WebSocket message parse error:', err);
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
  }, [symbol]);

  return {
    price,
    priceChange,
    priceChangePercent,
    volume,
    isConnected,
  };
}

// Hook for trade pair data
export function useTradePairs() {
  const [tradePairs, setTradePairs] = useState<TradePair[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTradePairs = async () => {
      try {
        const response = await api.get<TradePair[]>(endpoints.market.pairs);
        setTradePairs(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : '获取交易对失败');
        console.error('Trade pairs fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchTradePairs();
  }, []);

  return {
    tradePairs,
    loading,
    error,
  };
}

// Hook for market search
export function useMarketSearch(query: string, type = 'symbol') {
  const [results, setResults] = useState<MarketData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      return;
    }

    const search = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await api.get<MarketData[]>(endpoints.market.search, {
          params: { q: query, type },
        });
        setResults(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : '搜索失败');
        console.error('Market search error:', err);
      } finally {
        setLoading(false);
      }
    };

    const debounceTimer = setTimeout(search, 300);
    return () => clearTimeout(debounceTimer);
  }, [query, type]);

  return {
    results,
    loading,
    error,
  };
}