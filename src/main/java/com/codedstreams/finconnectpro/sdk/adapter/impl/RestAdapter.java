package com.codedstreams.finconnectpro.sdk.adapter.impl;


import com.codedstreams.finconnectpro.sdk.adapter.FinancialAdapter;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.dto.*;
import com.codedstreams.finconnectpro.sdk.exception.FinancialConnectionException;
import com.codedstreams.finconnectpro.sdk.exception.TransactionFailedException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST API adapter implementation for modern financial services and fintech platforms.
 * <p>
 * This adapter handles communication with RESTful financial APIs using JSON over HTTP/HTTPS.
 * It supports authentication, request/response mapping, and error handling for REST-based financial services.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see FinancialAdapter
 */
@Component
public class RestAdapter implements FinancialAdapter {

    private RestTemplate restTemplate;
    private String baseUrl;
    private HttpHeaders defaultHeaders;
    private boolean connected = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(ConnectionConfigDto config) {
        try {
            this.baseUrl = buildBaseUrl(config);
            this.restTemplate = createRestTemplate(config);
            this.defaultHeaders = createDefaultHeaders(config);
            this.connected = true;

        } catch (Exception e) {
            this.connected = false;
            throw new FinancialConnectionException(
                    "REST connection failed to " + config.getHost(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        this.connected = false;
        this.restTemplate = null;
        this.baseUrl = null;
        this.defaultHeaders = null;
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
            throw new FinancialConnectionException("REST adapter is not connected");
        }

        try {
            // Prepare HTTP request
            HttpEntity<TransactionRequestDto> requestEntity = new HttpEntity<>(request, defaultHeaders);

            // Send POST request to transactions endpoint
            ResponseEntity<TransactionResponseDto> response = restTemplate.exchange(
                    baseUrl + "/transactions",
                    HttpMethod.POST,
                    requestEntity,
                    TransactionResponseDto.class
            );

            // Validate response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new TransactionFailedException(
                        request.getTransactionId(),
                        "REST API returned status: " + response.getStatusCode(),
                        "REST_ERROR"
                );
            }

        } catch (Exception e) {
            throw new TransactionFailedException(
                    request.getTransactionId(),
                    "REST transaction failed: " + e.getMessage(),
                    "REST_ERROR",
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
            // Poll for incoming messages (webhook simulation)
            ResponseEntity<FinancialMessageDto> response = restTemplate.exchange(
                    baseUrl + "/messages/receive",
                    HttpMethod.GET,
                    new HttpEntity<>(defaultHeaders),
                    FinancialMessageDto.class
            );

            return response.getStatusCode() == HttpStatus.OK ? response.getBody() : null;

        } catch (Exception e) {
            throw new FinancialConnectionException("Error receiving REST message", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(FinancialMessageDto message) {
        if (!connected) {
            throw new FinancialConnectionException("REST adapter is not connected");
        }

        try {
            HttpEntity<FinancialMessageDto> requestEntity = new HttpEntity<>(message, defaultHeaders);

            restTemplate.exchange(
                    baseUrl + "/messages/send",
                    HttpMethod.POST,
                    requestEntity,
                    Void.class
            );

        } catch (Exception e) {
            throw new FinancialConnectionException("Error sending REST message", e);
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
        health.setLastCheck(java.time.LocalDateTime.now());

        if (connected) {
            try {
                // Perform actual health check by calling health endpoint
                ResponseEntity<Map> response = restTemplate.exchange(
                        baseUrl + "/health",
                        HttpMethod.GET,
                        new HttpEntity<>(defaultHeaders),
                        Map.class
                );

                health.setStatusMessage("REST connection healthy - Status: " + response.getStatusCode());
                health.setMetrics(Map.of(
                        "responseTime", System.currentTimeMillis(),
                        "httpStatus", response.getStatusCodeValue()
                ));

            } catch (Exception e) {
                health.setStatusMessage("REST health check failed: " + e.getMessage());
                health.setConnected(false);
            }
        } else {
            health.setStatusMessage("REST connection not established");
        }

        return health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.REST_API;
    }

    // Helper methods

    private String buildBaseUrl(ConnectionConfigDto config) {
        String protocol = config.getPort() == 443 ? "https" : "http";
        return String.format("%s://%s:%d", protocol, config.getHost(), config.getPort());
    }

    private RestTemplate createRestTemplate(ConnectionConfigDto config) {
        RestTemplate template = new RestTemplate();

        // Configure timeouts
        // template.setRequestFactory(createRequestFactory(config));

        // Configure interceptors for authentication, logging, etc.
        // template.getInterceptors().add(createAuthInterceptor(config));

        return template;
    }

    private HttpHeaders createDefaultHeaders(ConnectionConfigDto config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        // Add authentication headers
        if (config.getUsername() != null && config.getPassword() != null) {
            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
        }

        // Add additional headers from config
        if (config.getAdditionalConfig() != null) {
            Object headersConfig = config.getAdditionalConfig().get("headers");
            if (headersConfig instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> headerMap = (Map<String, String>) headersConfig;
                headerMap.forEach(headers::set);
            }
        }

        return headers;
    }
}
