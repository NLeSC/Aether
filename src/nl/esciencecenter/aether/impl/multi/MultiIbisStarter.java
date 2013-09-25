/* $Id: MultiIbisStarter.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.multi;


import java.util.Properties;

import nl.esciencecenter.aether.CapabilitySet;
import nl.esciencecenter.aether.Credentials;
import nl.esciencecenter.aether.Ibis;
import nl.esciencecenter.aether.IbisCapabilities;
import nl.esciencecenter.aether.IbisCreationFailedException;
import nl.esciencecenter.aether.IbisFactory;
import nl.esciencecenter.aether.IbisStarter;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.RegistryEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MultiIbisStarter extends IbisStarter {

    static final Logger logger = LoggerFactory
            .getLogger(MultiIbisStarter.class);

    public MultiIbisStarter(String nickName, String iplVersion,
            String implementationVersion) {
        super(nickName, iplVersion, implementationVersion);
    }

    public boolean matches(IbisCapabilities capabilities, PortType[] types) {
        return true;
    }

    public CapabilitySet unmatchedIbisCapabilities(
            IbisCapabilities capabilities, PortType[] types) {
        return new CapabilitySet();
    }

    public PortType[] unmatchedPortTypes(IbisCapabilities capabilities,
            PortType[] types) {
        return new PortType[0];
    }

    public Ibis startIbis(IbisFactory factory,
            RegistryEventHandler registryEventHandler,
            Properties userProperties, IbisCapabilities capabilities,
            Credentials credentials, byte[] applicationTag, PortType[] portTypes,
            String specifiedSubImplementation)
            throws IbisCreationFailedException {
        return new MultiIbis(factory, registryEventHandler, userProperties,
                capabilities, credentials, applicationTag, portTypes,
                specifiedSubImplementation, this);
    }
}
