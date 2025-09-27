package com.codedstreams.finconnectpro.sdk.domain.enums;

/**
 * Enumeration of financial message types.
 * <p>
 * Defines the types of messages that can be sent or received through
 * the financial network adapters.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public enum MessageType {

    /**
     * Single order message for trading systems.
     */
    ORDER_SINGLE,

    /**
     * Order cancellation request.
     */
    ORDER_CANCEL,

    /**
     * Trade execution report.
     */
    EXECUTION_REPORT,

    /**
     * Connection heartbeat message.
     */
    HEARTBEAT,

    /**
     * Session logon message.
     */
    LOGON,

    /**
     * Session logout message.
     */
    LOGOUT,

    /**
     * Payment instruction message.
     */
    PAYMENT_INSTRUCTION,

    /**
     * Payment confirmation message.
     */
    PAYMENT_CONFIRMATION,

    /**
     * Account balance inquiry.
     */
    BALANCE_INQUIRY,

    /**
     * Transaction status query.
     */
    TRANSACTION_STATUS,

    /**
     * Fund transfer request.
     */
    FUNDS_TRANSFER,

    /**
     * Security price inquiry.
     */
    PRICE_INQUIRY
}
