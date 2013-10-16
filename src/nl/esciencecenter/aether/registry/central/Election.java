package nl.esciencecenter.aether.registry.central;

import nl.esciencecenter.aether.impl.AetherIdentifier;

public class Election {
    
    private final Event event;

    public Election(Event event) {
        this.event = event;
    }

    public String getName() {
        return event.getDescription();
    }

    public AetherIdentifier getWinner() {
        return event.getIbis();
    }
    
    public Event getEvent() {
        return event;
    }
    
    public String toString() {
        return getName() + " won by " + getWinner();
    }
}
