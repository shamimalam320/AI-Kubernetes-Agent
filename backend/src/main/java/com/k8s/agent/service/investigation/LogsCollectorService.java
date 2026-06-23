package com.k8s.agent.service.investigation;

import com.k8s.agent.dto.investigation.LogsCollectionResult;
import com.k8s.agent.dto.investigation.PodLogs;
import com.k8s.agent.dto.investigation.ProblematicPod;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for collecting logs from problematic Kubernetes pods.
 * Focuses on error detection and relevant log extraction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogsCollectorService {

    private final CoreV1Api coreV1Api;
    
    private static final int MAX_LOG_LINES = 100;
    private static final Pattern ERROR_PATTERN = Pattern.compile(
        "(?i)(error|exception|fatal|failed|panic|crash|oom|killed)",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Collects logs from a list of problematic pods.
     *
     * @param problematicPods list of pods to collect logs from
     * @return LogsCollectionResult containing collected logs
     */
    public LogsCollectionResult collectLogs(List<ProblematicPod> problematicPods) {
        log.info("Starting log collection for {} problematic pods", problematicPods.size());
        
        List<PodLogs> collectedLogs = new ArrayList<>();
        int podsWithErrors = 0;
        boolean hasCriticalErrors = false;
        
        for (ProblematicPod pod : problematicPods) {
            try {
                PodLogs podLogs = collectPodLogs(pod);
                if (podLogs != null) {
                    collectedLogs.add(podLogs);
                    if (podLogs.isHasErrors()) {
                        podsWithErrors++;
                        if (containsCriticalError(podLogs.getLogs())) {
                            hasCriticalErrors = true;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to collect logs for pod {}/{}: {}",
                    pod.getNamespace(), pod.getName(), e.getMessage());
            }
        }
        
        log.info("Log collection complete: {} pods checked, {} with error logs",
            problematicPods.size(), podsWithErrors);
        
        return LogsCollectionResult.builder()
            .podLogs(collectedLogs)
            .totalPodsChecked(problematicPods.size())
            .podsWithErrorLogs(podsWithErrors)
            .hasCriticalErrors(hasCriticalErrors)
            .build();
    }

    /**
     * Collects logs from a single pod.
     *
     * @param pod the problematic pod
     * @return PodLogs containing the collected logs
     */
    private PodLogs collectPodLogs(ProblematicPod pod) {
        try {
            log.debug("Collecting logs for pod {}/{}", pod.getNamespace(), pod.getName());
            
            // Get pod logs (last 100 lines)
            String logs = coreV1Api.readNamespacedPodLog(
                pod.getName(),
                pod.getNamespace(),
                null,           // container (null = first container)
                false,          // follow
                null,           // insecureSkipTLSVerifyBackend
                null,           // limitBytes
                "false",        // pretty
                false,          // previous
                null,           // sinceSeconds
                MAX_LOG_LINES,  // tailLines
                null            // timestamps
            );
            
            if (logs == null || logs.isEmpty()) {
                log.debug("No logs available for pod {}/{}", pod.getNamespace(), pod.getName());
                return null;
            }
            
            // Check if logs contain errors
            boolean hasErrors = ERROR_PATTERN.matcher(logs).find();
            int lineCount = logs.split("\n").length;
            
            // Extract error-focused logs if too large
            String processedLogs = logs;
            if (lineCount > MAX_LOG_LINES) {
                processedLogs = extractErrorLines(logs);
            }
            
            return PodLogs.builder()
                .podName(pod.getName())
                .namespace(pod.getNamespace())
                .containerName("default") // Could be enhanced to specify container
                .logs(processedLogs)
                .hasErrors(hasErrors)
                .lineCount(lineCount)
                .build();
                
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                log.debug("Pod {}/{} not found or no logs available",
                    pod.getNamespace(), pod.getName());
            } else {
                log.warn("Failed to read logs for pod {}/{}: {} - {}",
                    pod.getNamespace(), pod.getName(), e.getCode(), e.getMessage());
            }
            return null;
        }
    }

    /**
     * Extracts lines containing errors from logs.
     *
     * @param logs the full log content
     * @return filtered logs containing error lines
     */
    private String extractErrorLines(String logs) {
        StringBuilder errorLogs = new StringBuilder();
        String[] lines = logs.split("\n");
        int errorLineCount = 0;
        
        for (String line : lines) {
            if (ERROR_PATTERN.matcher(line).find()) {
                errorLogs.append(line).append("\n");
                errorLineCount++;
                if (errorLineCount >= MAX_LOG_LINES) {
                    break;
                }
            }
        }
        
        // If no error lines found, return last N lines
        if (errorLogs.length() == 0) {
            int startIndex = Math.max(0, lines.length - MAX_LOG_LINES);
            for (int i = startIndex; i < lines.length; i++) {
                errorLogs.append(lines[i]).append("\n");
            }
        }
        
        return errorLogs.toString();
    }

    /**
     * Checks if logs contain critical errors.
     *
     * @param logs the log content
     * @return true if critical errors found
     */
    private boolean containsCriticalError(String logs) {
        String lowerLogs = logs.toLowerCase();
        return lowerLogs.contains("fatal") ||
               lowerLogs.contains("panic") ||
               lowerLogs.contains("oom") ||
               lowerLogs.contains("killed") ||
               lowerLogs.contains("crash");
    }
}
