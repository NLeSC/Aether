package nl.esciencecenter.aether.impl.stacking.lrmc;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.ReceivePortIdentifier;

class LRMCReceivePortIdentifier implements ReceivePortIdentifier {

    private static final long serialVersionUID = 1L;

    AetherIdentifier ibis;
    String name;

    LRMCReceivePortIdentifier(AetherIdentifier ibis, String name) {
        this.ibis = ibis;
        this.name = name;
    }

    public AetherIdentifier ibisIdentifier() {
        return ibis;
    }

    public String name() {
        return name;
    }
}
