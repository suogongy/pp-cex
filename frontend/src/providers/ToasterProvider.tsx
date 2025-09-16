'use client';

import { Toaster } from 'react-hot-toast';

export function ToasterProvider({ children }: { children: React.ReactNode }) {
  return (
    <>
      {children}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#363636',
            color: '#fff',
          },
          success: {
            duration: 3000,
            theme: {
              primary: '#4aed88',
              secondary: '#1a202c',
            },
          },
          error: {
            duration: 5000,
            theme: {
              primary: '#f87171',
              secondary: '#1a202c',
            },
          },
          loading: {
            duration: Infinity,
            theme: {
              primary: '#3b82f6',
              secondary: '#1a202c',
            },
          },
        }}
      />
    </>
  );
}