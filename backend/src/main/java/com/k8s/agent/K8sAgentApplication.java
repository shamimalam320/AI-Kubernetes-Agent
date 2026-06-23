package com.k8s.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for AI Kubernetes Agent
 * 
 * This application provides AI-powered Kubernetes troubleshooting capabilities
 * by analyzing cluster state, logs, and events to identify root causes and
 * suggest fixes for common Kubernetes issues.
 */
@SpringBootApplication
public class K8sAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(K8sAgentApplication.class, args);
    }
}

