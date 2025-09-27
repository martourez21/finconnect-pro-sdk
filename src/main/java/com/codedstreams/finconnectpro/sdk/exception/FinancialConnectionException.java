package com.codedstreams.finconnectpro.sdk.exception;

/**
 * Exception thrown when a financial network connection cannot be established or maintained.
 * <p>
 * This exception indicates problems with network connectivity, authentication,
 * or protocol-specific connection issues.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class FinancialConnectionException extends RuntimeException {

    private final String connectionId;
    private final String protocolType;

    /**
     * Constructs a new FinancialConnectionException with the specified detail message.
     *
     * @param message the detail message explaining the connection failure
     */
    public FinancialConnectionException(String message) {
        super(message);
        this.connectionId = null;
        this.protocolType = null;
    }

    /**
     * Constructs a new FinancialConnectionException with the specified detail message and cause.
     *
     * @param message the detail message explaining the connection failure
     * @param cause the underlying cause of the exception
     */
    public FinancialConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.connectionId = null;
        this.protocolType = null;
    }

    /**
     * Constructs a new FinancialConnectionException with connection-specific details.
     *
     * @param message the detail message explaining the connection failure
     * @param connectionId the identifier of the connection that failed
     * @param protocolType the protocol type of the failed connection
     */
    public FinancialConnectionException(String message, String connectionId, String protocolType) {
        super(message);
        this.connectionId = connectionId;
        this.protocolType = protocolType;
    }

    /**
     * Gets the connection identifier associated with this exception.
     *
     * @return the connection ID, or null if not associated with a specific connection
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the protocol type associated with this exception.
     *
     * @return the protocol type, or null if not associated with a specific protocol
     */
    public String getProtocolType() {
        return protocolType;
    }
}
