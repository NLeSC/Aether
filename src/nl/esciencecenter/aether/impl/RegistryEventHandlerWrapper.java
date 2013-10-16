package nl.esciencecenter.aether.impl;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.RegistryEventHandler;

public class RegistryEventHandlerWrapper implements RegistryEventHandler {
    
    private final RegistryEventHandler handler;
    private final Aether ibis;
    
    public RegistryEventHandlerWrapper(RegistryEventHandler h, Aether i) {
	handler = h;
	ibis = i;
    }

    public void died(AetherIdentifier corpse) {
	if (handler != null) {
	    handler.died(corpse);
	}
	ibis.died(corpse);
    }

    public void electionResult(String electionName, AetherIdentifier winner) {
	if (handler != null) {
	    handler.electionResult(electionName, winner);
	}
    }

    public void gotSignal(String signal, AetherIdentifier source) {
	if (handler != null) {
	    handler.gotSignal(signal, source);
	}
    }

    public void joined(AetherIdentifier joinedIbis) {
	if (handler != null) {
	    handler.joined(joinedIbis);
	}
    }

    public void left(AetherIdentifier leftIbis) {
	if (handler != null) {
	    handler.left(leftIbis);
	}
	ibis.left(leftIbis);
    }

    public void poolClosed() {
	if (handler != null) {
	    handler.poolClosed();
	}
    }

    public void poolTerminated(AetherIdentifier source) {
	if (handler != null) {
	    handler.poolTerminated(source);
	}
    }
}
