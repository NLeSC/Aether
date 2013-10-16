package ibis.ipl.examples;

import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReadMessage;
import nl.esciencecenter.aether.ReceivePort;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.WriteMessage;

import java.io.IOException;

/**
 * This program is to be run as two instances. One is a server, the other a
 * client. The client sends a hello message to the server. The server prints it.
 * This version uses explicit receive.
 */

public class Hello {

    PortType portType = new PortType(PortType.COMMUNICATION_RELIABLE,
            PortType.SERIALIZATION_DATA, PortType.RECEIVE_EXPLICIT,
            PortType.CONNECTION_ONE_TO_ONE);

    Capabilities ibisCapabilities = new Capabilities(
            Capabilities.ELECTIONS_STRICT);

    private void server(Aether myIbis) throws IOException {

        // Create a receive port and enable connections.
        ReceivePort receiver = myIbis.createReceivePort(portType, "server");
        receiver.enableConnections();

        // Read the message.
        ReadMessage r = receiver.receive();
        String s = r.readString();
        r.finish();
        System.out.println("Server received: " + s);

        // Close receive port.
        receiver.close();
    }

    private void client(Aether myIbis, AetherIdentifier server) throws IOException {

        // Create a send port for sending requests and connect.
        SendPort sender = myIbis.createSendPort(portType);
        sender.connect(server, "server");

        // Send the message.
        WriteMessage w = sender.newMessage();
        w.writeString("Hi there");
        w.finish();

        // Close ports.
        sender.close();
    }

    private void run() throws Exception {
        // Create an ibis instance.
        Aether ibis = AetherFactory.createIbis(ibisCapabilities, null, portType);

        // Elect a server
        AetherIdentifier server = ibis.registry().elect("Server");

        System.out.println("Server is " + server);

        // If I am the server, run server, else run client.
        if (server.equals(ibis.identifier())) {
            server(ibis);
        } else {
            client(ibis, server);
        }

        // End ibis.
        ibis.end();
    }

    public static void main(String args[]) {
        try {
            new Hello().run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
