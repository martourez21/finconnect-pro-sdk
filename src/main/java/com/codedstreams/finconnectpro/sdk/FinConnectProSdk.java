package com.codedstreams.finconnectpro.sdk;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the FinConnect Pro SDK.
 * <p>
 * This class serves as the primary configuration class for the SDK and enables
 * auto-configuration of all FinConnect Pro components when included as a dependency.
 * </p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @SpringBootApplication
 * @Import(FinConnectProSdk.class)
 * public class MyBankApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyBankApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @since 2025.1.0
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class FinConnectProSdk {

    /**
     * SDK version constant.
     */
    public static final String VERSION = "1.0.0";

    /**
     * Default constructor for Spring configuration.
     */
    public FinConnectProSdk() {
        // SDK auto-configuration
    }

    /**
     * Returns the current version of the SDK.
     *
     * @return the SDK version string
     */
    public static String getVersion() {
        return VERSION;
    }
}
