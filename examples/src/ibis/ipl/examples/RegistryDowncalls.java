package ibis.ipl.examples;

import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;

/**
 * This program shows how to handle events from the registry using downcalls. It
 * will run for 30 seconds, then stop. You can start as many instances of this
 * application as you like.
 */

public class RegistryDowncalls {

    Capabilities ibisCapabilities = new Capabilities(
            Capabilities.MEMBERSHIP_TOTALLY_ORDERED);

    private void run() throws Exception {
        // Create an ibis instance, pass "null" as the event handler, enabling
        // downcalls
        Aether ibis = AetherFactory.createIbis(ibisCapabilities, null);

        // poll the registry once every second for new events
        for (int i = 0; i < 30; i++) {

            // poll for new ibises
            AetherIdentifier[] joinedIbises = ibis.registry().joinedIbises();
            for (AetherIdentifier joinedIbis : joinedIbises) {
                System.err.println("Ibis joined: " + joinedIbis);
            }

            // poll for left ibises
            AetherIdentifier[] leftIbises = ibis.registry().leftIbises();
            for (AetherIdentifier leftIbis : leftIbises) {
                System.err.println("Ibis left: " + leftIbis);
            }

            // poll for died ibises
            AetherIdentifier[] diedIbises = ibis.registry().diedIbises();
            for (AetherIdentifier diedIbis : diedIbises) {
                System.err.println("Ibis died: " + diedIbis);
            }

            // sleep for a second
            Thread.sleep(1000);
        }

        // End ibis.
        ibis.end();
    }

    public static void main(String args[]) {
        try {
            new RegistryDowncalls().run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
