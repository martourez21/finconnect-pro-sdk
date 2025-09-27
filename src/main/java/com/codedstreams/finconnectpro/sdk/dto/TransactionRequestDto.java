package com.codedstreams.finconnectpro.sdk.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object for transaction requests.
 * <p>
 * This class encapsulates all the information required to initiate a financial
 * transaction through any of the supported protocols. It includes transaction
 * details, party information, and additional metadata.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class TransactionRequestDto {

    private String transactionId;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String beneficiaryAccount;
    private String beneficiaryName;
    private String beneficiaryBankCode;
    private String description;
    private Map<String, Object> metadata;
    private String transactionType;
    private String urgency;
    private String referenceNumber;

    /**
     * Default constructor.
     */
    public TransactionRequestDto() {
        // Default constructor
    }

    /**
     * Gets the unique transaction identifier.
     *
     * @return the transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the unique transaction identifier.
     *
     * @param transactionId the transaction ID to set
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Gets the source account number for the transaction.
     *
     * @return the account number
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the source account number for the transaction.
     *
     * @param accountNumber the account number to set
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Gets the transaction amount.
     *
     * @return the transaction amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the transaction amount.
     *
     * @param amount the transaction amount to set
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Gets the currency code for the transaction (ISO 4217).
     *
     * @return the currency code (e.g., "USD", "EUR")
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the currency code for the transaction (ISO 4217).
     *
     * @param currency the currency code to set (e.g., "USD", "EUR")
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Gets the beneficiary account number.
     *
     * @return the beneficiary account number
     */
    public String getBeneficiaryAccount() {
        return beneficiaryAccount;
    }

    /**
     * Sets the beneficiary account number.
     *
     * @param beneficiaryAccount the beneficiary account number to set
     */
    public void setBeneficiaryAccount(String beneficiaryAccount) {
        this.beneficiaryAccount = beneficiaryAccount;
    }

    /**
     * Gets the beneficiary name.
     *
     * @return the beneficiary name
     */
    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    /**
     * Sets the beneficiary name.
     *
     * @param beneficiaryName the beneficiary name to set
     */
    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    /**
     * Gets the beneficiary bank code (BIC, SWIFT code, etc.).
     *
     * @return the beneficiary bank code
     */
    public String getBeneficiaryBankCode() {
        return beneficiaryBankCode;
    }

    /**
     * Sets the beneficiary bank code (BIC, SWIFT code, etc.).
     *
     * @param beneficiaryBankCode the beneficiary bank code to set
     */
    public void setBeneficiaryBankCode(String beneficiaryBankCode) {
        this.beneficiaryBankCode = beneficiaryBankCode;
    }

    /**
     * Gets the transaction description or purpose.
     *
     * @return the transaction description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the transaction description or purpose.
     *
     * @param description the transaction description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets additional metadata for the transaction.
     *
     * @return a map of metadata key-value pairs
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets additional metadata for the transaction.
     *
     * @param metadata a map of metadata key-value pairs to set
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the transaction type (e.g., "TRANSFER", "PAYMENT", "WITHDRAWAL").
     *
     * @return the transaction type
     */
    public String getTransactionType() {
        return transactionType;
    }

    /**
     * Sets the transaction type (e.g., "TRANSFER", "PAYMENT", "WITHDRAWAL").
     *
     * @param transactionType the transaction type to set
     */
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Gets the urgency level of the transaction (e.g., "NORMAL", "URGENT").
     *
     * @return the urgency level
     */
    public String getUrgency() {
        return urgency;
    }

    /**
     * Sets the urgency level of the transaction (e.g., "NORMAL", "URGENT").
     *
     * @param urgency the urgency level to set
     */
    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    /**
     * Gets the client reference number for the transaction.
     *
     * @return the reference number
     */
    public String getReferenceNumber() {
        return referenceNumber;
    }

    /**
     * Sets the client reference number for the transaction.
     *
     * @param referenceNumber the reference number to set
     */
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRequestDto that = (TransactionRequestDto) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "TransactionRequestDto{" +
                "transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", beneficiaryAccount='" + beneficiaryAccount + '\'' +
                '}';
    }

    /**
     * Builder class for creating TransactionRequestDto instances fluently.
     */
    public static class Builder {
        private final TransactionRequestDto instance;

        /**
         * Constructs a new Builder.
         */
        public Builder() {
            this.instance = new TransactionRequestDto();
        }

        /**
         * Sets the transaction identifier.
         *
         * @param transactionId the transaction ID
         * @return this builder instance
         */
        public Builder transactionId(String transactionId) {
            instance.setTransactionId(transactionId);
            return this;
        }

        /**
         * Sets the account number.
         *
         * @param accountNumber the account number
         * @return this builder instance
         */
        public Builder accountNumber(String accountNumber) {
            instance.setAccountNumber(accountNumber);
            return this;
        }

        /**
         * Sets the transaction amount and currency.
         *
         * @param amount the transaction amount
         * @param currency the currency code
         * @return this builder instance
         */
        public Builder amount(BigDecimal amount, String currency) {
            instance.setAmount(amount);
            instance.setCurrency(currency);
            return this;
        }

        /**
         * Sets the beneficiary details.
         *
         * @param account the beneficiary account number
         * @param name the beneficiary name
         * @return this builder instance
         */
        public Builder beneficiary(String account, String name) {
            instance.setBeneficiaryAccount(account);
            instance.setBeneficiaryName(name);
            return this;
        }

        /**
         * Sets the transaction description.
         *
         * @param description the description
         * @return this builder instance
         */
        public Builder description(String description) {
            instance.setDescription(description);
            return this;
        }

        /**
         * Sets the transaction type.
         *
         * @param transactionType the transaction type
         * @return this builder instance
         */
        public Builder transactionType(String transactionType) {
            instance.setTransactionType(transactionType);
            return this;
        }

        /**
         * Builds the TransactionRequestDto instance.
         *
         * @return the configured TransactionRequestDto
         */
        public TransactionRequestDto build() {
            return instance;
        }
    }

    /**
     * Creates a new builder instance for fluent configuration.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
