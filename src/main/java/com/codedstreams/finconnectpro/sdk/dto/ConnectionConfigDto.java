package com.codedstreams.finconnectpro.sdk.dto;

import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;

import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object for connection configuration parameters.
 * <p>
 * This class encapsulates all the configuration settings required to establish
 * a connection to a financial network using a specific protocol.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 */
public class ConnectionConfigDto {

    private ProtocolType protocolType;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Map<String, Object> additionalConfig;
    private Integer timeoutMs;
    private Integer retryAttempts;
    private String connectionName;

    /**
     * Default constructor.
     */
    public ConnectionConfigDto() {
        this.timeoutMs = 30000;
        this.retryAttempts = 3;
    }

    /**
     * Constructs a new ConnectionConfigDto with essential parameters.
     *
     * @param protocolType the protocol type for the connection
     * @param host the hostname or IP address of the financial network
     * @param port the port number for the connection
     */
    public ConnectionConfigDto(ProtocolType protocolType, String host, Integer port) {
        this();
        this.protocolType = protocolType;
        this.host = host;
        this.port = port;
    }

    /**
     * Gets the protocol type for this connection.
     *
     * @return the protocol type
     */
    public ProtocolType getProtocolType() {
        return protocolType;
    }

    /**
     * Sets the protocol type for this connection.
     *
     * @param protocolType the protocol type to set
     */
    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    /**
     * Gets the hostname or IP address of the financial network.
     *
     * @return the host address
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname or IP address of the financial network.
     *
     * @param host the host address to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port number for the connection.
     *
     * @return the port number
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the port number for the connection.
     *
     * @param port the port number to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Gets the username for authentication.
     *
     * @return the username, or null if not required
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for authentication.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password for authentication.
     *
     * @return the password, or null if not required
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for authentication.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets additional protocol-specific configuration parameters.
     *
     * @return a map of additional configuration parameters
     */
    public Map<String, Object> getAdditionalConfig() {
        return additionalConfig;
    }

    /**
     * Sets additional protocol-specific configuration parameters.
     *
     * @param additionalConfig a map of additional configuration parameters
     */
    public void setAdditionalConfig(Map<String, Object> additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    /**
     * Gets the connection timeout in milliseconds.
     *
     * @return the timeout in milliseconds
     */
    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Sets the connection timeout in milliseconds.
     *
     * @param timeoutMs the timeout in milliseconds to set
     */
    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Gets the number of retry attempts for failed connections.
     *
     * @return the number of retry attempts
     */
    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    /**
     * Sets the number of retry attempts for failed connections.
     *
     * @param retryAttempts the number of retry attempts to set
     */
    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    /**
     * Gets the descriptive name for this connection.
     *
     * @return the connection name, or null if not set
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * Sets the descriptive name for this connection.
     *
     * @param connectionName the connection name to set
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionConfigDto that = (ConnectionConfigDto) o;
        return protocolType == that.protocolType &&
                Objects.equals(host, that.host) &&
                Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolType, host, port);
    }

    @Override
    public String toString() {
        return "ConnectionConfigDto{" +
                "protocolType=" + protocolType +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", connectionName='" + connectionName + '\'' +
                '}';
    }

    /**
     * Builder class for creating ConnectionConfigDto instances fluently.
     */
    public static class Builder {
        private final ConnectionConfigDto instance;

        /**
         * Constructs a new Builder.
         */
        public Builder() {
            this.instance = new ConnectionConfigDto();
        }

        /**
         * Sets the protocol type.
         *
         * @param protocolType the protocol type
         * @return this builder instance
         */
        public Builder protocolType(ProtocolType protocolType) {
            instance.setProtocolType(protocolType);
            return this;
        }

        /**
         * Sets the host address.
         *
         * @param host the host address
         * @return this builder instance
         */
        public Builder host(String host) {
            instance.setHost(host);
            return this;
        }

        /**
         * Sets the port number.
         *
         * @param port the port number
         * @return this builder instance
         */
        public Builder port(Integer port) {
            instance.setPort(port);
            return this;
        }

        /**
         * Sets the authentication credentials.
         *
         * @param username the username
         * @param password the password
         * @return this builder instance
         */
        public Builder credentials(String username, String password) {
            instance.setUsername(username);
            instance.setPassword(password);
            return this;
        }

        /**
         * Sets the connection timeout.
         *
         * @param timeoutMs the timeout in milliseconds
         * @return this builder instance
         */
        public Builder timeoutMs(Integer timeoutMs) {
            instance.setTimeoutMs(timeoutMs);
            return this;
        }

        /**
         * Sets the connection name.
         *
         * @param connectionName the descriptive name
         * @return this builder instance
         */
        public Builder connectionName(String connectionName) {
            instance.setConnectionName(connectionName);
            return this;
        }

        /**
         * Builds the ConnectionConfigDto instance.
         *
         * @return the configured ConnectionConfigDto
         */
        public ConnectionConfigDto build() {
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

