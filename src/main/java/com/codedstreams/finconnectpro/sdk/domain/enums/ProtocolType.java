package com.codedstreams.finconnectpro.sdk.domain.enums;

/**
 * Enumeration of supported financial protocols.
 * <p>
 * This enum defines all the financial protocols that the SDK can handle.
 * Each protocol represents a different financial messaging standard or API.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public enum ProtocolType {

    /**
     * FIX Protocol version 4.2 - Financial Information Exchange.
     * Used for electronic trading and market data.
     */
    FIX_4_2,

    /**
     * FIX Protocol version 5.0 - Financial Information Exchange.
     * Enhanced version with additional features for trading.
     */
    FIX_5_0,

    /**
     * REST API protocol for modern web-based financial services.
     * Uses JSON/HTTP for communication with fintech platforms.
     */
    REST_API,

    /**
     * ISO-8583 protocol for card payments and ATM networks.
     * Standard for Visa, Mastercard, and other card processors.
     */
    ISO_8583,

    /**
     * SWIFT XML protocol for international payments (MX messages).
     * Modern XML-based standard for cross-border transactions.
     */
    SWIFT_XML,

    /**
     * SEPA XML protocol for European payments (ISO 20022).
     * Standard for Single Euro Payments Area transactions.
     */
    SEPA_XML,

    /**
     * SOAP Web Services protocol for enterprise banking systems.
     * Used for corporate banking and legacy system integration.
     */
    SOAP_API,

    /**
     * Generic ISO 20022 XML protocol for global financial messaging.
     * Universal standard adopted by 200+ countries.
     */
    ISO_20022,

    /**
     * Proprietary financial network protocols.
     * Used for custom or specialized financial network connections.
     */
    FINANCIAL_NETWORK_CONNECTIX
}
