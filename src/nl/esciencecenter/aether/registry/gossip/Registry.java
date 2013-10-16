package nl.esciencecenter.aether.registry.gossip;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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
import nl.esciencecenter.aether.impl.AetherIdentifier;
import nl.esciencecenter.aether.impl.Location;
import nl.esciencecenter.aether.registry.statistics.Statistics;
import nl.esciencecenter.aether.util.ThreadPool;
import nl.esciencecenter.aether.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registry extends nl.esciencecenter.aether.registry.Registry implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(Registry.class);

    private final Capabilities capabilities;

    private final TypedProperties properties;

    private final String poolName;

    private final AetherIdentifier identifier;

    private final Statistics statistics;

    private final MemberSet members;

    private final ElectionSet elections;

    private final CommunicationHandler commHandler;

    private final Upcaller upcaller;

    // data structures the user can poll

    private final ArrayList<AetherIdentifier> joinedIbises;

    private final ArrayList<AetherIdentifier> leftIbises;

    private final ArrayList<AetherIdentifier> diedIbises;

    private final ArrayList<String> signals;

    private boolean stopped;

    /**
     * Creates a Gossip Registry.
     * 
     * @param capabilities
     *            capabilities required of this registry
     * @param eventHandler
     *            Registry handler to pass events to.
     * @param userProperties
     *            properties of this registry.
     * @param ibisData
     *            Ibis implementation data to attach to the IbisIdentifier.
     * @param credentials
     *            credentials used for authenticating this ibis at the server
     * @param implementationVersion
     *            the identification of this ibis implementation, including
     *            version, class and such. Must be identical for all Ibises in a
     *            single poolName.
     * @param applicationTag
     *            A tag provided by the application constructing this ibis.
     * @throws IOException
     *             in case of trouble.
     * @throws ConfigurationException
     *             In case invalid properties were given.
     */
    public Registry(Capabilities capabilities,
            RegistryEventHandler eventHandler, Properties userProperties,
            byte[] ibisData, String implementationVersion,
            Credentials credentials, byte[] applicationTag)
            throws ConfigurationException, IOException,
            ConfigurationException {
        this.capabilities = capabilities;

        if (capabilities
                .hasCapability(Capabilities.MEMBERSHIP_TOTALLY_ORDERED)) {
            throw new ConfigurationException(
                    "gossip registry does not support totally ordered membership");
        }

        if (capabilities.hasCapability(Capabilities.CLOSED_WORLD)) {
            throw new ConfigurationException(
                    "gossip registry does not support closed world");
        }

        if (capabilities.hasCapability(Capabilities.ELECTIONS_STRICT)) {
            throw new ConfigurationException(
                    "gossip registry does not support strict elections");
        }

        if (capabilities.hasCapability(Capabilities.TERMINATION)) {
            throw new ConfigurationException(
                    "gossip registry does not support termination");
        }

        properties = RegistryProperties.getHardcodedProperties();
        properties.addProperties(userProperties);

        if ((capabilities.hasCapability(Capabilities.MEMBERSHIP_UNRELIABLE))
                && eventHandler == null) {
            joinedIbises = new ArrayList<AetherIdentifier>();
            leftIbises = new ArrayList<AetherIdentifier>();
            diedIbises = new ArrayList<AetherIdentifier>();
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

        UUID id = UUID.randomUUID();

        poolName = properties.getProperty(AetherProperties.POOL_NAME);

        if (poolName == null) {
            throw new ConfigurationException(
                    "cannot initialize registry, property "
                            + AetherProperties.POOL_NAME + " is not specified");
        }

        Location location = Location.defaultLocation(properties, null);

        if (properties.getBooleanProperty(RegistryProperties.STATISTICS)) {
            statistics = new Statistics(Protocol.OPCODE_NAMES);
            statistics.setID(id.toString() + "@" + location.toString(),
                    poolName);

            long interval = properties
                    .getIntProperty(RegistryProperties.STATISTICS_INTERVAL) * 1000;

            statistics.startWriting(interval);
        } else {
            statistics = null;
        }

        members = new MemberSet(properties, this, statistics);
        elections = new ElectionSet(properties, this);

        commHandler = new CommunicationHandler(properties, this, members,
                elections, statistics);

        identifier = new AetherIdentifier(id.toString(), ibisData, commHandler
                .getAddress().toBytes(), location, poolName, applicationTag);

        commHandler.start();
        members.start();

        boolean printMembers = properties
                .getBooleanProperty(RegistryProperties.PRINT_MEMBERS);

        if (printMembers) {
            new MemberPrinter(members);
        }

        ThreadPool.createNew(this, "pool management thread");

        if (logger.isDebugEnabled()) {
            logger.debug("registry for " + identifier + " initiated");
        }
    }

    @Override
    public AetherIdentifier getIbisIdentifier() {
        return identifier;
    }

    CommunicationHandler getCommHandler() {
        return commHandler;
    }

    public AetherIdentifier elect(String electionName) throws IOException {
        if (!capabilities.hasCapability(Capabilities.ELECTIONS_UNRELIABLE)) {
            throw new ConfigurationException(
                    "No election support requested");
        }

        AetherIdentifier[] candidates = elections.elect(electionName);

        return members.getFirstLiving(candidates);
    }

    public AetherIdentifier elect(String electionName, long timeoutMillis)
            throws IOException {
        if (!capabilities.hasCapability(Capabilities.ELECTIONS_UNRELIABLE)) {
            throw new ConfigurationException(
                    "No election support requested");
        }

        AetherIdentifier[] candidates = elections.elect(electionName,
                timeoutMillis);

        return members.getFirstLiving(candidates);

    }

    public AetherIdentifier getElectionResult(String election) throws IOException {
        if (!capabilities.hasCapability(Capabilities.ELECTIONS_UNRELIABLE)) {
            throw new ConfigurationException(
                    "No election support requested");
        }

        AetherIdentifier[] candidates = elections.getElectionResult(election);

        return members.getFirstLiving(candidates);

    }
     
    public String[] wonElections() {
        ArrayList<String> result = new ArrayList<String>();
        synchronized(elections) {
            for (Election e : elections) {
                if (e.getWinner().equals(identifier)) {
                    result.add(e.getName());
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public AetherIdentifier getElectionResult(String electionName,
            long timeoutMillis) throws IOException {
        if (!capabilities.hasCapability(Capabilities.ELECTIONS_UNRELIABLE)) {
            throw new ConfigurationException(
                    "No election support requested");
        }

        AetherIdentifier[] candidates = elections.getElectionResult(electionName,
                timeoutMillis);

        return members.getFirstLiving(candidates);

    }

    public void maybeDead(nl.esciencecenter.aether.AetherIdentifier suspect) throws IOException {
        try {
            members.maybeDead((AetherIdentifier) suspect);
        } catch (ClassCastException e) {
            logger.error("illegal ibis identifier given: " + e);
        }
    }

    public void assumeDead(nl.esciencecenter.aether.AetherIdentifier deceased) throws IOException {
        try {
            members.assumeDead((AetherIdentifier) deceased);
        } catch (ClassCastException e) {
            logger.error("illegal ibis identifier given: " + e);
        }

    }

    public void signal(String signal,
            nl.esciencecenter.aether.AetherIdentifier... ibisIdentifiers) throws IOException {
        if (!capabilities.hasCapability(Capabilities.SIGNALS)) {
            throw new ConfigurationException("No string support requested");
        }

        try {
            AetherIdentifier[] implIdentifiers = (AetherIdentifier[]) ibisIdentifiers;

            commHandler.sendSignals(signal, implIdentifiers);

        } catch (ClassCastException e) {
            throw new IOException("wrong type of identifiers given: " + e);
        }
    }

    public synchronized nl.esciencecenter.aether.AetherIdentifier[] joinedIbises() {
        if (joinedIbises == null) {
            throw new ConfigurationException(
                    "Resize downcalls not configured");
        }

        nl.esciencecenter.aether.AetherIdentifier[] result = joinedIbises
                .toArray(new nl.esciencecenter.aether.AetherIdentifier[0]);
        joinedIbises.clear();
        return result;
    }

    public synchronized nl.esciencecenter.aether.AetherIdentifier[] leftIbises() {
        if (leftIbises == null) {
            throw new ConfigurationException(
                    "Resize downcalls not configured");
        }
        nl.esciencecenter.aether.AetherIdentifier[] result = leftIbises
                .toArray(new nl.esciencecenter.aether.AetherIdentifier[0]);
        leftIbises.clear();
        return result;
    }

    public synchronized nl.esciencecenter.aether.AetherIdentifier[] diedIbises() {
        if (diedIbises == null) {
            throw new ConfigurationException(
                    "Resize downcalls not configured");
        }

        nl.esciencecenter.aether.AetherIdentifier[] result = diedIbises
                .toArray(new nl.esciencecenter.aether.AetherIdentifier[0]);
        diedIbises.clear();
        return result;
    }

    public synchronized String[] receivedSignals() {
        if (signals == null) {
            throw new ConfigurationException(
                    "Registry downcalls not configured");
        }

        String[] result = signals.toArray(new String[0]);
        signals.clear();
        return result;
    }

    public int getPoolSize() {
        throw new ConfigurationException(
                "getPoolSize not supported by gossip registry");
    }

    public synchronized void waitUntilPoolClosed() {
        throw new ConfigurationException(
                "waitForAll not supported by gossip registry");

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
        throw new ConfigurationException(
                "Sequence numbers not supported by" + "gossip registry");
    }

    public Map<String, String> managementProperties() {
        // no properties (as of yet)
        return new HashMap<String, String>();
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
                "gossip registry does not have any properties that can be set");
    }

    public void setManagementProperty(String key, String value)
            throws NoSuchPropertyException {
        throw new NoSuchPropertyException(
                "gossip registry does not have any properties that can be set");
    }

    public void printManagementProperties(PrintStream stream) {
        // NOTHING
    }

    // functions called by pool to tell the registry an event has occured

    synchronized void ibisJoined(AetherIdentifier ibis) {
        if (joinedIbises != null) {
            joinedIbises.add(ibis);
        }

        if (upcaller != null) {
            upcaller.ibisJoined(ibis);
        }
    }

    synchronized void ibisLeft(AetherIdentifier ibis) {
        if (leftIbises != null) {
            leftIbises.add(ibis);
        }

        if (upcaller != null) {
            upcaller.ibisLeft(ibis);
        }
    }

    synchronized void ibisDied(AetherIdentifier ibis) {
        if (diedIbises != null) {
            diedIbises.add(ibis);
        }

        if (upcaller != null) {
            upcaller.ibisDied(ibis);
        }
    }

    synchronized void signal(String signal, AetherIdentifier source) {
        if (signals != null) {
            signals.add(signal);
        }

        if (upcaller != null) {
            upcaller.signal(signal, source);
        }
    }

    synchronized void electionResult(String name, AetherIdentifier winner) {
        if (upcaller != null) {
            upcaller.electionResult(name, winner);
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public String getPoolName() {
        return poolName;
    }

    @Override
    public void leave() throws IOException {
	if (logger.isDebugEnabled()) {
	    logger.debug("leaving: setting stopped state");
	}
        synchronized (this) {
            stopped = true;
            notifyAll();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("leaving: telling pool we are leaving");
        }
        members.leave(identifier);
        members.leave();

        // logger.debug("leaving: broadcasting leave");
        commHandler.broadcastLeave();

        if (logger.isDebugEnabled()) {
            logger.debug("leaving: writing statistics");
        }
        if (statistics != null) {
            statistics.write();
            statistics.end();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("leaving: done!");
        }
    }

    public void run() {
        long interval = properties
                .getIntProperty(RegistryProperties.GOSSIP_INTERVAL) * 1000;

        while (!isStopped()) {
            commHandler.gossip();

            int timeout = (int) (Math.random() * interval);
            synchronized (this) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                    // IGNORE
                }
            }
        }

    }

    public boolean hasTerminated() {
        throw new ConfigurationException(
                "gossip registry does not support termination");
    }

    public boolean isClosed() {
        throw new ConfigurationException(
                "gossip registry does not support closed world");
    }

    public void terminate() throws IOException {
        throw new ConfigurationException(
                "gossip registry does not support termination");
    }

    public nl.esciencecenter.aether.AetherIdentifier waitUntilTerminated() {
        throw new ConfigurationException(
                "gossip registry does not support termination");
    }

    @Override
    public AetherIdentifier getRandomPoolMember() {
       Member[] random = members.getRandomMembers(1);
       
       if (random.length == 1) {
           return random[0].getIdentifier();
       } else {
           return null;
       }
    }

}
