/* $Id: ReceivePortIdentifier.java 12055 2010-05-19 11:19:16Z ceriel $ */

package nl.esciencecenter.aether;

/**
 * Identifies a {@link nl.esciencecenter.aether.ReceivePort ReceivePort}.
 */
public interface ReceivePortIdentifier extends java.io.Serializable {
    /**
     * Returns the name of the {@link nl.esciencecenter.aether.ReceivePort ReceivePort}
     * corresponding to this identifier.
     * @return
     *          the name of the receiveport.
     */
    public String name();

    /**
     * Returns the {@link nl.esciencecenter.aether.AetherIdentifier IbisIdentifier} of the
     * {@link nl.esciencecenter.aether.ReceivePort ReceivePort} corresponding
     * to this identifier.
     * @return
     *          the ibis identifier.
     */
    public AetherIdentifier ibisIdentifier();

    /**
     * The hashCode method is mentioned here just as a reminder that an
     * implementation must probably redefine it, because two objects
     * representing the same <code>ReceivePortIdentifier</code> must
     * result in the same hashcode (and compare equal).
     * To explicitly specify it in the interface does not help, because
     * java.lang.Object already implements it, but, anyway, here it is.
     * 
     * {@inheritDoc}
     */
    public int hashCode();

    /**
     * The equals method is mentioned here just as a reminder that an
     * implementation must probably redefine it, because two objects
     * representing the same <code>ReceivePortIdentifier</code> must
     * compare equal (and result in the same hashcode).
     * To explicitly specify it in the interface does not help, because
     * java.lang.Object already implements it, but, anyway, here it is.
     * 
     * {@inheritDoc}
     */
    public boolean equals(Object other);
}
