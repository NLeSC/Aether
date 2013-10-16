package nl.esciencecenter.aether.impl.multi;

import nl.esciencecenter.aether.AetherIdentifier;

final class IbisIdentifierWrapper implements Comparable<IbisIdentifierWrapper> {
    final AetherIdentifier id;
    final String ibisName;

    IbisIdentifierWrapper(String ibisName, AetherIdentifier id) {
        this.id = id;
        this.ibisName = ibisName;
    }

    public int compareTo(IbisIdentifierWrapper w) {
        int compare = this.ibisName.compareTo(w.ibisName);
        if (compare == 0) {
            return id.compareTo(w.id);
        }
        else {
            return compare;
        }
    }
}
