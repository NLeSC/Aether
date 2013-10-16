package nl.esciencecenter.aether.impl.multi;


import java.util.HashMap;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.ReceivePortIdentifier;

public class MultiReceivePortIdentifier implements ReceivePortIdentifier {

    /**
     * Serial Version ID - Generated
     */
    private static final long serialVersionUID = 3918962573170503300L
    ;
    private final String name;
    private final AetherIdentifier id;

    private final HashMap<String, ReceivePortIdentifier>subIds = new HashMap<String, ReceivePortIdentifier>();

    public MultiReceivePortIdentifier(AetherIdentifier id, String name) {
        this.name = name;
        this.id = id;
    }

    public AetherIdentifier ibisIdentifier() {
        return id;
    }

    public String name() {
        return name;
    }

    ReceivePortIdentifier getSubId(String ibisName) {
        return subIds.get(ibisName);
    }

    void addSubId(String ibisName, ReceivePortIdentifier subId) {
        subIds.put(ibisName, subId);
    }
}
