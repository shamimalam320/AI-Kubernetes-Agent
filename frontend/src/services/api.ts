import axios from 'axios';
import type { HealthResponse, InvestigationResult, ApiResponse } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Check backend health status
 */
export const checkHealth = async (): Promise<HealthResponse> => {
  const response = await apiClient.get<HealthResponse>('/api/v1/health');
  return response.data;
};

/**
 * Perform comprehensive Kubernetes cluster investigation
 */
export const investigateCluster = async (): Promise<ApiResponse<InvestigationResult>> => {
  const response = await apiClient.post<ApiResponse<InvestigationResult>>('/api/v1/investigate');
  return response.data;
};

/**
 * Perform quick health check of the cluster
 */
export const quickHealthCheck = async (): Promise<ApiResponse<InvestigationResult>> => {
  const response = await apiClient.get<ApiResponse<InvestigationResult>>('/api/v1/investigate/quick');
  return response.data;
};

