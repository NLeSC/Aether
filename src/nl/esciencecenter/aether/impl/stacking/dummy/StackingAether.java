package nl.esciencecenter.aether.impl.stacking.dummy;


import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
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

public class StackingAether implements Aether {

    Aether base;

    public StackingAether(AetherFactory factory,
            RegistryEventHandler registryEventHandler,
            Properties userProperties, Capabilities capabilities,
            Credentials credentials, byte[] applicationTag, PortType[] portTypes,
            String specifiedSubImplementation,
            StackingAetherStarter stackingIbisStarter)
            throws CreationFailedException {
        base = factory.createIbis(registryEventHandler, capabilities,
                userProperties, credentials, applicationTag, portTypes,
                specifiedSubImplementation);
    }

    public void end() throws IOException {
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

    public String getVersion() {
        return "StackingIbis on top of " + base.getVersion();
    }

    public Properties properties() {
        return base.properties();
    }

    public SendPort createSendPort(PortType portType) throws IOException {
        return createSendPort(portType, null, null, null);
    }

    public SendPort createSendPort(PortType portType, String name)
            throws IOException {
        return createSendPort(portType, name, null, null);
    }

    public SendPort createSendPort(PortType portType, String name,
            SendPortDisconnectUpcall cU, Properties props) throws IOException {
        return new StackingSendPort(portType, this, name, cU, props);
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

    public ReceivePort createReceivePort(PortType portType, String name,
            MessageUpcall u, ReceivePortConnectUpcall cU, Properties props)
            throws IOException {
        return new StackingReceivePort(portType, this, name, u, cU, props);
    }

}
