package com.k8s.agent.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;

/**
 * Configuration class for Kubernetes Java Client.
 * Provides beans for ApiClient and Kubernetes API clients.
 */
@Configuration
@Slf4j
public class KubernetesClientConfig {

    @Value("${kubernetes.config-path}")
    private String kubeconfigPath;

    /**
     * Creates and configures the Kubernetes ApiClient.
     * Attempts to load from kubeconfig file first, falls back to in-cluster config.
     *
     * @return configured ApiClient
     * @throws IOException if configuration loading fails
     */
    @Bean
    public ApiClient apiClient() throws IOException {
        log.info("Initializing Kubernetes ApiClient");
        
        try {
            // Try to load from kubeconfig file
            log.debug("Attempting to load kubeconfig from: {}", kubeconfigPath);
            String expandedPath = kubeconfigPath.replace("~", System.getProperty("user.home"));
            
            try (FileReader reader = new FileReader(expandedPath)) {
                KubeConfig kubeConfig = KubeConfig.loadKubeConfig(reader);
                ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
                
                // Set reasonable timeouts
                client.setConnectTimeout(30000); // 30 seconds
                client.setReadTimeout(60000);    // 60 seconds
                
                log.info("Successfully loaded kubeconfig from file: {}", expandedPath);
                return client;
            }
        } catch (Exception e) {
            log.warn("Failed to load kubeconfig from file: {}. Attempting in-cluster config", e.getMessage());
            
            try {
                // Fall back to in-cluster configuration
                ApiClient client = ClientBuilder.cluster().build();
                client.setConnectTimeout(30000);
                client.setReadTimeout(60000);
                
                log.info("Successfully loaded in-cluster Kubernetes configuration");
                return client;
            } catch (Exception ex) {
                log.error("Failed to initialize Kubernetes client", ex);
                throw new IOException("Unable to initialize Kubernetes client: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * Creates CoreV1Api bean for interacting with core Kubernetes resources.
     * Used for pods, services, events, namespaces, etc.
     *
     * @param apiClient the configured ApiClient
     * @return CoreV1Api instance
     */
    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        log.debug("Creating CoreV1Api bean");
        return new CoreV1Api(apiClient);
    }

    /**
     * Creates AppsV1Api bean for interacting with application resources.
     * Used for deployments, statefulsets, daemonsets, etc.
     *
     * @param apiClient the configured ApiClient
     * @return AppsV1Api instance
     */
    @Bean
    public AppsV1Api appsV1Api(ApiClient apiClient) {
        log.debug("Creating AppsV1Api bean");
        return new AppsV1Api(apiClient);
    }
}

// Made with Bob
