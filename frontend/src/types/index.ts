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

// API Response wrapper
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}

// Pod Investigation Types
export interface ProblematicPod {
  name: string;
  namespace: string;
  status: string;
  reason: string;
  message: string;
  restartCount: number;
  containerStatuses: string[];
}

export interface PodInspectionResult {
  healthy: boolean;
  problematicPods: ProblematicPod[];
  totalPods: number;
  healthyPods: number;
  problematicPodsCount: number;
}

// Logs Collection Types
export interface PodLogs {
  podName: string;
  namespace: string;
  containerName: string;
  logs: string;
  errorPatterns: string[];
  hasCriticalErrors: boolean;
}

export interface LogsCollectionResult {
  podLogs: PodLogs[];
  totalPodsChecked: number;
  podsWithErrorLogs: number;
}

// Events Analysis Types
export interface KubernetesEvent {
  name: string;
  namespace: string;
  type: string;
  reason: string;
  message: string;
  involvedObjectKind: string;
  involvedObjectName: string;
  count: number;
  firstTimestamp: string;
  lastTimestamp: string;
}

export interface EventsAnalysisResult {
  healthy: boolean;
  criticalEvents: KubernetesEvent[];
  totalEvents: number;
  criticalEventsCount: number;
  mostCommonReason: string;
}

// Deployment Inspection Types
export interface ProblematicDeployment {
  name: string;
  namespace: string;
  desiredReplicas: number;
  availableReplicas: number;
  unavailableReplicas: number;
  status: string;
  reason: string;
  message: string;
}

export interface DeploymentInspectionResult {
  healthy: boolean;
  problematicDeployments: ProblematicDeployment[];
  totalDeployments: number;
  healthyDeployments: number;
  problematicDeploymentsCount: number;
}

// Network Inspection Types
export interface ServiceIssue {
  serviceName: string;
  namespace: string;
  serviceType: string;
  issueType: string;
  description: string;
  selector: Record<string, string> | null;
}

export interface NetworkInspectionResult {
  healthy: boolean;
  serviceIssues: ServiceIssue[];
  totalServices: number;
  healthyServices: number;
  servicesWithIssues: number;
}

// Main Investigation Result
export interface InvestigationResult {
  timestamp: string;
  clusterHealthy: boolean;
  podInspection?: PodInspectionResult;
  logsCollection?: LogsCollectionResult;
  eventsAnalysis?: EventsAnalysisResult;
  deploymentInspection?: DeploymentInspectionResult;
  networkInspection?: NetworkInspectionResult;
  investigationDurationSeconds: number;
}

