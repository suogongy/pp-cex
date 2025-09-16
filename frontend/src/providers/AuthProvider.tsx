'use client';

import { useAuth } from '@/hooks/useAuth';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  // This is a wrapper component that provides auth context
  // The actual AuthProvider is implemented in useAuth.ts
  return <>{children}</>;
}