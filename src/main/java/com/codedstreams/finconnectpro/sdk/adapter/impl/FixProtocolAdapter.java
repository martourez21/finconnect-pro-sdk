package com.codedstreams.finconnectpro.sdk.adapter.impl;

import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.domain.enums.TransactionStatus;
import com.codedstreams.finconnectpro.sdk.dto.*;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix40.NewOrderSingle;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.codedstreams.finconnectpro.sdk.domain.enums.TransactionStatus.PENDING;

/**
 * FIX Protocol adapter implementation.
 * <p>
 * This adapter handles communication using the Financial Information Exchange (FIX) protocol,
 * which is widely used in electronic trading for securities, derivatives, and other financial instruments.
 * Supports both FIX 4.2 and FIX 5.0 versions.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see FinancialAdapter
 */
@Component
public class FixProtocolAdapter implements FinancialAdapter, Application {

    private static final Logger logger = LoggerFactory.getLogger(FixProtocolAdapter.class);

    private boolean connected = false;
    private ConnectionConfigDto currentConfig;
    private Initiator initiator;
    private SessionID sessionId;
    private final Map<String, Message> pendingResponses = new ConcurrentHashMap<>();
    private final Map<String, TransactionResponseDto> completedTransactions = new ConcurrentHashMap<>();
    private int sequenceNumber = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(ConnectionConfigDto config) {
        try {
            logger.info("Initializing FIX connection to {}:{} for protocol {}",
                    config.getHost(), config.getPort(), config.getProtocolType());

            this.currentConfig = config;

            // Create FIX session settings
            SessionSettings settings = createSessionSettings(config);
            MessageStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
            MessageFactory messageFactory = new DefaultMessageFactory();

            // Create and start initiator
            this.initiator = new SocketInitiator(this, storeFactory, settings, logFactory, messageFactory);
            this.initiator.start();

            // Wait for connection establishment
            waitForConnection(30000); // 30 second timeout

            logger.info("FIX connection established successfully");

        } catch (Exception e) {
            this.connected = false;
            logger.error("FIX connection failed to {}:{}", config.getHost(), config.getPort(), e);
            throw new FinancialConnectionException(
                    "FIX connection failed to " + config.getHost() + ":" + config.getPort(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        try {
            if (connected && initiator != null) {
                logger.info("Disconnecting FIX session");

                // Send logout message
                if (sessionId != null) {
                    sendLogoutMessage();
                }

                // Stop initiator
                initiator.stop();
                this.connected = false;
                this.currentConfig = null;
                this.sessionId = null;
                this.pendingResponses.clear();
                this.completedTransactions.clear();

                logger.info("FIX connection disconnected successfully");
            }
        } catch (Exception e) {
            logger.error("FIX disconnect failed", e);
            throw new FinancialConnectionException("FIX disconnect failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return connected && sessionId != null &&
                Session.lookupSession(sessionId) != null &&
                Session.lookupSession(sessionId).isLoggedOn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionResponseDto sendTransaction(TransactionRequestDto request) {
        if (!isConnected()) {
            throw new FinancialConnectionException("FIX adapter is not connected");
        }

        try {
            logger.debug("Sending FIX transaction: {}", request.getTransactionId());

            // Convert to FIX message
            Message fixMessage = convertToFixMessage(request);
            String clOrdId = request.getTransactionId();

            // Store the request for response matching
            pendingResponses.put(clOrdId, fixMessage);

            // Send the message
            boolean sent = Session.sendToTarget(fixMessage, sessionId);
            if (!sent) {
                throw new FinancialConnectionException("Failed to send FIX message");
            }

            // Wait for response with timeout
            return waitForExecutionReport(clOrdId, 30000); // 30 second timeout

        } catch (Exception e) {
            logger.error("FIX transaction failed for {}", request.getTransactionId(), e);
            throw new TransactionFailedException(
                    request.getTransactionId(),
                    "FIX transaction failed: " + e.getMessage(),
                    "FIX_ERROR", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FinancialMessageDto receiveMessage() {
        // In FIX protocol, messages are received asynchronously via callbacks
        // This method can return the next available message from a queue
        return null; // Implementation would require a message queue
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(FinancialMessageDto message) {
        if (!isConnected()) {
            throw new FinancialConnectionException("FIX adapter is not connected");
        }

        try {
            Message fixMessage = convertFinancialMessageToFix(message);
            Session.sendToTarget(fixMessage, sessionId);
            logger.debug("Sent FIX message: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error sending FIX message", e);
            throw new FinancialConnectionException("Error sending FIX message", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HealthCheckDto healthCheck() {
        HealthCheckDto health = new HealthCheckDto();
        health.setProtocolType(getSupportedProtocol());
        health.setConnected(isConnected());
        health.setLastCheck(LocalDateTime.now());

        if (isConnected()) {
            health.setStatusMessage("FIX connection healthy");
            Session session = Session.lookupSession(sessionId);
            health.setMetrics(Map.of(
                    "sessionStatus", session != null ? "LoggedOn" : "Unknown",
                    "sequenceNumber", getCurrentSequenceNumber(),
                    "pendingRequests", pendingResponses.size(),
                    "sessionId", sessionId != null ? sessionId.toString() : "None"
            ));
        } else {
            health.setStatusMessage("FIX connection not established");
        }

        return health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProtocolType getSupportedProtocol() {
        return currentConfig != null ? currentConfig.getProtocolType() : ProtocolType.FIX_4_2;
    }

    // QuickFIX/J Application interface methods

    @Override
    public void onCreate(SessionID sessionId) {
        logger.info("FIX session created: {}", sessionId);
        this.sessionId = sessionId;
    }

    @Override
    public void onLogon(SessionID sessionId) {
        logger.info("FIX session logon: {}", sessionId);
        this.connected = true;
        this.sessionId = sessionId;
    }

    @Override
    public void onLogout(SessionID sessionId) {
        logger.info("FIX session logout: {}", sessionId);
        this.connected = false;
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        logger.debug("Sending admin message: {}", message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        logger.debug("Received admin message: {}", message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        logger.debug("Sending app message: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        logger.debug("Received app message: {}", message);

        try {
            // Handle execution reports
            if (message instanceof quickfix.fix44.ExecutionReport) {
                handleExecutionReport((quickfix.fix44.ExecutionReport) message);
            }
            // Handle other message types as needed
        } catch (Exception e) {
            logger.error("Error processing incoming FIX message", e);
        }
    }

    // FIX protocol specific methods

    private SessionSettings createSessionSettings(ConnectionConfigDto config) throws ConfigError {
        Map<Object, Object> settingsMap = new HashMap<>();

        // Connection settings
        settingsMap.put("ConnectionType", "initiator");
        settingsMap.put("SenderCompID", extractSenderCompId(config));
        settingsMap.put("TargetCompID", extractTargetCompId(config));
        settingsMap.put("SocketConnectHost", config.getHost());
        settingsMap.put("SocketConnectPort", config.getPort().toString());

        // Session settings
        settingsMap.put("StartTime", "00:00:00");
        settingsMap.put("EndTime", "23:59:59");
        settingsMap.put("HeartBtInt", "30");
        settingsMap.put("ResetOnLogon", "Y");
        settingsMap.put("FileStorePath", "fix_sessions");

        // Protocol version
        String beginString = getBeginString(config.getProtocolType());
        settingsMap.put("BeginString", beginString);

        SessionSettings settings = new SessionSettings();
        settings.setString(sessionId, "BeginString", beginString);
        settings.setString(sessionId, "SenderCompID", extractSenderCompId(config));
        settings.setString(sessionId, "TargetCompID", extractTargetCompId(config));
        settings.setString(sessionId, "ConnectionType", "initiator");
        settings.setString(sessionId, "SocketConnectHost", config.getHost());
        settings.setString(sessionId, "SocketConnectPort", config.getPort().toString());
        settings.setLong(sessionId, "HeartBtInt", 30);

        return settings;
    }

    private Message convertToFixMessage(TransactionRequestDto request) throws FieldNotFound {
        NewOrderSingle order = new NewOrderSingle();

        Date now = new Date();
        LocalDateTime ldt = now.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        order.set(new ClOrdID(request.getTransactionId()));
        order.set(new HandlInst('1'));
        order.set(new Symbol(extractSymbol(request)));
        order.set(new Side(getFixSide(request)));
        order.setUtcTimeStamp(
                quickfix.field.TransactTime.FIELD,
                LocalDateTime.now(),
                true
        );
        order.set(new OrdType(OrdType.LIMIT));


        // Set quantity and price
        if (request.getAmount() != null) {
            order.set(new OrderQty(request.getAmount().doubleValue()));
        }
        if (request.getMetadata() != null && request.getMetadata().containsKey("price")) {
            order.set(new Price(Double.parseDouble(request.getMetadata().get("price").toString())));
        }

        // Set additional fields from metadata
        if (request.getMetadata() != null) {
            setAdditionalFields(order, request.getMetadata());
        }

        return order;
    }

    private FinancialMessageDto parseFixMessage(Message fixMessage) throws FieldNotFound {
        FinancialMessageDto message = new FinancialMessageDto();
        message.setMessageId(fixMessage.getField(new MsgType()).getValue());
        message.setProtocolType(getSupportedProtocol());
        message.setTimestamp(LocalDateTime.now());

        Map<String, Object> body = new HashMap<>();

        Iterator<Field<?>> iterator = fixMessage.iterator();
        while (iterator.hasNext()) {
            Field<?> field = iterator.next();
            body.put(String.valueOf(field.getTag()), field.getObject());
        }
        message.setBody(body);

        return message;
    }

    private void handleExecutionReport(quickfix.fix44.ExecutionReport executionReport) throws FieldNotFound {
        String clOrdId = executionReport.getClOrdID().getValue();
        String execId = executionReport.getExecID().getValue();

        TransactionResponseDto response = new TransactionResponseDto();
        response.setTransactionId(clOrdId);
        response.setReferenceNumber(execId);
        response.setTransactionDate(LocalDateTime.now());

        // Determine status from ExecType
        char execType = executionReport.getExecType().getValue();
        switch (execType) {
            case '0' -> response.setStatus(TransactionStatus.PENDING);
            case '2' -> response.setStatus(TransactionStatus.COMPLETED);
            case '8' -> response.setStatus(TransactionStatus.REJECTED);
            default -> response.setStatus(TransactionStatus.IN_PROGRESS);
        }

        completedTransactions.put(clOrdId, response);
        pendingResponses.remove(clOrdId);

        logger.info("Processed execution report for {}: {}", clOrdId, response.getStatus());
    }

    private TransactionResponseDto waitForExecutionReport(String clOrdId, long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            TransactionResponseDto response = completedTransactions.get(clOrdId);
            if (response != null) {
                completedTransactions.remove(clOrdId);
                return response;
            }
            Thread.sleep(100); // Wait 100ms between checks
        }

        throw new FinancialConnectionException("Timeout waiting for execution report: " + clOrdId);
    }

    private void waitForConnection(long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (connected) {
                return;
            }
            Thread.sleep(100);
        }

        throw new FinancialConnectionException("Timeout waiting for FIX connection");
    }

    private void sendLogoutMessage() {
        try {
            quickfix.fix44.Logout logout = new quickfix.fix44.Logout();
            Session.sendToTarget(logout, sessionId);
            Thread.sleep(1000); // Wait for logout to complete
        } catch (Exception e) {
            logger.warn("Error sending logout message", e);
        }
    }

    // Helper methods

    private String getBeginString(ProtocolType protocolType) {
        return switch (protocolType) {
            case FIX_4_2 -> "FIX.4.2";
            case FIX_5_0 -> "FIX.5.0";
            default -> "FIX.4.4";
        };
    }

    private String extractSenderCompId(ConnectionConfigDto config) {
        if (config.getAdditionalConfig() != null && config.getAdditionalConfig().containsKey("SenderCompID")) {
            return config.getAdditionalConfig().get("SenderCompID").toString();
        }
        return "FINCONNECT";
    }

    private String extractTargetCompId(ConnectionConfigDto config) {
        if (config.getAdditionalConfig() != null && config.getAdditionalConfig().containsKey("TargetCompID")) {
            return config.getAdditionalConfig().get("TargetCompID").toString();
        }
        return "EXCHANGE";
    }

    private String extractSymbol(TransactionRequestDto request) {
        if (request.getMetadata() != null && request.getMetadata().containsKey("symbol")) {
            return request.getMetadata().get("symbol").toString();
        }
        return "DEFAULT";
    }

    private char getFixSide(TransactionRequestDto request) {
        if (request.getMetadata() != null && request.getMetadata().containsKey("side")) {
            String side = request.getMetadata().get("side").toString().toUpperCase();
            return side.equals("SELL") ? Side.SELL : Side.BUY;
        }
        return Side.BUY;
    }

    private void setAdditionalFields(NewOrderSingle order, Map<String, Object> metadata) {
        if (metadata.containsKey("timeInForce")) {
            order.set(new TimeInForce(TimeInForce.DAY)); // Default to DAY
        }
        if (metadata.containsKey("currency")) {
            order.set(new Currency(metadata.get("currency").toString()));
        }
    }

    private int getCurrentSequenceNumber() {
        if (sessionId != null) {
            Session session = Session.lookupSession(sessionId);
            return session != null ? session.getExpectedSenderNum() : sequenceNumber;
        }
        return sequenceNumber;
    }

    private Message convertFinancialMessageToFix(FinancialMessageDto message) {
        // Implementation for converting generic financial messages to FIX format
        // This would depend on the specific message type and content
        return new NewOrderSingle(); // Placeholder
    }
}