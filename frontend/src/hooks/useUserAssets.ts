'use client';

import { useState, useEffect } from 'react';
import { Asset } from '@/types';

export function useUserAssets() {
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 模拟用户资产数据
    const mockAssets: Asset[] = [
      {
        id: 'bitcoin',
        symbol: 'BTC',
        name: 'Bitcoin',
        totalBalance: 0.5,
        availableBalance: 0.45,
        frozenBalance: 0.05,
        btcValue: 0.5,
        usdValue: 22500,
      },
      {
        id: 'ethereum',
        symbol: 'ETH',
        name: 'Ethereum',
        totalBalance: 5.2,
        availableBalance: 4.8,
        frozenBalance: 0.4,
        btcValue: 3.2,
        usdValue: 16640,
      },
      {
        id: 'usdt',
        symbol: 'USDT',
        name: 'Tether',
        totalBalance: 1000,
        availableBalance: 950,
        frozenBalance: 50,
        btcValue: 0.025,
        usdValue: 1000,
      },
    ];

    const timer = setTimeout(() => {
      setAssets(mockAssets);
      setLoading(false);
    }, 800);

    return () => clearTimeout(timer);
  }, []);

  return { assets, loading };
}