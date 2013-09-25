package nl.esciencecenter.aether.registry.central;


import java.util.Comparator;

import nl.esciencecenter.aether.impl.IbisIdentifier;

/**
 * Compares two IbisIdentifiers made by this registry by numerically sorting
 * ID's
 * 
 */
public class IbisComparator implements Comparator<IbisIdentifier> {

    public int compare(IbisIdentifier one, IbisIdentifier other) {
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
