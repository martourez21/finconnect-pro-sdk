package com.codedstreams.finconnectpro.sdk.service.handler;

import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.domain.enums.TransactionStatus;
import com.codedstreams.finconnectpro.sdk.dto.TransactionRequestDto;
import com.codedstreams.finconnectpro.sdk.dto.TransactionResponseDto;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;
import com.codedstreams.finconnectpro.sdk.service.ConnectionManagerService;
import com.codedstreams.finconnectpro.sdk.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the TransactionService interface.
 * <p>
 * This service handles the processing of financial transactions by routing them
 * through the appropriate protocol adapters. It includes business logic for
 * protocol selection, request validation, and response processing.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see TransactionService
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final ConnectionManagerService connectionManager;
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("100000");
    private static final BigDecimal INTERNATIONAL_THRESHOLD = new BigDecimal("50000");

    /**
     * Constructs a new TransactionServiceImpl with the specified connection manager.
     *
     * @param connectionManager the connection manager service to use
     */
    public TransactionServiceImpl(ConnectionManagerService connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionResponseDto processTransaction(String connectionId, TransactionRequestDto request) {
        validateTransactionRequest(request);

        try {
            FinancialAdapter adapter = connectionManager.getAdapter(connectionId);
            TransactionResponseDto response = adapter.sendTransaction(request);

            // Enhance response with additional metadata
            enhanceTransactionResponse(response, request);

            return response;

        } catch (Exception e) {
            TransactionResponseDto errorResponse = createErrorResponse(request, e.getMessage());
            throw new TransactionFailedException(
                    request.getTransactionId(),
                    "Transaction processing failed",
                    "PROCESSING_ERROR", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionResponseDto processTransaction(TransactionRequestDto request) {
        ProtocolType protocolType = determineProtocolForTransaction(request);
        return processTransaction(protocolType, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionResponseDto processTransaction(ProtocolType protocolType, TransactionRequestDto request) {
        String connectionId = connectionManager.findBestConnection(protocolType);
        if (connectionId == null) {
            throw new FinancialConnectionException(
                    "No active connection available for protocol: " + protocolType);
        }
        return processTransaction(connectionId, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateTransactionRequest(TransactionRequestDto request) {
        if (request == null) {
            return false;
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
            return false;
        }
        if (request.getTransactionId() == null || request.getTransactionId().trim().isEmpty()) {
            request.setTransactionId(generateTransactionId());
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProtocolType determineProtocolForTransaction(TransactionRequestDto request) {
        // Business logic for protocol selection based on transaction characteristics
        BigDecimal amount = request.getAmount();
        String currency = request.getCurrency();

        // High-value transactions typically use SWIFT
        if (amount.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            return ProtocolType.SWIFT_XML;
        }

        // International transactions
        if (isInternationalTransaction(request)) {
            return ProtocolType.SWIFT_XML;
        }

        // EUR transactions within SEPA zone
        if ("EUR".equals(currency) && isSepaTransaction(request)) {
            return ProtocolType.SEPA_XML;
        }

        // Default to REST API for smaller, domestic transactions
        return ProtocolType.REST_API;
    }

    /**
     * Enhances the transaction response with additional metadata.
     *
     * @param response the transaction response to enhance
     * @param request the original transaction request
     */
    private void enhanceTransactionResponse(TransactionResponseDto response, TransactionRequestDto request) {
        if (response.getTransactionDate() == null) {
            response.setTransactionDate(LocalDateTime.now());
        }
        if (response.getTransactionId() == null) {
            response.setTransactionId(request.getTransactionId());
        }
    }

    /**
     * Creates an error response for failed transactions.
     *
     * @param request the original transaction request
     * @param errorMessage the error message
     * @return an error response DTO
     */
    private TransactionResponseDto createErrorResponse(TransactionRequestDto request, String errorMessage) {
        TransactionResponseDto response = new TransactionResponseDto();
        response.setTransactionId(request.getTransactionId());
        response.setStatus(TransactionStatus.FAILED);
        response.setTransactionDate(LocalDateTime.now());
        response.setMessage(errorMessage);
        return response;
    }

    /**
     * Checks if a transaction is international based on currency and account details.
     *
     * @param request the transaction request to check
     * @return true if the transaction is international, false otherwise
     */
    private boolean isInternationalTransaction(TransactionRequestDto request) {
        // Simplified logic - in practice, this would check country codes, BIC, etc.
        return request.getBeneficiaryAccount() != null &&
                request.getBeneficiaryAccount().length() > 10; // Basic heuristic
    }

    /**
     * Checks if a transaction is within the SEPA zone.
     *
     * @param request the transaction request to check
     * @return true if the transaction is within SEPA, false otherwise
     */
    private boolean isSepaTransaction(TransactionRequestDto request) {
        // Simplified logic - in practice, this would validate SEPA country codes
        return request.getBeneficiaryAccount() != null &&
                request.getBeneficiaryAccount().startsWith("EU");
    }

    /**
     * Generates a unique transaction identifier.
     *
     * @return a unique transaction ID
     */
    private String generateTransactionId() {
        return "txn-" + UUID.randomUUID().toString().substring(0, 8) +
                "-" + System.currentTimeMillis();
    }
}
