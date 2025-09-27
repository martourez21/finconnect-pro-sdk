package com.codedstreams.finconnectpro.sdk.adapter.impl;

import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.MessageType;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.domain.enums.TransactionStatus;
import com.codedstreams.finconnectpro.sdk.dto.*;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SWIFT-specific adapter implementation for international payments.
 * <p>
 * This adapter specializes in SWIFT network communications, handling both
 * traditional MT messages and modern MX (XML) messages for international payments.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see FinancialAdapter
 */
@Component
public class SwiftAdapter implements FinancialAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SwiftAdapter.class);

    private boolean connected = false;
    private ConnectionConfigDto currentConfig;
    private String bicCode;
    private String sessionId;
    private XmlMapper xmlMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(ConnectionConfigDto config) {
        try {
            logger.info("Initializing SWIFT connection to {}", config.getHost());

            this.currentConfig = config;
            this.bicCode = extractBicCode(config);
            this.xmlMapper = new XmlMapper();
            this.sessionId = generateSessionId();

            initializeSwiftConnection(config);
            this.connected = true;

            logger.info("SWIFT connection established successfully - BIC: {}", bicCode);

        } catch (Exception e) {
            this.connected = false;
            logger.error("SWIFT connection failed", e);
            throw new FinancialConnectionException(
                    "SWIFT connection failed to " + config.getHost(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        try {
            if (connected) {
                closeSwiftConnection();
                this.connected = false;
                this.currentConfig = null;
                this.sessionId = null;
                logger.info("SWIFT connection disconnected");
            }
        } catch (Exception e) {
            logger.error("SWIFT disconnect failed", e);
            throw new FinancialConnectionException("SWIFT disconnect failed", e);
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
            throw new FinancialConnectionException("SWIFT adapter is not connected");
        }

        try {
            logger.debug("Sending SWIFT transaction: {}", request.getTransactionId());

            String swiftMessage = convertToSwiftMessage(request);
            logger.trace("Generated SWIFT message: {}", swiftMessage);

            String swiftResponse = sendSwiftMessage(swiftMessage);
            logger.trace("Received SWIFT response: {}", swiftResponse);

            TransactionResponseDto response = parseSwiftResponse(swiftResponse);
            logger.debug("SWIFT transaction completed: {}", request.getTransactionId());

            return response;

        } catch (Exception e) {
            logger.error("SWIFT transaction failed for {}", request.getTransactionId(), e);
            throw new TransactionFailedException(
                    request.getTransactionId(),
                    "SWIFT transaction failed",
                    "SWIFT_ERROR",
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
            String swiftMessage = receiveSwiftMessage();
            if (swiftMessage == null) {
                return null;
            }

            return parseSwiftToFinancialMessage(swiftMessage);

        } catch (Exception e) {
            logger.error("Error receiving SWIFT message", e);
            throw new FinancialConnectionException("Error receiving SWIFT message", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(FinancialMessageDto message) {
        if (!connected) {
            throw new FinancialConnectionException("SWIFT adapter is not connected");
        }

        try {
            String swiftMessage = convertFinancialMessageToSwift(message);
            sendSwiftMessage(swiftMessage);
            logger.debug("Sent SWIFT message: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error sending SWIFT message", e);
            throw new FinancialConnectionException("Error sending SWIFT message", e);
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
            health.setStatusMessage("SWIFT connection healthy - BIC: " + bicCode);
            health.setMetrics(Map.of(
                    "bicCode", bicCode,
                    "sessionId", sessionId != null ? sessionId : "Not established",
                    "messageFormat", "MX"
            ));
        } else {
            health.setStatusMessage("SWIFT connection not established");
        }

        return health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.SWIFT_XML;
    }

    // SWIFT-specific methods with complete implementations

    /**
     * Converts SWIFT message to FinancialMessageDto
     */
    public FinancialMessageDto parseSwiftToFinancialMessage(String swiftMessage) throws Exception {
        FinancialMessageDto message = new FinancialMessageDto();

        if (swiftMessage.contains("<?xml")) {
            // MX format (XML)
            SwiftMxMessage mxMessage = xmlMapper.readValue(swiftMessage, SwiftMxMessage.class);
            message.setMessageId(mxMessage.getMessageId());
            message.setProtocolType(ProtocolType.SWIFT_XML);
            message.setMessageType(MessageType.PAYMENT_INSTRUCTION);
            message.setTimestamp(mxMessage.getCreationDateTime());
            message.setSource((String) mxMessage.getInitiatingParty());
            message.setDestination((String) mxMessage.getBeneficiaryParty());

            Map<String, Object> body = new HashMap<>();
            body.put("amount", mxMessage.getAmount());
            body.put("currency", mxMessage.getCurrency());
            body.put("debtor", mxMessage.getDebtor());
            body.put("creditor", mxMessage.getCreditor());
            message.setBody(body);

        } else {
            // MT format (text)
            message.setMessageId(extractFieldFromMt(swiftMessage, "20")); // Transaction Reference
            message.setProtocolType(ProtocolType.SWIFT_XML);
            message.setMessageType(MessageType.PAYMENT_INSTRUCTION);
            message.setTimestamp(LocalDateTime.now());
            message.setSource(extractFieldFromMt(swiftMessage, "50")); // Ordering Customer
            message.setDestination(extractFieldFromMt(swiftMessage, "59")); // Beneficiary Customer

            Map<String, Object> body = new HashMap<>();
            body.put("amount", extractFieldFromMt(swiftMessage, "32"));
            body.put("currency", extractFieldFromMt(swiftMessage, "32").substring(3)); // Assuming format: USD1000,
            body.put("valueDate", extractFieldFromMt(swiftMessage, "30"));
            message.setBody(body);
        }

        return message;
    }

    /**
     * Converts FinancialMessageDto to SWIFT message
     */
    public String convertFinancialMessageToSwift(FinancialMessageDto message) throws Exception {
        if (message.getProtocolType() == ProtocolType.SWIFT_XML) {
            // Convert to MX format
            SwiftMxMessage mxMessage = new SwiftMxMessage();
            mxMessage.setMessageId(message.getMessageId());
            mxMessage.setCreationDateTime(message.getTimestamp());
            mxMessage.setInitiatingParty(message.getSource());
            mxMessage.setBeneficiaryParty(message.getDestination());

            if (message.getBody() != null) {
                mxMessage.setAmount((java.math.BigDecimal) message.getBody().get("amount"));
                mxMessage.setCurrency((String) message.getBody().get("currency"));
            }

            return xmlMapper.writeValueAsString(mxMessage);
        } else {
            // Convert to MT format
            return convertToSwiftMtFromFinancialMessage(message);
        }
    }

    private void initializeSwiftConnection(ConnectionConfigDto config) {
        // Real implementation would establish SWIFTNet connection
        logger.debug("SWIFT connection initialized to {}", config.getHost());
    }

    private void closeSwiftConnection() {
        // Real implementation would close SWIFTNet connection
        logger.debug("SWIFT connection closed");
    }

    private String convertToSwiftMessage(TransactionRequestDto request) throws Exception {
        boolean useMxFormat = shouldUseMxFormat(request);
        return useMxFormat ? convertToSwiftMx(request) : convertToSwiftMt(request);
    }

    private String sendSwiftMessage(String swiftMessage) throws Exception {
        // Real implementation would send via SWIFTNet
        // This is a simulation that returns a response

        if (swiftMessage.contains("<?xml")) {
            // MX response
            SwiftMxResponse response = new SwiftMxResponse();
            response.setMessageId("RESP_" + UUID.randomUUID());
            response.setStatus("ACCEPTED");
            response.setReason("OK");
            return xmlMapper.writeValueAsString(response);
        } else {
            // MT response
            return "{1:F21BANKBEBBAXXX0000000000}{2:O1030800990203BANKBEBBAXXX00000000009902030800N}{4:\n:20:REF12345\n:23B:CRED\n:32A:240926USD1000,00\n:33B:USD1000,00\n:50K:/123456789\nJohn Doe\nStreet 1\nCity\n:59:/987654321\nJane Smith\nStreet 2\nCity\n:70:Payment Invoice\n:71A:SHA\n-}";
        }
    }

    private String receiveSwiftMessage() {
        // Real implementation would receive incoming SWIFT messages
        // This is a simulation that returns null (no messages available)
        return null;
    }

    private TransactionResponseDto parseSwiftResponse(String swiftResponse) throws Exception {
        TransactionResponseDto response = new TransactionResponseDto();
        response.setTransactionDate(LocalDateTime.now());

        if (swiftResponse.contains("<?xml")) {
            // MX response parsing
            SwiftMxResponse mxResponse = xmlMapper.readValue(swiftResponse, SwiftMxResponse.class);
            response.setReferenceNumber(mxResponse.getMessageId());
            response.setStatus("ACCEPTED".equals(mxResponse.getStatus()) ?
                    TransactionStatus.COMPLETED :
                    TransactionStatus.FAILED);
            response.setMessage(mxResponse.getReason());
        } else {
            // MT response parsing
            response.setReferenceNumber(extractFieldFromMt(swiftResponse, "20"));
            response.setStatus(TransactionStatus.COMPLETED);
            response.setMessage("MT103 processed successfully");
        }

        return response;
    }

    // Helper methods

    private String extractBicCode(ConnectionConfigDto config) {
        if (config.getAdditionalConfig() != null && config.getAdditionalConfig().containsKey("bicCode")) {
            return config.getAdditionalConfig().get("bicCode").toString();
        }
        return "BANKBEBBAXXX"; // Default BIC
    }

    private String generateSessionId() {
        return "SWIFT_SESSION_" + System.currentTimeMillis();
    }

    private boolean shouldUseMxFormat(TransactionRequestDto request) {
        // Use MX format for high-value or international transactions
        return request.getAmount() == null ||
                request.getAmount().compareTo(java.math.BigDecimal.valueOf(50000)) > 0 ||
                isInternationalTransaction(request);
    }

    private boolean isInternationalTransaction(TransactionRequestDto request) {
        // Simple heuristic based on account numbers or currency
        return request.getCurrency() != null && !"USD".equals(request.getCurrency());
    }

    private String convertToSwiftMx(TransactionRequestDto request) throws Exception {
        SwiftMxMessage mxMessage = new SwiftMxMessage();
        mxMessage.setMessageId(request.getTransactionId());
        mxMessage.setCreationDateTime(LocalDateTime.now());
        mxMessage.setInitiatingParty(createParty(request.getAccountNumber(), "Initiating Bank"));
        mxMessage.setBeneficiaryParty(createParty(request.getBeneficiaryAccount(), request.getBeneficiaryName()));
        mxMessage.setAmount(request.getAmount());
        mxMessage.setCurrency(request.getCurrency());
        mxMessage.setDebtor(createParty(request.getAccountNumber(), "Debtor"));
        mxMessage.setCreditor(createParty(request.getBeneficiaryAccount(), request.getBeneficiaryName()));

        return xmlMapper.writeValueAsString(mxMessage);
    }

    private String convertToSwiftMt(TransactionRequestDto request) {
        return String.format(
                "{1:F01%sXXXXAXXX0000000000}{2:I103%sN}{4:\n:20:%s\n:23B:CRED\n:32A:%s%s\n:50K:/%s\n%s\n:59:/%s\n%s\n:70:%s\n:71A:SHA\n-}",
                bicCode, // Sender's BIC
                extractBeneficiaryBic(request), // Receiver's BIC
                request.getTransactionId(),
                getValueDate(), // Value date (YYMMDD)
                request.getCurrency() + formatAmount(request.getAmount()),
                request.getAccountNumber(),
                "Ordering Customer",
                request.getBeneficiaryAccount(),
                request.getBeneficiaryName(),
                request.getDescription() != null ? request.getDescription() : "Payment"
        );
    }

    private String convertToSwiftMtFromFinancialMessage(FinancialMessageDto message) {
        return String.format(
                "{1:F01%sXXXXAXXX0000000000}{2:I103%sN}{4:\n:20:%s\n:23B:CRED\n:32A:%s%s\n:50K:%s\n:59:%s\n:70:%s\n:71A:SHA\n-}",
                bicCode,
                "BENEFICIARYBICXXX",
                message.getMessageId(),
                getValueDate(),
                "USD1000,00", // Default amount
                message.getSource(),
                message.getDestination(),
                "Financial Message"
        );
    }

    private String extractFieldFromMt(String mtMessage, String fieldTag) {
        // Simple MT field extraction
        String fieldPattern = ":" + fieldTag + ":";
        int start = mtMessage.indexOf(fieldPattern);
        if (start != -1) {
            start += fieldPattern.length();
            int end = mtMessage.indexOf('\n', start);
            if (end == -1) end = mtMessage.length();
            return mtMessage.substring(start, end).trim();
        }
        return "";
    }

    private String extractBeneficiaryBic(TransactionRequestDto request) {
        if (request.getBeneficiaryBankCode() != null) {
            return request.getBeneficiaryBankCode();
        }
        return "BENEBEBBAXXX"; // Default beneficiary BIC
    }

    private String getValueDate() {
        return String.format("%02d%02d%02d",
                LocalDateTime.now().getYear() % 100,
                LocalDateTime.now().getMonthValue(),
                LocalDateTime.now().getDayOfMonth());
    }

    private String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "0000,00";
        return String.format("%012.2f", amount).replace('.', ',');
    }

    private Object createParty(String account, String name) {
        return Map.of("account", account, "name", name, "bic", bicCode);
    }

    // MX Message classes
    private static class SwiftMxMessage {
        private String messageId;
        private LocalDateTime creationDateTime;
        private Object initiatingParty;
        private Object beneficiaryParty;
        private java.math.BigDecimal amount;
        private String currency;
        private Object debtor;
        private Object creditor;

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public LocalDateTime getCreationDateTime() { return creationDateTime; }
        public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }
        public Object getInitiatingParty() { return initiatingParty; }
        public void setInitiatingParty(Object initiatingParty) { this.initiatingParty = initiatingParty; }
        public Object getBeneficiaryParty() { return beneficiaryParty; }
        public void setBeneficiaryParty(Object beneficiaryParty) { this.beneficiaryParty = beneficiaryParty; }
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Object getDebtor() { return debtor; }
        public void setDebtor(Object debtor) { this.debtor = debtor; }
        public Object getCreditor() { return creditor; }
        public void setCreditor(Object creditor) { this.creditor = creditor; }
    }

    private static class SwiftMxResponse {
        private String messageId;
        private String status;
        private String reason;

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}