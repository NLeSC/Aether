/* $Id: LrmcIbisStarter.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.stacking.lrmc;


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

public final class LRMCAetherStarter extends nl.esciencecenter.aether.AetherStarter {

    static final Logger logger = LoggerFactory
            .getLogger("ibis.ipl.impl.stacking.lrmc.LrmcIbisStarter");

    public LRMCAetherStarter(String nickName, String iplVersion,
            String implementationVersion) {
        super(nickName, iplVersion, implementationVersion);
    }

    public boolean matches(Capabilities capabilities, PortType[] types) {
        //pretend we can do everything
        return true;
    }

    public CapabilitySet unmatchedIbisCapabilities(
            Capabilities capabilities, PortType[] types) {
        return new CapabilitySet();
    }

    public PortType[] unmatchedPortTypes(Capabilities capabilities,
            PortType[] types) {
        return new PortType[0];
    }

    public Aether startIbis(AetherFactory factory,
            RegistryEventHandler registryEventHandler,
            Properties userProperties, Capabilities capabilities, Credentials credentials,
            byte[] applicationTag, PortType[] portTypes, String specifiedSubImplementation) throws CreationFailedException {
        return new LRMCAether(factory, registryEventHandler,
                userProperties, capabilities, credentials, applicationTag, portTypes, specifiedSubImplementation, this);
    }
}
