package com.codedstreams.finconnectpro.sdk.service.handler;

import com.codedstreams.finconnectpro.sdk.adapter.AdapterFactory;
import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.dto.ConnectionConfigDto;
import com.codedstreams.finconnectpro.sdk.dto.HealthCheckDto;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.service.ConnectionManagerService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/**
 * Implementation of the ConnectionManagerService interface.
 * <p>
 * This service manages the lifecycle of financial network connections, including
 * creation, maintenance, and cleanup of connections to various financial protocols.
 * It uses a concurrent map to safely manage connections in multi-threaded environments.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see ConnectionManagerService
 * @see FinancialAdapter
 */
@Service
public class ConnectionManagerServiceImpl implements ConnectionManagerService {

    private final Map<String, FinancialAdapter> activeConnections;
    private final Map<String, ConnectionConfigDto> connectionConfigs;
    private final AdapterFactory adapterFactory;

    /**
     * Constructs a new ConnectionManagerServiceImpl with the specified adapter factory.
     *
     * @param adapterFactory the adapter factory to use for creating adapters
     */
    public ConnectionManagerServiceImpl(AdapterFactory adapterFactory) {
        this.activeConnections = new ConcurrentHashMap<>();
        this.connectionConfigs = new ConcurrentHashMap<>();
        this.adapterFactory = adapterFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createConnection(ConnectionConfigDto config) {
        validateConnectionConfig(config);

        try {
            FinancialAdapter adapter = adapterFactory.createAdapter(config.getProtocolType());
            adapter.connect(config);

            String connectionId = generateConnectionId();
            activeConnections.put(connectionId, adapter);
            connectionConfigs.put(connectionId, config);

            return connectionId;

        } catch (Exception e) {
            throw new FinancialConnectionException(
                    "Failed to create connection for protocol: " + config.getProtocolType(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection(String connectionId) {
        FinancialAdapter adapter = activeConnections.get(connectionId);
        if (adapter == null) {
            throw new FinancialConnectionException("Connection not found: " + connectionId);
        }

        try {
            adapter.disconnect();
            activeConnections.remove(connectionId);
            connectionConfigs.remove(connectionId);
        } catch (Exception e) {
            throw new FinancialConnectionException(
                    "Failed to close connection: " + connectionId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FinancialAdapter getAdapter(String connectionId) {
        FinancialAdapter adapter = activeConnections.get(connectionId);
        if (adapter == null) {
            throw new FinancialConnectionException("Connection not found: " + connectionId);
        }
        return adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HealthCheckDto checkConnectionHealth(String connectionId) {
        FinancialAdapter adapter = getAdapter(connectionId);
        return adapter.healthCheck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, HealthCheckDto> checkAllConnections() {
        Map<String, HealthCheckDto> healthChecks = new ConcurrentHashMap<>();

        for (Map.Entry<String, FinancialAdapter> entry : activeConnections.entrySet()) {
            try {
                HealthCheckDto health = entry.getValue().healthCheck();
                healthChecks.put(entry.getKey(), health);
            } catch (Exception e) {
                HealthCheckDto errorHealth = new HealthCheckDto();
                errorHealth.setConnected(false);
                errorHealth.setStatusMessage("Health check failed: " + e.getMessage());
                healthChecks.put(entry.getKey(), errorHealth);
            }
        }

        return healthChecks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getConnectionStatuses() {
        Map<String, String> statuses = new ConcurrentHashMap<>();

        for (Map.Entry<String, FinancialAdapter> entry : activeConnections.entrySet()) {
            statuses.put(entry.getKey(),
                    entry.getValue().isConnected() ? "CONNECTED" : "DISCONNECTED");
        }

        return statuses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String findBestConnection(ProtocolType protocolType) {
        return activeConnections.entrySet().stream()
                .filter(entry -> entry.getValue().getSupportedProtocol() == protocolType)
                .filter(entry -> entry.getValue().isConnected())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates the connection configuration parameters.
     *
     * @param config the connection configuration to validate
     * @throws IllegalArgumentException if the configuration is invalid
     */
    private void validateConnectionConfig(ConnectionConfigDto config) {
        if (config == null) {
            throw new IllegalArgumentException("Connection configuration cannot be null");
        }
        if (config.getProtocolType() == null) {
            throw new IllegalArgumentException("Protocol type cannot be null");
        }
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (config.getPort() == null || config.getPort() <= 0) {
            throw new IllegalArgumentException("Port must be a positive number");
        }
    }

    /**
     * Generates a unique connection identifier.
     *
     * @return a unique connection ID string
     */
    private String generateConnectionId() {
        return "conn-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Gets the number of active connections managed by this service.
     *
     * @return the count of active connections
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * Closes all active connections.
     * <p>
     * This method should be called during application shutdown to ensure
     * all connections are properly closed.
     * </p>
     */
    public void closeAllConnections() {
        for (String connectionId : activeConnections.keySet()) {
            try {
                closeConnection(connectionId);
            } catch (Exception e) {
                // Log error but continue closing other connections
                System.err.println("Error closing connection " + connectionId + ": " + e.getMessage());
            }
        }
    }
}
