package nl.esciencecenter.aether.registry.central.client;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.ConfigurationException;
import nl.esciencecenter.aether.NoSuchPropertyException;
import nl.esciencecenter.aether.RegistryEventHandler;
import nl.esciencecenter.aether.impl.AetherIdentifier;
import nl.esciencecenter.aether.registry.central.Event;
import nl.esciencecenter.aether.registry.central.Protocol;
import nl.esciencecenter.aether.registry.central.RegistryProperties;
import nl.esciencecenter.aether.registry.statistics.Statistics;
import nl.esciencecenter.aether.support.RemoteException;
import nl.esciencecenter.aether.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central registry.
 */
public final class Registry extends nl.esciencecenter.aether.registry.Registry {

    private static final Logger logger = LoggerFactory
            .getLogger(Registry.class);

    // A thread that forwards the events to the user event handler
    private final Upcaller upcaller;

    private final Statistics statistics;

    // Handles incoming and outgoing communication with other registries and
    // the server.
    private final CommunicationHandler communicationHandler;

    // client-side representation of the Pool the local Ibis is in.
    private final Pool pool;

    private final AetherIdentifier identifier;

    private final Capabilities capabilities;

    // data structures that the user can poll

    private final ArrayList<nl.esciencecenter.aether.AetherIdentifier> joinedIbises;

    private final ArrayList<nl.esciencecenter.aether.AetherIdentifier> leftIbises;

    private final ArrayList<nl.esciencecenter.aether.AetherIdentifier> diedIbises;

    private final ArrayList<String> signals;

    /**
     * Creates a Central Registry.
     * 
     * @param capabilities
     *            Required capabilities of this registry
     * @param eventHandler
     *            Registry handler to pass events to.
     * @param userProperties
     *            properties of this registry.
     * @param data
     *            Ibis implementation data to attach to the IbisIdentifier.
     * @param implementationVersion
     *            the identification of this ibis implementation, including
     *            version, class and such. Must be identical for all ibises in a
     *            single pool.
     * @param credentials
     *            Security credentials
     * @param tag
     *            A tag provided by the user constructing this Ibis.
     * @throws IOException
     *             in case of trouble.
     * @throws ConfigurationException
     *             In case invalid properties/capabilities were given.
     */
    public Registry(Capabilities capabilities,
            RegistryEventHandler eventHandler, Properties userProperties,
            byte[] data, String implementationVersion, Credentials credentials,
            byte[] tag) throws ConfigurationException, IOException {
        logger.debug("creating central registry");

        this.capabilities = capabilities;

        TypedProperties properties = RegistryProperties
                .getHardcodedProperties();
        properties.addProperties(userProperties);

        if (capabilities == null) {
            throw new ConfigurationException(
                    "Capabilities for registry not specified");
        }

        if ((capabilities.hasCapability(Capabilities.MEMBERSHIP_UNRELIABLE) || capabilities
                .hasCapability(Capabilities.MEMBERSHIP_TOTALLY_ORDERED))
                && eventHandler == null) {
            joinedIbises = new ArrayList<nl.esciencecenter.aether.AetherIdentifier>();
            leftIbises = new ArrayList<nl.esciencecenter.aether.AetherIdentifier>();
            diedIbises = new ArrayList<nl.esciencecenter.aether.AetherIdentifier>();
        } else {
            joinedIbises = null;
            leftIbises = null;
            diedIbises = null;
        }

        if (capabilities.hasCapability(Capabilities.SIGNALS)
                && eventHandler == null) {
            signals = new ArrayList<String>();
        } else {
            signals = null;
        }

        if (eventHandler != null) {
            upcaller = new Upcaller(eventHandler);
        } else {
            upcaller = null;
        }

        if (properties.getBooleanProperty(RegistryProperties.STATISTICS)) {
            statistics = new Statistics(Protocol.OPCODE_NAMES);
            if (logger.isDebugEnabled()) {
        	logger.debug("statistics: on");
            }
        } else {
            statistics = null;
            if (logger.isDebugEnabled()) {
        	logger.debug("statistics: off");
            }
        }

        pool = new Pool(capabilities, properties, this, statistics);

        try {
            communicationHandler = new CommunicationHandler(properties, pool,
                    statistics);

            identifier = communicationHandler.join(data, implementationVersion,
                    credentials, tag);

            communicationHandler.bootstrap();

        } catch (RemoteException e) {
            // error caused by server "complaining"
            throw new ConfigurationException(e.getMessage());
        }

        // start writing statistics
        if (statistics != null) {
            statistics.setID(identifier.getID() + "@" + identifier.location(),
                    pool.getName());
            statistics
                    .startWriting(properties
                            .getIntProperty(RegistryProperties.STATISTICS_INTERVAL) * 1000);
        }

   
        if (logger.isDebugEnabled()) {
            logger.debug("registry for " + identifier + " initiated");
        }
    }

    @Override
    public AetherIdentifier getIbisIdentifier() {
        return identifier;
    }

    public AetherIdentifier elect(String electionName) throws IOException {
        return elect(electionName, 0);
    }
    
    public String[] wonElections() {
        return pool.wonElections(identifier);
    }

    public AetherIdentifier elect(String electionName, long timeoutMillis)
            throws IOException {
        if (pool.isStopped()) {
            throw new IOException(
                    "cannot do election, registry already stopped");
        }

        if (!capabilities.hasCapability(Capabilities.ELECTIONS_UNRELIABLE)
                && !capabilities
                        .hasCapability(Capabilities.ELECTIONS_STRICT)) {
            throw new ConfigurationException(
                    "No election support requested");
        }

        AetherIdentifier result = pool.getElectionResult(electionName, -1);

        if (result == null) {
            result = communicationHandler.elect(electionName, timeoutMillis);
        }

        return result;
    }

    public AetherIdentifier getElectionResult(String election) throws IOException {
        return getElectionResult(election, 0);
    }

    public AetherIdentifier getElectionResult(String electionName,
            long timeoutMillis) throws IOException {
        if (pool.isStopped()) {
            throw new IOException(
                    "cannot do getElectionResult, registry already stopped");
        }

        if (!capabilities.hasCapability(Capabilities.ELECTIONS_UNRELIABLE)
                && !capabilities
                        .hasCapability(Capabilities.ELECTIONS_STRICT)) {
            throw new ConfigurationException(
                    "No election support requested");
        }

        logger.debug("getting election result for: \"" + electionName + "\"");

        return pool.getElectionResult(electionName, timeoutMillis);
    }

    public void maybeDead(nl.esciencecenter.aether.AetherIdentifier ibisIdentifier)
            throws IOException {
        if (pool.isStopped()) {
            throw new IOException(
                    "cannot do maybeDead, registry already stopped");
        }

        if (pool.mustReportMaybeDead(ibisIdentifier)) {
            communicationHandler.maybeDead(ibisIdentifier);
        }

    }

    public void assumeDead(nl.esciencecenter.aether.AetherIdentifier ibisIdentifier)
            throws IOException {
        if (pool.isStopped()) {
            throw new IOException(
                    "cannot do assumeDead, registry already stopped");
        }

        communicationHandler.assumeDead(ibisIdentifier);
    }

    public void signal(String signal,
            nl.esciencecenter.aether.AetherIdentifier... ibisIdentifiers) throws IOException {
        if (pool.isStopped()) {
            throw new IOException(
                    "cannot send signals, registry already stopped");
        }

        if (!capabilities.hasCapability(Capabilities.SIGNALS)) {
            throw new ConfigurationException("No signal support requested");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("telling " + ibisIdentifiers.length
        	    + " ibisses a string: " + signal);
        }

        communicationHandler.signal(signal, ibisIdentifiers);
    }

    public synchronized nl.esciencecenter.aether.AetherIdentifier[] joinedIbises() {
        if (joinedIbises == null) {
            throw new ConfigurationException(
                    "Resize downcalls not configured");
        }

        nl.esciencecenter.aether.AetherIdentifier[] retval = joinedIbises
                .toArray(new nl.esciencecenter.aether.AetherIdentifier[joinedIbises.size()]);
        joinedIbises.clear();
        return retval;
    }

    public synchronized nl.esciencecenter.aether.AetherIdentifier[] leftIbises() {
        if (leftIbises == null) {
            throw new ConfigurationException(
                    "Resize downcalls not configured");
        }
        nl.esciencecenter.aether.AetherIdentifier[] retval = leftIbises
                .toArray(new nl.esciencecenter.aether.AetherIdentifier[leftIbises.size()]);
        leftIbises.clear();
        return retval;
    }

    public synchronized nl.esciencecenter.aether.AetherIdentifier[] diedIbises() {
        if (diedIbises == null) {
            throw new ConfigurationException(
                    "Resize downcalls not configured");
        }

        nl.esciencecenter.aether.AetherIdentifier[] retval = diedIbises
                .toArray(new nl.esciencecenter.aether.AetherIdentifier[diedIbises.size()]);
        diedIbises.clear();
        return retval;
    }

    public synchronized String[] receivedSignals() {
        if (signals == null) {
            throw new ConfigurationException(
                    "Registry downcalls not configured");
        }

        String[] retval = signals.toArray(new String[signals.size()]);
        signals.clear();
        return retval;
    }

    public int getPoolSize() {
        if (!pool.isClosedWorld()) {
            throw new ConfigurationException(
                    "getPoolSize called but open world run");
        }

        return pool.getSize();
    }

    public String getPoolName() {
        return identifier.poolName();
    }

    public boolean isClosed() {
        return pool.isClosed();
    }

    public void waitUntilPoolClosed() {
        if (!pool.isClosedWorld()) {
            throw new ConfigurationException(
                    "waitForAll called but open world run");
        }

        pool.waitUntilPoolClosed();
    }

    public void enableEvents() {
        if (upcaller == null) {
            throw new ConfigurationException("Registry not configured to "
                    + "produce events");
        }

        upcaller.enableEvents();
    }

    public void disableEvents() {
        if (upcaller == null) {
            throw new ConfigurationException("Registry not configured to "
                    + "produce events");
        }

        upcaller.disableEvents();
    }

    @Override
    public long getSequenceNumber(String name) throws IOException {
        if (pool.isStopped()) {
            throw new IOException(
                    "cannot send signals, registry already stopped");
        }

        return communicationHandler.getSeqno(name);
    }

    @Override
    public void leave() throws IOException {
        if (pool.isStopped()) {
            throw new IOException("cannot leave, registry already stopped");
        }

        communicationHandler.leave();
        
        if (statistics != null) {
            statistics.write();
            statistics.end();
        }
        
    }

    /**
     * Handles incoming user events.
     */
    synchronized void handleEvent(Event event) {
	if (logger.isDebugEnabled()) {
	    logger.debug("new event passed to user: " + event);
	}

        if (event.getType() == Event.SIGNAL) {
            boolean match = false;
            // see if this signal is send to us.
            for (AetherIdentifier ibis : event.getDestinations()) {
                if (ibis.equals(identifier)) {
                    match = true;
                }
            }
            if (!match) {
                // do not handle this event any further
                return;
            }
        }

        // generate an upcall for this event
        if (upcaller != null) {
            upcaller.newEvent(event);
        }

        switch (event.getType()) {
        case Event.JOIN:
            if (joinedIbises != null) {
                joinedIbises.add(event.getIbis());
            }
            break;
        case Event.LEAVE:
            if (leftIbises != null) {
                leftIbises.add(event.getIbis());
            }
            break;
        case Event.DIED:
            if (leftIbises != null) {
                leftIbises.add(event.getIbis());
            }
            break;
        case Event.SIGNAL:
            if (signals != null) {
                signals.add(event.getDescription());
            }
            break;
        case Event.ELECT:
        case Event.UN_ELECT:
        case Event.POOL_CLOSED:
        case Event.POOL_TERMINATED:
            // Not handled here
            break;
        default:
            logger.error("unknown event type in registry: " + event);
        }
    }

    public Map<String, String> managementProperties() {
        return statistics.getMap();
    }

    public String getManagementProperty(String key)
            throws NoSuchPropertyException {
        String result = managementProperties().get(key);

        if (result == null) {
            throw new NoSuchPropertyException(key + " is not a valid property");
        }
        return result;
    }

    public void setManagementProperties(Map<String, String> properties)
            throws NoSuchPropertyException {
        throw new NoSuchPropertyException(
                "central registry does not have any properties that can be set");
    }

    public void setManagementProperty(String key, String value)
            throws NoSuchPropertyException {
        throw new NoSuchPropertyException(
                "central registry does not have any properties that can be set");
    }

    public void printManagementProperties(PrintStream stream) {
        // NOTHING
    }

    public boolean hasTerminated() {
        if (!capabilities.hasCapability(Capabilities.TERMINATION)) {
            throw new ConfigurationException("Registry not configured to "
                    + "support termination");
        }

        return pool.hasTerminated();
    }

    public void terminate() throws IOException {
        if (!capabilities.hasCapability(Capabilities.TERMINATION)) {
            throw new ConfigurationException("Registry not configured to "
                    + "support termination");
        }

        //check if already terminated, no need to do twice.
        if (!pool.hasTerminated()) {
            communicationHandler.terminate();
        }
    }

    public nl.esciencecenter.aether.AetherIdentifier waitUntilTerminated() {
        if (!capabilities.hasCapability(Capabilities.TERMINATION)) {
            throw new ConfigurationException("Registry not configured to "
                    + "support termination");
        }

        return pool.waitUntilTerminated();
    }

    // jmx function
    public synchronized boolean getTerminated() {
        return hasTerminated();
    }

    // jmx function
    public synchronized boolean getClosed() {
        return isClosed();
    }

    public String getTime() {
        return new Date() + "";
    }

    @Override
    public AetherIdentifier getRandomPoolMember() {
        return pool.getRandomMember().getIbis();
    }

}
