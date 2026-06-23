import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { ProgressUpdate } from '../types';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

class WebSocketService {
  private client: Client | null = null;
  private connected: boolean = false;

  /**
   * Connect to WebSocket server and subscribe to progress updates.
   */
  connect(userId: string, onProgress: (update: ProgressUpdate) => void): void {
    if (this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL) as any,
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log('WebSocket connected');
      this.connected = true;

      // Subscribe to user-specific progress updates
      this.client?.subscribe(
        `/user/${userId}/topic/investigation-progress`,
        (message) => {
          try {
            const update: ProgressUpdate = JSON.parse(message.body);
            onProgress(update);
          } catch (error) {
            console.error('Failed to parse progress update:', error);
          }
        }
      );
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
    };

    this.client.onWebSocketClose = () => {
      console.log('WebSocket closed');
      this.connected = false;
    };

    this.client.activate();
  }

  /**
   * Disconnect from WebSocket server.
   */
  disconnect(): void {
    if (this.client && this.connected) {
      this.client.deactivate();
      this.connected = false;
      console.log('WebSocket disconnected');
    }
  }

  /**
   * Check if WebSocket is connected.
   */
  isConnected(): boolean {
    return this.connected;
  }
}

export const websocketService = new WebSocketService();

// Made with Bob
