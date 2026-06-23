package com.k8s.agent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration.
 * Enables real-time communication for investigation progress updates.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Configure message broker for pub/sub messaging.
     * - /topic: Broadcast messages to all subscribers
     * - /app: Application destination prefix for client messages
     *
     * @param config Message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    /**
     * Register STOMP endpoints for WebSocket connections.
     * - /ws: WebSocket endpoint
     * - SockJS fallback for browsers that don't support WebSocket
     *
     * @param registry STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173"
                )
                .withSockJS();
    }
}

// Made with Bob
