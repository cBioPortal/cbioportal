/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/
package org.mskcc.cbio.portal.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.File;
import java.net.URLEncoder;

/**
 * Provides methods for linking to IGV.
 *
 * @author Benjamin Gross.
 */
public class IGVLinking {

	private static final Log LOG = LogFactory.getLog(IGVLinking.class);
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("MM/dd/yyyy"); //  MM/dd/yyyy HH:mm:ss

	public static String getIGVActionForSEGViewing(String cancerTypeId, String encodedGeneList)
	{
		// routine defined in igv_webstart.js
		String segFileURL = GlobalProperties.getSegfileUrl() + cancerTypeId + "_scna_hg18.seg";
		return "prepIGVLaunch('" + segFileURL + "','" + encodedGeneList + "')";
	}

	// returns null if exception has been thrown during processing
	public static String getIGVActionForBAMViewing(String cancerStudyStableId, String caseId,
												   String chromosome, long startPos, long endPos)
	{
		if (!IGVLinking.validBAMActionArgs(cancerStudyStableId, caseId) ||
			!IGVLinking.encryptionBinLocated()) {
			return null;
		}

		String bamFileURL = getBAMFileURL(caseId);
		if (bamFileURL == null) return null;
		
		String action = null;
		try {
			action = getAction(bamFileURL, chromosome, startPos, endPos);
		}
		catch (Exception e) {
			if (LOG.isInfoEnabled()) LOG.info("getIGVActionForBAMViewing(), error processing request...");
		}

		return action;
	}

	private static boolean validBAMActionArgs(String cancerStudy, String caseId)
	{
		return (caseId != null && GlobalProperties.getIGVBAMLinkingStudies().contains(cancerStudy));
	}

	private static boolean encryptionBinLocated()
	{
		return new File(GlobalProperties.getProperty(GlobalProperties.OPENSSL_BINARY)).exists();
	}

	private static String getBAMFileURL(String caseId)
	{
		String token = IGVLinking.getToken(caseId);
		return (token != null) ?
			(GlobalProperties.getProperty(GlobalProperties.BROAD_BAM_URL) + token) : null;
			
	}

	private static String getToken(String caseId)
	{
		File messageToEncrypt = null;
		File token = null;
		String urlEncodedToken = null;

		try {
			messageToEncrypt = getMessageToEncrypt(caseId, IGVLinking.getCurrentTime());
			token = IGVLinking.encrypt(messageToEncrypt);
			urlEncodedToken = IGVLinking.getURLEncodedToken(token);
		}
		catch (Exception e) {
			urlEncodedToken = null;
		}
		finally {
			FileUtils.deleteQuietly(messageToEncrypt);
			FileUtils.deleteQuietly(token);
		}

		return urlEncodedToken;
	}

	private static String getCurrentTime()
	{
		return TIMESTAMP_FORMAT.format(Calendar.getInstance().getTime());
	}

	private static File getMessageToEncrypt(String caseId, String timestamp) throws Exception
	{
		File token = FileUtils.getFile(FileUtils.getTempDirectoryPath(), "broad-bam-token.txt");
		FileUtils.writeStringToFile(token, "<" + caseId + "><"  + timestamp + ">", "UTF-8", false);
		return token;
	}

	private static String getURLEncodedToken(File token) throws Exception
	{
		return URLEncoder.encode(IGVLinking.getFileContents(token), "US-ASCII");
	}

	private static String getFileContents(File file) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		LineIterator it = null;

		try {
			it = FileUtils.lineIterator(file, "UTF-8");
			while (it.hasNext()) {
				sb.append(it.nextLine());
			}
		}
		finally {
			if (it != null) it.close();
		}

		return sb.toString();
	}

	private static String getAction(String bamFileURL, String chromosome, long startPos, long endPos) throws Exception
	{
		// an IGV locus string, e.g. chr1:000-200
		String locus = chromosome + ":" + String.valueOf(startPos) + "-" + String.valueOf(endPos);
		return "prepIGVLaunch('" + bamFileURL + "','" + URLEncoder.encode(locus, "US-ASCII") + "')";
	}

	private static File encrypt(File messageToEncrypt) throws Exception
	{

		File encryptedMessage = null;
		File signedMessage = null;
		File base64Message = null;

		try {
			encryptedMessage = IGVLinking.getEncryptedMessage(messageToEncrypt);
			signedMessage = IGVLinking.getSignedMessage(encryptedMessage);
			base64Message = IGVLinking.getBase64Message(signedMessage);
		}
		catch (Exception e) {
			FileUtils.deleteQuietly(base64Message);
			throw e;
		}
		finally {
			FileUtils.deleteQuietly(encryptedMessage);
			FileUtils.deleteQuietly(signedMessage);
		}

		return base64Message;
	}

	private static File getEncryptedMessage(File messageToEncrypt) throws Exception
	{
		File encryptedMessage = null;

		try {
			encryptedMessage = FileUtils.getFile(FileUtils.getTempDirectoryPath(), "broad-bam-encrypted.txt");
			IGVLinking.execute(IGVLinking.getEncryptCommand(messageToEncrypt, encryptedMessage));
		}
		catch (Exception e) {
			FileUtils.deleteQuietly(encryptedMessage);
			throw e;
		}

		return encryptedMessage;
	}

	private static String getEncryptCommand(File messageToEncrypt, File encryptedMessage) throws Exception
	{
		return (GlobalProperties.getProperty(GlobalProperties.OPENSSL_BINARY) +
				" rsautl -encrypt" +
				" -inkey " + GlobalProperties.getProperty(GlobalProperties.ENCRYPTION_KEY) +
				" -keyform PEM -pubin" +
				" -in " + messageToEncrypt.getCanonicalPath() + 
				" -out " + encryptedMessage.getCanonicalPath());
	}

	private static File getSignedMessage(File encryptedMessage) throws Exception
	{
		File signedMessage = null;

		try {
			signedMessage = FileUtils.getFile(FileUtils.getTempDirectoryPath(), "broad-bam-signed-encrypted.txt");
			IGVLinking.execute(IGVLinking.getSignCommand(encryptedMessage, signedMessage));
		}
		catch (Exception e) {
			FileUtils.deleteQuietly(signedMessage);
			throw e;
		}

		return signedMessage;
	}

	private static String getSignCommand(File encryptedMessage, File signedMessage) throws Exception
	{
		return (GlobalProperties.getProperty(GlobalProperties.OPENSSL_BINARY) +
				" rsautl -sign" +
				" -inkey " + GlobalProperties.getProperty(GlobalProperties.SIGNATURE_KEY) +
				" -keyform PEM" +
				" -in " + encryptedMessage.getCanonicalPath() + 
				" -out " + signedMessage.getCanonicalPath());
	}

	private static File getBase64Message(File signedMessage) throws Exception
	{
		File base64Message = null;

		try {
			base64Message = FileUtils.getFile(FileUtils.getTempDirectoryPath(), "broad-bam-base64-signed-encrypted.txt");
			IGVLinking.execute(IGVLinking.getBase64Command(signedMessage, base64Message));
		}
		catch (Exception e) {
			FileUtils.deleteQuietly(base64Message);
			throw e;
		}

		return base64Message;
	}

	private static String getBase64Command(File signedMessage, File base64Message) throws Exception
	{
		return (GlobalProperties.getProperty(GlobalProperties.OPENSSL_BINARY) +
				" enc -base64" +
				" -in " + signedMessage.getCanonicalPath() + 
				" -out " + base64Message.getCanonicalPath());
	}

	private static void execute(String command) throws Exception
	{
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		if (process.exitValue() != 0) throw new RuntimeException();
	}
}
