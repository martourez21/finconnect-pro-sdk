package com.codedstreams.finconnectpro.sdk.adapter.impl;

import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.MessageType;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.domain.enums.TransactionStatus;
import com.codedstreams.finconnectpro.sdk.dto.*;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ISO-8583 adapter implementation for card payment networks and ATM systems.
 * <p>
 * This adapter handles communication using the ISO-8583 standard, which is widely used
 * for credit/debit card transactions, ATM operations, and point-of-sale systems.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see FinancialAdapter
 */
@Component
public class Iso8583Adapter implements FinancialAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Iso8583Adapter.class);

    private boolean connected = false;
    private ConnectionConfigDto currentConfig;
    private ISOPackager packager;
    private String merchantId;
    private String terminalId;

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(ConnectionConfigDto config) {
        try {
            logger.info("Initializing ISO-8583 connection to {}:{}",
                    config.getHost(), config.getPort());

            this.currentConfig = config;
            this.merchantId = extractMerchantId(config);
            this.terminalId = extractTerminalId(config);

            // Load ISO-8583 packager
            initializePackager();

            // Initialize connection (simulated - real implementation would use sockets)
            initializeIso8583Connection(config);
            this.connected = true;

            logger.info("ISO-8583 connection established successfully");

        } catch (Exception e) {
            this.connected = false;
            logger.error("ISO-8583 connection failed", e);
            throw new FinancialConnectionException(
                    "ISO-8583 connection failed to " + config.getHost() + ":" + config.getPort(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        try {
            if (connected) {
                closeIso8583Connection();
                this.connected = false;
                this.currentConfig = null;
                logger.info("ISO-8583 connection disconnected");
            }
        } catch (Exception e) {
            logger.error("ISO-8583 disconnect failed", e);
            throw new FinancialConnectionException("ISO-8583 disconnect failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionResponseDto sendTransaction(TransactionRequestDto request) {
        if (!connected) {
            throw new FinancialConnectionException("ISO-8583 adapter is not connected");
        }

        try {
            logger.debug("Sending ISO-8583 transaction: {}", request.getTransactionId());

            // Convert to ISO-8583 message
            ISOMsg isoMessage = convertToIso8583Message(request);

            // Send message and get response
            ISOMsg isoResponse = sendIso8583Message(isoMessage);

            // Parse response
            TransactionResponseDto response = parseIso8583Response(isoResponse);
            logger.debug("ISO-8583 transaction completed: {}", request.getTransactionId());

            return response;

        } catch (Exception e) {
            logger.error("ISO-8583 transaction failed for {}", request.getTransactionId(), e);
            throw new TransactionFailedException(
                    request.getTransactionId(),
                    "ISO-8583 transaction failed",
                    "ISO8583_ERROR",
                    e
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FinancialMessageDto receiveMessage() {
        if (!connected) {
            return null;
        }

        try {
            ISOMsg isoMessage = receiveIso8583Message();
            if (isoMessage == null) {
                return null;
            }

            return parseIso8583ToFinancialMessage(isoMessage);

        } catch (Exception e) {
            logger.error("Error receiving ISO-8583 message", e);
            throw new FinancialConnectionException("Error receiving ISO-8583 message", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(FinancialMessageDto message) {
        if (!connected) {
            throw new FinancialConnectionException("ISO-8583 adapter is not connected");
        }

        try {
            ISOMsg isoMessage = convertFinancialMessageToIso8583(message);
            sendIso8583Message(isoMessage);
            logger.debug("Sent ISO-8583 message: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error sending ISO-8583 message", e);
            throw new FinancialConnectionException("Error sending ISO-8583 message", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HealthCheckDto healthCheck() {
        HealthCheckDto health = new HealthCheckDto();
        health.setProtocolType(getSupportedProtocol());
        health.setConnected(connected);
        health.setLastCheck(LocalDateTime.now());

        if (connected) {
            health.setStatusMessage("ISO-8583 connection healthy");
            health.setMetrics(Map.of(
                    "merchantId", merchantId,
                    "terminalId", terminalId,
                    "packager", packager != null ? "Loaded" : "Not loaded"
            ));
        } else {
            health.setStatusMessage("ISO-8583 connection not established");
        }

        return health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.ISO_8583;
    }

    // ISO-8583 specific methods

    private void initializePackager() throws ISOException {
        // Load ISO-8583 packager configuration
        InputStream packagerConfig = getClass().getResourceAsStream("/iso8583/iso87ascii.xml");
        if (packagerConfig != null) {
            this.packager = new GenericPackager(packagerConfig);
        } else {
            // Fallback to generic packager
            this.packager = new GenericPackager();
        }
        logger.debug("ISO-8583 packager initialized");
    }

    private void initializeIso8583Connection(ConnectionConfigDto config) {
        // Real implementation would establish socket connection to acquirer
        logger.debug("ISO-8583 connection initialized to {}:{}", config.getHost(), config.getPort());
    }

    private void closeIso8583Connection() {
        // Real implementation would close socket connection
        logger.debug("ISO-8583 connection closed");
    }

    private ISOMsg convertToIso8583Message(TransactionRequestDto request) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);

        // Set MTI (Message Type Indicator) - 0200 for financial transaction
        isoMsg.setMTI("0200");

        // Set processing code
        isoMsg.set(3, "000000"); // Purchase

        // Set amount (field 4)
        if (request.getAmount() != null) {
            String amount = String.format("%012d", request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue());
            isoMsg.set(4, amount);
        }

        // Set transmission date/time (field 7)
        String dateTime = String.format("%02d%02d%02d%02d%02d%02d",
                LocalDateTime.now().getMonthValue(),
                LocalDateTime.now().getDayOfMonth(),
                LocalDateTime.now().getHour(),
                LocalDateTime.now().getMinute(),
                LocalDateTime.now().getSecond(),
                0);
        isoMsg.set(7, dateTime);

        // Set STAN (Systems Trace Audit Number) - field 11
        isoMsg.set(11, String.format("%06d", System.currentTimeMillis() % 1000000));

        // Set local time (field 12)
        isoMsg.set(12, String.format("%02d%02d%02d",
                LocalDateTime.now().getHour(),
                LocalDateTime.now().getMinute(),
                LocalDateTime.now().getSecond()));

        // Set local date (field 13)
        isoMsg.set(13, String.format("%02d%02d",
                LocalDateTime.now().getMonthValue(),
                LocalDateTime.now().getDayOfMonth()));

        // Set merchant data
        isoMsg.set(42, merchantId); // Card Acceptor ID
        isoMsg.set(41, terminalId); // Card Acceptor Terminal ID

        // Set currency (field 49)
        if (request.getCurrency() != null) {
            isoMsg.set(49, getCurrencyCode(request.getCurrency()));
        }

        // Set additional fields
        setAdditionalFields(isoMsg, request);

        return isoMsg;
    }

    private ISOMsg sendIso8583Message(ISOMsg isoMessage) throws ISOException {
        // Real implementation would send via socket and wait for response
        // This is a simulation that returns a response message

        // Create response message
        ISOMsg response = new ISOMsg();
        response.setMTI("0210"); // Financial transaction response

        // Copy key fields from request
        response.set(3, isoMessage.getString(3)); // Processing code
        response.set(4, isoMessage.getString(4)); // Amount
        response.set(7, isoMessage.getString(7)); // Transmission date/time
        response.set(11, isoMessage.getString(11)); // STAN
        response.set(39, "00"); // Response code - Approved

        // Set response text
        response.set(44, "APPROVED");

        return response;
    }

    private ISOMsg receiveIso8583Message() {
        // Real implementation would listen on socket for incoming messages
        // This is a simulation that returns null (no messages available)
        return null;
    }

    private TransactionResponseDto parseIso8583Response(ISOMsg isoResponse) throws ISOException {
        TransactionResponseDto response = new TransactionResponseDto();
        response.setTransactionDate(LocalDateTime.now());

        // Extract response code (field 39)
        String responseCode = isoResponse.getString(39);
        response.setReferenceNumber(isoResponse.getString(11)); // STAN as reference

        // Map ISO-8583 response code to transaction status
        switch (responseCode) {
            case "00" -> response.setStatus(TransactionStatus.COMPLETED);
            case "01", "02", "03" -> response.setStatus(TransactionStatus.FAILED);
            case "05", "08" -> response.setStatus(TransactionStatus.REVERSED);
            default -> response.setStatus(TransactionStatus.PENDING);
        }

        // Set response message
        if (isoResponse.hasField(44)) {
            response.setMessage(isoResponse.getString(44));
        }

        return response;
    }

    /**
     * Converts ISO-8583 message to FinancialMessageDto
     */
    public FinancialMessageDto parseIso8583ToFinancialMessage(ISOMsg isoMessage) throws ISOException {
        FinancialMessageDto message = new FinancialMessageDto();
        message.setMessageId(isoMessage.getString(11)); // Use STAN as message ID
        message.setProtocolType(ProtocolType.ISO_8583);
        message.setMessageType(parseMessageType(isoMessage.getMTI()));
        message.setTimestamp(LocalDateTime.now());

        // Extract headers
        Map<String, Object> headers = new HashMap<>();
        headers.put("MTI", isoMessage.getMTI());
        headers.put("ProcessingCode", isoMessage.getString(3));
        message.setHeaders(headers);

        // Extract body (all fields)
        Map<String, Object> body = new HashMap<>();
        for (int i = 2; i <= 128; i++) {
            if (isoMessage.hasField(i)) {
                body.put("Field" + i, isoMessage.getString(i));
            }
        }
        message.setBody(body);

        return message;
    }

    /**
     * Converts FinancialMessageDto to ISO-8583 message
     */
    public ISOMsg convertFinancialMessageToIso8583(FinancialMessageDto message) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);

        // Set MTI from message type or default
        String mti = determineMTI(message.getMessageType());
        isoMsg.setMTI(mti);

        // Set fields from message body
        if (message.getBody() != null) {
            for (Map.Entry<String, Object> entry : message.getBody().entrySet()) {
                if (entry.getKey().startsWith("Field")) {
                    try {
                        int fieldNumber = Integer.parseInt(entry.getKey().substring(5));
                        isoMsg.set(fieldNumber, entry.getValue().toString());
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid field number in message: {}", entry.getKey());
                    }
                }
            }
        }

        // Set mandatory fields if not provided
        if (!isoMsg.hasField(7)) {
            String dateTime = String.format("%02d%02d%02d%02d%02d%02d",
                    LocalDateTime.now().getMonthValue(),
                    LocalDateTime.now().getDayOfMonth(),
                    LocalDateTime.now().getHour(),
                    LocalDateTime.now().getMinute(),
                    LocalDateTime.now().getSecond(),
                    0);
            isoMsg.set(7, dateTime);
        }

        if (!isoMsg.hasField(11)) {
            isoMsg.set(11, String.format("%06d", System.currentTimeMillis() % 1000000));
        }

        return isoMsg;
    }

    // Helper methods

    private String extractMerchantId(ConnectionConfigDto config) {
        if (config.getAdditionalConfig() != null && config.getAdditionalConfig().containsKey("merchantId")) {
            return config.getAdditionalConfig().get("merchantId").toString();
        }
        return "DEFAULT_MERCHANT";
    }

    private String extractTerminalId(ConnectionConfigDto config) {
        if (config.getAdditionalConfig() != null && config.getAdditionalConfig().containsKey("terminalId")) {
            return config.getAdditionalConfig().get("terminalId").toString();
        }
        return "DEFAULT_TERMINAL";
    }

    private String getCurrencyCode(String currency) {
        return switch (currency.toUpperCase()) {
            case "USD" -> "840";
            case "EUR" -> "978";
            case "GBP" -> "826";
            case "JPY" -> "392";
            default -> "840"; // USD as default
        };
    }

    private void setAdditionalFields(ISOMsg isoMsg, TransactionRequestDto request) throws ISOException {
        // Set track 2 data if available
        if (request.getMetadata() != null && request.getMetadata().containsKey("track2")) {
            isoMsg.set(35, request.getMetadata().get("track2").toString());
        }

        // Set card number if available
        if (request.getMetadata() != null && request.getMetadata().containsKey("cardNumber")) {
            isoMsg.set(2, request.getMetadata().get("cardNumber").toString());
        }
    }

    private MessageType parseMessageType(String mti) {
        return switch (mti) {
            case "0100", "0200" -> MessageType.PAYMENT_INSTRUCTION;   // Authorization/financial request
            case "0110", "0210" -> MessageType.PAYMENT_CONFIRMATION;  // Response to above
            case "0400", "0420" -> MessageType.FUNDS_TRANSFER;        // Reversal / chargeback
            case "0800" -> MessageType.HEARTBEAT;                     // Network management
            default -> MessageType.TRANSACTION_STATUS;                // Fallback
        };
    }

    private String determineMTI(MessageType messageType) {
        return switch (messageType) {
            case PAYMENT_INSTRUCTION -> "0200";    // Financial transaction
            case PAYMENT_CONFIRMATION -> "0210";   // Response
            case BALANCE_INQUIRY -> "0100";        // Inquiry
            case FUNDS_TRANSFER -> "0400";         // Reversal/transfer
            case HEARTBEAT -> "0800";              // Network management
            default -> "0200";                     // Default to financial request
        };
    }

}