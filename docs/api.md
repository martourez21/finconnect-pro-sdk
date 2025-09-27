# FinConnect Pro SDK - API Documentation

## Overview
Complete API reference for the FinConnect Pro SDK with examples and usage patterns.

## Core Interfaces

### FinancialNetworkFacade
Main entry point for SDK usage.

**Key Methods:**
```java
// Establish connection
String connectionId = facade.establishConnection(config);

// Execute payment with auto-protocol selection
TransactionResponseDto response = facade.executePayment(request);

// Check system health
HealthCheckDto health = facade.getOverallSystemHealth();
FinancialAdapter Interface
Protocol adapter contract.

java
public interface FinancialAdapter {
    void connect(ConnectionConfigDto config);
    TransactionResponseDto sendTransaction(TransactionRequestDto request);
    HealthCheckDto healthCheck();
}
Data Transfer Objects
ConnectionConfigDto
java
ConnectionConfigDto config = ConnectionConfigDto.builder()
    .protocolType(ProtocolType.SWIFT_XML)
    .host("swift.bank.com")
    .port(5001)
    .credentials("user", "pass")
    .build();
TransactionRequestDto
java
TransactionRequestDto request = TransactionRequestDto.builder()
    .transactionId("TXN-123")
    .amount(new BigDecimal("1000.00"))
    .currency("USD")
    .accountNumber("123456789")
    .beneficiaryAccount("987654321")
    .build();
Response Handling
java
if (response.isSuccess()) {
    // Payment successful
} else if (response.isFailed()) {
    // Handle failure
    logger.error("Transaction failed: {}", response.getMessage());
}
Exception Handling
java
try {
    TransactionResponseDto response = facade.executePayment(request);
} catch (TransactionFailedException e) {
    // RFC 9745 compliant error handling
    logger.error("Transaction {} failed: {}", e.getTransactionId(), e.getMessage());
}
```