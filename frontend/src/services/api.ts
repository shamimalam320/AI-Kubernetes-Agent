import axios from 'axios';
import type { HealthResponse } from '../types';

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
 * Investigate Kubernetes cluster
 * TODO: Implement in prompt 02
 */
export const investigateCluster = async () => {
  // TODO: Implement API call
  console.log('Investigation API - to be implemented');
  return { status: 'pending', message: 'Not implemented yet' };
};

