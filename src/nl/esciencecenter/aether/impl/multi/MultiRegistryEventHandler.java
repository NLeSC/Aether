package nl.esciencecenter.aether.impl.multi;


import java.io.IOException;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.RegistryEventHandler;

public class MultiRegistryEventHandler implements RegistryEventHandler {

    private final RegistryEventHandler subHandler;

    private final MultiAether ibis;

    private MultiRegistry registry;

    private String ibisName;

    public MultiRegistryEventHandler(MultiAether ibis,
            RegistryEventHandler subHandler) {
        this.ibis = ibis;
        this.subHandler = subHandler;
    }

    public synchronized void died(AetherIdentifier corpse) {
        while (registry == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Ignored
            }
        }
        try {
            MultiAetherIdentifier id = ibis.mapIdentifier(corpse, ibisName);
            if (!registry.died.containsKey(id)) {
                registry.died.put(id, id);
                subHandler.died(id);
            }
        } catch (IOException e) {
            // TODO What the hell to do.
        }
    }

    public synchronized void electionResult(String electionName,
            AetherIdentifier winner) {
        while (registry == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Ignored
            }
        }
        try {
            MultiAetherIdentifier id = ibis.mapIdentifier(winner, ibisName);
            if (!registry.elected.containsKey(electionName)) {
                registry.elected.put(electionName, id);
                subHandler.electionResult(electionName, id);
            } else {
                MultiAetherIdentifier oldWinner = registry.elected
                        .get(electionName);
                if (!oldWinner.equals(id)) {
                    registry.elected.put(electionName, id);
                    subHandler.electionResult(electionName, id);
                }
            }
        } catch (IOException e) {
            // TODO What the hell to do
        }
    }

    public void gotSignal(String signal, AetherIdentifier source) {
        subHandler.gotSignal(signal, source);
    }

    public synchronized void joined(AetherIdentifier joinedIbis) {
        while (registry == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Ignored
            }
        }
        try {
            MultiAetherIdentifier id = ibis.mapIdentifier(joinedIbis, ibisName);
            if (!registry.joined.containsKey(id)) {
                registry.joined.put(id, id);
                subHandler.joined(id);
            }
        } catch (IOException e) {
            // TODO What the hell to do here?
        }
    }

    public synchronized void left(AetherIdentifier leftIbis) {
        while (registry == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Ignored
            }
        }
        try {
            MultiAetherIdentifier id = ibis.mapIdentifier(leftIbis, ibisName);
            if (!registry.left.containsKey(id)) {
                registry.left.put(id, id);
                subHandler.left(id);
            }
        } catch (IOException e) {
            // TODO What the hell to do here?
        }
    }

    public synchronized void setName(String ibisName) {
        this.ibisName = ibisName;
    }

    public synchronized void setRegistry(MultiRegistry registry) {
        this.registry = (MultiRegistry) ibis.registry();
        notifyAll();
    }

    public void poolClosed() {
        // FIXME: implement
    }

    public void poolTerminated(AetherIdentifier source) {
        // FIXME: implement
    }

}
