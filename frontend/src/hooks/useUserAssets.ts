'use client';

import { useState, useEffect, useCallback } from 'react';
import { api, endpoints } from '@/lib/api';
import { UserAsset } from '@/types';

interface UserAssetsHook {
  assets: UserAsset[];
  totalBtcValue: string;
  totalUsdValue: string;
  loading: boolean;
  error: string | null;
  refresh: () => void;
  getAsset: (currency: string) => UserAsset | null;
}

export function useUserAssets(): UserAssetsHook {
  const [assets, setAssets] = useState<UserAsset[]>([]);
  const [totalBtcValue, setTotalBtcValue] = useState<string>('0');
  const [totalUsdValue, setTotalUsdValue] = useState<string>('0');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAssets = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.get<UserAsset[]>(endpoints.user.assets);
      setAssets(response);

      // Calculate total values
      const btcTotal = response.reduce((sum, asset) => sum + parseFloat(asset.btcValue), 0);
      const usdTotal = response.reduce((sum, asset) => sum + parseFloat(asset.usdValue), 0);
      setTotalBtcValue(btcTotal.toFixed(8));
      setTotalUsdValue(usdTotal.toFixed(2));
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取资产信息失败');
      console.error('User assets fetch error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAssets();
  }, [fetchAssets]);

  const getAsset = useCallback((currency: string): UserAsset | null => {
    return assets.find(asset => asset.currency === currency) || null;
  }, [assets]);

  return {
    assets,
    totalBtcValue,
    totalUsdValue,
    loading,
    error,
    refresh: fetchAssets,
    getAsset,
  };
}

// Hook for single asset
export function useUserAsset(currency: string) {
  const [asset, setAsset] = useState<UserAsset | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!currency) return;

    const fetchAsset = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await api.get<UserAsset>(endpoints.user.asset.replace(':currency', currency));
        setAsset(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : '获取资产信息失败');
        console.error('User asset fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchAsset();
  }, [currency]);

  return {
    asset,
    loading,
    error,
    refresh: () => {
      if (currency) {
        const fetchAsset = async () => {
          try {
            setLoading(true);
            setError(null);

            const response = await api.get<UserAsset>(endpoints.user.asset.replace(':currency', currency));
            setAsset(response);
          } catch (err) {
            setError(err instanceof Error ? err.message : '获取资产信息失败');
            console.error('User asset fetch error:', err);
          } finally {
            setLoading(false);
          }
        };
        fetchAsset();
      }
    },
  };
}

// Hook for financial flows
export function useFinancialFlows(params?: {
  currency?: string;
  type?: number;
  page?: number;
  pageSize?: number;
  startDate?: string;
  endDate?: string;
}) {
  const [flows, setFlows] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchFlows = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await api.get<{
          list: any[];
          total: number;
          page: number;
          pageSize: number;
        }>(endpoints.user.financialFlows, { params });

        setFlows(response.list);
        setTotal(response.total);
      } catch (err) {
        setError(err instanceof Error ? err.message : '获取资金流水失败');
        console.error('Financial flows fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchFlows();
  }, [params]);

  return {
    flows,
    total,
    loading,
    error,
  };
}

// Hook for user settings
export function useUserSettings() {
  const [settings, setSettings] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchSettings = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.get<any>(endpoints.user.settings);
      setSettings(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取用户设置失败');
      console.error('User settings fetch error:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchSettings();
  }, [fetchSettings]);

  const updateSettings = useCallback(async (newSettings: any) => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.patch<any>(endpoints.user.updateSettings, newSettings);
      setSettings(response);

      // Save to localStorage for persistence
      localStorage.setItem('user_preferences', JSON.stringify(response));

      return response;
    } catch (err) {
      setError(err instanceof Error ? err.message : '更新用户设置失败');
      console.error('User settings update error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    settings,
    loading,
    error,
    updateSettings,
    refresh: fetchSettings,
  };
}

// Hook for security settings
export function useSecuritySettings() {
  const [security, setSecurity] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSecuritySettings = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await api.get<any>(endpoints.user.security);
        setSecurity(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : '获取安全设置失败');
        console.error('Security settings fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchSecuritySettings();
  }, []);

  const enable2FA = useCallback(async (secret: string, code: string) => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.post<any>('/auth/2fa/enable', { secret, code });
      setSecurity(response);
      return response;
    } catch (err) {
      setError(err instanceof Error ? err.message : '启用2FA失败');
      console.error('2FA enable error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const disable2FA = useCallback(async (code: string) => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.post<any>('/auth/2fa/disable', { code });
      setSecurity(response);
      return response;
    } catch (err) {
      setError(err instanceof Error ? err.message : '禁用2FA失败');
      console.error('2FA disable error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    security,
    loading,
    error,
    enable2FA,
    disable2FA,
    refresh: async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await api.get<any>(endpoints.user.security);
        setSecurity(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : '获取安全设置失败');
        console.error('Security settings fetch error:', err);
      } finally {
        setLoading(false);
      }
    },
  };
}