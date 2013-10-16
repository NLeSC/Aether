package nl.esciencecenter.aether.impl.multi;

import nl.esciencecenter.aether.AetherIdentifier;

final class AetherIdentifierWrapper implements Comparable<AetherIdentifierWrapper> {
    final AetherIdentifier id;
    final String ibisName;

    AetherIdentifierWrapper(String ibisName, AetherIdentifier id) {
        this.id = id;
        this.ibisName = ibisName;
    }

    public int compareTo(AetherIdentifierWrapper w) {
        int compare = this.ibisName.compareTo(w.ibisName);
        if (compare == 0) {
            return id.compareTo(w.id);
        }
        else {
            return compare;
        }
    }
}
