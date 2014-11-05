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

import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.mercurial.*;

import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class MercurialServiceImpl implements MercurialService
{

	private static Pattern hgIdChangesetPattern = Pattern.compile("^(\\w+)$");
	private static Pattern hgStatusPattern = Pattern.compile("^\\w{1}? (\\w+)$");
	private static Log LOG = LogFactory.getLog(MercurialServiceImpl.class);

	private MercurialServer mercurialServer;

	public MercurialServiceImpl(MercurialServer mercurialServer)
	{
		this.mercurialServer = mercurialServer;
	}

	@Override
	public boolean updatesAvailable(String repositoryName)
	{
		boolean updatesAvailable = false;
		File repository = getRepository(repositoryName);

		try {
			prepareRepository(repository);
			String remoteChangeset = getValue(executeCommand(repository, "hg id --id default"),
			                                  hgIdChangesetPattern);
			if (remoteChangeset.isEmpty()) return false;

			String localChangeset = getValue(executeCommand(repository, "hg id --id"),
			                                 hgIdChangesetPattern);
			if (localChangeset.isEmpty()) return false;

			updatesAvailable = (!remoteChangeset.equals(localChangeset));
		}
		catch(IOException e) {
			logMessage("updatesAvailable(), exception: ");
			logMessage(e.getMessage());
		}

		return updatesAvailable;
	}

	@Override
	public List<String> pullUpdate(String repositoryName)
	{
		List<String> toReturn = null;
		File repository = getRepository(repositoryName);

		try {
			prepareRepository(repository);
			executeCommand(repository, "hg pull");
			// does diff between what has been pulled and what is in working set.
			toReturn = getUpdatedStudies(executeCommand(repository, "hg status --rev .:tip"),
			                             hgStatusPattern);
			executeCommand(repository, "hg update");
			return toReturn;
		}
		catch(IOException e) {
			logMessage("pullUpdate(), exception: ");
			logMessage(e.getMessage());
		}

		return Collections.EMPTY_LIST;
	}

	private File getRepository(String repositoryName)
	{
		File repository = new File(repositoryName);
		if (!repository.exists()) {
			logMessage("getRepository(), repository not found: " + repositoryName);
			throw new IllegalArgumentException("repository not found: " + repositoryName);
		}
		return repository;
	}

	private void prepareRepository(File repository) throws IOException
	{
		executeCommand(repository, "hg revert --all");
		executeCommand(repository, "hg update -C default");
	}

	private List<String> executeCommand(File repository, String command) throws IOException
	{
		logMessage("executeCommand(): " + repository.getCanonicalPath() + ", " + command);
		mercurialServer.start(repository);
		List<String> output = mercurialServer.executeCommand(command);
		mercurialServer.stop();
		logMessage("Output:");
		for (String outputLine : output) {
			logMessage(outputLine);
		}
		return output;
	}

	private String getValue(List<String> serverOutput, Pattern pattern)
	{
		for (String lineOfOutput : serverOutput) {
			Matcher matcher = pattern.matcher(lineOfOutput);
			if (matcher.find()) {
				return matcher.group(1);	
			}
		}
		return "";
	}

	private List<String> getUpdatedStudies(List<String> serverOutput, Pattern pattern)
	{
		Set<String> toReturn = new HashSet<String>();
		for (String lineOfOutput : serverOutput) {
			if (lineOfOutput.startsWith("?")) continue; // ignore untracked files
			Matcher matcher = pattern.matcher(lineOfOutput);
			if (matcher.find()) {
				String cancerStudy = matcher.group(1);
				int caseListSubdirIndex = cancerStudy.indexOf(FileUtils.CASE_LIST_DIRECTORY_NAME);
				if (caseListSubdirIndex != -1) {
					toReturn.add(cancerStudy.substring(0, caseListSubdirIndex-1));
				}
				else {
					toReturn.add(cancerStudy.substring(0, cancerStudy.lastIndexOf("/")-1));
				}
			}
		}
		return new ArrayList<String>(toReturn);
	}

	protected void logMessage(String message)
	{
        if (LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }
}