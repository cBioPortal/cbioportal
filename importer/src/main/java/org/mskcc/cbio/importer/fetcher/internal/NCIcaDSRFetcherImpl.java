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
package org.mskcc.cbio.importer.fetcher.internal;

// imports
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;

import org.mskcc.cbio.portal.web_api.ConnectionManager;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.commons.logging.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.*;
import org.apache.commons.httpclient.params.*;
import org.apache.commons.httpclient.methods.*;

import org.springframework.beans.factory.annotation.Value;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.net.URLEncoder;

class NCIcaDSRFetcherImpl extends FetcherBaseImpl implements NCIcaDSRFetcher
{
    private static final int READ_TIMEOUT = 60000; // ms
    private static final int NO_REVISION_FOUND = -1;
    private static final String NAME_EXP = "/queryResponse/field[@name='longName']";
    private static final String DESC_EXP = "/queryResponse/field[@name='preferredDefinition']";
	private static final Log LOG = LogFactory.getLog(BiotabFetcherImpl.class);

	private String caDSRURL;
	@Value("${nci.cadsr.url}")
	public void setCADSRURL(String caDSRURL) { this.caDSRURL = caDSRURL; }

    private XPathExpression preferredNameExp;
    private XPathExpression preferredDescExp;

	public NCIcaDSRFetcherImpl() throws Exception
    {
        XPath xPath =  XPathFactory.newInstance().newXPath();
        preferredNameExp = xPath.compile(NAME_EXP);
        preferredDescExp = xPath.compile(DESC_EXP);
    }

	@Override
    public NCIcaDSREntry fetchDSREntry(String cdiId)
    {
        HttpClient client = getHttpClient();
        HttpMethodParams params = client.getParams();
        params.setSoTimeout(READ_TIMEOUT);
        GetMethod method = new GetMethod(getURLToXML(cdiId));

        try {
            if (client.executeMethod(method) == HttpStatus.SC_OK) {
                returnEntry(method.getResponseBodyAsStream());
            }
        }
        catch (Exception e) {
            logMessage(LOG, e.getMessage());
        }
        finally {
            method.releaseConnection();
        }

        return null;
	}

    private HttpClient getHttpClient()
    {
        MultiThreadedHttpConnectionManager connectionManager =
            ConnectionManager.getConnectionManager();
        return new HttpClient(connectionManager);
    }

    private String getURLToXML(String cdiId)
    {
        String encoded = null;

        try {
            encoded = 
                URLEncoder.encode(caDSRURL.replace(ClinicalAttributesNamespace.CDE_TAG, cdiId), "US-ASCII");
        }
        catch(Exception e) {}

        return encoded;
    }

    private NCIcaDSREntry returnEntry(InputStream is) throws Exception
    {
       DocumentBuilder builder = getDocumentBuilder(); 
       Document document = builder.parse(is);

       NCIcaDSREntry entry = new NCIcaDSREntry();
       entry.preferredName = preferredNameExp.evaluate(document);
       entry.preferredDefinition = preferredDescExp.evaluate(document);

       return entry;
    }

    private DocumentBuilder getDocumentBuilder() throws Exception
    {
        DocumentBuilderFactory builderFactory =
            DocumentBuilderFactory.newInstance();
        return builderFactory.newDocumentBuilder();
    }
}
