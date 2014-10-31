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

import org.mskcc.cbio.importer.mercurial.MercurialServer;

import org.apache.commons.io.*;

import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.charset.*;

public class MercurialServerImpl implements MercurialServer
{
	private static final List<String> startCommand = initStartCommand();
	private static final List<String> initStartCommand()
	{
		String[] command = new String[] {
			"hg", "--config", "ui.interactive=True", "serve", "--cmdserver", "pipe"
		};
		return Arrays.asList(command);
	}
	private static final int BUF_SIZE = 1024;
	private static final byte[] RUN_COMMAND = "runcommand\n".getBytes();

	private Process process;
	private CharsetEncoder encoder;
	private CharsetDecoder decoder;
	private ByteArrayOutputStream baos;

	public MercurialServerImpl()
	{
		this.baos = new ByteArrayOutputStream();
		this.encoder = newEncoder("UTF-8");
		this.decoder = newDecoder("UTF-8");
	}

	private CharsetEncoder newEncoder(String charset)
	{
		Charset encoding = Charset.forName(charset);
        CharsetEncoder encoder = encoding.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        return encoder;
    }

    private CharsetDecoder newDecoder(String charset)
	{
		Charset encoding = Charset.forName(charset);
        CharsetDecoder decoder = encoding.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        return decoder;
    }

	@Override
	public void start(File directory) throws IOException
	{
		if (process != null) stop();
		ProcessBuilder processBuilder = new ProcessBuilder(startCommand);
		processBuilder.directory(directory);
		process = processBuilder.start();
		readHelloMessage(); 
	}

	private void readHelloMessage() throws IOException
	{
		MercurialInputStream mis = new MercurialInputStream(process.getInputStream());
    	IOUtils.readLines(new InputStreamReader(mis, decoder));
	}

	@Override
	public void stop()
	{
		IOUtils.closeQuietly(process.getOutputStream());
		IOUtils.closeQuietly(process.getInputStream());
		try {
			process.waitFor();
		}
		catch (InterruptedException e) {
			process = null;
		}
	}

	@Override
	public List<String> executeCommand(String command) throws IOException
	{
		List<String> cmdLine = Arrays.asList(command.split(" "));
		baos.reset();
		for (int lc = 1; lc < cmdLine.size(); lc++) {
			String s = cmdLine.get(lc);
            encode(s);
			if (lc < cmdLine.size()-1) {
            	baos.write('\0');
			}
        }

      	OutputStream os = process.getOutputStream();
       	os.write(RUN_COMMAND);
       	writeBigEndian(baos.size(), os);
       	baos.writeTo(os);
       	os.flush();
       	return readStream();
	}

    private void encode(String s) throws IOException
    {
        ByteBuffer byteBuffer = encoder.encode(CharBuffer.wrap(s));
        baos.write(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.limit());
    }

    private static void writeBigEndian(int n, OutputStream os) throws IOException
    {
        byte[] bytes = new byte[] { (byte) (n >>> 24), (byte) (n >>> 16), (byte) (n >>> 8), (byte) n };
        os.write(bytes);
    }

    private List<String> readStream() throws IOException
    {
    	int offset = 0;
    	int bytesRead = 0;
    	byte[] buffer = new byte[BUF_SIZE];
    	MercurialInputStream mis = null;
    	while (true) {
    		mis = new MercurialInputStream(process.getInputStream());
    		char channel = mis.getChannel();
    		if (channel == 'o') {
    			while (bytesRead < mis.getLength()) {
    				if (offset + mis.getLength() > buffer.length) {
    					buffer = resizeBuffer(buffer);
    				}
    				bytesRead += mis.read(buffer, offset, mis.getRemaining());
    				offset += bytesRead;
    			}
    			bytesRead = 0;
    			continue;
    		}
    		break;
    	}

   		return IOUtils.readLines(new InputStreamReader(new ByteArrayInputStream(buffer, 0, offset-1), decoder));
    }

    private byte[] resizeBuffer(byte[] buffer)
    {
    	byte[] toReturn = new byte[buffer.length + BUF_SIZE];
    	System.arraycopy(buffer, 0, toReturn, 0, buffer.length);
    	return toReturn;
    }
}
