/**
 * This package describes the Ibis Portability Layer (IPL), which is to be implemented by Ibis implementations.
 * Ibis is a programming environment that combines Java's "run everywhere" portability both with flexible
 * treatment of dynamically available networks and processor pools, and with highly efficient, object-based communication.
 * <p>
 * Communication is based on {@link nl.esciencecenter.aether.ReceivePort ReceivePorts} and {@link nl.esciencecenter.aether.SendPort SendPorts}
 * of a certain port type, which is represented by a {@link nl.esciencecenter.aether.PortType PortType}. Port capabilities
 * are given to the port type (for example ports are "ONE_TO_ONE" and "RELIABLE" and support "OBJECT_SERIALIZATION").
 * {@link nl.esciencecenter.aether.ReceivePort ReceivePorts} and {@link nl.esciencecenter.aether.SendPort SendPorts} both have a port type.
 * Only receive ports and send ports of the same port type can communicate. Any number of receive ports and
 * send ports can be created on a JVM (even of the same port type).
 * <p>
 * First, an {@link nl.esciencecenter.aether.Aether Ibis} instance must be created. This is achieved by
 * calling one of the <code>createIbis</code> methods of the {@link nl.esciencecenter.aether.AetherFactory IbisFactory},
 * specifying the {@link nl.esciencecenter.aether.Capabilities capabilities} and port types required.
 * <p>
 * SendPorts and ReceivePorts are created by an Ibis instance using the
 * {@link nl.esciencecenter.aether.Aether#createSendPort(PortType, String) createSendPort} and
 * {@link nl.esciencecenter.aether.Aether#createReceivePort(PortType, String) createReceivePort} methods. When creating a
 * ReceivePort, it can be supplied with an {@link nl.esciencecenter.aether.MessageUpcall MessageUpcall} object. If so, upcalls
 * are generated when messages arrive. If not, explicit receive must be used to read messages.
 * <p>
 * The system provides a globally unique {@link nl.esciencecenter.aether.ReceivePortIdentifier ReceivePortIdentifier} and
 * {@link nl.esciencecenter.aether.SendPortIdentifier SendPortIdentifier} for every ReceivePort and SendPort. These identifiers
 * are implementation specific and serializable (and can be sent over the network/saved in a file etc.).
 * <p>
 * A SendPort takes the initiative to connect to or disconnect from ReceivePorts (otherwise the one-way traffic
 * scheme is violated). A SendPort can connect to one or more ReceivePorts using their ReceivePortIdentifiers
 * or their Ibis identifiers and names. Additional ReceivePorts may be connected to at any time, provided
 * that the port type allows it. A SendPort can disconnect from one or more ReceivePorts using their
 * ReceivePortIdentifiers. Additional ReceivePorts may be disconnected at any time.
 * When a SendPort is no longer used it must be closed using the
 * {@link nl.esciencecenter.aether.SendPort#close() close} method. When this method is called, all connections the SendPort has
 * are disconnected. When a ReceivePort is no longer used it must be closed using the
 * {@link nl.esciencecenter.aether.ReceivePort#close() close} method.
 * This call will block until connections to SendPorts are disconnected (by the SendPorts).
 * <p>
 * A {@link nl.esciencecenter.aether.WriteMessage message} can be sent from a SendPort to the set of ReceivePorts it is connected to.
 * To do this, a {@link nl.esciencecenter.aether.WriteMessage write message} is obtained from the SendPort (this allows streaming,
 * as the destination is known). Data can be added to the message using "write" methods (this data may be immediately
 * streamed to the ReceivePorts) of this message. The Ibis system may or may not asynchronously start sending the message.
 * The message can be finished using the {@link nl.esciencecenter.aether.WriteMessage#finish() finish} method. When the finish returns,
 * all data has been copied (and now may be changed), and the message may no longer be used.
 * <p>
 * When a {@link nl.esciencecenter.aether.ReadMessage message} arrives at a ReceivePort, how it is handled depends on the way the
 * ReceivePort was instantiated. If messages are to be handled by means of upcalls, a "new" thread is started and the
 * upcall is invoked with this message as parameter. When the message is no longer used it MAY be returned to the system
 * using the {@link nl.esciencecenter.aether.ReadMessage#finish() finish} method (after which the message may no longer be used).
 * If messages are to be handled by means of explicit receive calls, a message is delivered when the
 * {@link nl.esciencecenter.aether.ReceivePort#receive() receive} method is called. When the message is no longer used it MUST be
 * returned to the system using the {@link nl.esciencecenter.aether.ReadMessage#finish() finish} method (after which the message may
 * no longer be used). This allows the underlying implementation to deliver a next message.
 * 
 * All Ibis implementations must be thread-safe.
 */
package nl.esciencecenter.aether;

