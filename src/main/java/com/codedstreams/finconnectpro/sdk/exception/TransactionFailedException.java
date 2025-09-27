package com.codedstreams.finconnectpro.sdk.exception;

/**
 * Exception thrown when a financial transaction fails to process.
 * <p>
 * This exception indicates that a transaction was rejected by the financial network
 * or failed during processing due to business rules, validation errors, or network issues.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class TransactionFailedException extends RuntimeException {

    private final String transactionId;
    private final String errorCode;
    private final String protocolType;

    /**
     * Constructs a new TransactionFailedException with transaction details.
     *
     * @param transactionId the identifier of the failed transaction
     * @param message the detail message explaining the failure
     * @param errorCode the error code returned by the financial network
     */
    public TransactionFailedException(String transactionId, String message, String errorCode) {
        super(message);
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.protocolType = null;
    }

    /**
     * Constructs a new TransactionFailedException with transaction details and cause.
     *
     * @param transactionId the identifier of the failed transaction
     * @param message the detail message explaining the failure
     * @param errorCode the error code returned by the financial network
     * @param cause the underlying cause of the exception
     */
    public TransactionFailedException(String transactionId, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.protocolType = null;
    }

    /**
     * Constructs a new TransactionFailedException with full context.
     *
     * @param transactionId the identifier of the failed transaction
     * @param message the detail message explaining the failure
     * @param errorCode the error code returned by the financial network
     * @param protocolType the protocol type used for the transaction
     */
    public TransactionFailedException(String transactionId, String message, String errorCode, String protocolType) {
        super(message);
        this.transactionId = transactionId;
        this.errorCode = errorCode;
        this.protocolType = protocolType;
    }

    /**
     * Gets the transaction identifier associated with this exception.
     *
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Gets the error code returned by the financial network.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the protocol type used for the failed transaction.
     *
     * @return the protocol type, or null if not specified
     */
    public String getProtocolType() {
        return protocolType;
    }
}
