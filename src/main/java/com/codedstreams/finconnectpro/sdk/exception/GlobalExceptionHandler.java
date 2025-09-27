package com.codedstreams.finconnectpro.sdk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Global exception handler for the FinConnect Pro SDK.
 * <p>
 * This class handles all exceptions thrown by the SDK and returns RFC 9745 compliant
 * error responses. RFC 9745 standardizes the format for HTTP API problem details,
 * providing consistent error handling across all financial network operations.
 * </p>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9745.html">RFC 9745 - HTTP API Problem Details</a>
 * @author Nestor Martourez
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * RFC 9745 Problem Details object.
     */
    public static class ProblemDetail {
        private final String type;
        private final String title;
        private final int status;
        private final String detail;
        private final String instance;
        private final OffsetDateTime timestamp;
        private final String errorId;

        /**
         * Constructs a new ProblemDetail instance.
         *
         * @param type the problem type URI
         * @param title the problem title
         * @param status the HTTP status code
         * @param detail the problem detail
         * @param instance the instance URI
         */
        public ProblemDetail(String type, String title, int status, String detail, String instance) {
            this.type = type;
            this.title = title;
            this.status = status;
            this.detail = detail;
            this.instance = instance;
            this.timestamp = OffsetDateTime.now();
            this.errorId = UUID.randomUUID().toString();
        }

        // Getters for all fields
        public String getType() { return type; }
        public String getTitle() { return title; }
        public int getStatus() { return status; }
        public String getDetail() { return detail; }
        public String getInstance() { return instance; }
        public OffsetDateTime getTimestamp() { return timestamp; }
        public String getErrorId() { return errorId; }
    }

    /**
     * Extended Problem Details for financial-specific errors.
     */
    public static class FinancialProblemDetail extends ProblemDetail {
        private final String errorCode;
        private final String transactionId;
        private final String protocolType;

        /**
         * Constructs a new FinancialProblemDetail instance.
         *
         * @param type the problem type URI
         * @param title the problem title
         * @param status the HTTP status code
         * @param detail the problem detail
         * @param instance the instance URI
         * @param errorCode the financial error code
         * @param transactionId the transaction identifier
         * @param protocolType the protocol type
         */
        public FinancialProblemDetail(String type, String title, int status, String detail,
                                      String instance, String errorCode, String transactionId, String protocolType) {
            super(type, title, status, detail, instance);
            this.errorCode = errorCode;
            this.transactionId = transactionId;
            this.protocolType = protocolType;
        }

        // Getters for additional fields
        public String getErrorCode() { return errorCode; }
        public String getTransactionId() { return transactionId; }
        public String getProtocolType() { return protocolType; }
    }

    // Problem type URIs according to RFC 9745
    private static final String PROBLEM_BASE_URI = "https://api.finconnect.pro/problems/";
    private static final String CONNECTION_PROBLEM_TYPE = PROBLEM_BASE_URI + "connection-error";
    private static final String TRANSACTION_PROBLEM_TYPE = PROBLEM_BASE_URI + "transaction-error";
    private static final String PROTOCOL_PROBLEM_TYPE = PROBLEM_BASE_URI + "protocol-error";
    private static final String VALIDATION_PROBLEM_TYPE = PROBLEM_BASE_URI + "validation-error";
    private static final String INTERNAL_PROBLEM_TYPE = PROBLEM_BASE_URI + "internal-error";

    /**
     * Handles FinancialConnectionException instances.
     *
     * @param ex the thrown exception
     * @param request the web request
     * @return RFC 9745 compliant problem detail response
     */
    @ExceptionHandler(FinancialConnectionException.class)
    public ResponseEntity<FinancialProblemDetail> handleFinancialConnectionException(
            FinancialConnectionException ex, WebRequest request) {

        FinancialProblemDetail problemDetail = new FinancialProblemDetail(
                CONNECTION_PROBLEM_TYPE,
                "Financial Connection Error",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                request.getDescription(false),
                "CONNECTION_FAILURE",
                ex.getConnectionId(),
                ex.getProtocolType()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Content-Type", "application/problem+json") // RFC 9745 media type
                .body(problemDetail);
    }

    /**
     * Handles TransactionFailedException instances.
     *
     * @param ex the thrown exception
     * @param request the web request
     * @return RFC 9745 compliant problem detail response
     */
    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<FinancialProblemDetail> handleTransactionFailedException(
            TransactionFailedException ex, WebRequest request) {

        HttpStatus status = determineTransactionErrorStatus(ex.getErrorCode());

        FinancialProblemDetail problemDetail = new FinancialProblemDetail(
                TRANSACTION_PROBLEM_TYPE,
                "Transaction Processing Error",
                status.value(),
                ex.getMessage(),
                request.getDescription(false),
                ex.getErrorCode(),
                ex.getTransactionId(),
                ex.getProtocolType()
        );

        return ResponseEntity
                .status(status)
                .header("Content-Type", "application/problem+json")
                .body(problemDetail);
    }

    /**
     * Handles ProtocolNotSupportedException instances.
     *
     * @param ex the thrown exception
     * @param request the web request
     * @return RFC 9745 compliant problem detail response
     */
    @ExceptionHandler(ProtocolNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleProtocolNotSupportedException(
            ProtocolNotSupportedException ex, WebRequest request) {

        ProblemDetail problemDetail = new ProblemDetail(
                PROTOCOL_PROBLEM_TYPE,
                "Protocol Not Supported",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/problem+json")
                .body(problemDetail);
    }

    /**
     * Handles IllegalArgumentException instances (validation errors).
     *
     * @param ex the thrown exception
     * @param request the web request
     * @return RFC 9745 compliant problem detail response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ProblemDetail problemDetail = new ProblemDetail(
                VALIDATION_PROBLEM_TYPE,
                "Validation Error",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false)
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/problem+json")
                .body(problemDetail);
    }

    /**
     * Handles all other uncaught exceptions.
     *
     * @param ex the thrown exception
     * @param request the web request
     * @return RFC 9745 compliant problem detail response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, WebRequest request) {

        // Log the full exception for internal debugging
        logger.error("Unhandled exception occurred", ex);

        ProblemDetail problemDetail = new ProblemDetail(
                INTERNAL_PROBLEM_TYPE,
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please contact support with error ID: " +
                        UUID.randomUUID().toString(),
                request.getDescription(false)
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/problem+json")
                .body(problemDetail);
    }

    /**
     * Determines the appropriate HTTP status code for transaction errors based on error codes.
     *
     * @param errorCode the financial error code
     * @return the appropriate HTTP status code
     */
    private HttpStatus determineTransactionErrorStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // Map financial error codes to HTTP status codes
        return switch (errorCode.toUpperCase()) {
            case "INSUFFICIENT_FUNDS", "LIMIT_EXCEEDED" -> HttpStatus.PAYMENT_REQUIRED;
            case "INVALID_ACCOUNT", "BENEFICIARY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "UNAUTHORIZED", "AUTHENTICATION_FAILED" -> HttpStatus.UNAUTHORIZED;
            case "VALIDATION_ERROR", "INVALID_AMOUNT" -> HttpStatus.BAD_REQUEST;
            case "TIMEOUT", "NETWORK_ERROR" -> HttpStatus.GATEWAY_TIMEOUT;
            case "MAINTENANCE", "SERVICE_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Creates a standardized problem detail response builder.
     *
     * @param type the problem type URI
     * @param title the problem title
     * @param status the HTTP status code
     * @return a ResponseEntity builder with RFC 9745 headers
     */
    private ResponseEntity.BodyBuilder createProblemResponseBuilder(String type, String title, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .header("Content-Type", "application/problem+json")
                .header("Content-Language", "en") // RFC 9745 recommends Content-Language
                .header("Problem-Type", type); // Custom header for problem type
    }
}