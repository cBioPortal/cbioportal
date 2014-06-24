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
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Method;

/**
 * Class which provides commandline execution.
 */
public class Shell {

	// our logger
	private static Log LOG = LogFactory.getLog(Shell.class);

	/**
	 * Function which executes either a fetcher or importer based on caller.  Class args are
	 * required if we are instantiating an instance of a class that implements Fetcher or Importer interface.
	 *
	 * @param referenceMetadata ReferenceMetadata
	 * @param caller Object
	 * @param classArgs Object[] 
	 * @param workingDirectory String
	 * @return boolean
	 */
	public static boolean exec(ReferenceMetadata referenceMetadata, Object caller,
							   Object[] classArgs, String workingDirectory) {

		String command = null;
		List<String> commandArgs = null;
		if (caller instanceof Fetcher) {
			command = referenceMetadata.getFetcherName();
			commandArgs = referenceMetadata.getFetcherArgs();
		}
		else if (caller instanceof Importer) {
			command = referenceMetadata.getImporterName();
			commandArgs = referenceMetadata.getImporterArgs();
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("exec() unrecognized caller type:" + caller.getClass().getName() + ", exiting...");
			}
			return false;
		}

		// sanity check
		if (command == null || commandArgs == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("exec(), invalid command or command args, exiting...");
			}
			return false;
		}
		else if (LOG.isInfoEnabled()) {
			LOG.info("exec(), command: " + command);
			LOG.info("exec(), commandArgs: " + commandArgs);
		}

		boolean toReturn = false;
		try {
			if (command.contains("org.mskcc.cbio")) {
				// we are either going to use a cgds package importer which has a main method
				// or one of our own classes which implements the Importer interface.
				// Check for a main method, if found, use it, otherwise assume we have a class
				// that implements the Importer interface.
				Method mainMethod = ClassLoader.getMethod(command, "main");
				if (mainMethod != null) {
					mainMethod.invoke(null, (Object)commandArgs.toArray(new String[0]));
				}
				else {
					Importer importer = (Importer)ClassLoader.getInstance(command, classArgs);
					importer.importReferenceData(referenceMetadata);
				}
				toReturn = true;
			}
			// some type of script
			else {
				List<String> commandList = new ArrayList<String>();
				commandList.add(command);
				commandList.addAll(commandArgs);
				toReturn = Shell.exec(commandList, workingDirectory);
			}
		}
		catch(Exception e) {
			if (LOG.isInfoEnabled()) {
				LOG.info(e.toString() + " thrown during shell command: " + e.getMessage() + ", aborting...");
			}
		}

		// should not get here
		return toReturn;
	}

	/**
	 * Executes the given command via java ProcessBuilder.
	 *
	 * @param command List<String>
	 * @param workingDirectory String
	 * @return boolean
	 */
    public static boolean exec(List<String> command, String workingDirectory) {

		if (LOG.isInfoEnabled()) {
			LOG.info("exec():, command: " + command);
			LOG.info("exec():, workingDirectory: " + workingDirectory);
		}
       
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.directory(new File(workingDirectory));
			Process process = processBuilder.start();
			StreamSink stdoutSink = new StreamSink(process.getInputStream());
			StreamSink stderrSink = new StreamSink(process.getErrorStream());
			stdoutSink.start();
			stderrSink.start();
			process.waitFor();
			return process.exitValue() == 0;
        }
		catch (Exception e) {
			if (LOG.isInfoEnabled()) {
				LOG.info(e.toString() + " thrown during shell command: " + command);
			}
			return false;
        }
    }
}
