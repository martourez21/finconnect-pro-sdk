package com.codedstreams.finconnectpro.sdk.domain.enums;

/**
 * Enumeration of financial channel types.
 * <p>
 * Defines the different types of financial channels and networks
 * that the SDK can connect to.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public enum ChannelType {

    /**
     * Bank connectivity channels for core banking systems.
     */
    BANK_CONNECTIVITY,

    /**
     * Foreign exchange and bulk transfer channels.
     */
    FX_BULK_TRANSFERS,

    /**
     * Liquidity provider channels for market making.
     */
    LIQUIDITY_PROVIDERS,

    /**
     * Institutional payment channels for large transfers.
     */
    INSTITUTIONAL_PAYMENTS,

    /**
     * Card network channels (Visa, Mastercard, etc.).
     */
    CARD_NETWORKS,

    /**
     * Secondary market channels for securities trading.
     */
    SECONDARY_CHANNELS,

    /**
     * Regulatory reporting channels for compliance.
     */
    REGULATORY_REPORTING,

    /**
     * Clearing and settlement channels.
     */
    CLEARING_SETTLEMENT,

    /**
     * Trading exchange channels for securities.
     */
    TRADING_EXCHANGES,

    /**
     * Alternative payment system channels.
     */
    ALTERNATIVE_PAYMENTS
}
