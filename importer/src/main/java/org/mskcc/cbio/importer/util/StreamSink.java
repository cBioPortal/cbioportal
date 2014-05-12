/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

// package
package org.mskcc.cbio.importer.util;

// imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class which provides commandline execution.
 */
public class StreamSink extends Thread {

	// our logger
	private static Log LOG = LogFactory.getLog(StreamSink.class);

	// private members
	private InputStream in;
    private OutputStream out = null;
    private int buffersize = 60 * 1024;
    private byte[] bytebuffer = null;

	/**
	 * Constructor.
	 *
	 * @param inStream InputStream
	 */
	public StreamSink(InputStream inStream) {

		// init members
        in = inStream;
    }

	/**
	 * Reads given stream.
	 */
    public void transfer() throws IOException {
       
		bytebuffer = new byte[buffersize];
        if (out == null) {
            while (true) {
                int bytes_read = in.read(bytebuffer,0,buffersize);
                if (bytes_read == -1) return;
            }
        }
        while (true) {
            int bytes_read = in.read(bytebuffer,0,buffersize);
            if (bytes_read == -1) return;
            out.write(bytebuffer,0,bytes_read);
        }
    }

	/**
	 * Our implementation of run.
	 */
    public void run() {

        try {
            transfer();
        }
		catch (IOException e) {
			if (LOG.isInfoEnabled()) {
				LOG.info(e.toString() + ", error during stream copy in class StreamSink");
			}
        }
    }
}
