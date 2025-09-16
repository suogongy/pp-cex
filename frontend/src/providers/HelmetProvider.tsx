'use client';

import { HelmetProvider } from 'react-helmet-async';

export function HelmetProvider({ children }: { children: React.ReactNode }) {
  return (
    <HelmetProvider>
      {children}
    </HelmetProvider>
  );
}