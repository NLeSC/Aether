/* $Id: SmartSocketsSendPort.java 14985 2012-12-14 14:58:20Z ceriel $ */

package nl.esciencecenter.aether.impl.smartsockets;

import ibis.smartsockets.virtual.VirtualSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.SendPortDisconnectUpcall;
import nl.esciencecenter.aether.impl.Aether;
import nl.esciencecenter.aether.impl.Protocol;
import nl.esciencecenter.aether.impl.ReceivePortIdentifier;
import nl.esciencecenter.aether.impl.SendPort;
import nl.esciencecenter.aether.impl.SendPortConnectionInfo;
import nl.esciencecenter.aether.impl.SendPortIdentifier;
import nl.esciencecenter.aether.impl.WriteMessage;
import nl.esciencecenter.aether.io.BufferedArrayOutputStream;
import nl.esciencecenter.aether.io.Conversion;
import nl.esciencecenter.aether.io.OutputStreamSplitter;
import nl.esciencecenter.aether.io.SplitterException;

final class SmartSocketsSendPort extends SendPort implements Protocol {

    private class Conn extends SendPortConnectionInfo {
        VirtualSocket s;

        OutputStream out;

        Conn(VirtualSocket s, SmartSocketsSendPort port, ReceivePortIdentifier target)
                throws IOException {
            super(port, target);
            this.s = s;
            out = s.getOutputStream();
            splitter.add(out);
        }
                
        public String connectionType() {
            return s.toString();
        }

        public void closeConnection() {
            try {
                s.close();
            } catch (Throwable e) {
                // ignored
            } finally {
                try {
                    splitter.remove(out);
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    final OutputStreamSplitter splitter;

    final BufferedArrayOutputStream bufferedStream;

    SmartSocketsSendPort(Aether ibis, PortType type, String name,
            SendPortDisconnectUpcall cU, Properties props) throws IOException {
        super(ibis, type, name, cU, props);

        splitter = new OutputStreamSplitter(
          		!type.hasCapability(PortType.CONNECTION_ONE_TO_ONE) && 
          		!type.hasCapability(PortType.CONNECTION_MANY_TO_ONE),
        		type.hasCapability(PortType.CONNECTION_ONE_TO_MANY) || 
        		type.hasCapability(PortType.CONNECTION_MANY_TO_MANY));

        bufferedStream = new BufferedArrayOutputStream(splitter);
        initStream(bufferedStream);
    }

    protected long totalWritten() {
        return splitter.bytesWritten();
    }

    protected void resetWritten() {
        splitter.resetBytesWritten();
    }

    SendPortIdentifier getIdent() {
        return ident;
    }

    protected SendPortConnectionInfo doConnect(ReceivePortIdentifier receiver,
            long timeoutMillis, boolean fillTimeout) throws IOException {

        VirtualSocket s = ((SmartSocketsAether) ibis).connect(this, receiver, (int) timeoutMillis,
                        fillTimeout);
        Conn c = new Conn(s, this, receiver);
        if (out != null) {
            out.writeByte(NEW_RECEIVER);
        }
        initStream(bufferedStream);
        return c;
    }

    protected void sendDisconnectMessage(ReceivePortIdentifier receiver,
            SendPortConnectionInfo conn) throws IOException {

        out.writeByte(CLOSE_ONE_CONNECTION);

        byte[] receiverBytes = receiver.toBytes();
        byte[] receiverLength = new byte[Conversion.INT_SIZE];
        Conversion.defaultConversion.int2byte(receiverBytes.length,
                receiverLength, 0);
        out.writeArray(receiverLength);
        out.writeArray(receiverBytes);
        out.flush();
     
        // FIXME!
        //
        // This is here to make sure the close is processed before a new 
        // connections can be made (by this sendport). Without this ack, 
        // an application that uses a single sendport that connects/disconnects
        // for each message may get an 'AlreadyConnectedException', because the 
        // connect overtakes the disconnect...
        //
        // Unfortunately, it also causes a deadlock in 1-to-1 explict receive 
        // applications -- J
        Conn c = (Conn) conn;
        c.s.getInputStream().read();
    }

    protected void announceNewMessage() throws IOException {
        out.writeByte(NEW_MESSAGE);
        if (type.hasCapability(PortType.COMMUNICATION_NUMBERED)) {
            out.writeLong(ibis.registry().getSequenceNumber(name));
        }
    }

    protected void finishMessage(WriteMessage w, long cnt)
            throws IOException {
        if (type.hasCapability(PortType.CONNECTION_ONE_TO_MANY)
                || type.hasCapability(PortType.CONNECTION_MANY_TO_MANY)) {
            // exception may have been saved by the splitter. Get them
            // now.
            SplitterException e = splitter.getExceptions();
            if (e != null) {
                gotSendException(w, e);
            }
        }
        super.finishMessage(w, cnt);
    }

    protected void handleSendException(WriteMessage w, IOException x) {
        ReceivePortIdentifier[] ports = null;
        synchronized (this) {
            ports = receivers.keySet()
                            .toArray(new ReceivePortIdentifier[0]);
        }

        if (x instanceof SplitterException) {
            SplitterException e = (SplitterException) x;

            Exception[] exceptions = e.getExceptions();
            OutputStream[] streams = e.getStreams();

            for (int i = 0; i < ports.length; i++) {
                Conn c = (Conn) getInfo(ports[i]);
                for (int j = 0; j < streams.length; j++) {
                    if (c.out == streams[j]) {
                        lostConnection(ports[i], exceptions[j]);
                        break;
                    }
                }
            }
        } else {
            // Just close all connections. ???
            for (int i = 0; i < ports.length; i++) {
                lostConnection(ports[i], x);
            }
        }
    }

    protected void closePort() {

        try {
            out.writeByte(CLOSE_ALL_CONNECTIONS);
            out.close();
            bufferedStream.close();
        } catch (Throwable e) {
            // ignored
        }

        out = null;
    }

    Properties getProperties() {
        return properties;
    }
}
