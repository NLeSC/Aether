/* $Id: ReceivePortIdentifier.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implementation of the <code>ReceivePortIdentifier</code> interface.
 * This class can be extended by Ibis implementations.
 */
public class ReceivePortIdentifier implements nl.esciencecenter.aether.ReceivePortIdentifier {

    /** 
     * Generated
     */
    private static final long serialVersionUID = -6757071659785922784L;

    /** The name of the corresponding receiveport. */
    public final String name;

    /** The IbisIdentifier of the Ibis instance that created the receiveport. */
    public final AetherIdentifier ibis;

    /**
     * Constructor, initializing the fields with the specified parameters.
     * @param name the name of the receiveport.
     * @param ibis the Ibis instance that created the receiveport.
     */
    public ReceivePortIdentifier(String name, AetherIdentifier ibis) {
        if (name == null) {
            throw new NullPointerException("name is null in ReceivePortIdentifier");
        }
        if (ibis == null) {
            throw new NullPointerException("Ibis identifier is null in ReceivePortIdentifier");
        }
        this.name = name;
        this.ibis = ibis;
    }

    /**
     * Constructs a <code>ReceivePortIdentifier</code> from the specified coded
     * form.
     * @param codedForm the coded form.
     * @exception IOException is thrown in case of trouble.
     */
    public ReceivePortIdentifier(byte[] codedForm) throws IOException {
        this(codedForm, 0, codedForm.length);
    }

    /**
     * Constructs a <code>ReceivePortIdentifier</code> from the specified coded
     * form, at a particular offset and size.
     * @param codedForm the coded form.
     * @param offset offset in the coded form.
     * @param length length of the coded form.
     * @exception IOException is thrown in case of trouble.
     */
    public ReceivePortIdentifier(byte[] codedForm, int offset, int length)
            throws IOException {
        this(new DataInputStream(
                new ByteArrayInputStream(codedForm, offset, length)));
    }

    /**
     * Constructs a <code>ReceivePortIdentifier</code> by reading it from the
     * specified input stream.
     * @param dis the input stream.
     * @exception IOException is thrown in case of trouble.
     */
    public ReceivePortIdentifier(DataInput dis) throws IOException {
        name = dis.readUTF();
        ibis = new AetherIdentifier(dis);
    }

    /**
     * Returns the coded form of this <code>ReceivePortIdentifier</code>.
     * @return the coded form.
     */
    public byte[] toBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeUTF(name);
            ibis.writeTo(dos);
            dos.flush();
            return bos.toByteArray();
        } catch(Exception e) {
            // should not happen.
            return null;
        }
    }

    private boolean equals(ReceivePortIdentifier other) {
        if (other == this) {
            return true;
        }
        return name.equals(other.name) && ibis.equals(other.ibis);
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof ReceivePortIdentifier) {
            return equals((ReceivePortIdentifier) other);
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode() ^ ibis.hashCode();
    }

    public String name() {
        return name;
    }

    public nl.esciencecenter.aether.AetherIdentifier ibisIdentifier() {
        return ibis;
    }

    public String toString() {
        return ("(ReceivePortIdentifier: name = " + name
                + ", ibis = " + ibis + ")");
    }
}
