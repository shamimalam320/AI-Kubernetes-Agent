import axios, { AxiosInstance } from 'axios';
import type { ApiResponse, InvestigationResponse, Investigation } from '../types';
import { authService } from './auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Create axios instance with default config
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
apiClient.interceptors.request.use((config) => {
  const token = authService.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle auth errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      authService.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const apiService = {
  /**
   * Trigger investigation with AI diagnosis.
   */
  investigate: async (): Promise<InvestigationResponse> => {
    const response = await apiClient.post<ApiResponse<InvestigationResponse>>(
      '/api/v1/investigate'
    );
    return response.data.data!;
  },

  /**
   * Get investigation history for current user.
   */
  getHistory: async (): Promise<Investigation[]> => {
    const response = await apiClient.get<ApiResponse<Investigation[]>>(
      '/api/v1/investigate/history'
    );
    return response.data.data || [];
  },

  /**
   * Get specific investigation by ID.
   */
  getInvestigation: async (id: string): Promise<Investigation> => {
    const response = await apiClient.get<ApiResponse<Investigation>>(
      `/api/v1/investigate/history/${id}`
    );
    return response.data.data!;
  },

  /**
   * Health check endpoint.
   */
  healthCheck: async (): Promise<string> => {
    const response = await apiClient.get<ApiResponse<string>>(
      '/api/v1/investigate/health'
    );
    return response.data.data || 'OK';
  },
};

// Made with Bob
