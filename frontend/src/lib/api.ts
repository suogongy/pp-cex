import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { notification } from 'antd';
import { ApiResponse } from '@/types';

class ApiClient {
  private instance: AxiosInstance;

  constructor() {
    this.instance = axios.create({
      baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.instance.interceptors.request.use(
      (config) => {
        // Add token from localStorage
        const token = localStorage.getItem('access_token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Add timestamp for caching
        config.params = {
          ...config.params,
          _t: Date.now(),
        };

        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.instance.interceptors.response.use(
      (response: AxiosResponse<ApiResponse>) => {
        const { code, message, data } = response.data;

        if (code === 200) {
          return data;
        }

        // Handle business errors
        if (code >= 400 && code < 500) {
          notification.warning({
            message: '提示',
            description: message || '请求失败',
          });
        } else if (code >= 500) {
          notification.error({
            message: '错误',
            description: message || '服务器错误',
          });
        }

        return Promise.reject(new Error(message || '请求失败'));
      },
      (error) => {
        // Handle network errors
        if (error.response) {
          const { status, data } = error.response;

          switch (status) {
            case 401:
              // Unauthorized - clear token and redirect to login
              localStorage.removeItem('access_token');
              localStorage.removeItem('refresh_token');
              window.location.href = '/login';
              break;
            case 403:
              notification.error({
                message: '权限不足',
                description: '您没有权限访问此资源',
              });
              break;
            case 404:
              notification.error({
                message: '资源不存在',
                description: '请求的资源不存在',
              });
              break;
            case 429:
              notification.error({
                message: '请求过于频繁',
                description: '请稍后再试',
              });
              break;
            case 500:
              notification.error({
                message: '服务器错误',
                description: '服务器内部错误，请稍后再试',
              });
              break;
            default:
              notification.error({
                message: '网络错误',
                description: '网络连接错误，请检查网络设置',
              });
          }
        } else if (error.request) {
          // Network error
          notification.error({
            message: '网络错误',
            description: '无法连接到服务器，请检查网络设置',
          });
        } else {
          // Other errors
          notification.error({
            message: '错误',
            description: error.message || '未知错误',
          });
        }

        return Promise.reject(error);
      }
    );
  }

  // Generic request methods
  async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get(url, config);
  }

  async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, data, config);
  }

  async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put(url, data, config);
  }

  async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.patch(url, data, config);
  }

  async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, config);
  }

  // Upload file
  async upload<T = any>(url: string, file: File, onProgress?: (progress: number) => void): Promise<T> {
    const formData = new FormData();
    formData.append('file', file);

    return this.instance.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      },
    });
  }

  // Download file
  async download(url: string, filename?: string): Promise<void> {
    const response = await this.instance.get(url, {
      responseType: 'blob',
    });

    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename || 'download';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }
}

// Create API instance
export const api = new ApiClient();

// API endpoints
export const endpoints = {
  // Auth
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    logout: '/auth/logout',
    refresh: '/auth/refresh',
    profile: '/auth/profile',
    updateProfile: '/auth/profile',
    changePassword: '/auth/change-password',
    forgotPassword: '/auth/forgot-password',
    resetPassword: '/auth/reset-password',
  },

  // User
  user: {
    assets: '/user/assets',
    asset: '/user/assets/:currency',
    orders: '/user/orders',
    order: '/user/orders/:id',
    trades: '/user/trades',
    financialFlows: '/user/financial-flows',
    settings: '/user/settings',
    updateSettings: '/user/settings',
    security: '/user/security',
    kyc: '/user/kyc',
    uploadKyc: '/user/kyc/upload',
  },

  // Market
  market: {
    pairs: '/market/pairs',
    pair: '/market/pairs/:symbol',
    ticker: '/market/ticker/:symbol',
    kline: '/market/kline/:symbol',
    depth: '/market/depth/:symbol',
    trades: '/market/trades/:symbol',
    overview: '/market/overview',
    hotPairs: '/market/hot-pairs',
    search: '/market/search',
  },

  // Trade
  trade: {
    create: '/trade/orders',
    cancel: '/trade/orders/:id/cancel',
    orders: '/trade/orders',
    order: '/trade/orders/:id',
    trades: '/trade/trades',
    fee: '/trade/fee',
  },

  // Wallet
  wallet: {
    addresses: '/wallet/addresses',
    createAddress: '/wallet/addresses',
    rechargeRecords: '/wallet/recharge',
    withdrawRecords: '/wallet/withdraw',
    withdraw: '/wallet/withdraw',
    estimateFee: '/wallet/estimate-fee',
    whitelist: '/wallet/whitelist',
    addToWhitelist: '/wallet/whitelist',
  },

  // Finance
  finance: {
    deposit: '/finance/deposit',
    withdraw: '/finance/withdraw',
    transfer: '/finance/transfer',
    history: '/finance/history',
    summary: '/finance/summary',
  },

  // Admin
  admin: {
    users: '/admin/users',
    user: '/admin/users/:id',
    orders: '/admin/orders',
    trades: '/admin/trades',
    assets: '/admin/assets',
    deposits: '/admin/deposits',
    withdrawals: '/admin/withdrawals',
    settings: '/admin/settings',
    system: '/admin/system',
  },
};

// WebSocket connection manager
export class WebSocketManager {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private listeners: Map<string, Function[]> = new Map();

  constructor(private url: string) {}

  connect() {
    try {
      this.ws = new WebSocket(this.url);

      this.ws.onopen = () => {
        console.log('WebSocket connected');
        this.reconnectAttempts = 0;
        this.emit('connected');
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          this.emit('message', data);
          this.emit(data.type, data);
        } catch (error) {
          console.error('WebSocket message parse error:', error);
        }
      };

      this.ws.onclose = () => {
        console.log('WebSocket disconnected');
        this.emit('disconnected');
        this.reconnect();
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.emit('error', error);
      };
    } catch (error) {
      console.error('WebSocket connection error:', error);
      this.reconnect();
    }
  }

  private reconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        console.log(`WebSocket reconnecting... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
        this.connect();
      }, this.reconnectDelay * this.reconnectAttempts);
    }
  }

  send(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    }
  }

  on(event: string, callback: Function) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(callback);
  }

  off(event: string, callback: Function) {
    const callbacks = this.listeners.get(event);
    if (callbacks) {
      const index = callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
    }
  }

  private emit(event: string, data?: any) {
    const callbacks = this.listeners.get(event);
    if (callbacks) {
      callbacks.forEach(callback => callback(data));
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}