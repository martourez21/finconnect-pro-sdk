package com.codedstreams.finconnectpro.sdk.adapter;

import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.dto.*;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;

/**
 * Core interface for financial protocol adapters.
 * <p>
 * This interface defines the contract that all financial protocol adapters must implement.
 * Adapters are responsible for handling communication with specific financial networks
 * and protocols, providing a unified interface regardless of the underlying protocol.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see ConnectionConfigDto
 * @see FinancialMessageDto
 * @see TransactionRequestDto
 * @see TransactionResponseDto
 */
public interface FinancialAdapter {

    /**
     * Establishes a connection to the financial network using the provided configuration.
     *
     * @param config the connection configuration parameters
     * @throws FinancialConnectionException if the connection cannot be established
     */
    void connect(ConnectionConfigDto config);

    /**
     * Closes the connection to the financial network.
     * <p>
     * This method should perform a graceful disconnect, ensuring that any pending
     * messages are processed before closing the connection.
     * </p>
     */
    void disconnect();

    /**
     * Checks if the adapter is currently connected to the financial network.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Sends a transaction request to the financial network and returns the response.
     *
     * @param request the transaction request to send
     * @return the transaction response from the financial network
     * @throws TransactionFailedException if the transaction fails to process
     */
    TransactionResponseDto sendTransaction(TransactionRequestDto request);

    /**
     * Receives a message from the financial network.
     * <p>
     * This method should be non-blocking and return immediately if no message is available.
     * </p>
     *
     * @return the received financial message, or null if no message is available
     */
    FinancialMessageDto receiveMessage();

    /**
     * Sends a raw financial message to the financial network.
     *
     * @param message the financial message to send
     */
    void sendMessage(FinancialMessageDto message);

    /**
     * Performs a health check on the connection and returns status information.
     *
     * @return health check information including connection status and metrics
     */
    HealthCheckDto healthCheck();

    /**
     * Gets the protocol type that this adapter supports.
     *
     * @return the supported protocol type
     */
    ProtocolType getSupportedProtocol();
}
