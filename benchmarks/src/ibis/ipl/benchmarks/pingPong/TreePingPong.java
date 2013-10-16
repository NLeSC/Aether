package ibis.ipl.benchmarks.pingPong;

/* $Id: TreePingPong.java 11529 2009-11-18 15:53:11Z ceriel $ */

import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReadMessage;
import nl.esciencecenter.aether.ReceivePort;
import nl.esciencecenter.aether.Registry;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.WriteMessage;

class TreePingPong {

    static final int TREE_NODES=1023;

static class Sender {
    SendPort sport;
    ReceivePort rport;

    Sender(ReceivePort rport, SendPort sport) {
        this.rport = rport;
        this.sport = sport;
    }

    void send(int count, int repeat) throws Exception {
	Tree t = new Tree(TREE_NODES);

        for (int r = 0; r < repeat; r++) {

            long time = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeObject(t);
                writeMessage.finish();

                ReadMessage readMessage = rport.receive();
		t = (Tree) readMessage.readObject();
                readMessage.finish();
            }

            time = System.currentTimeMillis() - time;

            double speed = (time * 1000.0) / count;
	    double tp = ((count * TREE_NODES * Tree.PAYLOAD) / (1024*1024)) / (time / 1000.0);

            System.err.println(count + " calls took "
                    + (time / 1000.0) + " s, time/call = " + speed
                    + " us, throughput = " + tp + " MB/s, msg size = " + (TREE_NODES * Tree.PAYLOAD));
        }
    }
}

static class ExplicitReceiver {

    SendPort sport;

    ReceivePort rport;

    ExplicitReceiver(ReceivePort rport, SendPort sport) {
        this.rport = rport;
        this.sport = sport;
    }

    void receive(int count, int repeat) throws Exception {
	Tree t = null;//new Tree(TREE_NODES);

        for (int r = 0; r < repeat; r++) {
            for (int i = 0; i < count; i++) {

                ReadMessage readMessage = rport.receive();
		t = (Tree) readMessage.readObject();
                readMessage.finish();

                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeObject(t);
                writeMessage.finish();
            }
        }
    }
}


    static Aether ibis;

    static Registry registry;

    public static void main(String[] args) {
        int count = 1000;
        int repeat = 50;
        int rank = 0;

        try {
            Capabilities s = new Capabilities(
                    Capabilities.CLOSED_WORLD,
                    Capabilities.ELECTIONS_STRICT);
            
            PortType t = new PortType(
                    PortType.SERIALIZATION_OBJECT,
                    PortType.CONNECTION_ONE_TO_ONE,
                    PortType.COMMUNICATION_RELIABLE,
                    PortType.RECEIVE_EXPLICIT);
            
            ibis = AetherFactory.createIbis(s, null, t);

            registry = ibis.registry();


            SendPort sport = ibis.createSendPort(t, "send port");
            ReceivePort rport;
            AetherIdentifier master = registry.elect("latency");
            AetherIdentifier remote;

            if (master.equals(ibis.identifier())) {
                rank = 0;
                remote = registry.getElectionResult("client");
            } else {
                registry.elect("client");
                rank = 1;
                remote = master;
            }

	    Sender sender = null;
	    ExplicitReceiver receiver = null;
            if (rank == 0) {
                rport = ibis.createReceivePort(t, "test port");
                rport.enableConnections();
                sport.connect(remote, "test port");
                sender = new Sender(rport, sport);
                sender.send(count, repeat);
            } else {
                sport.connect(remote, "test port");
                rport = ibis.createReceivePort(t, "test port");
                rport.enableConnections();
                receiver = new ExplicitReceiver(rport, sport);
                receiver.receive(count, repeat);
            }

            /* free the send ports first */
            sport.close();
            rport.close();
            ibis.end();
        } catch (Exception e) {
            System.err.println("Got exception " + e);
            System.err.println("StackTrace:");
            e.printStackTrace();
        }
    }
}
