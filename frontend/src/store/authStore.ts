import { create } from 'zustand';
import { authService } from '../services/auth';

interface AuthState {
  token: string | null;
  username: string | null;
  email: string | null;
  isAuthenticated: boolean;
  login: (token: string, username: string, email: string) => void;
  logout: () => void;
  initialize: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  username: null,
  email: null,
  isAuthenticated: false,
  
  initialize: () => {
    const token = authService.getToken();
    const username = authService.getUsername();
    const email = authService.getEmail();
    
    if (token && username) {
      set({ 
        token, 
        username, 
        email, 
        isAuthenticated: true 
      });
    }
  },
  
  login: (token, username, email) => {
    set({ token, username, email, isAuthenticated: true });
  },
  
  logout: () => {
    authService.logout();
    set({ token: null, username: null, email: null, isAuthenticated: false });
  },
}));

// Initialize auth state from localStorage on app start
useAuthStore.getState().initialize();

// Made with Bob
