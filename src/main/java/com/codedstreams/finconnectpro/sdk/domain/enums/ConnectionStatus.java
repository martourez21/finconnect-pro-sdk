package com.codedstreams.finconnectpro.sdk.domain.enums;

/**
 * Enumeration of connection status states.
 * <p>
 * Represents the various states a financial network connection can be in.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public enum ConnectionStatus {

    /**
     * Connection is established and active.
     */
    CONNECTED,

    /**
     * Connection is not established.
     */
    DISCONNECTED,

    /**
     * Connection is in the process of being established.
     */
    CONNECTING,

    /**
     * Connection has encountered an error.
     */
    ERROR,

    /**
     * Connection is attempting to reconnect after a failure.
     */
    RECONNECTING,

    /**
     * Connection is active but no data is being transmitted.
     */
    IDLE,

    /**
     * Connection is being gracefully terminated.
     */
    DISCONNECTING
}
