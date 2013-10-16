package nl.esciencecenter.aether.impl.multi;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.SendPortIdentifier;

public class MultiSendPortIdentifier implements SendPortIdentifier {

    /**
     * Serial Version UID - Generated
     */
    private static final long serialVersionUID = -1400675486608710003L;

    private final AetherIdentifier id;
    private final String name;

    public MultiSendPortIdentifier(AetherIdentifier identifier, String name) {
        this.id = identifier;
        this.name = name;
    }

    public AetherIdentifier ibisIdentifier() {
        return id;
    }

    public String name() {
        return name;
    }

}
