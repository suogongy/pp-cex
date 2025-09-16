'use client';

import { useEffect, useRef } from 'react';

interface TradingViewWidgetProps {
  symbol: string;
  theme?: 'light' | 'dark';
  interval?: string;
  autosize?: boolean;
  height?: number;
  width?: number;
}

export function TradingViewWidget({
  symbol,
  theme = 'light',
  interval = '60',
  autosize = true,
  height = 500,
  width = undefined,
}: TradingViewWidgetProps) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const loadTradingViewScript = () => {
      return new Promise<void>((resolve, reject) => {
        if (window.TradingView) {
          resolve();
          return;
        }

        const script = document.createElement('script');
        script.src = 'https://s3.tradingview.com/tv.js';
        script.async = true;
        script.onload = () => resolve();
        script.onerror = () => reject(new Error('Failed to load TradingView script'));
        document.head.appendChild(script);
      });
    };

    const initWidget = async () => {
      try {
        await loadTradingViewScript();

        if (!containerRef.current || !window.TradingView) {
          return;
        }

        // Clear existing widget
        containerRef.current.innerHTML = '';

        new window.TradingView.widget({
          container_id: containerRef.current.id,
          symbol: symbol,
          interval: interval,
          timezone: 'Asia/Shanghai',
          theme: theme,
          style: '1',
          locale: 'zh_CN',
          toolbar_bg: '#f1f3f6',
          enable_publishing: false,
          allow_symbol_change: true,
          save_image: false,
          details: true,
          hotlist: true,
          calendar: true,
          show_popup_button: true,
          popup_width: '1000',
          popup_height: '650',
          studies: ['RSI@tv-basicstudies', 'MACD@tv-basicstudies', 'StochasticRSI@tv-basicstudies'],
          width: autosize ? '100%' : width,
          height: autosize ? '100%' : height,
          overrides: {
            'mainSeriesProperties.candleStyle.upColor': '#26a69a',
            'mainSeriesProperties.candleStyle.downColor': '#ef5350',
            'mainSeriesProperties.candleStyle.borderUpColor': '#26a69a',
            'mainSeriesProperties.candleStyle.borderDownColor': '#ef5350',
            'mainSeriesProperties.candleStyle.wickUpColor': '#26a69a',
            'mainSeriesProperties.candleStyle.wickDownColor': '#ef5350',
            'paneProperties.background': '#ffffff',
            'paneProperties.vertGridProperties.color': '#e0e0e0',
            'paneProperties.horzGridProperties.color': '#e0e0e0',
          },
          custom_css_url: '',
        });
      } catch (error) {
        console.error('Failed to initialize TradingView widget:', error);
      }
    };

    initWidget();

    return () => {
      if (containerRef.current) {
        containerRef.current.innerHTML = '';
      }
    };
  }, [symbol, theme, interval, autosize, height, width]);

  return (
    <div
      ref={containerRef}
      id={`tradingview_widget_${symbol}`}
      style={{
        height: autosize ? '100%' : `${height}px`,
        width: autosize ? '100%' : width ? `${width}px` : '100%',
      }}
      className="tradingview-widget-container"
    >
      <div className="tradingview-widget-copyright">
        <a
          href={`https://www.tradingview.com/symbols/${symbol}/`}
          rel="noopener noreferrer"
          target="_blank"
        >
          <span className="blue-text">{symbol} Chart</span>
        </a>{' '}
        by TradingView
      </div>
    </div>
  );
}