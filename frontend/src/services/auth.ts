import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  message: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
}

export const authService = {
  /**
   * Register a new user.
   */
  register: async (data: RegisterData): Promise<AuthResponse> => {
    const response = await axios.post<ApiResponse<AuthResponse>>(
      `${API_BASE_URL}/api/v1/auth/register`,
      data
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.message || 'Registration failed');
    }
    
    const authData = response.data.data;
    
    // Store token and user info
    localStorage.setItem('token', authData.token);
    localStorage.setItem('username', authData.username);
    localStorage.setItem('email', authData.email);
    
    return authData;
  },

  /**
   * Login with username and password.
   */
  login: async (credentials: LoginCredentials): Promise<AuthResponse> => {
    const response = await axios.post<ApiResponse<AuthResponse>>(
      `${API_BASE_URL}/api/v1/auth/login`,
      credentials
    );
    
    if (!response.data.success || !response.data.data) {
      throw new Error(response.data.message || 'Login failed');
    }
    
    const authData = response.data.data;
    
    // Store token and user info
    localStorage.setItem('token', authData.token);
    localStorage.setItem('username', authData.username);
    localStorage.setItem('email', authData.email);
    
    return authData;
  },

  /**
   * Logout and clear local storage.
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
  },

  /**
   * Get stored JWT token.
   */
  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  /**
   * Set JWT token in local storage.
   */
  setToken: (token: string) => {
    localStorage.setItem('token', token);
  },

  /**
   * Get stored username.
   */
  getUsername: (): string | null => {
    return localStorage.getItem('username');
  },

  /**
   * Get stored email.
   */
  getEmail: (): string | null => {
    return localStorage.getItem('email');
  },

  /**
   * Check if user is authenticated.
   */
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  },

  /**
   * Validate JWT token with backend.
   */
  validateToken: async (): Promise<boolean> => {
    try {
      const token = authService.getToken();
      if (!token) return false;

      const response = await axios.get<ApiResponse<string>>(
        `${API_BASE_URL}/api/v1/auth/validate`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      return response.data.success;
    } catch (error) {
      return false;
    }
  },
};

// Made with Bob
