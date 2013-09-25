package nl.esciencecenter.aether.registry;


import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.IbisCapabilities;
import nl.esciencecenter.aether.IbisConfigurationException;
import nl.esciencecenter.aether.IbisProperties;
import nl.esciencecenter.aether.NoSuchPropertyException;
import nl.esciencecenter.aether.RegistryEventHandler;
import nl.esciencecenter.aether.impl.Ibis;
import nl.esciencecenter.aether.impl.IbisIdentifier;
import nl.esciencecenter.aether.impl.Location;

/**
 * 
 * Registry implementation that does nothing. Throws an Exception most calls.
 * 
 */
public final class NullRegistry extends nl.esciencecenter.aether.registry.Registry {

    private final IbisIdentifier identifier;

    @Override
    public long getSequenceNumber(String name) throws IOException {
        throw new IbisConfigurationException(
                "sequence numbers not supported by NullRegistry");
    }

    /**
     * Creates a Null Registry.
     * 
     * @param handler
     *                registry handler to pass events to.
     * @param properties
     *                properties of this registry.
     * @param data
     *                Ibis implementation data to attach to the IbisIdentifier.
     * @param tag
     *                A tag provided by the application for this ibis instance.
     * @param implementationVersion
     *                the identification of this ibis implementation, including
     *                version, class and such. Must be identical for all ibises
     *                in a single pool.
     * @throws IOException
     *                 in case of trouble.
     * @throws IbisConfigurationException
     *                 In case invalid properties were given.
     */
    public NullRegistry(IbisCapabilities capabilities,
            RegistryEventHandler handler, Properties properties, byte[] data,
            String implementationVersion, Credentials credentials, byte[] tag)
            throws IOException {

        if (handler != null) {
            throw new IbisConfigurationException(
                    "upcalls not supported by NullRegistry");
        }

        String id = properties.getProperty(Ibis.ID_PROPERTY);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        Location location = Location.defaultLocation(properties, null);

        String pool = properties.getProperty(IbisProperties.POOL_NAME);

        identifier = new IbisIdentifier(id, data, null, location,
                pool, tag);
    }

    @Override
    public void leave() throws IOException {
        // NOTHING
    }

    public void assumeDead(nl.esciencecenter.aether.IbisIdentifier ibis) throws IOException {
        // NOTHING
    }

    public nl.esciencecenter.aether.IbisIdentifier elect(String election) throws IOException {
        throw new IbisConfigurationException(
                "elections not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.IbisIdentifier elect(String election, long timeoutMillis) throws IOException {
        throw new IbisConfigurationException(
                "elections not supported by NullRegistry");
    }
    
    public nl.esciencecenter.aether.IbisIdentifier getElectionResult(String election)
            throws IOException {
        throw new IbisConfigurationException(
                "elections not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.IbisIdentifier getElectionResult(String election,
            long timeoutMillis) throws IOException {
        throw new IbisConfigurationException(
                "elections not supported by NullRegistry");
    }
    
    public String[] wonElections() {
        throw new IbisConfigurationException(
                "elections not supported by NullRegistry");
    }

    public void maybeDead(nl.esciencecenter.aether.IbisIdentifier ibis) throws IOException {
        // NOTHING
    }

    public void signal(String string, nl.esciencecenter.aether.IbisIdentifier... ibisses)
            throws IOException {
        throw new IbisConfigurationException(
                "signals not supported by NullRegistry");
    }

    @Override
    public IbisIdentifier getIbisIdentifier() {
        return identifier;
    }

    public nl.esciencecenter.aether.IbisIdentifier[] diedIbises() {
        throw new IbisConfigurationException(
                "died not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.IbisIdentifier[] joinedIbises() {
        throw new IbisConfigurationException(
                "joins not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.IbisIdentifier[] leftIbises() {
        throw new IbisConfigurationException(
                "leaves not supported by NullRegistry");
    }

    public String[] receivedSignals() {
        throw new IbisConfigurationException(
                "signals not supported by NullRegistry");
    }

    public void disableEvents() {
        // empty ?
    }

    public void enableEvents() {
        // empty ?
    }

    public int getPoolSize() {
        throw new IbisConfigurationException(
                "pool size not supported by NullRegistry");
    }
    
    public String getPoolName() {
        return identifier.poolName();
    }

    public void waitUntilPoolClosed() {
        throw new IbisConfigurationException(
                "waitUntilPoolClosed not supported by NullRegistry");
    }
    
    public boolean isClosed() {
        throw new IbisConfigurationException(
        "closed world not supported by NullRegistry");
    }

    public Map<String, String> managementProperties() {
        return new HashMap<String, String>();
    }

    public String getManagementProperty(String key) throws NoSuchPropertyException {
        throw new NoSuchPropertyException("no properties supported by null registry");
    }

    public void setManagementProperties(Map<String, String> properties) throws NoSuchPropertyException {
        throw new NoSuchPropertyException("no properties supported by null registry");
    }

    public void setManagementProperty(String key, String value) throws NoSuchPropertyException {
        throw new NoSuchPropertyException("no properties supported by null registry");
    }

    public void printManagementProperties(PrintStream stream) {
        //NOTHING
    }

    public boolean hasTerminated() {
        throw new IbisConfigurationException(
        "termination not supported by NullRegistry");
    }

    public void terminate() throws IOException {
        throw new IbisConfigurationException(
        "termination not supported by NullRegistry");
    }

    public IbisIdentifier waitUntilTerminated() {
        throw new IbisConfigurationException(
        "termination not supported by NullRegistry");
    }

    @Override
    public IbisIdentifier getRandomPoolMember() {
        return null;
    }
}
