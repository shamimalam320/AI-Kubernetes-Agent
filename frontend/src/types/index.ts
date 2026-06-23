// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}

// Authentication Types
export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: {
    id: string;
    email: string;
  };
}

// Investigation Types
export interface Investigation {
  id: string;
  userId: string;
  rootCause: string;
  explanation: string;
  suggestedFix: string;
  confidence: number;
  status: string;
  investigationData: string;
  createdAt: string;
}

export interface DiagnosisResult {
  root_cause: string;
  explanation: string;
  suggested_fix: string;
  kubectl_commands: string[];
  prevention: string;
  confidence: number;
  warning?: string;
}

export interface InvestigationResponse {
  investigation: any;
  diagnosis: DiagnosisResult;
  status: string;
  timestamp: string;
}

// WebSocket Types
export interface ProgressUpdate {
  step: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  timestamp: string;
  message?: string;
}

// Made with Bob
