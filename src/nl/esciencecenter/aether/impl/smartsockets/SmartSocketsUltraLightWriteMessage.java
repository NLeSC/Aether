package nl.esciencecenter.aether.impl.smartsockets;

// import ibis.io.SingleBufferArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import nl.esciencecenter.aether.PortType;
import nl.esciencecenter.aether.SendPort;
import nl.esciencecenter.aether.WriteMessage;
import nl.esciencecenter.aether.io.BufferedArrayOutputStream;
import nl.esciencecenter.aether.io.DataOutputStream;
import nl.esciencecenter.aether.io.SerializationFactory;
import nl.esciencecenter.aether.io.SerializationOutput;

public class SmartSocketsUltraLightWriteMessage implements WriteMessage {

    private final SmartSocketsUltraLightSendPort port;
    private final SerializationOutput out;
    private final DataOutputStream bout;
    private final ByteArrayOutputStream b;

    SmartSocketsUltraLightWriteMessage(SmartSocketsUltraLightSendPort port) throws IOException { 
        this.port = port;

        PortType type = port.getPortType();

        String serialization = null;

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

        b = new ByteArrayOutputStream();
        bout = new BufferedArrayOutputStream(b);
        // bout = new SingleBufferArrayOutputStream(buffer);
        out = SerializationFactory.createSerializationOutput(serialization, bout, port.properties);		
    }

    public long bytesWritten() throws IOException {
        return bout.bytesWritten();
    }


    public int capacity() throws IOException {
        // return bout.bufferSize();
        return -1;
    }

    public int remaining() throws IOException {
        // return (int) (bout.bufferSize() - bout.bytesWritten());
        return -1;
    }


    public long finish() throws IOException {
        out.flush();
        out.close();

        long bytes = bout.bytesWritten();

        // System.err.println("Written == " + bytes);

        port.finishedMessage(b.toByteArray());
        return bytes;
    }

    public void finish(IOException exception) {
        try { 
            port.finishedMessage(exception);
        } catch (Exception e) {
            // ignore ? 
        }
    }

    public void flush() throws IOException {
        // empty
    }

    public SendPort localPort() {
        return port;
    }

    public void reset() throws IOException {
        // bout.reset();
        out.reset(true);
    }

    public int send() throws IOException {
        // empty -- excpetion ? 
        return 0;
    }

    public void sync(int ticket) throws IOException {
        // empty
    }

    public void writeArray(boolean[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(byte[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(char[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(short[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(int[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(long[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(float[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(double[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(Object[] value) throws IOException {
        out.writeArray(value);
    }

    public void writeArray(boolean[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(byte[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(char[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(short[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(int[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(long[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(float[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(double[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeArray(Object[] value, int offset, int length) throws IOException {
        out.writeArray(value, offset, length);
    }

    public void writeBoolean(boolean value) throws IOException {
        out.writeBoolean(value);
    }

    public void writeByte(byte value) throws IOException {
        out.writeByte(value);
    }

    public void writeChar(char value) throws IOException {
        out.writeChar(value);		
    }

    public void writeDouble(double value) throws IOException {
        out.writeDouble(value);		
    }

    public void writeFloat(float value) throws IOException {
        out.writeFloat(value);		
    }

    public void writeInt(int value) throws IOException {
        out.writeInt(value);		
    }

    public void writeLong(long value) throws IOException {
        out.writeLong(value);		
    }

    public void writeObject(Object value) throws IOException {
        out.writeObject(value);		
    }

    public void writeShort(short value) throws IOException {
        out.writeShort(value);				
    }

    public void writeString(String value) throws IOException {
        out.writeString(value);
    }

    @Override
    public void writeByteBuffer(ByteBuffer value) throws IOException {
	out.writeByteBuffer(value);	
    }


}
