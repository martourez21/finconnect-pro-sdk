package com.codedstreams.finconnectpro.sdk.dto;

import com.codedstreams.finconnectpro.sdk.domain.enums.MessageType;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object for financial messages.
 * <p>
 * This class represents a generic financial message that can be sent or received
 * through any of the supported financial protocols. It contains metadata about
 * the message as well as the message content.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class FinancialMessageDto {

    private String messageId;
    private MessageType messageType;
    private ProtocolType protocolType;
    private String sessionId;
    private Map<String, Object> headers;
    private Map<String, Object> body;
    private LocalDateTime timestamp;
    private String source;
    private String destination;
    private Integer sequenceNumber;
    private boolean requiresAck;

    /**
     * Default constructor.
     */
    public FinancialMessageDto() {
        this.timestamp = LocalDateTime.now();
        this.requiresAck = false;
    }

    /**
     * Gets the unique message identifier.
     *
     * @return the message ID
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the unique message identifier.
     *
     * @param messageId the message ID to set
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets the type of financial message.
     *
     * @return the message type
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Sets the type of financial message.
     *
     * @param messageType the message type to set
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * Gets the protocol type for this message.
     *
     * @return the protocol type
     */
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    /**
     * Sets the protocol type for this message.
     *
     * @param protocolType the protocol type to set
     */
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * Gets the session identifier associated with this message.
     *
     * @return the session ID, or null if not associated with a session
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session identifier associated with this message.
     *
     * @param sessionId the session ID to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the message headers containing metadata.
     *
     * @return a map of header key-value pairs
     */
    public Map<String, Object> getHeaders() {
        return headers;
    }

    /**
     * Sets the message headers containing metadata.
     *
     * @param headers a map of header key-value pairs to set
     */
    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    /**
     * Gets the message body containing the actual content.
     *
     * @return a map of body key-value pairs
     */
    public Map<String, Object> getBody() {
        return body;
    }

    /**
     * Sets the message body containing the actual content.
     *
     * @param body a map of body key-value pairs to set
     */
    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    /**
     * Gets the timestamp when the message was created or received.
     *
     * @return the message timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when the message was created or received.
     *
     * @param timestamp the message timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the source system or participant of the message.
     *
     * @return the source identifier
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the source system or participant of the message.
     *
     * @param source the source identifier to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets the destination system or participant of the message.
     *
     * @return the destination identifier
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the destination system or participant of the message.
     *
     * @param destination the destination identifier to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets the sequence number for ordered message processing.
     *
     * @return the sequence number, or null if not sequenced
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the sequence number for ordered message processing.
     *
     * @param sequenceNumber the sequence number to set
     */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Checks if this message requires an acknowledgment.
     *
     * @return true if acknowledgment is required, false otherwise
     */
    public boolean isRequiresAck() {
        return requiresAck;
    }

    /**
     * Sets whether this message requires an acknowledgment.
     *
     * @param requiresAck true if acknowledgment is required, false otherwise
     */
    public void setRequiresAck(boolean requiresAck) {
        this.requiresAck = requiresAck;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinancialMessageDto that = (FinancialMessageDto) o;
        return Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public String toString() {
        return "FinancialMessageDto{" +
                "messageId='" + messageId + '\'' +
                ", messageType=" + messageType +
                ", protocolType=" + protocolType +
                ", timestamp=" + timestamp +
                '}';
    }
}
