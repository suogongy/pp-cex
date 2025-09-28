// API Response Types
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

// User Types
export interface User {
  id: string;
  username: string;
  email: string;
  avatar?: string;
  isKycVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

// Auth Types
export interface LoginForm {
  email: string;
  password: string;
  captcha?: string;
}

export interface RegisterForm {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  captcha?: string;
  inviteCode?: string;
}

// Trading Types
export interface TradingPair {
  id: string;
  baseAsset: string;
  quoteAsset: string;
  symbol: string;
  price: number;
  change24h: number;
  volume24h: number;
  high24h: number;
  low24h: number;
}

export interface Order {
  id: string;
  symbol: string;
  side: 'buy' | 'sell';
  type: 'market' | 'limit';
  amount: number;
  price: number;
  filled: number;
  status: 'pending' | 'filled' | 'cancelled';
  createdAt: string;
}

export interface Trade {
  id: string;
  symbol: string;
  side: 'buy' | 'sell';
  amount: number;
  price: number;
  fee: number;
  createdAt: string;
}

// Asset Types
export interface Asset {
  id: string;
  symbol: string;
  name: string;
  icon?: string;
  totalBalance: number;
  availableBalance: number;
  frozenBalance: number;
  btcValue: number;
  usdValue: number;
}

// Market Data Types
export interface MarketData {
  id: string;
  name: string;
  symbol: string;
  price: number;
  change24h: number;
  volume24h: number;
  marketCap: number;
}

export interface MarketTicker {
  symbol: string;
  price: number;
  change24h: number;
  volume24h: number;
  high24h: number;
  low24h: number;
}

export interface KlineData {
  timestamp: number;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

// Wallet Types
export interface WalletAddress {
  id: string;
  currency: string;
  address: string;
  tag?: string;
  createdAt: string;
}

export interface DepositRecord {
  id: string;
  currency: string;
  amount: number;
  address: string;
  txHash: string;
  status: 'pending' | 'confirmed' | 'failed';
  createdAt: string;
}

export interface WithdrawRecord {
  id: string;
  currency: string;
  amount: number;
  address: string;
  fee: number;
  txHash?: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  createdAt: string;
}