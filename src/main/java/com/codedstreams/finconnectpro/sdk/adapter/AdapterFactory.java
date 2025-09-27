package com.codedstreams.finconnectpro.sdk.adapter;

import com.codedstreams.finconnectpro.sdk.adapter.impl.*;
import com.codedstreams.finconnectpro.sdk.domain.enums.ProtocolType;
import com.codedstreams.finconnectpro.sdk.exception.ProtocolNotSupportedException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory class for creating financial protocol adapters.
 * <p>
 * This factory is responsible for creating the appropriate adapter instances
 * based on the requested protocol type. It uses a registry pattern to map
 * protocol types to their corresponding adapter implementations.
 * </p>
 *
 * @author Nestor Martourez
 * @version 1.0.0
 * @see FinancialAdapter
 * @see ProtocolType
 */
@Component
public class AdapterFactory {

    private final Map<ProtocolType, Supplier<FinancialAdapter>> adapterRegistry;

    /**
     * Constructs a new AdapterFactory and initializes the adapter registry.
     */
    public AdapterFactory() {
        this.adapterRegistry = new EnumMap<>(ProtocolType.class);
        initializeRegistry();
    }

    /**
     * Initializes the registry with protocol type to adapter mappings.
     */
    private void initializeRegistry() {
        adapterRegistry.put(ProtocolType.FIX_4_2, FixProtocolAdapter::new);
        adapterRegistry.put(ProtocolType.FIX_5_0, FixProtocolAdapter::new);
        adapterRegistry.put(ProtocolType.REST_API, RestAdapter::new);
        adapterRegistry.put(ProtocolType.ISO_8583, Iso8583Adapter::new);
        adapterRegistry.put(ProtocolType.SWIFT_XML, SwiftAdapter::new);
        adapterRegistry.put(ProtocolType.SEPA_XML, XmlAdapter::new);
        adapterRegistry.put(ProtocolType.SOAP_API, XmlAdapter::new);
        adapterRegistry.put(ProtocolType.ISO_20022, XmlAdapter::new);
    }

    /**
     * Creates a new adapter instance for the specified protocol type.
     *
     * @param protocolType the protocol type for which to create an adapter
     * @return a new adapter instance
     * @throws ProtocolNotSupportedException if the protocol type is not supported
     */
    public FinancialAdapter createAdapter(ProtocolType protocolType) {
        Supplier<FinancialAdapter> adapterSupplier = adapterRegistry.get(protocolType);
        if (adapterSupplier == null) {
            throw new ProtocolNotSupportedException(
                    "Protocol not supported: " + protocolType);
        }
        return adapterSupplier.get();
    }

    /**
     * Registers a custom adapter implementation for a protocol type.
     * <p>
     * This method allows users to extend the SDK with custom adapter implementations
     * for specialized protocols or enhanced functionality.
     * </p>
     *
     * @param protocolType the protocol type to register
     * @param adapterSupplier a supplier that creates the adapter instance
     */
    public void registerAdapter(ProtocolType protocolType,
                                Supplier<FinancialAdapter> adapterSupplier) {
        adapterRegistry.put(protocolType, adapterSupplier);
    }

    /**
     * Checks if the factory supports the specified protocol type.
     *
     * @param protocolType the protocol type to check
     * @return true if supported, false otherwise
     */
    public boolean supportsProtocol(ProtocolType protocolType) {
        return adapterRegistry.containsKey(protocolType);
    }

    /**
     * Gets all supported protocol types.
     *
     * @return an array of supported protocol types
     */
    public ProtocolType[] getSupportedProtocols() {
        return adapterRegistry.keySet().toArray(new ProtocolType[0]);
    }
}
