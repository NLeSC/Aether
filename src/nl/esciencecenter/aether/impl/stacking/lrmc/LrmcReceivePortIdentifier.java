package nl.esciencecenter.aether.impl.stacking.lrmc;

import nl.esciencecenter.aether.IbisIdentifier;
import nl.esciencecenter.aether.ReceivePortIdentifier;

class LrmcReceivePortIdentifier implements ReceivePortIdentifier {

    private static final long serialVersionUID = 1L;

    IbisIdentifier ibis;
    String name;

    LrmcReceivePortIdentifier(IbisIdentifier ibis, String name) {
        this.ibis = ibis;
        this.name = name;
    }

    public IbisIdentifier ibisIdentifier() {
        return ibis;
    }

    public String name() {
        return name;
    }
}
