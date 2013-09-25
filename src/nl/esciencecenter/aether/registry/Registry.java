/* $Id: Registry.java 12339 2010-09-16 14:14:07Z ceriel $ */

package nl.esciencecenter.aether.registry;


import java.io.IOException;
import java.util.Properties;

import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.IbisCapabilities;
import nl.esciencecenter.aether.IbisConfigurationException;
import nl.esciencecenter.aether.IbisProperties;
import nl.esciencecenter.aether.RegistryEventHandler;
import nl.esciencecenter.aether.impl.IbisIdentifier;

/**
 * This implementation of the {@link nl.esciencecenter.aether.Registry} interface defines the
 * API between an Ibis implementation and the registry. This way, an Ibis
 * implementation can dynamically load any registry implementation.
 */
public abstract class Registry implements nl.esciencecenter.aether.Registry {

    /**
     * Notifies the registry that the calling Ibis instance is leaving.
     * 
     * @exception IOException
     *                    may be thrown when communication with the registry
     *                    fails.
     */
    public abstract void leave() throws IOException;

    /**
     * Obtains a sequence number from the registry. Each sequencer has a name,
     * which must be provided to this call.
     * 
     * @param name
     *                the name of this sequencer.
     * @exception IOException
     *                    may be thrown when communication with the registry
     *                    fails.
     */
    public abstract long getSequenceNumber(String name) throws IOException;

    /**
     * Creates a registry for the specified Ibis instance.
     * 
     * @param handler
     *                the handler for registry events, or <code>null</code> if
     *                no registry events are needed.
     * @param properties
     *                to get some properties from, and to pass on to the
     *                registry.
     * @param data
     *                the implementation dependent data in the IbisIdentifier.
     * @param implementationVersion
     *                the identification of this Ibis implementation. Must be
     *                identical for all Ibises in a single pool.
     * @param tag
     *                the application level tag for the Ibis which is
     *                constructing this registry.
     * @param credentials
     *                authentication object to authenticate ibis at registry
     * @exception Throwable
     *                    can be any exception resulting from looking up the
     *                    registry constructor or the invocation attempt.
     */
    public static Registry createRegistry(IbisCapabilities capabilities,
            RegistryEventHandler handler, Properties properties, byte[] data,
            String implementationVersion, byte[] tag, Credentials credentials) throws Throwable {

        String registryName = properties
                .getProperty(IbisProperties.REGISTRY_IMPLEMENTATION);

        if (registryName == null) {
            throw new IbisConfigurationException("Could not create registry: "
                    + "property " + IbisProperties.REGISTRY_IMPLEMENTATION
                    + "  is not set.");
        } else if (registryName.equalsIgnoreCase("central")) {
            // shorthand for central registry
            return new nl.esciencecenter.aether.registry.central.client.Registry(capabilities, handler,
                    properties, data, implementationVersion, credentials, tag);
        } else if (registryName.equalsIgnoreCase("gossip")) {
            // shorthand for gossip registry
            return new nl.esciencecenter.aether.registry.gossip.Registry(capabilities, handler,
                    properties, data, implementationVersion, credentials, tag);
        } else if (registryName.equalsIgnoreCase("null")) {
            // shorthand for null registry
            return new nl.esciencecenter.aether.registry.NullRegistry(capabilities, handler,
                    properties, data, implementationVersion, credentials, tag);
        }

        Class<?> c = Class.forName(registryName);

        try {
            return (Registry) c.getConstructor(
                    new Class[] { IbisCapabilities.class,
                            RegistryEventHandler.class, Properties.class,
                            byte[].class, String.class, Credentials.class, byte[].class }).newInstance(
                    new Object[] { capabilities, handler, properties, data, implementationVersion, credentials, tag});
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Returns the Ibis identifier.
     */
    public abstract IbisIdentifier getIbisIdentifier();
    
    public abstract IbisIdentifier getRandomPoolMember();
    
    public abstract String[] wonElections();
}
