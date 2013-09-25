/* $Id: Protocol.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.nio;

interface Protocol {
    static final byte NEW_RECEIVER = 1;

    static final byte NEW_MESSAGE = 2;

    static final byte CLOSE_ALL_CONNECTIONS = 3;

    static final byte CLOSE_ONE_CONNECTION = 4;

    static final byte CONNECTION_REQUEST = 5;
}
