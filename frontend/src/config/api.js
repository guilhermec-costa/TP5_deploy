export const API_CONFIG = {
  BASE_URL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  ENDPOINTS: {
    ITEMS: '/api/items',
    HEALTH: '/api/health'
  }
};

export const API_URL = `${API_CONFIG.BASE_URL}/api/items`;
