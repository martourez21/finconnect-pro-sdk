package com.codedstreams.finconnectpro.sdk.dto;

import com.codedstreams.finconnectpro.sdk.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object for transaction responses.
 * <p>
 * This class encapsulates the response received from a financial network after
 * processing a transaction request. It includes status information, reference
 * numbers, and any additional response data.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class TransactionResponseDto {

    private String transactionId;
    private TransactionStatus status;
    private String referenceNumber;
    private LocalDateTime transactionDate;
    private String message;
    private Map<String, Object> additionalInfo;
    private String errorCode;
    private String errorDescription;
    private String networkReference;
    private BigDecimal fees;
    private String currency;

    /**
     * Default constructor.
     */
    public TransactionResponseDto() {
        this.transactionDate = LocalDateTime.now();
    }

    /**
     * Gets the original transaction identifier.
     *
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the original transaction identifier.
     *
     * @param transactionId the transaction ID to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Gets the status of the transaction.
     *
     * @return the transaction status
     */
    public TransactionStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the transaction.
     *
     * @param status the transaction status to set
     */
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    /**
     * Gets the reference number assigned by the financial network.
     *
     * @return the reference number
     */
    public String getReferenceNumber() {
        return referenceNumber;
    }

    /**
     * Sets the reference number assigned by the financial network.
     *
     * @param referenceNumber the reference number to set
     */
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    /**
     * Gets the date and time when the transaction was processed.
     *
     * @return the transaction date
     */
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets the date and time when the transaction was processed.
     *
     * @param transactionDate the transaction date to set
     */
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Gets a human-readable message about the transaction result.
     *
     * @return the message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets a human-readable message about the transaction result.
     *
     * @param message the message text to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets additional information from the financial network.
     *
     * @return a map of additional information
     */
    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Sets additional information from the financial network.
     *
     * @param additionalInfo a map of additional information to set
     */
    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * Gets the error code if the transaction failed.
     *
     * @return the error code, or null if successful
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code if the transaction failed.
     *
     * @param errorCode the error code to set
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets the error description if the transaction failed.
     *
     * @return the error description, or null if successful
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Sets the error description if the transaction failed.
     *
     * @param errorDescription the error description to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * Gets the network-specific reference number.
     *
     * @return the network reference
     */
    public String getNetworkReference() {
        return networkReference;
    }

    /**
     * Sets the network-specific reference number.
     *
     * @param networkReference the network reference to set
     */
    public void setNetworkReference(String networkReference) {
        this.networkReference = networkReference;
    }

    /**
     * Gets the transaction fees charged by the network.
     *
     * @return the fees amount
     */
    public BigDecimal getFees() {
        return fees;
    }

    /**
     * Sets the transaction fees charged by the network.
     *
     * @param fees the fees amount to set
     */
    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    /**
     * Gets the currency code for the fees.
     *
     * @return the currency code
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency code for the fees.
     *
     * @param currency the currency code to set
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Checks if the transaction was successful.
     *
     * @return true if the transaction completed successfully, false otherwise
     */
    public boolean isSuccess() {
        return status == TransactionStatus.COMPLETED ||
                status == TransactionStatus.SETTLED ||
                status == TransactionStatus.AUTHORIZED;
    }

    /**
     * Checks if the transaction failed.
     *
     * @return true if the transaction failed, false otherwise
     */
    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    /**
     * Checks if the transaction is still pending.
     *
     * @return true if the transaction is pending, false otherwise
     */
    public boolean isPending() {
        return status == TransactionStatus.PENDING ||
                status == TransactionStatus.IN_PROGRESS ||
                status == TransactionStatus.AWAITING_AUTHORIZATION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionResponseDto that = (TransactionResponseDto) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "TransactionResponseDto{" +
                "transactionId='" + transactionId + '\'' +
                ", status=" + status +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
