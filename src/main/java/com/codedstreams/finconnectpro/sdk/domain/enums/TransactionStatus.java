package com.codedstreams.finconnectpro.sdk.domain.enums;

/**
 * Enumeration of transaction status states.
 * <p>
 * Represents the various states a financial transaction can be in during its lifecycle.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public enum TransactionStatus {

    /**
     * Transaction has been received and is being processed.
     */
    PENDING,

    /**
     * Transaction has been successfully completed.
     */
    COMPLETED,

    /**
     * Transaction has failed during processing.
     */
    FAILED,

    /**
     * Transaction is rejected during processing.
     */
    REJECTED,

    /**
     * Transaction was cancelled by the user or system.
     */
    CANCELLED,

    /**
     * Transaction is being processed by the financial network.
     */
    IN_PROGRESS,

    /**
     * Transaction has been reversed (chargeback or refund).
     */
    REVERSED,

    /**
     * Transaction is awaiting authorization.
     */
    AWAITING_AUTHORIZATION,

    /**
     * Transaction has been authorized but not settled.
     */
    AUTHORIZED,

    /**
     * Transaction has been settled.
     */
    SETTLED,

    /**
     * Transaction is being investigated.
     */
    UNDER_INVESTIGATION
}
