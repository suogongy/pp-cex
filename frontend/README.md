# CEX 前端项目

> 🚀 基于 Next.js 15 + TypeScript + Ant Design 的现代化中心化交易平台前端

## 📖 项目概述

本项目是 CEX 中心化交易平台的前端应用，采用现代化的前端技术栈构建，提供完整的加密货币交易体验。项目专注于学习目的，实现了 CEX 系统的核心功能和交互。

### 🎯 设计理念

- **用户体验优先**：流畅的交易界面和直观的操作流程
- **实时性能**：WebSocket 实时行情和交易数据推送
- **安全可靠**：多重安全验证和风险控制
- **响应式设计**：完美适配桌面端和移动端

## 🛠️ 技术栈

### 核心框架
- **Next.js 15** - React 全栈框架，支持 SSR/SSG
- **TypeScript 5.6** - 类型安全的 JavaScript 超集
- **React 18.3** - 用户界面构建库

### UI 组件库
- **Ant Design 5.20** - 企业级 UI 设计语言
- **Tailwind CSS 3.4** - 原子化 CSS 框架
- **Radix UI** - 无障碍访问的基础组件
- **Framer Motion** - 高性能动画库

### 状态管理与数据
- **Zustand 5.0** - 轻量级状态管理
- **React Query 3.39** - 服务器状态管理
- **Axios 1.7** - HTTP 客户端
- **Socket.io Client 4.7** - 实时通信

### Web3 集成
- **Wagmi 2.12** - React Hooks for Ethereum
- **Viem 2.21** - TypeScript Interface for Ethereum
- **Ethers 6.13** - 以太坊库
- **Web3.js 4.11** - 以太坊 JavaScript API

### 图表与可视化
- **ECharts 5.5** - 企业级图表库
- **Recharts 2.12** - React 图表组件
- **ECharts for React 3.0** - ECharts React 封装

### 开发工具
- **ESLint + Prettier** - 代码质量工具
- **Jest + Testing Library** - 单元测试
- **Playwright** - 端到端测试
- **Webpack Bundle Analyzer** - 打包分析

## 🚀 快速开始

### 环境要求

- Node.js >= 18.0.0
- npm >= 8.0.0
- 现代浏览器（Chrome 90+, Firefox 88+, Safari 14+）

### 安装依赖

```bash
# 进入前端项目目录
cd frontend

# 安装依赖
npm install
```

### 开发环境启动

```bash
# 启动开发服务器
npm run dev

# 访问 http://localhost:3000
```

### 生产环境构建

```bash
# 构建生产版本
npm run build

# 启动生产服务器
npm start
```

### Docker 部署

```bash
# 使用 Docker Compose 启动
docker-compose -f ../backend/docker-compose.dev.yml up frontend

# 或者使用项目部署脚本
cd ..
./scripts/deploy.sh dev up
```

## 📁 项目结构

```
frontend/
├── src/                          # 源代码目录
│   ├── app/                      # Next.js App Router
│   │   ├── (auth)/               # 认证相关页面
│   │   ├── dashboard/            # 仪表板页面
│   │   ├── trade/                # 交易页面
│   │   ├── wallet/               # 钱包页面
│   │   ├── market/               # 市场页面
│   │   ├── settings/             # 设置页面
│   │   ├── api/                  # API 路由
│   │   ├── layout.tsx            # 根布局
│   │   ├── page.tsx              # 首页
│   │   └── globals.css           # 全局样式
│   ├── components/               # 组件库
│   │   ├── ui/                   # 基础 UI 组件
│   │   ├── forms/                # 表单组件
│   │   ├── charts/               # 图表组件
│   │   ├── trading/              # 交易相关组件
│   │   └── layout/               # 布局组件
│   ├── hooks/                    # 自定义 Hooks
│   ├── stores/                   # Zustand 状态管理
│   ├── services/                 # API 服务
│   ├── utils/                    # 工具函数
│   ├── types/                    # TypeScript 类型定义
│   └── constants/                # 常量定义
├── public/                       # 静态资源
│   ├── favicon.ico
│   ├── images/
│   └── locales/                  # 国际化文件
├── tests/                        # 测试文件
├── docs/                         # 组件文档
├── next.config.js               # Next.js 配置
├── tailwind.config.js           # Tailwind CSS 配置
├── tsconfig.json                # TypeScript 配置
├── jest.config.js               # Jest 测试配置
├── playwright.config.ts        # Playwright E2E 配置
└── package.json                 # 项目依赖
```

## 🎮 核心功能模块

### 🔐 用户认证系统
- 多因素认证（2FA、邮箱、短信）
- OAuth2 社交登录
- JWT Token 管理
- 会话状态管理

### 💰 交易功能
- 实时行情显示
- 限价单/市价单交易
- K线图表分析
- 深度图展示
- 交易历史记录

### 🏠 钱包管理
- 多币种资产展示
- 充值提现功能
- 地址管理
- 资产流水记录

### 📊 市场数据
- 实时价格推送
- 市场趋势分析
- 热门币种排行
- 24小时涨跌幅统计

### 🛡️ 安全功能
- Google 验证器绑定
- 防钓鱼码设置
- 登录设备管理
- 异常登录提醒

## 🔧 开发指南

### 代码规范

项目使用 ESLint + Prettier 进行代码格式化：

```bash
# 代码检查
npm run lint

# 格式化代码
npm run format

# 类型检查
npm run type-check
```

### 测试

```bash
# 运行单元测试
npm test

# 运行测试覆盖率
npm run test:coverage

# 运行端到端测试
npm run e2e

# 运行带 UI 的端到端测试
npm run e2e:ui
```

### 性能优化

```bash
# 分析打包大小
npm run analyze
```

### 环境变量

创建 `.env.local` 文件：

```env
# API 配置
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080

# Web3 配置
NEXT_PUBLIC_CHAIN_ID=1
NEXT_PUBLIC_NETWORK_ID=1

# 第三方服务
NEXT_PUBLIC_GOOGLE_ANALYTICS_ID=your_ga_id
NEXT_PUBLIC_SENTRY_DSN=your_sentry_dsn
```

## 📚 组件使用指南

### 基础组件

```tsx
import { Button, Input, Card } from '@/components/ui';

function MyComponent() {
  return (
    <Card className="p-4">
      <Input placeholder="请输入内容" />
      <Button type="primary">提交</Button>
    </Card>
  );
}
```

### 状态管理

```tsx
import { useAuthStore } from '@/stores/auth';

function UserProfile() {
  const { user, logout } = useAuthStore();

  return (
    <div>
      <h1>欢迎，{user?.username}</h1>
      <Button onClick={logout}>退出登录</Button>
    </div>
  );
}
```

### Web3 集成

```tsx
import { useAccount, useBalance } from 'wagmi';

function WalletInfo() {
  const { address } = useAccount();
  const { data: balance } = useBalance({ address });

  return (
    <div>
      <p>地址：{address}</p>
      <p>余额：{balance?.formatted} ETH</p>
    </div>
  );
}
```

## 🚀 部署指南

### Vercel 部署

1. 将代码推送到 GitHub
2. 在 Vercel 中导入项目
3. 配置环境变量
4. 部署

### Docker 部署

```bash
# 构建镜像
docker build -t cex-frontend .

# 运行容器
docker run -p 3000:3000 cex-frontend
```

### Nginx 反向代理

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

## 🔗 相关链接

### 📚 项目文档
- [🏠 项目根目录 README](../README.md) - 项目概览和快速开始
- [⚙️ 后端项目文档](../backend/README.md) - 微服务架构和开发指南
- [📋 技术架构设计](../docs/01-总体架构设计.md) - 系统架构详细说明
- [🔌 API 接口文档](../docs/03-API接口设计.md) - RESTful API 规范
- [🚀 部署指南](../docs/06-部署设计.md) - 完整部署方案

### 🛠️ 技术资源
- [Next.js 官方文档](https://nextjs.org/docs)
- [Ant Design 组件库](https://ant.design/components)
- [Tailwind CSS 文档](https://tailwindcss.com/docs)
- [Wagmi Web3 文档](https://wagmi.sh/)

## 🤝 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源 - 查看 [LICENSE](../../LICENSE) 文件了解详情。

## 📞 联系我们

- 项目地址：[GitHub Repository](https://github.com/your-org/pp-cex)
- 问题反馈：[Issues](https://github.com/your-org/pp-cex/issues)
- 邮箱：dev@cex.com

---

⭐ 如果这个项目对您有帮助，请给个 Star 支持一下！