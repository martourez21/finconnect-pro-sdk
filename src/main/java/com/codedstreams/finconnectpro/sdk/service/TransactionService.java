package com.codedstreams.finconnectpro.sdk.service;

import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.dto.TransactionRequestDto;
import com.codedstreams.finconnectpro.sdk.dto.TransactionResponseDto;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;

/**
 * Service interface for processing financial transactions.
 * <p>
 * This service provides high-level transaction processing capabilities across
 * different financial protocols. It handles transaction routing, protocol selection,
 * and response processing.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see TransactionRequestDto
 * @see TransactionResponseDto
 */
public interface TransactionService {

    /**
     * Processes a transaction using a specific connection.
     *
     * @param connectionId the connection identifier to use for the transaction
     * @param request the transaction request to process
     * @return the transaction response from the financial network
     * @throws TransactionFailedException if the transaction processing fails
     */
    TransactionResponseDto processTransaction(String connectionId, TransactionRequestDto request);

    /**
     * Processes a transaction by automatically selecting the best available connection
     * for the inferred protocol type.
     *
     * @param request the transaction request to process
     * @return the transaction response from the financial network
     * @throws TransactionFailedException if the transaction processing fails
     */
    TransactionResponseDto processTransaction(TransactionRequestDto request);

    /**
     * Processes a transaction using the specified protocol type, automatically
     * selecting an appropriate connection.
     *
     * @param protocolType the protocol type to use for the transaction
     * @param request the transaction request to process
     * @return the transaction response from the financial network
     * @throws TransactionFailedException if the transaction processing fails
     */
    TransactionResponseDto processTransaction(ProtocolType protocolType, TransactionRequestDto request);

    /**
     * Validates a transaction request before processing.
     *
     * @param request the transaction request to validate
     * @return true if the request is valid, false otherwise
     */
    boolean validateTransactionRequest(TransactionRequestDto request);

    /**
     * Determines the appropriate protocol type for a transaction request based on
     * the request parameters and business rules.
     *
     * @param request the transaction request to analyze
     * @return the recommended protocol type for the transaction
     */
    ProtocolType determineProtocolForTransaction(TransactionRequestDto request);
}
