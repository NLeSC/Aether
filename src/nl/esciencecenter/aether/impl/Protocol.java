/* $Id: SmartSocketsProtocol.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl;

public interface Protocol {
    
    static final byte NEW_RECEIVER = 1;

    static final byte NEW_MESSAGE = 2;

    static final byte CLOSE_ALL_CONNECTIONS = 3;

    static final byte CLOSE_ONE_CONNECTION = 4;

    static final byte QUIT_IBIS = 14;

    static final byte REPLY = 127;
}
