/* $Id: TcpIbisStarter.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.tcp;


import java.util.ArrayList;
import java.util.Properties;

import nl.esciencecenter.aether.CapabilitySet;
import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.CreationFailedException;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.RegistryEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TcpAetherStarter extends nl.esciencecenter.aether.AetherStarter {

    static final Logger logger = LoggerFactory
            .getLogger("ibis.ipl.impl.tcp.TcpIbisStarter");

    static final Capabilities ibisCapabilities = new Capabilities(
            Capabilities.CLOSED_WORLD,
            Capabilities.MEMBERSHIP_TOTALLY_ORDERED,
            Capabilities.MEMBERSHIP_UNRELIABLE, Capabilities.SIGNALS,
            Capabilities.ELECTIONS_UNRELIABLE,
            Capabilities.ELECTIONS_STRICT, Capabilities.MALLEABLE,
            Capabilities.TERMINATION);

    static final PortType portCapabilities = new PortType(
            PortType.SERIALIZATION_OBJECT_SUN,
            PortType.SERIALIZATION_OBJECT_IBIS, PortType.SERIALIZATION_OBJECT,
            PortType.SERIALIZATION_DATA, PortType.SERIALIZATION_BYTE,
            PortType.COMMUNICATION_FIFO, PortType.COMMUNICATION_NUMBERED,
            PortType.COMMUNICATION_RELIABLE, PortType.CONNECTION_DOWNCALLS,
            PortType.CONNECTION_UPCALLS, PortType.CONNECTION_TIMEOUT,
            PortType.CONNECTION_MANY_TO_MANY, PortType.CONNECTION_MANY_TO_ONE,
            PortType.CONNECTION_ONE_TO_MANY, PortType.CONNECTION_ONE_TO_ONE,
            PortType.RECEIVE_POLL, PortType.RECEIVE_AUTO_UPCALLS,
            PortType.RECEIVE_EXPLICIT, PortType.RECEIVE_POLL_UPCALLS,
            PortType.RECEIVE_TIMEOUT);

    public TcpAetherStarter(String nickName, String iplVersion,
            String implementationVersion) {
        super(nickName, iplVersion, implementationVersion);
    }

    @Override
    public boolean matches(Capabilities capabilities, PortType[] types) {
        if (!capabilities.matchCapabilities(ibisCapabilities)) {
            return false;
        }
        for (PortType portType : types) {
            if (!portType.matchCapabilities(portCapabilities)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CapabilitySet unmatchedIbisCapabilities(
            Capabilities capabilities, PortType[] types) {
        return capabilities.unmatchedCapabilities(ibisCapabilities);
    }

    @Override
    public PortType[] unmatchedPortTypes(Capabilities capabilities,
            PortType[] types) {
        ArrayList<PortType> result = new ArrayList<PortType>();

        for (PortType portType : types) {
            if (!portType.matchCapabilities(portCapabilities)) {
                result.add(portType);
            }
        }
        return result.toArray(new PortType[0]);
    }

    @Override
    public Aether startIbis(AetherFactory factory,
            RegistryEventHandler registryEventHandler,
            Properties userProperties, Capabilities capabilities,
            Credentials credentials, byte[] applicationTag,
            PortType[] portTypes, String specifiedSubImplementation)
            throws CreationFailedException {
        return new TcpAether(registryEventHandler, capabilities, credentials,
                applicationTag, portTypes, userProperties, this);
    }
}
