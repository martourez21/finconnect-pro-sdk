package com.codedstreams.finconnectpro.sdk.facade;

import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.dto.*;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;
import com.codedstreams.finconnectpro.sdk.service.ConnectionManagerService;
import com.codedstreams.finconnectpro.sdk.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Main facade class for the FinConnect Pro SDK.
 * <p>
 * This class provides a simplified, high-level interface for all financial network
 * operations. It encapsulates the complexity of protocol handling, connection
 * management, and transaction processing behind a clean API.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see ConnectionManagerService
 * @see TransactionService
 */
@Service
public class FinancialNetworkFacade {

    private final ConnectionManagerService connectionManager;
    private final TransactionService transactionService;

    /**
     * Constructs a new FinancialNetworkFacade with the required services.
     *
     * @param connectionManager the connection manager service
     * @param transactionService the transaction service
     */
    public FinancialNetworkFacade(ConnectionManagerService connectionManager,
                                  TransactionService transactionService) {
        this.connectionManager = connectionManager;
        this.transactionService = transactionService;
    }

    /**
     * Establishes a connection to a financial network using the specified configuration.
     *
     * @param config the connection configuration parameters
     * @return a unique connection identifier for the established connection
     * @throws FinancialConnectionException if the connection cannot be established
     */
    public String establishConnection(ConnectionConfigDto config) {
        return connectionManager.createConnection(config);
    }

    /**
     * Closes an existing connection to a financial network.
     *
     * @param connectionId the unique identifier of the connection to close
     * @throws FinancialConnectionException if the connection cannot be closed
     */
    public void closeConnection(String connectionId) {
        connectionManager.closeConnection(connectionId);
    }

    /**
     * Executes a payment transaction using the most appropriate protocol.
     *
     * @param request the payment transaction request
     * @return the transaction response from the financial network
     * @throws TransactionFailedException if the transaction fails to process
     */
    public TransactionResponseDto executePayment(TransactionRequestDto request) {
        return transactionService.processTransaction(request);
    }

    /**
     * Executes a payment transaction using a specific connection.
     *
     * @param connectionId the connection identifier to use
     * @param request the payment transaction request
     * @return the transaction response from the financial network
     * @throws TransactionFailedException if the transaction fails to process
     */
    public TransactionResponseDto executePayment(String connectionId, TransactionRequestDto request) {
        return transactionService.processTransaction(connectionId, request);
    }

    /**
     * Executes a payment transaction using a specific protocol type.
     *
     * @param protocolType the protocol type to use
     * @param request the payment transaction request
     * @return the transaction response from the financial network
     * @throws TransactionFailedException if the transaction fails to process
     */
    public TransactionResponseDto executePayment(ProtocolType protocolType, TransactionRequestDto request) {
        return transactionService.processTransaction(protocolType, request);
    }

    /**
     * Sends a raw financial message through a specific connection.
     *
     * @param connectionId the connection identifier to use
     * @param message the financial message to send
     */
    public void sendMessage(String connectionId, FinancialMessageDto message) {
        connectionManager.getAdapter(connectionId).sendMessage(message);
    }

    /**
     * Receives a financial message from a specific connection.
     *
     * @param connectionId the connection identifier to use
     * @return the received financial message, or null if no message is available
     */
    public FinancialMessageDto receiveMessage(String connectionId) {
        return connectionManager.getAdapter(connectionId).receiveMessage();
    }

    /**
     * Checks the health of a specific connection.
     *
     * @param connectionId the unique connection identifier
     * @return health check information for the connection
     */
    public HealthCheckDto checkConnectionHealth(String connectionId) {
        return connectionManager.checkConnectionHealth(connectionId);
    }

    /**
     * Checks the health of all active connections.
     *
     * @return a map of connection IDs to their health check information
     */
    public Map<String, HealthCheckDto> getSystemHealth() {
        return connectionManager.checkAllConnections();
    }

    /**
     * Gets the status of all managed connections.
     *
     * @return a map of connection IDs to their current status
     */
    public Map<String, String> getConnectionStatuses() {
        return connectionManager.getConnectionStatuses();
    }

    /**
     * Performs a quick health check of the entire system.
     *
     * @return a summary health check containing overall system status
     */
    public HealthCheckDto getOverallSystemHealth() {
        Map<String, HealthCheckDto> allHealthChecks = getSystemHealth();

        HealthCheckDto summary = new HealthCheckDto();
        summary.setProtocolType(ProtocolType.REST_API); // Default for summary
        summary.setConnected(!allHealthChecks.isEmpty());
        summary.setStatusMessage("System health check completed");

        // Calculate overall metrics
        long connectedCount = allHealthChecks.values().stream()
                .filter(HealthCheckDto::isConnected)
                .count();

        summary.setMetrics(Map.of(
                "totalConnections", allHealthChecks.size(),
                "connectedConnections", connectedCount,
                "successRate", calculateOverallSuccessRate(allHealthChecks)
        ));

        return summary;
    }

    /**
     * Validates a transaction request before processing.
     *
     * @param request the transaction request to validate
     * @return true if the request is valid, false otherwise
     */
    public boolean validateTransaction(TransactionRequestDto request) {
        return transactionService.validateTransactionRequest(request);
    }

    /**
     * Determines the recommended protocol for a transaction based on its characteristics.
     *
     * @param request the transaction request to analyze
     * @return the recommended protocol type
     */
    public ProtocolType recommendProtocol(TransactionRequestDto request) {
        return transactionService.determineProtocolForTransaction(request);
    }

    /**
     * Creates a standardized payment request with common parameters.
     *
     * @param fromAccount the source account number
     * @param toAccount the beneficiary account number
     * @param amount the transaction amount
     * @param currency the currency code (ISO 4217)
     * @param description the transaction description
     * @return a pre-configured TransactionRequestDto
     */
    public TransactionRequestDto createPaymentRequest(String fromAccount, String toAccount,
                                                      BigDecimal amount, String currency,
                                                      String description) {
        TransactionRequestDto request = new TransactionRequestDto();
        request.setAccountNumber(fromAccount);
        request.setBeneficiaryAccount(toAccount);
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setDescription(description);
        request.setTransactionType("PAYMENT");
        return request;
    }

    /**
     * Calculates the overall success rate from all health checks.
     *
     * @param healthChecks the health checks to analyze
     * @return the overall success rate percentage
     */
    private double calculateOverallSuccessRate(Map<String, HealthCheckDto> healthChecks) {
        if (healthChecks.isEmpty()) {
            return 0.0;
        }

        double totalSuccessRate = healthChecks.values().stream()
                .mapToDouble(HealthCheckDto::getSuccessRate)
                .average()
                .orElse(0.0);

        return Math.round(totalSuccessRate * 100.0) / 100.0; // Round to 2 decimal places
    }
}
