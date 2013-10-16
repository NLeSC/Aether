package nl.esciencecenter.aether.registry.central;


import java.util.Comparator;

import nl.esciencecenter.aether.impl.AetherIdentifier;

/**
 * Compares two IbisIdentifiers made by this registry by numerically sorting
 * ID's
 * 
 */
public class AetherComparator implements Comparator<AetherIdentifier> {

    public int compare(AetherIdentifier one, AetherIdentifier other) {
        try {

            int oneID = Integer.parseInt(one.getID());
            int otherID = Integer.parseInt(other.getID());

            return oneID - otherID;

        } catch (NumberFormatException e) {
            // IGNORE
        }
        return one.getID().compareTo(other.getID());
    }

}
