package nl.esciencecenter.aether.registry.gossip;


import java.util.Comparator;
import java.util.UUID;

import nl.esciencecenter.aether.impl.AetherIdentifier;

/**
 * Compares two IbisIdentifiers made by this registry by comparing
 * UUID's
 * 
 */
public class AetherComparator implements Comparator<AetherIdentifier> {

    public int compare(AetherIdentifier one, AetherIdentifier other) {

        UUID oneID = UUID.fromString(one.getID());
        UUID otherID = UUID.fromString(other.getID());

        return oneID.compareTo(otherID);

    }

}
