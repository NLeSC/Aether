package ibis.ipl.examples;

import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.RegistryEventHandler;

/**
 * This program shows how to handle events from the registry using upcalls. It
 * will run for 30 seconds, then stop. You can start as many instances of this
 * application as you like.
 */

public class RegistryUpcalls implements RegistryEventHandler {

    Capabilities ibisCapabilities = new Capabilities(
            Capabilities.MEMBERSHIP_TOTALLY_ORDERED);

    // Methods of the registry event handler. We only implement the
    // join/leave/died methods, as signals and elections are disabled

    public void joined(AetherIdentifier joinedIbis) {
        System.err.println("Got event from registry: " + joinedIbis
                + " joined pool");
    }

    public void died(AetherIdentifier corpse) {
        System.err.println("Got event from registry: " + corpse + " died!");
    }

    public void left(AetherIdentifier leftIbis) {
        System.err.println("Got event from registry: " + leftIbis + " left");
    }

    public void electionResult(String electionName, AetherIdentifier winner) {
        System.err.println("Got event from registry: " + winner
                + " won election " + electionName);
    }

    public void gotSignal(String signal, AetherIdentifier source) {
        System.err.println("Got event from registry: signal \"" + signal
                + "\" from " + source);
    }

    public void poolClosed() {
        System.err.println("Got event from registry: pool closed");
    }

    public void poolTerminated(AetherIdentifier source) {
        System.err.println("Got event from registry: pool terminated by "
                + source);
    }

    private void run() throws Exception {
        // Create an ibis instance, pass ourselves as the event handler
        Aether ibis = AetherFactory.createIbis(ibisCapabilities, this);
        ibis.registry().enableEvents();

        // sleep for 30 seconds
        Thread.sleep(30000);

        // End ibis.
        ibis.end();
    }

    public static void main(String args[]) {
        try {
            new RegistryUpcalls().run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
