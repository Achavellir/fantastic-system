export const environment = {
  production: true,
  apiUrl: 'https://your-domain.com/api', // Update this with your production API URL
  appName: 'SAMAP',
  appVersion: '1.0.0',
  appDescription: 'Secure Access Management & Audit Platform',
  features: {
    enableNotifications: true,
    enableRealTimeUpdates: true,
    enableAdvancedAnalytics: true,
    enableExport: true,
    enableAuditRetention: true
  },
  security: {
    tokenRefreshThreshold: 300000, // 5 minutes in milliseconds
    sessionTimeout: 3600000, // 1 hour in milliseconds
    maxLoginAttempts: 5,
    lockoutDuration: 1800000 // 30 minutes in milliseconds
  },
  ui: {
    defaultPageSize: 10,
    maxPageSize: 100,
    defaultTheme: 'light',
    enableDarkMode: true,
    animationDuration: 300
  },
  monitoring: {
    enableErrorTracking: true,
    enablePerformanceMonitoring: true,
    enableUserAnalytics: false // Disabled for privacy
  }
};
