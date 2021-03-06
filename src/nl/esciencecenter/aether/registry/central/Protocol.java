package nl.esciencecenter.aether.registry.central;

public final class Protocol {

    public static final byte MAGIC_BYTE = 54;
    
    public static final int VIRTUAL_PORT = 302;
    
    // opcodes

    public static final byte OPCODE_JOIN = 0;

    public static final byte OPCODE_LEAVE = 1;

    public static final byte OPCODE_GOSSIP = 2;

    public static final byte OPCODE_ELECT = 3;

    public static final byte OPCODE_SEQUENCE_NR = 4;

    public static final byte OPCODE_DEAD = 5;

    public static final byte OPCODE_MAYBE_DEAD = 6;

    public static final byte OPCODE_SIGNAL = 7;

    public static final byte OPCODE_PING = 8;

    public static final byte OPCODE_PUSH = 9;

    public static final byte OPCODE_BROADCAST = 10;

    public static final byte OPCODE_FORWARD = 11;

    public static final byte OPCODE_GET_STATE = 12;

    public static final byte OPCODE_HEARTBEAT = 13;

    public static final byte OPCODE_TERMINATE = 14;

    public static final int NR_OF_OPCODES = 15;

    public static final String[] OPCODE_NAMES = { "JOIN", "LEAVE", "GOSSIP",
            "ELECT", "SEQUENCE_NR", "DEAD", "MAYBE_DEAD", "SIGNAL", "PING",
            "PUSH", "BROADCAST", "FORWARD", "GET_STATE", "HEARTBEAT",
            "TERMINATE"};
}
