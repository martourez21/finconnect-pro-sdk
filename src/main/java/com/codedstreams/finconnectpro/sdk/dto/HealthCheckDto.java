package com.codedstreams.finconnectpro.sdk.dto;

import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object for health check information.
 * <p>
 * This class encapsulates the health status and metrics of a financial network
 * connection. It is used for monitoring and diagnostics purposes.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class HealthCheckDto {

    private ProtocolType protocolType;
    private boolean connected;
    private LocalDateTime lastCheck;
    private String statusMessage;
    private Map<String, Object> metrics;
    private Long responseTimeMs;
    private Integer errorCount;
    private Integer successCount;

    /**
     * Default constructor.
     */
    public HealthCheckDto() {
        this.lastCheck = LocalDateTime.now();
        this.errorCount = 0;
        this.successCount = 0;
    }

    /**
     * Gets the protocol type being checked.
     *
     * @return the protocol type
     */
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    /**
     * Sets the protocol type being checked.
     *
     * @param protocolType the protocol type to set
     */
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * Checks if the connection is currently established.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Sets whether the connection is currently established.
     *
     * @param connected true if connected, false otherwise
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Gets the timestamp of the last health check.
     *
     * @return the last check timestamp
     */
    public LocalDateTime getLastCheck() {
        return lastCheck;
    }

    /**
     * Sets the timestamp of the last health check.
     *
     * @param lastCheck the last check timestamp to set
     */
    public void setLastCheck(LocalDateTime lastCheck) {
        this.lastCheck = lastCheck;
    }

    /**
     * Gets a descriptive status message.
     *
     * @return the status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Sets a descriptive status message.
     *
     * @param statusMessage the status message to set
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Gets protocol-specific metrics and statistics.
     *
     * @return a map of metrics
     */
    public Map<String, Object> getMetrics() {
        return metrics;
    }

    /**
     * Sets protocol-specific metrics and statistics.
     *
     * @param metrics a map of metrics to set
     */
    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    /**
     * Gets the response time in milliseconds for the last health check.
     *
     * @return the response time in milliseconds
     */
    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    /**
     * Sets the response time in milliseconds for the last health check.
     *
     * @param responseTimeMs the response time to set
     */
    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    /**
     * Gets the count of errors encountered since the last reset.
     *
     * @return the error count
     */
    public Integer getErrorCount() {
        return errorCount;
    }

    /**
     * Sets the count of errors encountered since the last reset.
     *
     * @param errorCount the error count to set
     */
    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    /**
     * Gets the count of successful operations since the last reset.
     *
     * @return the success count
     */
    public Integer getSuccessCount() {
        return successCount;
    }

    /**
     * Sets the count of successful operations since the last reset.
     *
     * @param successCount the success count to set
     */
    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    /**
     * Increments the error count by one.
     */
    public void incrementErrorCount() {
        this.errorCount++;
    }

    /**
     * Increments the success count by one.
     */
    public void incrementSuccessCount() {
        this.successCount++;
    }

    /**
     * Calculates the success rate as a percentage.
     *
     * @return the success rate percentage (0-100), or 0 if no operations have been performed
     */
    public double getSuccessRate() {
        int total = errorCount + successCount;
        if (total == 0) {
            return 0.0;
        }
        return (successCount * 100.0) / total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthCheckDto that = (HealthCheckDto) o;
        return connected == that.connected &&
                protocolType == that.protocolType &&
                Objects.equals(lastCheck, that.lastCheck);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolType, connected, lastCheck);
    }

    @Override
    public String toString() {
        return "HealthCheckDto{" +
                "protocolType=" + protocolType +
                ", connected=" + connected +
                ", lastCheck=" + lastCheck +
                ", statusMessage='" + statusMessage + '\'' +
                ", successRate=" + getSuccessRate() + "%" +
                '}';
    }
}
