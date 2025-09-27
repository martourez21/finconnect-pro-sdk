package com.codedstreams.finconnectpro.sdk.exception;

import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;

/**
 * Exception thrown when a requested protocol is not supported by the SDK.
 * <p>
 * This exception indicates that the SDK does not have an adapter implementation
 * for the requested protocol type.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class ProtocolNotSupportedException extends RuntimeException {

    private final ProtocolType protocolType;

    /**
     * Constructs a new ProtocolNotSupportedException for the specified protocol.
     *
     * @param protocolType the unsupported protocol type
     */
    public ProtocolNotSupportedException(ProtocolType protocolType) {
        super("Protocol not supported: " + protocolType);
        this.protocolType = protocolType;
    }

    /**
     * Constructs a new ProtocolNotSupportedException with a custom message.
     *
     * @param message the detail message
     */
    public ProtocolNotSupportedException(String message) {
        super(message);
        this.protocolType = null;
    }

    /**
     * Gets the unsupported protocol type.
     *
     * @return the protocol type, or null if not specified
     */
    public ProtocolType getProtocolType() {
        return protocolType;
    }
}
