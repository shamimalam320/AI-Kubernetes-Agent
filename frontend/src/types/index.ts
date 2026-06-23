/**
 * Type definitions for the AI Kubernetes Agent
 */

export interface HealthResponse {
  status: string;
  service: string;
  timestamp: string;
}

export interface InvestigationRequest {
  namespace?: string;
  podName?: string;
}

export interface InvestigationResult {
  // TODO: Define in prompt 02
  status: string;
  message: string;
}

