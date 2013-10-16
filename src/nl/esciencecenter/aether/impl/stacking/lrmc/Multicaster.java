package nl.esciencecenter.aether.impl.stacking.lrmc;


import java.io.IOException;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.impl.stacking.lrmc.io.BufferedArrayInputStream;
import nl.esciencecenter.aether.impl.stacking.lrmc.io.BufferedArrayOutputStream;
import nl.esciencecenter.aether.impl.stacking.lrmc.io.LRMCInputStream;
import nl.esciencecenter.aether.impl.stacking.lrmc.io.LRMCOutputStream;
import nl.esciencecenter.aether.impl.stacking.lrmc.io.MessageReceiver;
import nl.esciencecenter.aether.impl.stacking.lrmc.util.Message;
import nl.esciencecenter.aether.impl.stacking.lrmc.util.MessageCache;
import nl.esciencecenter.aether.io.SerializationFactory;
import nl.esciencecenter.aether.io.SerializationInput;
import nl.esciencecenter.aether.io.SerializationOutput;
import nl.esciencecenter.aether.util.TypedProperties;

// TODO find a way to share destination arrays in messages --Rob

public class Multicaster implements MessageReceiver {

    private final int MESSAGE_SIZE;

    private final int MESSAGE_CACHE_SIZE;

    LabelRoutingMulticast lrmc;
    final PortType portType;
    final String name;

    private LRMCOutputStream os;

    BufferedArrayOutputStream bout;
    BufferedArrayInputStream bin;

    SerializationOutput sout;
    SerializationInput sin;

    private long totalData = 0;
    private long lastBytesWritten = 0;

    private MessageCache cache;

    private boolean finish = false;
    private boolean receiverDone = false;
    private Thread receiver = null;

    private InputStreams inputStreams = new InputStreams();

    private AetherIdentifier[] destination = null;

    LRMCSendPort sendPort = null;
    LRMCReceivePort receivePort = null;

    public Multicaster(LRMCAether ibis, PortType type, String name)
            throws IOException {
        TypedProperties tp = new TypedProperties(ibis.properties());
        this.MESSAGE_SIZE = tp.getIntProperty("lrmc.messageSize", 8 * 1024);
        this.MESSAGE_CACHE_SIZE = tp.getIntProperty("lrmc.messageCacheSize",
                1500);

        cache = new MessageCache(MESSAGE_CACHE_SIZE, MESSAGE_SIZE);

        lrmc = new LabelRoutingMulticast(ibis, this, cache, name);

        os = new LRMCOutputStream(lrmc, cache);

        bout = new BufferedArrayOutputStream(os, MESSAGE_SIZE);
        bin = new BufferedArrayInputStream(null);

        String serialization;

        if (type.hasCapability(PortType.SERIALIZATION_DATA)) {
            serialization = "data";
        } else if (type.hasCapability(PortType.SERIALIZATION_OBJECT_SUN)) {
            serialization = "sun";
        } else if (type.hasCapability(PortType.SERIALIZATION_OBJECT_IBIS)) {
            serialization = "ibis";
        } else if (type.hasCapability(PortType.SERIALIZATION_OBJECT)) {
            serialization = "object";
        } else {
            serialization = "byte";
        }
        sout = SerializationFactory.createSerializationOutput(serialization,
                bout, ibis.properties());
        sin = SerializationFactory.createSerializationInput(serialization, bin, ibis.properties());
        portType = type;
        this.name = name;
    }

    public synchronized void setDestination(AetherIdentifier[] dest) {
        destination = dest;
        cache.setDestinationSize(dest.length);
    }

    public void gotDone(int id) {
        // nothing
    }

    public boolean gotMessage(Message m) {

        LRMCInputStream tmp;

        // Fix: combined find call and add call into get().
        // There was a race here. (Ceriel)
        tmp = inputStreams.get(m.sender, cache);

        // tmp.addMessage(m);
        // inputStreams.hasData(tmp);
        // Fix: avoid race: message may have been read before setting
        // hasData. (Ceriel)
        return inputStreams.hasData(tmp, m);
    }

    void initializeSend(AetherIdentifier[] destinations) throws IOException {
        if (destination != destinations) {
            destination = destinations;
            lrmc.setDestination(destinations);
        }
        // We only want to return the number of bytes written in this bcast, so
        // reset the count.
        bout.resetBytesWritten();
        os.reset();
        sout.reset(true);
    }

    long finalizeSend() throws IOException {
        sout.flush();
        bout.forcedFlush();
        lastBytesWritten = bout.bytesWritten();
        totalData += lastBytesWritten;
        return lastBytesWritten;
    }

    long finalizeRead(LRMCInputStream stream) {
        inputStreams.returnStream(stream);
        long sz = bin.bytesRead();
        totalData += sz;
        return sz;
    }

    public LRMCReadMessage receive() {
        synchronized (this) {
            if (finish) {
                return null;
            }
            receiverDone = false;
            receiver = Thread.currentThread();
        }
        LRMCReadMessage o;
        try {
            LRMCInputStream stream = inputStreams.getNextFilledStream();

            if (stream == null) {
                return null;
            }

            // Plug it into the deserializer
            bin.setInputStream(stream);
            bin.resetBytesRead();

            o = new LRMCReadMessage(this, stream);
        } finally {
            synchronized (this) {
                receiverDone = true;
                if (finish) {
                    notifyAll();
                    return null;
                }
                receiver = null;
            }
        }
        return o;
    }

    public long bytesRead() {
        return bin.bytesRead();
    }

    public long bytesWritten() {
        return bout.bytesWritten();
    }

    public long totalBytes() {
        return totalData;
    }

    public void done() {
        synchronized (this) {
            finish = true;
            inputStreams.terminate();
            notifyAll();
            // we can tell the receiver thread, but we don't know that
            // it will actually finish, so we cannot join it.
            if (receiver != null) {
                // Wait until this is noticed.
                while (!receiverDone) {
                    try {
                        wait();
                    } catch (Exception e) {
                        // What to do here? (TODO)
                    }
                }
            }
        }
        try {
            os.close();

            // sout.close(); // don't close this one. It keeps on talking...
            sin.close();

            lrmc.done();
        } catch (IOException e) {
            // ignore, we tried ...
        }
    }

    public void removeReceivePort() {
        receivePort = null;
        if (sendPort == null) {
            // Apparently we are done ...
            done();
        }
    }
    
    public void removeSendPort() {
        sendPort = null;
        if (receivePort == null) {
            // Apparently we are done ...
            done();
        }
    }
}
