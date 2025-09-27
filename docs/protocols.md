# Protocol Guide

## Supported Protocols

### FIX Protocol
**Use Case:** Electronic trading
**Library:** QuickFIX/J
**Message Format:** Binary

```java
ConnectionConfigDto fixConfig = ConnectionConfigDto.builder()
    .protocolType(ProtocolType.FIX_4_2)
    .host("fix.exchange.com")
    .port(5001)
    .build();
ISO-8583
Use Case: Card payments
Library: jPOS
Message Format: Binary

SWIFT MX/MT
Use Case: International payments
Formats: XML (MX), Text (MT)

SEPA XML
Use Case: European payments
Standard: ISO 20022
Format: XML

Protocol Selection Guide
Use Case	Recommended Protocol
Stock trading	FIX 4.2/5.0
Card payments	ISO-8583
International wires	SWIFT MX
EU payments	SEPA XML
Modern APIs	REST API
Legacy systems	SOAP
```