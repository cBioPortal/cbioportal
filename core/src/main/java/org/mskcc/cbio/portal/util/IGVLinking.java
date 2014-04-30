/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.ConnectionManager;

import org.apache.commons.io.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.File;
import java.net.*;

/**
 * Provides methods for linking to IGV.
 *
 * @author Benjamin Gross.
 */
public class IGVLinking {

	private static final String TOKEN_REGEX = "<TOKEN>";
    private static final String SAMPLE_REGEX = "<SAMPLE_ID>";
    private static final String KNOWN_ID = "KNOWN_ID";
    private static final String TUMOR_BAM_SIGNATURE = "-Tumor";
    private static final String NORMAL_BAM_SIGNATURE = "-Normal";

	public static String[] getIGVArgsForSegViewing(String cancerStudyStableId, String encodedGeneList) throws Exception
	{
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        CopyNumberSegmentFile cnsf = DaoCopyNumberSegmentFile.getCopyNumberSegmentFile(cancerStudy.getInternalId());

		return new String[] { GlobalProperties.getSegfileUrl() + cnsf.filename, encodedGeneList, cnsf.referenceGenomeId.toString(), cnsf.filename };
	}

	// returns null if exception has been thrown during processing
	public static String[] getIGVArgsForBAMViewing(String cancerStudyStableId, String caseId, String locus)
	{
		if (!IGVLinking.validBAMViewingArgs(cancerStudyStableId, caseId, locus) ||
			!IGVLinking.encryptionBinLocated()) {
			return null;
		}
        
        String trackName = caseId;

		String tumorBAMFileURL = getBAMFileURL(caseId);
		if (tumorBAMFileURL == null) return null;

        // code added to view normal alongside tumor file
        String normalCaseId = getNormalSampleId(caseId);
        if (normalCaseId != null) {
            String normalBAMFileURL = getBAMFileURL(normalCaseId);
            if (normalBAMFileURL != null) {
                tumorBAMFileURL  += "," + normalBAMFileURL;
                trackName += "," + normalCaseId;
            }
        }
        
		String encodedLocus = getEncoded(locus);
		if (encodedLocus == null) return null;

		return new String[] { tumorBAMFileURL, encodedLocus, CopyNumberSegmentFile.ReferenceGenomeId.hg19.toString(), trackName };
	}

	public static boolean validBAMViewingArgs(String cancerStudy, String caseId, String locus)
	{
		return (caseId != null && caseId.length() > 0 &&
				locus != null && locus.length() > 0 &&
				cancerStudy != null && cancerStudy.length() > 0 &&
				bamExists(cancerStudy, caseId));
	}

    public static boolean bamExists(String cancerStudy, String caseId)
    {
        return GlobalProperties.getIGVBAMLinkingStudies().contains(cancerStudy)  && knownCaseId(caseId);
    }

	private static boolean encryptionBinLocated()
	{
		return new File(GlobalProperties.getProperty(GlobalProperties.OPENSSL_BINARY)).exists();
	}

	private static String getBAMFileURL(String caseId)
	{
		String token = IGVLinking.getToken(caseId);
		return (token == null) ? null :
			GlobalProperties.getProperty(GlobalProperties.BROAD_BAM_URL).replace(TOKEN_REGEX, token);
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
		return Long.toString(Calendar.getInstance().getTime().getTime());
	}

	private static File getMessageToEncrypt(String caseId, String timestamp) throws Exception
	{
		File token = FileUtils.getFile(FileUtils.getTempDirectoryPath(), "broad-bam-token.txt");
		FileUtils.writeStringToFile(token, timestamp + " "  + caseId, "UTF-8", false);
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

	private static String getEncoded(String toEncode)
	{
		String encoded = null;
		try {
			encoded = URLEncoder.encode(toEncode, "US-ASCII");
		}
		catch(Exception e){}

		return encoded;
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

    private static boolean knownCaseId(String caseId)
    {
        String url = getBAMCheckingURL(caseId);
        if (url == null) return false;

        HttpClient client = getHttpClient();
        GetMethod method = new GetMethod(url);

        try {
            if (client.executeMethod(method) == HttpStatus.SC_OK) {
                return processSampleIdCheckResult(method.getResponseBodyAsString());
            }
        }
        catch (Exception e) {}
        finally {
            method.releaseConnection();
        }
        
        return false;
    }

    private static String getBAMCheckingURL(String caseId)
    {
        String encodedCaseId = getEncoded(caseId);
        String url = GlobalProperties.getProperty(GlobalProperties.BROAD_BAM_CHECKING_URL);
        return (url != null && !url.isEmpty() && encodedCaseId != null) ?
            url.replace(SAMPLE_REGEX, encodedCaseId) : null;
    }

    private static HttpClient getHttpClient()
    {
        MultiThreadedHttpConnectionManager connectionManager =
            ConnectionManager.getConnectionManager();
        return new HttpClient(connectionManager);
    }

    private static boolean processSampleIdCheckResult(String responseBody)
    {
        return (responseBody.equalsIgnoreCase(KNOWN_ID));
    }

    private static String getNormalSampleId(String tumorSampleId)
    {
        // most normal id's are simply the tumorSampleId with "Normal" replaced with "Tumor"
        String normalId = tumorSampleId.replace(TUMOR_BAM_SIGNATURE, NORMAL_BAM_SIGNATURE);
        if (knownCaseId(normalId)) return normalId;

        // in some cases, we need to truncate everything
        // following & including "Tumor" and replace with "Normal"
        String tumorSampleIdPrefix = getTumorSampleIdPrefix(tumorSampleId);
        if (tumorSampleIdPrefix != null) {
            String encodedTumorSampleId = getEncoded(tumorSampleIdPrefix +
                                                     NORMAL_BAM_SIGNATURE);
            if (encodedTumorSampleId != null &&
                knownCaseId(encodedTumorSampleId)) {
                return tumorSampleIdPrefix + NORMAL_BAM_SIGNATURE;
            }
        }

        return null;
    }

    private static String getTumorSampleIdPrefix(String tumorSampleId)
    {
        int tumorSignatureIndex = tumorSampleId.indexOf(TUMOR_BAM_SIGNATURE);
        return (tumorSignatureIndex > 0) ?
            tumorSampleId.substring(0, tumorSignatureIndex) : null;
    }
}
