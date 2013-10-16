package nl.esciencecenter.aether.impl.stacking.lrmc;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.ConfigurationException;
import nl.esciencecenter.aether.CreationFailedException;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.MessageUpcall;
import nl.esciencecenter.aether.NoSuchPropertyException;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReceivePort;
import nl.esciencecenter.aether.ReceivePortConnectUpcall;
import nl.esciencecenter.aether.Registry;
import nl.esciencecenter.aether.RegistryEventHandler;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.SendPortDisconnectUpcall;
import nl.esciencecenter.aether.impl.stacking.lrmc.util.DynamicObjectArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LRMCAether implements Aether {

    private static final Logger logger = LoggerFactory
            .getLogger(LRMCAether.class);

    static final PortType additionalPortType = new PortType(
            PortType.SERIALIZATION_DATA, PortType.COMMUNICATION_RELIABLE,
            PortType.CONNECTION_MANY_TO_ONE, PortType.RECEIVE_AUTO_UPCALLS);

    private static class EventHandler implements RegistryEventHandler {
        RegistryEventHandler h;

        LRMCAether ibis;

        EventHandler(RegistryEventHandler h, LRMCAether ibis) {
            this.h = h;
            this.ibis = ibis;
        }

        public void joined(AetherIdentifier id) {
            ibis.addIbis(id);
            if (h != null) {
                h.joined(id);
            }
        }

        public void left(AetherIdentifier id) {
            ibis.removeIbis(id);
            if (h != null) {
                h.left(id);
            }
        }

        public void died(AetherIdentifier id) {
            ibis.removeIbis(id);
            if (h != null) {
                h.died(id);
            }
        }

        public void gotSignal(String s, AetherIdentifier id) {
            if (h != null) {
                h.gotSignal(s, id);
            }
        }

        public void electionResult(String electionName, AetherIdentifier winner) {
            if (h != null) {
                h.electionResult(electionName, winner);
            }
        }

        public void poolClosed() {
            if (h != null) {
                h.poolClosed();
            }
        }

        public void poolTerminated(AetherIdentifier source) {
            if (h != null) {
                h.poolTerminated(source);
            }
        }
    }

    Aether base;

    int myID;

    PortType[] portTypes;

    Capabilities capabilities;

    private int nextIbisID = 0;

    private BitSet diedIbises = new BitSet();

    HashMap<AetherIdentifier, Integer> knownIbis = new HashMap<AetherIdentifier, Integer>();

    DynamicObjectArray<AetherIdentifier> ibisList = new DynamicObjectArray<AetherIdentifier>();

    HashMap<String, Multicaster> multicasters = new HashMap<String, Multicaster>();

    public LRMCAether(AetherFactory factory,
            RegistryEventHandler registryEventHandler,
            Properties userProperties, Capabilities capabilities,
            Credentials credentials, byte[] applicationTag, PortType[] portTypes,
            String specifiedSubImplementation, LRMCAetherStarter lrmcIbisStarter)
            throws CreationFailedException {
        List<PortType> requiredPortTypes = new ArrayList<PortType>();
        logger.info("Constructor LRMC Ibis");

        if (specifiedSubImplementation == null) {
            throw new CreationFailedException(
                    "LrmcIbis: child Ibis implementation not specified");
        }

        EventHandler h = null;

        if (registryEventHandler != null) {
            h = new EventHandler(registryEventHandler, this);
        }

        this.portTypes = portTypes;
        this.capabilities = capabilities;

        // add additional port-type as a requirement, and remove port-types
        // that we deal with ourselves.
        for (PortType portType: portTypes) {
            if (! ourPortType(portType)) {
                requiredPortTypes.add(portType);
            }
        }
       
        requiredPortTypes.add(additionalPortType);

        base = factory.createIbis(h, capabilities, userProperties, credentials, applicationTag,
                requiredPortTypes.toArray(new PortType[requiredPortTypes.size()]),
                specifiedSubImplementation);
    }

    public synchronized void addIbis(AetherIdentifier ibis) {

        if (!knownIbis.containsKey(ibis)) {
            knownIbis.put(ibis, new Integer(nextIbisID));
            ibisList.put(nextIbisID, ibis);

            logger.info("Adding Ibis " + nextIbisID + " " + ibis);

            if (ibis.equals(identifier())) {
                logger.info("I am " + nextIbisID + " " + ibis);
                myID = nextIbisID;
            }

            nextIbisID++;
            notifyAll();
        }
    }

    synchronized AetherIdentifier getId(int id) {

        if (diedIbises.get(id)) {
            return null;
        }

        AetherIdentifier ibisID = ibisList.get(id);

        if (ibisID == null) {
            try {
                wait(10000);
            } catch (Exception e) {
                // ignored
            }
            return ibisList.get(id);
        }
        return ibisID;
    }

    synchronized int getIbisID(AetherIdentifier ibis) {

        Integer s = knownIbis.get(ibis);

        if (s != null) {
            return s.intValue();
        } else {
            logger.debug("Ibis " + ibis + " not known!");
            return -1;
        }
    }

    public synchronized void removeIbis(AetherIdentifier ibis) {

        Integer tmp = knownIbis.remove(ibis);

        if (tmp != null) {
            logger.info("Removing ibis " + tmp.intValue() + " " + ibis);
            ibisList.remove(tmp.intValue());
        }
        diedIbises.set(tmp.intValue());
    }

    synchronized Multicaster getMulticaster(String name, PortType portType)
            throws IOException {
        Multicaster om = multicasters.get(name);
        if (om == null) {
            om = new Multicaster(this, portType, name);
            multicasters.put(name, om);
        } else {
            if (!om.portType.equals(portType)) {
                throw new IOException("Mismatch in port types for name " + name);
            }
        }
        return om;
    }

    public String getVersion() {
        return "LrmcIbis on top of " + base.getVersion();
    }

    public SendPort createSendPort(PortType portType, String name,
            SendPortDisconnectUpcall cU, Properties props) throws IOException {
        matchPortType(portType);
        if (ourPortType(portType)) {
            if (name == null) {
                throw new IOException("Anonymous  ports not supported");
            }
            if (cU != null
                    && !portType.hasCapability(PortType.CONNECTION_UPCALLS)) {
                throw new ConfigurationException(
                        "connection upcalls not supported by this porttype");
            }
            synchronized (this) {
                Multicaster mc = getMulticaster(name, portType);
                if (mc.sendPort != null) {
                    throw new IOException(
                            "A sendport with the same name already exists");
                }
                mc.sendPort = new LRMCSendPort(mc, this, props);
                return mc.sendPort;
            }
        }
        return new StackingSendPort(portType, this, name, cU, props);
    }

    public ReceivePort createReceivePort(PortType portType, String name,
            MessageUpcall u, ReceivePortConnectUpcall cU, Properties props)
            throws IOException {
        matchPortType(portType);
        if (ourPortType(portType)) {
            if (name == null) {
                throw new IOException("Anonymous  ports not supported");
            }
            if (cU != null
                    && !portType.hasCapability(PortType.CONNECTION_UPCALLS)) {
                throw new ConfigurationException(
                        "connection upcalls not supported by this porttype");
            }
            if (u != null
                    && !portType.hasCapability(PortType.RECEIVE_AUTO_UPCALLS)) {
                throw new ConfigurationException(
                        "upcalls not supported by this porttype");
            }
            if (u == null && !portType.hasCapability(PortType.RECEIVE_EXPLICIT)) {
                throw new ConfigurationException(
                        "explicit receive not supported by this porttype");
            }
            synchronized (this) {
                Multicaster mc = getMulticaster(name, portType);
                if (mc.receivePort != null) {
                    throw new IOException(
                            "A receiveport with the same name already exists");
                }
                mc.receivePort = new LRMCReceivePort(mc, this, u, props);
                return mc.receivePort;
            }
        }
        return new StackingReceivePort(portType, this, name, u, cU, props);
    }

    public ReceivePort createReceivePort(PortType portType,
            String receivePortName) throws IOException {
        return createReceivePort(portType, receivePortName, null, null, null);
    }

    public ReceivePort createReceivePort(PortType portType,
            String receivePortName, MessageUpcall messageUpcall)
            throws IOException {
        return createReceivePort(portType, receivePortName, messageUpcall,
                null, null);
    }

    public ReceivePort createReceivePort(PortType portType,
            String receivePortName,
            ReceivePortConnectUpcall receivePortConnectUpcall)
            throws IOException {
        return createReceivePort(portType, receivePortName, null,
                receivePortConnectUpcall, null);
    }

    public nl.esciencecenter.aether.SendPort createSendPort(PortType tp) throws IOException {
        return createSendPort(tp, null, null, null);
    }

    public nl.esciencecenter.aether.SendPort createSendPort(PortType tp, String name)
            throws IOException {
        return createSendPort(tp, name, null, null);
    }

    private void matchPortType(PortType tp) {
        boolean matched = false;
        for (PortType p : portTypes) {
            if (tp.equals(p)) {
                matched = true;
            }
        }
        if (!matched) {
            throw new ConfigurationException("PortType " + tp
                    + " not specified when creating this Ibis instance");
        }
    }

    public void end() throws IOException {
        for (Map.Entry<String, Multicaster> x : multicasters.entrySet()) {
            x.getValue().done();
        }
        base.end();
    }

    public Registry registry() {
        // return new
        // ibis.ipl.impl.registry.ForwardingRegistry(base.registry());
        return base.registry();
    }

    public Map<String, String> managementProperties() {
        return base.managementProperties();
    }

    public String getManagementProperty(String key)
            throws NoSuchPropertyException {
        return base.getManagementProperty(key);
    }

    public void setManagementProperties(Map<String, String> properties)
            throws NoSuchPropertyException {
        base.setManagementProperties(properties);
    }

    public void setManagementProperty(String key, String val)
            throws NoSuchPropertyException {
        base.setManagementProperty(key, val);
    }

    public void printManagementProperties(PrintStream stream) {
        base.printManagementProperties(stream);
    }

    public void poll() throws IOException {
        base.poll();
    }

    public AetherIdentifier identifier() {
        return base.identifier();
    }

    public Properties properties() {
        return base.properties();
    }

    
    /**
     * Determines if the specified port type is one that is implemented
     * by Lrmc ibis.
     * @param tp the port type
     * @return <code>true</code> if LRMC Ibis deals with this port type,
     * <code>false</code> if it is to be passed on to an underlying ibis.
     */
    private static boolean ourPortType(PortType tp) {
        return (tp.hasCapability(PortType.CONNECTION_MANY_TO_MANY) || tp
                .hasCapability(PortType.CONNECTION_ONE_TO_MANY))
                && !tp.hasCapability(PortType.COMMUNICATION_RELIABLE)
                && !tp.hasCapability(PortType.CONNECTION_UPCALLS)
                && !tp.hasCapability(PortType.CONNECTION_DOWNCALLS)
                && !tp.hasCapability(PortType.COMMUNICATION_NUMBERED)
                && !tp.hasCapability(PortType.COMMUNICATION_FIFO);
    }
}
