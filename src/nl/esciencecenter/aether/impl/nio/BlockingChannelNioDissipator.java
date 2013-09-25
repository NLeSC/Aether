/* $Id: BlockingChannelNioDissipator.java 11529 2009-11-18 15:53:11Z ceriel $ */

package nl.esciencecenter.aether.impl.nio;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;

/**
 * Dissipator which reads from a single channel, with the channel normally in
 * blocking mode.
 */
final class BlockingChannelNioDissipator extends NioDissipator {

    BlockingChannelNioDissipator(ReadableByteChannel channel)
            throws IOException {
        super(channel);

        if (!(channel instanceof SelectableChannel)) {
            throw new IOException("wrong type of channel given on creation of"
                    + " BlockingChannelNioDissipator");
        }
    }

    /**
     * fills the buffer upto at least "minimum" bytes.
     * 
     */
    protected void fillBuffer(int minimum) throws IOException {
        while (unUsedLength() < minimum) {
            readFromChannel();
        }
    }
}
