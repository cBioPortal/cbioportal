package org.mskcc.portal.remote;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.mskcc.portal.util.Config;
import org.mskcc.portal.util.SkinUtil;
import org.mskcc.portal.util.ResponseUtil;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * Enables Remote Queries of the Cancer Genomics Data Server (CGDS).
 */
public class CgdsProtocol {
    private XDebug xdebug;

    /**
     * cmd argument.
     */
    public static final String CMD = "cmd";

    /**
     * cancer_study_id argument.
     */
    //public static final String CANCER_STUDY_ID = "cancer_study_id";
    public static final String CANCER_STUDY_ID = "cancer_type_id";

    /**
     * case_list argument.
     */
    public static final String CASE_LIST = "case_list";

    /**
     * gene_list argument.
     */
    public static final String GENE_LIST = "gene_list";    

    public CgdsProtocol(XDebug xdebug) {
        this.xdebug = xdebug;
    }

    private PostMethod method;

    /**
     * Connects to remote data server with the specified list of name / value pairs.
     *
     * @param data Array of NameValuePair Objects.
     * @return Text Response.
     * @throws IOException IO / Network Error.
     */
    public String connect( NameValuePair[] data, XDebug xdebug ) throws IOException {

        //  Create a key, based on the NameValuePair[] data
        String key = createKey(data);
        xdebug.logMsg(this, "Using Cache Key:  " + key);

        //  Check Cache
        CacheManager singletonManager = CacheManager.getInstance();
        Cache memoryCache = singletonManager.getCache("memory_cache");
        xdebug.logMsg(this, "Cache Status:  " + memoryCache.getStatus().toString());

        //  If Content is found in cache, return it
        Element element = memoryCache.get(key);

        if (element != null) {
            xdebug.logMsg(this, "Cache Hit.  Using Memory.");
            return (String) element.getObjectValue();
        } else {
            xdebug.logMsg(this, "Cache Miss.  Connecting to Web API.");
            //  Otherwise, connect to Web API

            //  Get CGDS URL Property
            String cgdsUrl = Config.getInstance().getProperty("cgds.url");

            MultiThreadedHttpConnectionManager connectionManager =
                    ConnectionManager.getConnectionManager();
            xdebug.logMsg(this, "Number of connections in pool:  " +
                    connectionManager.getConnectionsInPool());
            xdebug.logMsg(this, "Max Connections per host:  " +
                    connectionManager.getParams().getDefaultMaxConnectionsPerHost());

            HttpClient client = new HttpClient(connectionManager);

            //  Create GET / POST Method
            method = new PostMethod(cgdsUrl);
            method.setRequestBody(data);
            // we need to added a cookie with session id to work with spring security
			if (SkinUtil.usersMustAuthenticate()) {
                method.setRequestHeader("Cookie", "JSESSIONID=" + xdebug.getRequest().getSession().getId());
            }
            try {

                //  Extract HTTP Status Code
                int statusCode = client.executeMethod(method);

                //  If all is OK, extract the response text
                if (statusCode == HttpStatus.SC_OK) {
                    String content = ResponseUtil.getResponseString(method);
                    Element newElement = new Element(key, content);
                    xdebug.logMsg(this, "Placing text in cache.");
                    memoryCache.put(newElement);
                    return content;
                } else {
                    //  Otherwise, throw HTTP Exception Object
                    throw new HttpException(statusCode + ": " + HttpStatus.getStatusText(statusCode)
                            + " Base URL:  " + cgdsUrl);
                }
            } finally {
                //  Must release connection back to Apache Commons Connection Pool
                method.releaseConnection();
            }
        }
    }

    private String createKey (NameValuePair[] data) {
        StringBuffer buf = new StringBuffer();
        for (NameValuePair nvp:  data) {
            buf.append (nvp.getName() + ":" + nvp.getValue() + " # ");
        }
        return buf.toString();
    }

    /**
     * Gets the URI.
     *
     * @return URI.
     * @throws org.apache.commons.httpclient.URIException
     *          URI Error.
     */
    public URI getURI() throws URIException {
        if (method != null) {
            return this.method.getURI();
        } else {
            return null;
        }
    }
}
