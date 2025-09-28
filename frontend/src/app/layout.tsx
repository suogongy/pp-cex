import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import { AntdRegistry } from '@ant-design/nextjs-registry';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { ReactQueryProvider } from '@/providers/ReactQueryProvider';
import { WagmiProvider } from '@/providers/WagmiProvider';
import { AuthProvider } from '@/providers/AuthProvider';
import { ToasterProvider } from '@/providers/ToasterProvider';
import { HelmetProvider } from '@/providers/HelmetProvider';
import '../styles/globals.css';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'CEX - 加密货币交易平台',
  description: '安全、稳定、高效的加密货币交易平台',
  keywords: ['加密货币', '比特币', '以太坊', '区块链', '交易'],
  authors: [{ name: 'CEX Team' }],
  openGraph: {
    title: 'CEX - 加密货币交易平台',
    description: '安全、稳定、高效的加密货币交易平台',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'CEX - 加密货币交易平台',
    description: '安全、稳定、高效的加密货币交易平台',
  },
  robots: 'index, follow',
};

export const viewport = {
  width: 'device-width',
  initialScale: 1,
};

const antdTheme = {
  token: {
    colorPrimary: '#2563eb',
    borderRadius: 6,
  },
  components: {
    Button: {
      borderRadius: 6,
      controlHeight: 40,
    },
    Input: {
      borderRadius: 6,
      controlHeight: 40,
    },
    Select: {
      borderRadius: 6,
      controlHeight: 40,
    },
    Card: {
      borderRadius: 8,
    },
    Table: {
      borderRadius: 8,
    },
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="zh-CN">
      <head>
        <link rel="icon" href="/favicon.ico" />
        <link rel="apple-touch-icon" href="/apple-touch-icon.png" />
        <meta name="theme-color" content="#2563eb" />
      </head>
      <body className={inter.className}>
        <AntdRegistry>
          <ConfigProvider locale={zhCN} theme={antdTheme}>
            <HelmetProvider>
              <WagmiProvider>
                <ReactQueryProvider>
                  <AuthProvider>
                    <ToasterProvider>
                      {children}
                    </ToasterProvider>
                  </AuthProvider>
                </ReactQueryProvider>
              </WagmiProvider>
            </HelmetProvider>
          </ConfigProvider>
        </AntdRegistry>
      </body>
    </html>
  );
}