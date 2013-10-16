package nl.esciencecenter.aether.support.management;


import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.server.Server;

public class Example {

    private static class Shutdown extends Thread {
        private final Server server;

        Shutdown(Server server) {
            this.server = server;
        }

        public void run() {
            server.end(-1);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] arguments) {

        // start a server
        Server server = null;
        try {
            server = new Server(new Properties());
        } catch (Throwable t) {
            System.err.println("Could not start Server: " + t);
            System.exit(1);
        }

        // print server description
        System.err.println(server.toString());

        // register shutdown hook
        try {
            Runtime.getRuntime().addShutdownHook(new Shutdown(server));
        } catch (Exception e) {
            System.err.println("warning: could not registry shutdown hook");
        }

        AttributeDescription load = new AttributeDescription(
                "java.lang:type=OperatingSystem", "SystemLoadAverage");

        AttributeDescription cpu = new AttributeDescription(
                "java.lang:type=OperatingSystem", "ProcessCpuTime");

        AttributeDescription vivaldi = new AttributeDescription("ibis",
                "vivaldi");

        AttributeDescription connections = new AttributeDescription("ibis",
                "connections");
        
        AttributeDescription sentBytesPerIbis = new AttributeDescription("ibis",
                "sentBytesPerIbis");
        
        AttributeDescription receivedBytesPerIbis = new AttributeDescription("ibis",
                "receivedBytesPerIbis");
        
        AttributeDescription wonElections = new AttributeDescription("ibis",
                "wonElections");
        
        AttributeDescription senderConnectionTypes = new AttributeDescription("ibis",
                "senderConnectionTypes");
        
        AttributeDescription receiverConnectionTypes = new AttributeDescription("ibis",
                "receiverConnectionTypes");
        
        
        while (true) {

            // get list of ibises in the pool named "test"
            AetherIdentifier[] ibises = server.getRegistryService().getMembers(
                    "test");

            // for each ibis, print these attributes
            if (ibises != null) {
                for (AetherIdentifier ibis : ibises) {
                    try {
                        System.err
                                .println(ibis
                                        + " [load, total cpu time, vivaldi coordinates] = "
                                        + Arrays.toString(server
                                                .getManagementService()
                                                .getAttributes(ibis, load, cpu,
                                                        vivaldi)));
                        System.err
                                .println(ibis
                                        + " connected to = "
                                        + Arrays
                                                .toString((AetherIdentifier[]) server
                                                        .getManagementService()
                                                        .getAttributes(ibis,
                                                                connections)[0]));
                        
                        Map<AetherIdentifier, Long> sent = (Map<AetherIdentifier, Long>)
                                (server.getManagementService().getAttributes(ibis, sentBytesPerIbis)[0]);
                        
                        if (sent != null) {
                            for (Entry<AetherIdentifier, Long> e : sent.entrySet()) {
                                System.err.println(ibis + " wrote " + e.getValue() + " bytes to " + e.getKey());
                            }
                        }
                        
                        Map<AetherIdentifier, Long> received = (Map<AetherIdentifier, Long>)
                                (server.getManagementService().getAttributes(ibis, receivedBytesPerIbis)[0]);
                        
                        if (received != null) {
                            for (Entry<AetherIdentifier, Long> e : received.entrySet()) {
                                System.err.println(ibis + " read " + e.getValue() + " bytes from " + e.getKey());
                            }
                        }
                        
                        String[] won = (String[])
                            (server.getManagementService().getAttributes(ibis, wonElections)[0]);
                
                        if (won != null) {
                            for (String s : won) {
                                System.err.println(ibis + " won election " + s);
                            }
                        }
                        
                        Map<AetherIdentifier, Set<String>> senderConnections =
                            (Map<AetherIdentifier, Set<String>>) (server.getManagementService().getAttributes(ibis, 
                                    senderConnectionTypes))[0];
                        
                        if (senderConnections != null) {
                            System.err.println("senderConnections: " + senderConnections.toString());
                        }
                        
                        Map<AetherIdentifier, Set<String>> receiverConnections =
                            (Map<AetherIdentifier, Set<String>>) (server.getManagementService().getAttributes(ibis, 
                                    receiverConnectionTypes))[0];
                        
                        if (receiverConnections != null) {
                            System.err.println("receiverConnections: " + receiverConnections.toString());
                        }

                    } catch (Exception e) {
                        System.err.println("Could not get management info: ");
                        e.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }

        }
    }

}
