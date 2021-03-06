package ibis.ipl.benchmarks.masterWorker;

/* $Id: MasterWorker.java 11529 2009-11-18 15:53:11Z ceriel $ */


import nl.esciencecenter.aether.Aether;
import nl.esciencecenter.aether.Capabilities;
import nl.esciencecenter.aether.AetherFactory;
import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReadMessage;
import nl.esciencecenter.aether.ReceivePort;
import nl.esciencecenter.aether.Registry;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.SendPortIdentifier;
import nl.esciencecenter.aether.WriteMessage;

import java.util.HashMap;

/**
 * Simulates master worker communication model. Workers request work/return
 * results to the master, and the master replies to the workers.
 */
final class MasterWorker {
    static final int COUNT = 10000;

    static final boolean ASSERT = false;

    Aether ibis;

    Registry registry;

    PortType oneToOneType;

    PortType manyToOneType;

    AetherIdentifier masterID;

    MasterWorker() {

        try {

            Capabilities s = new Capabilities(
                    Capabilities.ELECTIONS_STRICT
                    );
            
            manyToOneType = new PortType(
                    PortType.SERIALIZATION_OBJECT,
                    PortType.COMMUNICATION_RELIABLE,
                    PortType.RECEIVE_EXPLICIT,
                    PortType.CONNECTION_MANY_TO_ONE);
            
            oneToOneType = new PortType(PortType.SERIALIZATION_OBJECT,
                    PortType.CONNECTION_ONE_TO_ONE,
                    PortType.COMMUNICATION_RELIABLE,
                    PortType.RECEIVE_EXPLICIT);

            ibis = AetherFactory.createIbis(s, null, manyToOneType, oneToOneType);

            registry = ibis.registry();

            masterID = registry.elect("master");

            boolean master = masterID.equals(ibis.identifier());

 
            if (master) {
                master();
            } else {
                worker();
            }
        } catch (Exception e) {
            System.err.println("main caught exception: " + e);
            e.printStackTrace();
        }
    }

    void master() throws Exception {
        //map of sendports to workers, indexed on sendportidentifiers of the
        //worker's sendports
        HashMap<SendPortIdentifier, SendPort> workers
                = new HashMap<SendPortIdentifier, SendPort>();

        ReadMessage readMessage;
        WriteMessage writeMessage;
        SendPort sendPort;
        SendPortIdentifier origin;
        Object data;
        long start;
        long end;
        int max = 0;

        ReceivePort rport = ibis.createReceivePort(manyToOneType,
                "master receive port");
        rport.enableConnections();

        while (true) {
            start = System.currentTimeMillis();

            for (int i = 0; (i < COUNT); i++) {

                readMessage = rport.receive();
                origin = readMessage.origin();
                data = readMessage.readObject();
                readMessage.finish();

                sendPort = workers.get(origin);

                if (sendPort == null) {
                    sendPort = ibis.createSendPort(oneToOneType);
                    sendPort.connect(origin.ibisIdentifier(), "receiveport");

                    workers.put(origin, sendPort);

                    System.err.println("MASTER: new worker detected,"
                            + " total now: " + workers.size());
                }

                writeMessage = sendPort.newMessage();
                writeMessage.writeObject(data);
                writeMessage.finish();
            }

            end = System.currentTimeMillis();

            int speed = (int) (((COUNT * 1.0) / (end - start)) * 1000.0);

            if (speed > max) {
                max = speed;
            }

            System.err.println("MASTER: " + COUNT + " requests / "
                    + (end - start) + " ms (" + speed + " requests/s), max: "
                    + max);

        }
    }

    void worker() throws Exception {
        WriteMessage writeMessage;
        ReadMessage readMessage;
        Data original = new Data();
        Data result;
        ReceivePort rport = ibis.createReceivePort(oneToOneType, "receiveport");
        rport.enableConnections();
        SendPort sport = ibis.createSendPort(manyToOneType);

        sport.connect(masterID, "master receive port");

        while (true) {
            writeMessage = sport.newMessage();
            writeMessage.writeObject(original);
            writeMessage.finish();

            readMessage = rport.receive();
            result = (Data) readMessage.readObject();
            readMessage.finish();

            if (ASSERT) {
                if (!original.equals(result)) {
                    System.err.println("did not receive data correctly");
                    System.exit(1);
                }
            }
        }
    }

    public static void main(String args[]) {
        new MasterWorker();
    }
}

