/* $Id: TcpIbisStarter.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.tcp;


import java.util.ArrayList;
import java.util.Properties;

import nl.esciencecenter.aether.CapabilitySet;
import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Ibis;
import nl.esciencecenter.aether.IbisCapabilities;
import nl.esciencecenter.aether.IbisCreationFailedException;
import nl.esciencecenter.aether.IbisFactory;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.RegistryEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TcpIbisStarter extends nl.esciencecenter.aether.IbisStarter {

    static final Logger logger = LoggerFactory
            .getLogger("ibis.ipl.impl.tcp.TcpIbisStarter");

    static final IbisCapabilities ibisCapabilities = new IbisCapabilities(
            IbisCapabilities.CLOSED_WORLD,
            IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED,
            IbisCapabilities.MEMBERSHIP_UNRELIABLE, IbisCapabilities.SIGNALS,
            IbisCapabilities.ELECTIONS_UNRELIABLE,
            IbisCapabilities.ELECTIONS_STRICT, IbisCapabilities.MALLEABLE,
            IbisCapabilities.TERMINATION);

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

    public TcpIbisStarter(String nickName, String iplVersion,
            String implementationVersion) {
        super(nickName, iplVersion, implementationVersion);
    }

    @Override
    public boolean matches(IbisCapabilities capabilities, PortType[] types) {
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
            IbisCapabilities capabilities, PortType[] types) {
        return capabilities.unmatchedCapabilities(ibisCapabilities);
    }

    @Override
    public PortType[] unmatchedPortTypes(IbisCapabilities capabilities,
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
    public Ibis startIbis(IbisFactory factory,
            RegistryEventHandler registryEventHandler,
            Properties userProperties, IbisCapabilities capabilities,
            Credentials credentials, byte[] applicationTag,
            PortType[] portTypes, String specifiedSubImplementation)
            throws IbisCreationFailedException {
        return new TcpIbis(registryEventHandler, capabilities, credentials,
                applicationTag, portTypes, userProperties, this);
    }
}