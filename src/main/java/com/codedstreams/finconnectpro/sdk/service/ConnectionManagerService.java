package com.codedstreams.finconnectpro.sdk.service;

import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.dto.ConnectionConfigDto;
import com.codedstreams.finconnectpro.sdk.dto.HealthCheckDto;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;

import java.util.Map;

/**
 * Service interface for managing financial network connections.
 * <p>
 * This service provides functionality to create, manage, and monitor connections
 * to various financial networks using the appropriate protocol adapters.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see FinancialAdapter
 * @see ConnectionConfigDto
 */
public interface ConnectionManagerService {

    /**
     * Creates a new connection to a financial network using the specified configuration.
     *
     * @param config the connection configuration parameters
     * @return a unique connection identifier for the established connection
     * @throws FinancialConnectionException if the connection cannot be established
     */
    String createConnection(ConnectionConfigDto config);

    /**
     * Closes an existing connection identified by the connection ID.
     *
     * @param connectionId the unique identifier of the connection to close
     * @throws FinancialConnectionException if the connection cannot be closed
     */
    void closeConnection(String connectionId);

    /**
     * Retrieves an adapter for an existing connection.
     *
     * @param connectionId the unique connection identifier
     * @return the financial adapter for the connection
     * @throws FinancialConnectionException if the connection does not exist
     */
    FinancialAdapter getAdapter(String connectionId);

    /**
     * Checks the health of a specific connection.
     *
     * @param connectionId the unique connection identifier
     * @return health check information for the connection
     * @throws FinancialConnectionException if the connection does not exist
     */
    HealthCheckDto checkConnectionHealth(String connectionId);

    /**
     * Checks the health of all active connections.
     *
     * @return a map of connection IDs to their health check information
     */
    Map<String, HealthCheckDto> checkAllConnections();

    /**
     * Gets the status of all managed connections.
     *
     * @return a map of connection IDs to their current status
     */
    Map<String, String> getConnectionStatuses();

    /**
     * Finds the best available connection for a given protocol type.
     *
     * @param protocolType the protocol type to find a connection for
     * @return the connection identifier, or null if no suitable connection exists
     */
    String findBestConnection(ProtocolType protocolType);
}
