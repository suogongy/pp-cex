'use client';

import { WagmiProvider as WagmiCoreProvider } from 'wagmi';
import { createWeb3Modal } from '@web3modal/wagmi/react';
import { defaultWagmiConfig } from '@web3modal/wagmi/react/config';
import { mainnet, sepolia } from 'wagmi/chains';
import { injected } from 'wagmi/connectors';

// 1. Get projectId
const projectId = process.env.NEXT_PUBLIC_WALLETCONNECT_PROJECT_ID || 'demo';

// 2. Create wagmiConfig optimized for MetaMask
const metadata = {
  name: 'CEX - 加密货币交易平台',
  description: '安全、稳定、高效的加密货币交易平台',
  url: 'http://localhost:3000',
  icons: ['https://avatars.githubusercontent.com/u/37784886'],
};

// 支持主网和测试网
const chains = [mainnet, sepolia];

const wagmiConfig = defaultWagmiConfig({
  chains,
  projectId,
  metadata,
  ssr: true,
  // 专门配置MetaMask连接器
  connectors: [
    injected({
      target: 'metaMask',
      shimDisconnect: true,
      shimChainChangedDisconnect: true,
    }),
  ],
});

// 3. Create modal optimized for MetaMask
createWeb3Modal({
  wagmiConfig,
  projectId,
  enableAnalytics: false,        // 禁用分析避免Coinbase请求
  enableOnramp: false,          // 禁用入金避免问题
  enableSwaps: false,           // 禁用交换功能
  enableEmail: false,           // 禁用邮箱登录
  enableWalletFeatures: false,  // 禁用钱包功能
  featuredWalletIds: ['c57ca95b47569778a828d19178114f4db188b89b763c899ba0be1c6aea5a5e1'], // MetaMask
  themeMode: 'light',
  themeVariables: {
    '--w3m-accent': '#f6851b', // MetaMask橙色
    '--w3m-color-mix': '#f6851b',
    '--w3m-color-mix-strength': 20,
  },
  termsOfServiceUrl: 'https://www.metamask.io/terms/',
  privacyPolicyUrl: 'https://www.metamask.io/privacy/',
});

export function WagmiProvider({ children }: { children: React.ReactNode }) {
  return (
    <WagmiCoreProvider config={wagmiConfig}>
      {children}
    </WagmiCoreProvider>
  );
}