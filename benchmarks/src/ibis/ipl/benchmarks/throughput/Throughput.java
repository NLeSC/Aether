package ibis.ipl.benchmarks.throughput;

/* $Id: Throughput.java 12928 2011-01-24 10:12:31Z ceriel $ */


import nl.esciencecenter.aether.Ibis;
import nl.esciencecenter.aether.IbisCapabilities;
import nl.esciencecenter.aether.IbisFactory;
import nl.esciencecenter.aether.IbisIdentifier;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.ReadMessage;
import nl.esciencecenter.aether.ReceivePort;
import nl.esciencecenter.aether.Registry;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.WriteMessage;

import java.io.IOException;

public class Throughput {

    int count = 1000;

    int transferSize = 0;

    int rank;

    int remoteRank;

    int windowSize = Integer.MAX_VALUE;

    ReceivePort rport;

    SendPort sport;

    byte[] data = null;

    public static void main(String[] args) {
        new Throughput(args).run();
    }

    void send() throws IOException {
        int w = windowSize;

        System.err.println("count = " + count + " len = " + transferSize);
        for (int i = 0; i < count; i++) {
            WriteMessage writeMessage = sport.newMessage();
	    if (data != null) {
                writeMessage.writeArray(data);
	    }
            writeMessage.finish();

            if (--w == 0) {
                System.err.println("EEEEEEEEEEEK");
                ReadMessage readMessage = rport.receive();
		if (data != null) {
		    readMessage.readArray(data);
		}
                readMessage.finish();
                w = windowSize;
            }
        }
        ReadMessage readMessage = rport.receive();
        readMessage.finish();
    }

    void rcve() throws IOException {
        int w = windowSize;
        for (int i = 0; i < count; i++) {
            ReadMessage readMessage = rport.receive();
	    if (data != null) {
	        readMessage.readArray(data);
	    }
            readMessage.finish();

            if (--w == 0) {
                System.err.println("EEEEEEEEEEEK");
                WriteMessage writeMessage = sport.newMessage();
		if (data != null) {
                    writeMessage.writeArray(data);
		}
                writeMessage.finish();
                w = windowSize;
            }
        }
        WriteMessage writeMessage = sport.newMessage();
        writeMessage.finish();
    }

    Throughput(String[] args) {
        /* parse the commandline */
        int options = 0;
        for (int i = 0; i < args.length; i++) {
            if (false) {
            } else if (args[i].equals("-window")) {
                windowSize = Integer.parseInt(args[++i]);
                if (windowSize <= 0) {
                    windowSize = Integer.MAX_VALUE;
                }
            } else if (options == 0) {
                count = Integer.parseInt(args[i]);
                options++;
            } else if (options == 1) {
                transferSize = Integer.parseInt(args[i]);
                options++;
            }
        }

        if (options != 2) {
            System.err.println("Throughput <count> <size>");
            System.exit(11);
        }

	if (transferSize >= 0) {
	    data = new byte[transferSize];
	}
    }

    public void run() {
        try {
            IbisCapabilities s = new IbisCapabilities(
                    IbisCapabilities.ELECTIONS_STRICT
                    );
            PortType t = new PortType(
                    PortType.SERIALIZATION_BYTE,
                    PortType.CONNECTION_ONE_TO_ONE,
                    PortType.COMMUNICATION_RELIABLE,
                    PortType.RECEIVE_EXPLICIT);
                     
            Ibis ibis = IbisFactory.createIbis(s, null, true, null, t);

            Registry r = ibis.registry();

            IbisIdentifier master = r.elect("throughput");
            IbisIdentifier remote;

            if (master.equals(ibis.identifier())) {
                rank = 0;
                remote = r.getElectionResult("1");
                System.err.println(">>>>>>>> Righto, I'm the master");
            } else {
                r.elect("1");
                rank = 1;
                remote = master;
                System.err.println(">>>>>>>> Righto, I'm the slave");
            }

            rport = ibis.createReceivePort(t, "test port");
            rport.enableConnections();
            sport = ibis.createSendPort(t);
            sport.connect(remote, "test port");

            if (rank == 0) {
                // warmup
                send();
                long time = System.currentTimeMillis();
                send();
                time = System.currentTimeMillis() - time;
                double speed = (time * 1000.0) / count;
                System.out.print("Latency: " + count + " calls took "
                        + (time / 1000.0) + " seconds, time/call = " + speed
                        + " micros, ");
		if (data != null) {
                    double dataSent = ((double) transferSize * (count + count
                            / windowSize))
                            / (1024.0 * 1024.0);
                    System.out.println("Throughput: "
                            + (dataSent / (time / 1000.0)) + " MByte/s");
		}
            } else {
                rcve();
                rcve();
            }

            /* free the send ports first */
            sport.close();
            rport.close();
            ibis.end();
        } catch (Exception e) {
            System.out.println("Got exception " + e);
            System.out.println("StackTrace:");
            e.printStackTrace();
        }
    }
}
