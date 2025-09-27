package com.codedstreams.finconnectpro.sdk.adapter.impl;

import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.domain.enums.TransactionStatus;
import com.codedstreams.finconnectpro.sdk.dto.*;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * XML-based protocol adapter for SWIFT, SEPA, SOAP, and ISO 20022 messages.
 * <p>
 * This adapter handles financial messages in XML format using Spring Web Services.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see FinancialAdapter
 */
@Component
public class XmlAdapter implements FinancialAdapter {

    private static final Logger logger = LoggerFactory.getLogger(XmlAdapter.class);

    private boolean connected = false;
    private ConnectionConfigDto currentConfig;
    private WebServiceTemplate webServiceTemplate;
    private ProtocolType currentProtocol;
    private XmlMapper xmlMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(ConnectionConfigDto config) {
        try {
            logger.info("Initializing XML adapter for protocol: {}", config.getProtocolType());

            this.currentConfig = config;
            this.currentProtocol = config.getProtocolType();
            this.xmlMapper = new XmlMapper();

            initializeWebServiceTemplate(config);
            this.connected = true;

            logger.info("XML adapter connected successfully for protocol: {}", config.getProtocolType());

        } catch (Exception e) {
            this.connected = false;
            logger.error("XML connection failed for protocol: {}", config.getProtocolType(), e);
            throw new FinancialConnectionException(
                    "XML connection failed for protocol: " + config.getProtocolType(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        this.connected = false;
        this.currentConfig = null;
        this.webServiceTemplate = null;
        logger.info("XML adapter disconnected");
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
            throw new FinancialConnectionException("XML adapter is not connected");
        }

        try {
            logger.debug("Sending XML transaction: {}", request.getTransactionId());

            // Convert to appropriate XML format
            String xmlMessage = convertToXmlMessage(request);
            logger.trace("Generated XML message: {}", xmlMessage);

            // Send XML message
            String xmlResponse = sendXmlMessage(xmlMessage);
            logger.trace("Received XML response: {}", xmlResponse);

            // Parse XML response
            TransactionResponseDto response = parseXmlResponse(xmlResponse);
            logger.debug("XML transaction completed: {}", request.getTransactionId());

            return response;

        } catch (Exception e) {
            logger.error("XML transaction failed for {}", request.getTransactionId(), e);
            throw new TransactionFailedException(
                    request.getTransactionId(),
                    "XML transaction failed for protocol: " + currentProtocol,
                    "XML_ERROR",
                    e
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FinancialMessageDto receiveMessage() {
        // XML protocols typically use request-response or webhooks
        // This would need to be implemented based on specific protocol requirements
        logger.debug("Receive message not implemented for XML protocols");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(FinancialMessageDto message) {
        if (!connected) {
            throw new FinancialConnectionException("XML adapter is not connected");
        }

        try {
            String xmlMessage = convertFinancialMessageToXml(message);
            sendXmlMessage(xmlMessage);
            logger.debug("Sent XML message: {}", message.getMessageId());
        } catch (Exception e) {
            logger.error("Error sending XML message", e);
            throw new FinancialConnectionException("Error sending XML message", e);
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
            try {
                // Test connection by sending a simple health check message
                String healthCheckXml = createHealthCheckMessage();
                String response = sendXmlMessage(healthCheckXml);

                health.setStatusMessage("XML connection healthy - Protocol: " + currentProtocol);
                health.setMetrics(Map.of(
                        "protocol", currentProtocol.toString(),
                        "responseTime", "OK",
                        "messageFormat", "XML"
                ));
            } catch (Exception e) {
                health.setStatusMessage("XML health check failed: " + e.getMessage());
                health.setConnected(false);
            }
        } else {
            health.setStatusMessage("XML connection not established");
        }

        return health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProtocolType getSupportedProtocol() {
        return currentProtocol != null ? currentProtocol : ProtocolType.ISO_20022;
    }

    // XML-specific implementation methods

    private void initializeWebServiceTemplate(ConnectionConfigDto config) {
        this.webServiceTemplate = new WebServiceTemplate();

        // Configure timeouts
        // webServiceTemplate.setMessageSender(createMessageSender(config));

        // Configure interceptors for logging, security, etc.
        // webServiceTemplate.setInterceptors(new ClientInterceptor[]{new LoggingInterceptor()});

        logger.debug("WebServiceTemplate initialized for {}", config.getHost());
    }

    private String convertToXmlMessage(TransactionRequestDto request) throws Exception {
        return switch (currentProtocol) {
            case SWIFT_XML -> convertToSwiftMx(request);
            case SEPA_XML -> convertToSepaPain001(request);
            case SOAP_API -> convertToSoapEnvelope(request);
            case ISO_20022 -> convertToIso20022(request);
            default -> throw new UnsupportedOperationException("Unsupported XML protocol: " + currentProtocol);
        };
    }

    private String sendXmlMessage(String xmlMessage) throws Exception {
        Source source = new StringSource(xmlMessage);

        if (currentProtocol == ProtocolType.SOAP_API) {
            // SOAP requires specific action headers
            String soapAction = getSoapAction();
            SoapActionCallback callback = new SoapActionCallback(soapAction);

            Source result = webServiceTemplate.sendSourceAndReceive(
                    getServiceUrl(),
                    source,
                    callback,
                    message -> (Source) ((WebServiceMessage) message).getPayloadSource()
            );
            return sourceToString(result);

        } else {
            // RESTful XML or other protocols
            Source result = webServiceTemplate.sendSourceAndReceive(
                    getServiceUrl(),
                    source,
                    message -> (Source) ((WebServiceMessage) message).getPayloadSource()
            );
            return sourceToString(result);
        }

    }

    private TransactionResponseDto parseXmlResponse(String xmlResponse) throws Exception {
        return switch (currentProtocol) {
            case SWIFT_XML -> parseSwiftMxResponse(xmlResponse);
            case SEPA_XML -> parseSepaPain002Response(xmlResponse);
            case SOAP_API -> parseSoapResponse(xmlResponse);
            case ISO_20022 -> parseIso20022Response(xmlResponse);
            default -> parseGenericXmlResponse(xmlResponse);
        };
    }

    // XML Conversion Methods (Actual Implementation)

    private String convertToSwiftMx(TransactionRequestDto request) throws Exception {
        SwiftMxMessage mxMessage = new SwiftMxMessage();
        mxMessage.setMessageId(request.getTransactionId());
        mxMessage.setCreationDateTime(LocalDateTime.now());
        mxMessage.setInitiatingParty(createParty(request.getAccountNumber()));
        mxMessage.setPaymentAmount(request.getAmount());
        mxMessage.setCurrency(request.getCurrency());
        mxMessage.setDebtor(createParty(request.getAccountNumber()));
        mxMessage.setCreditor(createParty(request.getBeneficiaryAccount()));

        return xmlMapper.writeValueAsString(mxMessage);
    }

    private String convertToSepaPain001(TransactionRequestDto request) throws Exception {
        SepaPain001Message pain001 = new SepaPain001Message();
        pain001.setMessageId(request.getTransactionId());
        pain001.setCreationDateTime(LocalDateTime.now());
        pain001.setNumberOfTransactions(1);
        pain001.setInitiatingParty(createParty(request.getAccountNumber()));
        pain001.setPaymentInformation(createPaymentInfo(request));

        return xmlMapper.writeValueAsString(pain001);
    }

    private String convertToSoapEnvelope(TransactionRequestDto request) throws Exception {
        SoapEnvelope envelope = new SoapEnvelope();
        envelope.setBody(createSoapBody(request));
        return xmlMapper.writeValueAsString(envelope);
    }

    private String convertToIso20022(TransactionRequestDto request) throws Exception {
        Iso20022Message message = new Iso20022Message();
        message.setGroupHeader(createGroupHeader(request));
        message.setPaymentTransaction(createPaymentTransaction(request));
        return xmlMapper.writeValueAsString(message);
    }

    // Response Parsing Methods

    private TransactionResponseDto parseSwiftMxResponse(String xmlResponse) throws Exception {
        SwiftMxResponse response = xmlMapper.readValue(xmlResponse, SwiftMxResponse.class);
        return convertToTransactionResponse(response);
    }

    private TransactionResponseDto parseSepaPain002Response(String xmlResponse) throws Exception {
        SepaPain002Response response = xmlMapper.readValue(xmlResponse, SepaPain002Response.class);
        return convertToTransactionResponse(response);
    }

    private TransactionResponseDto parseSoapResponse(String xmlResponse) throws Exception {
        SoapResponse response = xmlMapper.readValue(xmlResponse, SoapResponse.class);
        return convertToTransactionResponse(response);
    }

    private TransactionResponseDto parseIso20022Response(String xmlResponse) throws Exception {
        Iso20022Response response = xmlMapper.readValue(xmlResponse, Iso20022Response.class);
        return convertToTransactionResponse(response);
    }

    // Helper Methods

    private String getServiceUrl() {
        String protocol = currentConfig.getPort() == 443 ? "https" : "http";
        return String.format("%s://%s:%d", protocol,
                currentConfig.getHost(), currentConfig.getPort());
    }

    private String getSoapAction() {
        if (currentConfig.getAdditionalConfig() != null) {
            return (String) currentConfig.getAdditionalConfig().get("soapAction");
        }
        return "http://tempuri.org/ProcessTransaction";
    }

    private String sourceToString(Source source) throws Exception {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, new StreamResult(writer));
        return writer.toString();
    }

    private String createHealthCheckMessage() throws Exception {
        return switch (currentProtocol) {
            case SWIFT_XML -> createSwiftHealthCheck();
            case SEPA_XML -> createSepaHealthCheck();
            case SOAP_API -> createSoapHealthCheck();
            case ISO_20022 -> createIso20022HealthCheck();
            default -> "<HealthCheck/>";
        };
    }

    // Placeholder classes for XML mapping (would be in separate files)

    private static class SwiftMxMessage {
        private String messageId;
        private LocalDateTime creationDateTime;
        private Object initiatingParty;
        private Object paymentAmount;
        private String currency;
        private Object debtor;
        private Object creditor;

        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public LocalDateTime getCreationDateTime() { return creationDateTime; }
        public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }
        public Object getInitiatingParty() { return initiatingParty; }
        public void setInitiatingParty(Object initiatingParty) { this.initiatingParty = initiatingParty; }
        public Object getPaymentAmount() { return paymentAmount; }
        public void setPaymentAmount(Object paymentAmount) { this.paymentAmount = paymentAmount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Object getDebtor() { return debtor; }
        public void setDebtor(Object debtor) { this.debtor = debtor; }
        public Object getCreditor() { return creditor; }
        public void setCreditor(Object creditor) { this.creditor = creditor; }
    }

    private static class SepaPain001Message {
        private String messageId;
        private LocalDateTime creationDateTime;
        private int numberOfTransactions;
        private Object initiatingParty;
        private Object paymentInformation;

        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public LocalDateTime getCreationDateTime() { return creationDateTime; }
        public void setCreationDateTime(LocalDateTime creationDateTime) { this.creationDateTime = creationDateTime; }
        public int getNumberOfTransactions() { return numberOfTransactions; }
        public void setNumberOfTransactions(int numberOfTransactions) { this.numberOfTransactions = numberOfTransactions; }
        public Object getInitiatingParty() { return initiatingParty; }
        public void setInitiatingParty(Object initiatingParty) { this.initiatingParty = initiatingParty; }
        public Object getPaymentInformation() { return paymentInformation; }
        public void setPaymentInformation(Object paymentInformation) { this.paymentInformation = paymentInformation; }
    }

    // Additional helper methods for object creation
    private Object createParty(String accountNumber) {
        return Map.of("accountNumber", accountNumber, "name", "Party Name");
    }

    private Object createPaymentInfo(TransactionRequestDto request) {
        return Map.of(
                "amount", request.getAmount(),
                "currency", request.getCurrency(),
                "debtor", createParty(request.getAccountNumber()),
                "creditor", createParty(request.getBeneficiaryAccount())
        );
    }

    private Object createSoapBody(TransactionRequestDto request) {
        return Map.of("transaction", request);
    }

    private Object createGroupHeader(TransactionRequestDto request) {
        return Map.of(
                "messageId", request.getTransactionId(),
                "creationDateTime", LocalDateTime.now()
        );
    }

    private Object createPaymentTransaction(TransactionRequestDto request) {
        return Map.of(
                "amount", request.getAmount(),
                "currency", request.getCurrency()
        );
    }

    // Response classes
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

    private static class SepaPain002Response {
        private String paymentId;
        private String transactionStatus;
        private String reasonCode;

        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        public String getTransactionStatus() { return transactionStatus; }
        public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }
        public String getReasonCode() { return reasonCode; }
        public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    }

    private static class SoapResponse {
        private String transactionId;
        private boolean success;
        private String message;

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    private static class Iso20022Response {
        private String transactionReference;
        private String status;
        private String additionalInformation;

        public String getTransactionReference() { return transactionReference; }
        public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAdditionalInformation() { return additionalInformation; }
        public void setAdditionalInformation(String additionalInformation) { this.additionalInformation = additionalInformation; }
    }

    private TransactionResponseDto convertToTransactionResponse(Object response) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setTransactionDate(LocalDateTime.now());

        if (response instanceof SwiftMxResponse swiftResponse) {
            dto.setStatus(swiftResponse.getStatus().equals("ACCEPTED") ?
                    TransactionStatus.COMPLETED :
                    TransactionStatus.FAILED);
            dto.setReferenceNumber(swiftResponse.getMessageId());
            dto.setMessage(swiftResponse.getReason());

        } else if (response instanceof SepaPain002Response sepaResponse) {
            dto.setStatus(sepaResponse.getTransactionStatus().equals("ACSC") ?
                    TransactionStatus.COMPLETED :
                    TransactionStatus.FAILED);
            dto.setReferenceNumber(sepaResponse.getPaymentId());
            dto.setMessage(sepaResponse.getReasonCode());

        } else if (response instanceof SoapResponse soapResponse) {
            dto.setStatus(soapResponse.isSuccess() ?
                    TransactionStatus.COMPLETED :
                    TransactionStatus.FAILED);
            dto.setReferenceNumber(soapResponse.getTransactionId());
            dto.setMessage(soapResponse.getMessage());

        } else if (response instanceof Iso20022Response isoResponse) {
            dto.setStatus(isoResponse.getStatus().equals("ACCEPTED") ?
                    TransactionStatus.COMPLETED :
                    TransactionStatus.FAILED);
            dto.setReferenceNumber(isoResponse.getTransactionReference());
            dto.setMessage(isoResponse.getAdditionalInformation());

        } else if (response instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) response;
            dto.setStatus(TransactionStatus.COMPLETED);
            dto.setReferenceNumber(responseMap.getOrDefault("reference", "UNKNOWN").toString());
            dto.setMessage(responseMap.getOrDefault("message", "Processed").toString());
            dto.setAdditionalInfo(responseMap);

        } else {
            // Default response for unknown types
            dto.setStatus(TransactionStatus.COMPLETED);
            dto.setReferenceNumber("AUTO_" + System.currentTimeMillis());
            dto.setMessage("Transaction processed successfully");
        }

        return dto;
    }

    private String convertFinancialMessageToXml(FinancialMessageDto message) throws Exception {
        return xmlMapper.writeValueAsString(message.getBody());
    }

    private TransactionResponseDto parseGenericXmlResponse(String xmlResponse) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = xmlMapper.readValue(xmlResponse, Map.class);

        TransactionResponseDto response = new TransactionResponseDto();
        response.setTransactionDate(LocalDateTime.now());
        response.setAdditionalInfo(responseMap);

        return response;
    }

    // Health check message creators
    private String createSwiftHealthCheck() throws Exception {
        SwiftMxMessage healthCheck = new SwiftMxMessage();
        healthCheck.setMessageId("HEALTH_CHECK_" + System.currentTimeMillis());
        healthCheck.setCreationDateTime(LocalDateTime.now());
        return xmlMapper.writeValueAsString(healthCheck);
    }

    private String createSepaHealthCheck() throws Exception {
        SepaPain001Message healthCheck = new SepaPain001Message();
        healthCheck.setMessageId("HEALTH_CHECK_" + System.currentTimeMillis());
        healthCheck.setCreationDateTime(LocalDateTime.now());
        return xmlMapper.writeValueAsString(healthCheck);
    }

    private String createSoapHealthCheck() throws Exception {
        SoapEnvelope envelope = new SoapEnvelope();
        envelope.setBody(Map.of("operation", "HealthCheck"));
        return xmlMapper.writeValueAsString(envelope);
    }

    private String createIso20022HealthCheck() throws Exception {
        Iso20022Message healthCheck = new Iso20022Message();
        healthCheck.setGroupHeader(createGroupHeader(new TransactionRequestDto()));
        return xmlMapper.writeValueAsString(healthCheck);
    }

    private static class SoapEnvelope {
        private Object body;

        public Object getBody() { return body; }
        public void setBody(Object body) { this.body = body; }
    }

    private static class Iso20022Message {
        private Object groupHeader;
        private Object paymentTransaction;

        public Object getGroupHeader() { return groupHeader; }
        public void setGroupHeader(Object groupHeader) { this.groupHeader = groupHeader; }
        public Object getPaymentTransaction() { return paymentTransaction; }
        public void setPaymentTransaction(Object paymentTransaction) { this.paymentTransaction = paymentTransaction; }
    }
}