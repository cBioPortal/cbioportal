/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.importer.mercurial.internal;

import java.io.*;

public class MercurialInputStream extends InputStream
{
    private int length;
    private final char channel;
    private int bytesRemaining;
    private final InputStream is;

    MercurialInputStream(InputStream is) throws IOException
    {
        this.is = is;
        channel = (char)is.read();
        length = bytesRemaining = readInt();
        if (bytesRemaining == -1) {
            throw new IOException("bytesRemaining == -1");
        }
        if (Character.isUpperCase(channel)) {
            bytesRemaining = 0;
        }
    }

    @Override
    public int read() throws IOException
    {
        if (bytesRemaining == 0) {
            return -1;
        }
        else {
            bytesRemaining--;
            return is.read();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        if (bytesRemaining == 0) {
            return -1;
        }
        if (len > bytesRemaining) {
            len = bytesRemaining;
        }
        int result = is.read(b, off, len);
        if (result == -1) {
            throw new IOException("Unexpected EOF");
        }
        bytesRemaining -= result;
        return result;
    }

    @Override
    public int available() throws IOException
    {
        int availableInStream = is.available();
        if (bytesRemaining == 0) {
            return 0;
        }
        return availableInStream > bytesRemaining ? bytesRemaining : availableInStream;
    }

    public char getChannel()
    {
        return channel;
    }

    public int getLength()
    {
        return length;
    }

    public int getRemaining()
    {
        return bytesRemaining;
    }

    private int readInt() throws IOException
    {
        return readBigEndian(is);
    }

    private static int readBigEndian(InputStream is) throws IOException
    {
        byte b0 = (byte) is.read();
        byte b1 = (byte) is.read();
        byte b2 = (byte) is.read();
        int b3 = is.read();
        if (b3 == -1) {
            return -1;
        }
        return (b0 << 24) + ((b1 & 0xFF) << 16) + ((b2 & 0xFF) << 8) + ((byte) b3 & 0xFF);
    }
}
