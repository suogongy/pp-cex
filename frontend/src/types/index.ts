export interface User {
  id: number;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  status: number;
  kycStatus: number;
  level: number;
  inviteCode?: string;
  referrerId?: number;
  apiKey?: string;
  googleAuthEnabled: boolean;
  emailVerified: boolean;
  phoneVerified: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserAsset {
  id: number;
  userId: number;
  currency: string;
  available: string;
  frozen: string;
  total: string;
  btcValue: string;
  usdValue: string;
  createdAt: string;
  updatedAt: string;
}

export interface TradePair {
  id: number;
  symbol: string;
  baseCurrency: string;
  quoteCurrency: string;
  pricePrecision: number;
  amountPrecision: number;
  minAmount: string;
  maxAmount: string;
  status: number;
  sort: number;
  createdAt: string;
  updatedAt: string;
}

export interface TradeOrder {
  id: number;
  userId: number;
  symbol: string;
  type: number; // 1-buy, 2-sell
  orderType: number; // 1-limit, 2-market
  price: string;
  amount: string;
  executedAmount: string;
  executedValue: string;
  fee: string;
  status: number; // 1-pending, 2-partial, 3-completed, 4-cancelled
  timeInForce: number; // 1-GTC, 2-IOC, 3-FOK
  createdAt: string;
  updatedAt: string;
}

export interface TradeDetail {
  id: number;
  orderId: number;
  symbol: string;
  type: number;
  price: string;
  amount: string;
  fee: string;
  createdAt: string;
}

export interface MarketData {
  symbol: string;
  price: string;
  priceChange: string;
  priceChangePercent: string;
  high24h: string;
  low24h: string;
  volume24h: string;
  quoteVolume24h: string;
  lastUpdate: string;
}

export interface KlineData {
  timestamp: number;
  open: string;
  high: string;
  low: string;
  close: string;
  volume: string;
}

export interface WalletAddress {
  id: number;
  userId: number;
  currency: string;
  address: string;
  memo?: string;
  network: string;
  type: number; // 1-hot, 2-cold
  status: number;
  createdAt: string;
  updatedAt: string;
}

export interface RechargeRecord {
  id: number;
  userId: number;
  currency: string;
  txHash: string;
  amount: string;
  fromAddress: string;
  toAddress: string;
  confirmations: number;
  requiredConfirmations: number;
  status: number; // 1-pending, 2-completed, 3-failed
  createdAt: string;
  updatedAt: string;
}

export interface WithdrawRecord {
  id: number;
  userId: number;
  currency: string;
  txHash?: string;
  amount: string;
  fee: string;
  toAddress: string;
  network: string;
  memo?: string;
  status: number; // 1-pending, 2-processing, 3-completed, 4-failed
  auditStatus: number; // 1-pending, 2-approved, 3-rejected
  createdAt: string;
  updatedAt: string;
}

export interface FinancialFlow {
  id: number;
  userId: number;
  type: number; // 1-recharge, 2-withdraw, 3-trade, 4-fee, 5-transfer
  currency: string;
  amount: string;
  balance: string;
  description: string;
  referenceId?: string;
  createdAt: string;
}

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PaginatedResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

export interface LoginForm {
  username: string;
  password: string;
  captcha?: string;
  captchaId?: string;
  googleCode?: string;
  rememberMe?: boolean;
}

export interface RegisterForm {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  phone?: string;
  inviteCode?: string;
  captcha?: string;
  captchaId?: string;
}

export interface UserSettings {
  notificationEnabled: boolean;
  emailNotification: boolean;
  smsNotification: boolean;
  language: string;
  timezone: string;
  theme: 'light' | 'dark' | 'auto';
}

export interface SecuritySettings {
  googleAuthEnabled: boolean;
  emailVerified: boolean;
  phoneVerified: boolean;
  withdrawalWhitelist: string[];
  withdrawalEnabled: boolean;
  apiEnabled: boolean;
}