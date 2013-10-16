package nl.esciencecenter.aether.impl.stacking.lrmc;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

import nl.esciencecenter.aether.AetherIdentifier;
import nl.esciencecenter.aether.ReadMessage;
import nl.esciencecenter.aether.ReceivePort;
import nl.esciencecenter.aether.SendPortIdentifier;
import nl.esciencecenter.aether.impl.stacking.lrmc.io.LRMCInputStream;
import nl.esciencecenter.aether.io.SerializationInput;
import nl.esciencecenter.aether.util.ThreadPool;

public class LRMCReadMessage implements ReadMessage {

    SerializationInput in;
    boolean isFinished = false;
    Multicaster om;
    long count = 0;
    LRMCInputStream stream;
    private boolean inUpcall = false;

    public LRMCReadMessage(Multicaster om, LRMCInputStream stream) {
        this.in = om.sin;
        this.om = om;
        this.stream = stream;
    }
    
    void setInUpcall(boolean val) {
        inUpcall = val;
    }

    protected final void checkNotFinished() throws IOException {
        if (isFinished) {
            throw new IOException(
                    "Operating on a message that was already finished");
        }
    }

    public SendPortIdentifier origin() {
        int source = om.bin.getInputStream().getSource();
        AetherIdentifier id = om.lrmc.ibis.getId(source);
        return new LRMCSendPortIdentifier(id, om.name);
    }

    protected int available() throws IOException {
        checkNotFinished();
        return in.available();
    }

    public boolean readBoolean() throws IOException {
        checkNotFinished();
        return in.readBoolean();
    }

    public byte readByte() throws IOException {
        checkNotFinished();
        return in.readByte();
    }

    public char readChar() throws IOException {
        checkNotFinished();
        return in.readChar();
    }

    public short readShort() throws IOException {
        checkNotFinished();
        return in.readShort();
    }

    public int readInt() throws IOException {
        checkNotFinished();
        return in.readInt();
    }

    public long readLong() throws IOException {
        checkNotFinished();
        return in.readLong();
    }

    public float readFloat() throws IOException {
        checkNotFinished();
        return in.readFloat();
    }

    public double readDouble() throws IOException {
        checkNotFinished();
        return in.readDouble();
    }

    public String readString() throws IOException {
        checkNotFinished();
        return in.readString();
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        checkNotFinished();
        return in.readObject();
    }

    public void readArray(boolean[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(byte[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(char[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(short[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(int[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(long[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(float[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(double[] destination) throws IOException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(Object[] destination) throws IOException,
            ClassNotFoundException {
        readArray(destination, 0, destination.length);
    }

    public void readArray(boolean[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(byte[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(char[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(short[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(int[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(long[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(float[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(double[] destination, int offset, int size)
            throws IOException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public void readArray(Object[] destination, int offset, int size)
            throws IOException, ClassNotFoundException {
        checkNotFinished();
        in.readArray(destination, offset, size);
    }

    public long bytesRead() throws IOException {
        long cnt = om.bin.bytesRead();
        long retval = cnt - count;
        count = cnt;
        return retval;
    }

    public int remaining() throws IOException {
        
        if (isFinished) { 
            return 0;
        }
        
        return om.bin.available();
    }

    public int size() throws IOException {
        return om.bin.bufferSize();
    }
    
    public long finish() throws IOException {
        if (!isFinished) {
            long retval = om.finalizeRead(stream);
            om.receivePort.doFinish();
            if (inUpcall) {
                ThreadPool.createNew(om.receivePort, "ReceivePort");
            }
            isFinished = true;
            return retval;
        }
        throw new IOException("ReadMessage already finished");
    }

    public void finish(IOException exception) {
        if (!isFinished) {
            isFinished = true;
            om.finalizeRead(stream);
            om.receivePort.doFinish();
            if (inUpcall) {
                ThreadPool.createNew(om.receivePort, "ReceivePort");
            }
        }
    }

    public ReceivePort localPort() {
        return om.receivePort;
    }

    public long sequenceNumber() {
        // Not supported.
        return 0;
    }

    public void readByteBuffer(ByteBuffer value) throws IOException,
	    ReadOnlyBufferException {
        checkNotFinished();
        in.readByteBuffer(value);	
    }

  

}
