import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../services/api';
import { useAuthStore } from '../store/authStore';

/**
 * Main dashboard component for the AI Kubernetes Agent
 */
export default function Dashboard() {
  const navigate = useNavigate();
  const { username, email, logout } = useAuthStore();
  const [investigating, setInvestigating] = useState(false);

  // Query backend health status
  const { data: health, isLoading } = useQuery({
    queryKey: ['health'],
    queryFn: apiService.healthCheck,
    refetchInterval: 30000, // Refresh every 30 seconds
  });

  const handleInvestigate = async () => {
    setInvestigating(true);
    try {
      const response = await apiService.investigate();
      console.log('Investigation complete:', response);
    } catch (error) {
      console.error('Investigation failed:', error);
    } finally {
      setInvestigating(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-blue-900 to-gray-900 flex items-center justify-center p-4">
      <div className="max-w-2xl w-full">
        <div className="bg-white/10 backdrop-blur-lg rounded-2xl shadow-2xl p-8 border border-white/20">
          {/* Header with User Info */}
          <div className="flex justify-between items-start mb-8">
            <div className="text-center flex-1">
              <h1 className="text-4xl font-bold text-white mb-2">
                AI Kubernetes Agent
              </h1>
              <p className="text-gray-300 text-lg">
                Troubleshoot Kubernetes with AI
              </p>
            </div>
            <div className="text-right">
              <div className="text-sm text-gray-300 mb-2">
                <div className="font-semibold">{username}</div>
                <div className="text-gray-400 text-xs">{email}</div>
              </div>
              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg transition"
              >
                Logout
              </button>
            </div>
          </div>

          {/* Status */}
          <div className="mb-8 p-4 bg-white/5 rounded-lg border border-white/10">
            <div className="flex items-center justify-between">
              <span className="text-gray-300">System Status:</span>
              {isLoading ? (
                <span className="text-yellow-400">Checking...</span>
              ) : health === 'OK' ? (
                <span className="text-green-400 flex items-center">
                  <span className="w-2 h-2 bg-green-400 rounded-full mr-2 animate-pulse"></span>
                  Ready
                </span>
              ) : (
                <span className="text-red-400">Offline</span>
              )}
            </div>
          </div>

          {/* Action Button */}
          <div className="text-center">
            <button
              onClick={handleInvestigate}
              disabled={investigating || !health}
              className="px-8 py-4 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-semibold rounded-lg shadow-lg transform transition hover:scale-105 disabled:hover:scale-100"
            >
              {investigating ? (
                <span className="flex items-center">
                  <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Investigating...
                </span>
              ) : (
                'Investigate Cluster'
              )}
            </button>
          </div>

          {/* Info */}
          <div className="mt-8 text-center text-sm text-gray-400">
            <p>Click the button to start AI-powered Kubernetes troubleshooting</p>
          </div>
        </div>
      </div>
    </div>
  );
}

// Made with Bob
