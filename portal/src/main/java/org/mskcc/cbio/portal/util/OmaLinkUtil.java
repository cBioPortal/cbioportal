package org.mskcc.cbio.portal.util;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Utility Class for Redirecting to the Online Mutation Assessor (OMA) Web Site.
 *
 * We redirect from the Portal to the OMA web site for two reasons:
 *
 * 1.  To track usage, e.g. how many people actually click on links from the portal to OMA.
 * 2.  To address the security settings on the OMA web site, which prevent HTTP Referral URLs
 *     from containing new line characters, which frequently occurs within the gene set box.
 *
 * @author Ethan Cerami.
 */
public class OmaLinkUtil {
    private final static String OMA_REDIRECT_LINK = "omaRedirect.do?";
    private final static String SITE_PARAM = "site";

    /**
     * Creates a Redirect Link from Portal to OMA.
     *
     * Incoming links look like this:
     * http://mutationassessor.org/?cm=var&var=17,7517830,G,C&fts=all
     *
     * Outgoing links look like this:
     * omaRedirect.do?path=/&cm=var&var=17,7517830,G,C&fts=all
     *
     * @param omaUrl  Incoming Link to OMA.
     * @return Redirect Link from Portal to OMA.
     * @throws MalformedURLException Malformed URL Error.
     */
    public static String createOmaRedirectLink(String omaUrl) throws MalformedURLException {
        omaUrl = conditionallyPrependHttp(omaUrl);
        URL url = new URL (omaUrl);
        String site = url.getHost();
        String path = url.getPath();
        String query = url.getQuery();
        return OMA_REDIRECT_LINK + SITE_PARAM + "=" + site + path + "&" + query;
    }

    /**
     * Creates a Direct Link to the OMA Web Site.
     *
     * Incoming query string like this:
     * path=/&cm=var&var=17,7517830,G,C&fts=all
     *
     * Outgoing links look like this:
     * http://mutationassessor.org/?cm=var&var=17,7517830,G,C&fts=all
     *
     * @param omaQueryString  OMA Query String.
     * @return Direct Link to OMA Web Site.
     * @throws MalformedURLException Malformed URL Error.
     */
    public static String createOmaLink(String omaQueryString) throws MalformedURLException {
        omaQueryString = removePath(omaQueryString);
        String params[] = omaQueryString.split("&");
        HashMap<String, String> paramMap = getParameterMap(params);
        String path = paramMap.get(SITE_PARAM);
        ArrayList<String> keyList = getKeyList(paramMap);

        String queryString = createQueryString(keyList, paramMap);
        return "http://" + path + "?" + queryString;
    }

    private static String conditionallyPrependHttp(String omaUrl) {
        if (!omaUrl.startsWith("http://")) {
            omaUrl = "http://" + omaUrl;
        }
        return omaUrl;
    }

    private static String removePath(String omaQueryString) {
        omaQueryString = omaQueryString.replaceAll(OMA_REDIRECT_LINK, "");
        if (omaQueryString.startsWith("?")) {
            omaQueryString = omaQueryString.substring(1);
        }
        return omaQueryString;
    }

    private static String createQueryString(ArrayList<String> keyList,
            HashMap<String, String> paramMap) {
        StringBuffer queryString = new StringBuffer();
        for (int i=0; i<keyList.size(); i++) {
            String name = keyList.get(i);
            queryString.append (name + "=" + paramMap.get(name));
            queryString.append(getDelimiter(i, keyList));
        }
        return queryString.toString();
    }

    private static String getDelimiter(int i, ArrayList<String> keyList) {
        if (i < keyList.size() -1) {
            return "&";
        } else {
            return "";
        }
    }

    private static HashMap<String, String> getParameterMap(String params[]) {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        for (String param : params) {
            String parts[] = param.split("=");
            paramMap.put(parts[0], parts[1]);
        }
        return paramMap;
    }

    private static ArrayList<String> getKeyList(HashMap<String, String> paramMap) {
        ArrayList<String> keyList = new ArrayList<String>();
        for (String currentKey:  paramMap.keySet()) {
            if (!currentKey.equals(SITE_PARAM)) {
                keyList.add(currentKey);
            }
        }
        Collections.sort(keyList);
        return keyList;
    }
}