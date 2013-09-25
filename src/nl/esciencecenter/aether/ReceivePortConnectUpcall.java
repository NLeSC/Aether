/* $Id: ReceivePortConnectUpcall.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether;

/**
 * Connection upcall interface for receiveports. An Ibis implementation may
 * choose to block while processing these upcalls.
 */
public interface ReceivePortConnectUpcall {
    /**
     * Upcall that indicates that a new connection is being initiated by
     * a {@link SendPort}.
     * If a {@link ReceivePort} has been configured with connection upcalls,
     * this upcall is generated for each attempt to set up a connection
     * with this {@link ReceivePort}.
     * This upcall should return <code>true</code> to accept the connection
     * and <code>false</code> to refuse the connection.
     * If the connection is refused, the connect call at the
     * {@link SendPort} throws a {@link ConnectionRefusedException}.
     * <p>
     * This upcall may run completely asynchronously, but only at most one is
     * alive at any time.
     *
     * @param receiver
     *          the {@link ReceivePort} receiving a connection attempt.
     * @param applicant
     *          identifier for the {@link SendPort} attempting to set up a
     *          connection.
     * @return
     *          <code>true</code> to accept the connection and
     *          <code>false</code> to refuse the connection.
     */
    public boolean gotConnection(ReceivePort receiver,
            SendPortIdentifier applicant);

    /**
     * Upcall that indicates that a connection to a sendport was lost.
     * If a {@link ReceivePort} has been configured with connection upcalls,
     * an upcall is generated for each connection that is lost.
     * This may be because the sender just closed the connection, in which
     * case the specified cause is <code>null</code>,
     * or it may be because there is some problem with the connection itself.
     * <p>
     * This upcall may run completely asynchronously,
     * but only at most one is alive at any time.
     *
     * @param receiver
     *          the {@link ReceivePort} losing a connection.
     * @param origin
     *          identifier for the {@link SendPort} to which the connection is
     *          lost.
     * @param cause
     *          the reason for this upcall.
     */
    public void lostConnection(ReceivePort receiver, SendPortIdentifier origin,
            Throwable cause);
}
