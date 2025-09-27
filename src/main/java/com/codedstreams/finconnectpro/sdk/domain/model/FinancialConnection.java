package com.codedstreams.finconnectpro.sdk.domain.model;

import com.codedstreams.finconnectpro.sdk.domain.enums.ConnectionStatus;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a connection to a financial network.
 * <p>
 * This class encapsulates all the information about a connection to a specific
 * financial network protocol, including its current status, configuration, and metrics.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class FinancialConnection {

    private String connectionId;
    private ProtocolType protocolType;
    private ConnectionStatus status;
    private LocalDateTime connectedAt;
    private LocalDateTime lastActivity;
    private Map<String, Object> configuration;
    private Integer messageCount;
    private String sessionId;

    /**
     * Default constructor.
     */
    public FinancialConnection() {
        this.messageCount = 0;
        this.status = ConnectionStatus.DISCONNECTED;
    }

    /**
     * Constructs a new FinancialConnection with the specified parameters.
     *
     * @param connectionId the unique identifier for this connection
     * @param protocolType the protocol type for this connection
     * @param configuration the configuration parameters for the connection
     */
    public FinancialConnection(String connectionId, ProtocolType protocolType,
                               Map<String, Object> configuration) {
        this();
        this.connectionId = connectionId;
        this.protocolType = protocolType;
        this.configuration = configuration;
    }

    /**
     * Gets the unique connection identifier.
     *
     * @return the connection ID
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Sets the unique connection identifier.
     *
     * @param connectionId the connection ID to set
     */
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Gets the protocol type for this connection.
     *
     * @return the protocol type
     */
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    /**
     * Sets the protocol type for this connection.
     *
     * @param protocolType the protocol type to set
     */
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * Gets the current connection status.
     *
     * @return the connection status
     */
    public ConnectionStatus getStatus() {
        return status;
    }

    /**
     * Sets the current connection status.
     *
     * @param status the connection status to set
     */
    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    /**
     * Gets the timestamp when the connection was established.
     *
     * @return the connection timestamp, or null if not connected
     */
    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    /**
     * Sets the timestamp when the connection was established.
     *
     * @param connectedAt the connection timestamp to set
     */
    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    /**
     * Gets the timestamp of the last activity on this connection.
     *
     * @return the last activity timestamp
     */
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    /**
     * Sets the timestamp of the last activity on this connection.
     *
     * @param lastActivity the last activity timestamp to set
     */
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    /**
     * Gets the configuration parameters for this connection.
     *
     * @return the configuration map
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration parameters for this connection.
     *
     * @param configuration the configuration map to set
     */
    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the total number of messages processed through this connection.
     *
     * @return the message count
     */
    public Integer getMessageCount() {
        return messageCount;
    }

    /**
     * Sets the total number of messages processed through this connection.
     *
     * @param messageCount the message count to set
     */
    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * Gets the session identifier for this connection.
     *
     * @return the session ID, or null if no session is active
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session identifier for this connection.
     *
     * @param sessionId the session ID to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Increments the message count by one and updates the last activity timestamp.
     */
    public void incrementMessageCount() {
        this.messageCount++;
        this.lastActivity = LocalDateTime.now();
    }

    /**
     * Checks if the connection is currently active.
     *
     * @return true if the connection is connected, false otherwise
     */
    public boolean isConnected() {
        return status == ConnectionStatus.CONNECTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinancialConnection that = (FinancialConnection) o;
        return Objects.equals(connectionId, that.connectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionId);
    }

    @Override
    public String toString() {
        return "FinancialConnection{" +
                "connectionId='" + connectionId + '\'' +
                ", protocolType=" + protocolType +
                ", status=" + status +
                ", connectedAt=" + connectedAt +
                ", messageCount=" + messageCount +
                '}';
    }
}
