package ibis.ipl.benchmarks.pingPong;

/* $Id: ArrayPingPong.java 11529 2009-11-18 15:53:11Z ceriel $ */

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

class ArrayPingPong {
static class Sender {
    SendPort sport;
    ReceivePort rport;

    Sender(ReceivePort rport, SendPort sport) {
        this.rport = rport;
        this.sport = sport;
    }

    void send(int count, int repeat, int arraySize) throws Exception {
	double[] a = new double[arraySize];

        for (int r = 0; r < repeat; r++) {

            long time = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeArray(a);
                writeMessage.finish();

                ReadMessage readMessage = rport.receive();
		readMessage.readArray(a);
                readMessage.finish();
            }

            time = System.currentTimeMillis() - time;

            double speed = (time * 1000.0) / count;
	    double tp = ((count * arraySize * 8) / (1024*1024)) / (time / 1000.0);

            System.err.println(count + " calls took "
                    + (time / 1000.0) + " s, time/call = " + speed
                    + " us, throughput = " + tp + " MB/s, msg size = " + (arraySize * 8));
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

    void receive(int count, int repeat, int arraySize) throws Exception {

	double[] a = new double[arraySize];

        for (int r = 0; r < repeat; r++) {
            for (int i = 0; i < count; i++) {
                ReadMessage readMessage = rport.receive();
		readMessage.readArray(a);
                readMessage.finish();

                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeArray(a);
                writeMessage.finish();
            }
        }
    }
}


    static Aether ibis;

    static Registry registry;

    public static void main(String[] args) {
        int count = 1000;
        int repeat = 10;
        int rank;

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
            } else {
                sport.connect(remote, "test port");
                rport = ibis.createReceivePort(t, "test port");
                rport.enableConnections();
                receiver = new ExplicitReceiver(rport, sport);
            }

	    int dataSize = 1;
	    for(int i=0; i<20; i++) {
		if (rank == 0) {
                    sender.send(count, repeat, dataSize);
		} else {
                    receiver.receive(count, repeat, dataSize);
		}

		dataSize *= 2;

		if(i >= 12) count /= 2;
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
