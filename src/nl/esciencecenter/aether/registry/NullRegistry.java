package nl.esciencecenter.aether.registry;


import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.ConfigurationException;
import nl.esciencecenter.aether.AetherProperties;
import nl.esciencecenter.aether.NoSuchPropertyException;
import nl.esciencecenter.aether.RegistryEventHandler;
import nl.esciencecenter.aether.impl.Aether;
import nl.esciencecenter.aether.impl.AetherIdentifier;
import nl.esciencecenter.aether.impl.Location;

/**
 * 
 * Registry implementation that does nothing. Throws an Exception most calls.
 * 
 */
public final class NullRegistry extends nl.esciencecenter.aether.registry.Registry {

    private final AetherIdentifier identifier;

    @Override
    public long getSequenceNumber(String name) throws IOException {
        throw new ConfigurationException(
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
     * @throws ConfigurationException
     *                 In case invalid properties were given.
     */
    public NullRegistry(Capabilities capabilities,
            RegistryEventHandler handler, Properties properties, byte[] data,
            String implementationVersion, Credentials credentials, byte[] tag)
            throws IOException {

        if (handler != null) {
            throw new ConfigurationException(
                    "upcalls not supported by NullRegistry");
        }

        String id = properties.getProperty(Aether.ID_PROPERTY);
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        Location location = Location.defaultLocation(properties, null);

        String pool = properties.getProperty(AetherProperties.POOL_NAME);

        identifier = new AetherIdentifier(id, data, null, location,
                pool, tag);
    }

    @Override
    public void leave() throws IOException {
        // NOTHING
    }

    public void assumeDead(nl.esciencecenter.aether.AetherIdentifier ibis) throws IOException {
        // NOTHING
    }

    public nl.esciencecenter.aether.AetherIdentifier elect(String election) throws IOException {
        throw new ConfigurationException(
                "elections not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.AetherIdentifier elect(String election, long timeoutMillis) throws IOException {
        throw new ConfigurationException(
                "elections not supported by NullRegistry");
    }
    
    public nl.esciencecenter.aether.AetherIdentifier getElectionResult(String election)
            throws IOException {
        throw new ConfigurationException(
                "elections not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.AetherIdentifier getElectionResult(String election,
            long timeoutMillis) throws IOException {
        throw new ConfigurationException(
                "elections not supported by NullRegistry");
    }
    
    public String[] wonElections() {
        throw new ConfigurationException(
                "elections not supported by NullRegistry");
    }

    public void maybeDead(nl.esciencecenter.aether.AetherIdentifier ibis) throws IOException {
        // NOTHING
    }

    public void signal(String string, nl.esciencecenter.aether.AetherIdentifier... ibisses)
            throws IOException {
        throw new ConfigurationException(
                "signals not supported by NullRegistry");
    }

    @Override
    public AetherIdentifier getIbisIdentifier() {
        return identifier;
    }

    public nl.esciencecenter.aether.AetherIdentifier[] diedIbises() {
        throw new ConfigurationException(
                "died not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.AetherIdentifier[] joinedIbises() {
        throw new ConfigurationException(
                "joins not supported by NullRegistry");
    }

    public nl.esciencecenter.aether.AetherIdentifier[] leftIbises() {
        throw new ConfigurationException(
                "leaves not supported by NullRegistry");
    }

    public String[] receivedSignals() {
        throw new ConfigurationException(
                "signals not supported by NullRegistry");
    }

    public void disableEvents() {
        // empty ?
    }

    public void enableEvents() {
        // empty ?
    }

    public int getPoolSize() {
        throw new ConfigurationException(
                "pool size not supported by NullRegistry");
    }
    
    public String getPoolName() {
        return identifier.poolName();
    }

    public void waitUntilPoolClosed() {
        throw new ConfigurationException(
                "waitUntilPoolClosed not supported by NullRegistry");
    }
    
    public boolean isClosed() {
        throw new ConfigurationException(
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
        throw new ConfigurationException(
        "termination not supported by NullRegistry");
    }

    public void terminate() throws IOException {
        throw new ConfigurationException(
        "termination not supported by NullRegistry");
    }

    public AetherIdentifier waitUntilTerminated() {
        throw new ConfigurationException(
        "termination not supported by NullRegistry");
    }

    @Override
    public AetherIdentifier getRandomPoolMember() {
        return null;
    }
}
