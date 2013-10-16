/* $Id: MultiIbis.java 13423 2011-07-04 11:54:01Z ceriel $ */

package nl.esciencecenter.aether.impl.multi;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.CreationFailedException;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.AetherProperties;
import nl.esciencecenter.aether.MessageUpcall;
import nl.esciencecenter.aether.NoSuchPropertyException;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReceivePort;
import nl.esciencecenter.aether.ReceivePortConnectUpcall;
import nl.esciencecenter.aether.ReceivePortIdentifier;
import nl.esciencecenter.aether.Registry;
import nl.esciencecenter.aether.RegistryEventHandler;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.SendPortDisconnectUpcall;
import nl.esciencecenter.aether.SendPortIdentifier;
import nl.esciencecenter.aether.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiAether implements Aether {

    /** Debugging output. */
    private static final Logger logger = LoggerFactory
            .getLogger(MultiAether.class);

    final MultiAetherIdentifier id;

    public static final PortType resolvePortType = new PortType(
            PortType.COMMUNICATION_RELIABLE, PortType.CONNECTION_MANY_TO_ONE,
            PortType.RECEIVE_EXPLICIT, PortType.SERIALIZATION_OBJECT);

    final HashMap<String, Aether> subIbisMap = new HashMap<String, Aether>();

    private final HashMap<AetherIdentifier, MultiAetherIdentifier> idMap = new HashMap<AetherIdentifier, MultiAetherIdentifier>();

    private final ArrayList<MultiSendPort> sendPorts = new ArrayList<MultiSendPort>();

    private final ArrayList<MultiReceivePort> receivePorts = new ArrayList<MultiReceivePort>();

    private final TypedProperties properties;

    private final MultiRegistry registry;

    private final ManageableMapper ManageableMapper;

    // TODO Wrap with getter and setter
    final HashMap<ReceivePort, MultiReceivePort> receivePortMap = new HashMap<ReceivePort, MultiReceivePort>();

    // TODO Wrap with getter and setter
    final Map<SendPort, MultiSendPort> sendPortMap = Collections
            .synchronizedMap(new HashMap<SendPort, MultiSendPort>());

    // TODO Wrap with getter and setter
    final HashMap<String, MultiNameResolver> resolverMap = new HashMap<String, MultiNameResolver>();

    final HashMap<String, MultiRegistryEventHandler> registryHandlerMap = new HashMap<String, MultiRegistryEventHandler>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MultiAether(AetherFactory factory,
            RegistryEventHandler registryEventHandler,
            Properties userProperties, Capabilities capabilities,
            Credentials credentials, byte[] applicationTag, PortType[] portTypes,
            String specifiedSubImplementation, MultiAetherStarter multiIbisStarter) {
        if (logger.isDebugEnabled()) {
            logger.debug("Constructing MultiIbis!");
        }
        HashMap<String, AetherIdentifier> subIdMap = new HashMap<String, AetherIdentifier>();
        if (logger.isDebugEnabled()) {
            org.slf4j.MDC.put("UID", String.valueOf(new Random().nextInt()));
        }
        if (!(userProperties instanceof TypedProperties)) {
            properties = new TypedProperties();
            properties.addProperties(userProperties);
        } else {
            properties = (TypedProperties) userProperties;
        }

        // add our own port-type to the required list
        PortType[] requiredPortTypes = new PortType[portTypes.length + 1];
        System.arraycopy(portTypes, 0, requiredPortTypes, 0, portTypes.length);
        requiredPortTypes[portTypes.length] = resolvePortType;

        // sub-implementations specified as impl:impl2:impl3
        String[] implementations = specifiedSubImplementation.split(":");

        // FIXME: although this looks nice, it is probably broken -Niels
        for (String implementation : implementations) {
            try {
                // add name of implementation to the poolname
                String poolName = userProperties
                        .getProperty(AetherProperties.POOL_NAME);
                Properties subProperties = new Properties(userProperties);
                subProperties.setProperty(AetherProperties.POOL_NAME, poolName
                        + ":" + implementation);

                MultiRegistryEventHandler handler = null;
                if (registryEventHandler != null) {
                    handler = new MultiRegistryEventHandler(this,
                            registryEventHandler);
                }

                Aether ibis = factory.createIbis(handler, capabilities,
                        subProperties, credentials, applicationTag, requiredPortTypes,
                        implementation);

                if (handler != null) {
                    handler.setName(implementation);
                    registryHandlerMap.put(implementation, handler);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Started ibis: " + implementation);
                }

                subIbisMap.put(implementation, ibis);
                subIdMap.put(implementation, ibis.identifier());

                // Start the name resolution service for this ibis
                try {
                    new MultiNameResolver(this, implementation);
                } catch (IOException e) {
                    throw new CreationFailedException(
                            "Unable to create resolver.", e);
                }
            } catch (Throwable t) {
                logger.warn("Could not start child ibis", t);
            }
        }

        if (subIbisMap.size() == 0) {
            throw new RuntimeException("Unable to create any children!");
        }

        String poolName = userProperties.getProperty(AetherProperties.POOL_NAME);
        Location location = Location.defaultLocation(userProperties);
        id = new MultiAetherIdentifier(UUID.randomUUID().toString(), subIdMap,
                null, location, poolName, applicationTag);

        for (String ibisName : subIdMap.keySet()) {
            AetherIdentifier subId = subIdMap.get(ibisName);
            idMap.put(subId, id);
        }

        // Now let the resolvers go!
        for (String ibisName : resolverMap.keySet()) {
            MultiNameResolver resolver = resolverMap.get(ibisName);
            synchronized (resolver) {
                resolver.notifyAll();
            }
        }

        // Now create the registry and let the event handlers go
        registry = new MultiRegistry(this);
        for (String ibisName : registryHandlerMap.keySet()) {
            MultiRegistryEventHandler handler = registryHandlerMap
                    .get(ibisName);
            handler.setRegistry(registry);
        }

        // Setup management stuff
        ManageableMapper = new ManageableMapper((Map) subIbisMap);

        if (logger.isInfoEnabled()) {
            logger.info("MultiIbis Started with ID: " + id);
        }
    }

    public synchronized void end() throws IOException {
        for (Aether ibis : subIbisMap.values()) {
            ibis.end();
        }
        // Kill all the receive ports
        for (MultiReceivePort port : receivePortMap.values()) {
            try {
                port.close(100);
            } catch (IOException e) {
                // Ignore
            }
        }
        for (MultiSendPort port : sendPortMap.values()) {
            port.quit(port);
        }
        MultiNameResolver.quit();
    }

    public synchronized Registry registry() {
        return registry;
    }

    public synchronized void poll() throws IOException {
        for (Aether ibis : subIbisMap.values()) {
            ibis.poll();
        }
    }

    public synchronized AetherIdentifier identifier() {
        return id;
    }

    public synchronized String getVersion() {
        StringBuffer buffer = new StringBuffer("MultiIbis on top of");
        for (Aether ibis : subIbisMap.values()) {
            buffer.append(' ');
            buffer.append(ibis.getVersion());
        }
        return buffer.toString();
    }

    public synchronized Properties properties() {
        return properties;
    }

    public SendPort createSendPort(PortType portType) throws IOException {
        return createSendPort(portType, null, null, null);
    }

    public SendPort createSendPort(PortType portType, String name)
            throws IOException {
        return createSendPort(portType, name, null, null);
    }

    public synchronized SendPort createSendPort(PortType portType, String name,
            SendPortDisconnectUpcall cU, Properties props) throws IOException {
        MultiSendPort port = new MultiSendPort(portType, this, name, cU, props);
        sendPorts.add(port);
        return port;
    }

    public synchronized void closeSendPort(MultiSendPort port) {
        sendPorts.remove(port);
    }

    public synchronized void closeReceivePort(MultiReceivePort port) {
        receivePorts.remove(port);
    }

    public ReceivePort createReceivePort(PortType portType, String name)
            throws IOException {
        return createReceivePort(portType, name, null, null, null);
    }

    public ReceivePort createReceivePort(PortType portType, String name,
            MessageUpcall u) throws IOException {
        return createReceivePort(portType, name, u, null, null);
    }

    public ReceivePort createReceivePort(PortType portType, String name,
            ReceivePortConnectUpcall cU) throws IOException {
        return createReceivePort(portType, name, null, cU, null);
    }

    public synchronized ReceivePort createReceivePort(PortType portType,
            String name, MessageUpcall u, ReceivePortConnectUpcall cU,
            Properties props) throws IOException {
        MultiReceivePort port = new MultiReceivePort(portType, this, name, u,
                cU, props);
        receivePorts.add(port);
        return port;
    }

    public MultiAetherIdentifier mapIdentifier(AetherIdentifier ibisId,
            String ibisName) throws IOException {
        MultiAetherIdentifier id = idMap.get(ibisId);
        while (id == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to resolve: " + ibisId);
            }
            MultiNameResolver resolver = resolverMap.get(ibisName);
            resolver.resolve(ibisId, ibisName);
            id = idMap.get(ibisId);
        }
        if (logger.isDebugEnabled())
            logger.debug("Mapped Identifier: " + ibisId + " to:" + id);
        return id;
    }

    public String getManagementProperty(String key)
            throws NoSuchPropertyException {
        return ManageableMapper.getManagementProperty(key);
    }

    public Map<String, String> managementProperties() {
        return ManageableMapper.managementProperties();
    }

    public void printManagementProperties(PrintStream stream) {
        ManageableMapper.printManagementProperties(stream);
    }

    public void setManagementProperties(Map<String, String> properties)
            throws NoSuchPropertyException {
        ManageableMapper.setManagementProperties(properties);
    }

    public void setManagementProperty(String key, String value)
            throws NoSuchPropertyException {
        ManageableMapper.setManagementProperty(key, value);
    }

    private final HashMap<SendPortIdentifier, MultiSendPortIdentifier> sendPortIdMap = new HashMap<SendPortIdentifier, MultiSendPortIdentifier>();

    public SendPortIdentifier mapSendPortIdentifier(SendPortIdentifier johnDoe,
            String ibisName) throws IOException {
        MultiSendPortIdentifier id = null;
        if (sendPortIdMap.containsKey(johnDoe)) {
            return sendPortIdMap.get(johnDoe);
        }
        id = new MultiSendPortIdentifier(mapIdentifier(
                johnDoe.ibisIdentifier(), ibisName), johnDoe.name());
        sendPortIdMap.put(johnDoe, id);
        return id;
    }

    private final HashMap<ReceivePortIdentifier, MultiReceivePortIdentifier> receivePortIdMap = new HashMap<ReceivePortIdentifier, MultiReceivePortIdentifier>();

    public ReceivePortIdentifier mapReceivePortIdentifier(
            ReceivePortIdentifier johnDoe, String ibisName) throws IOException {
        MultiReceivePortIdentifier id = null;
        if (receivePortIdMap.containsKey(johnDoe)) {
            return receivePortIdMap.get(johnDoe);
        }
        id = new MultiReceivePortIdentifier(mapIdentifier(johnDoe
                .ibisIdentifier(), ibisName), johnDoe.name());
        receivePortIdMap.put(johnDoe, id);
        return id;
    }

    public void resolved(MultiAetherIdentifier id) {
        for (String subIbisName : subIbisMap.keySet()) {
            AetherIdentifier subId = id.subIdForIbis(subIbisName);
            if (subId != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Resolved: " + subId + " to: " + id);
                }
                idMap.put(subId, id);
            }
        }
    }

    public boolean isResolved(AetherIdentifier toResolve) {
        return idMap.get(toResolve) != null;
    }

}
