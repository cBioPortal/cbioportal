package org.mskcc.cbio.portal.test.util;

import junit.framework.TestCase;
import org.mskcc.portal.util.OmaLinkUtil;

import java.net.MalformedURLException;

/**
 * Tests the OmaLink Utility Class.
 *
 * @author Ethan Cerami.
 */
public class TestOmaLinkUtil extends TestCase {
    private String queryStringParams1 = "cm=var&fts=all&var=17,7517830,G,C";
    private String queryStringParams2 = "from=601&prot=EGFR_HUMAN&to=800&var=C620Y";

    public void testOmaLinkUtil1() throws MalformedURLException {
        String omaLinkIn = "http://mutationassessor.org/?" + queryStringParams1;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        assertEquals("omaRedirect.do?site=mutationassessor.org/&" + queryStringParams1, omaLinkOut);

        String omaLink = OmaLinkUtil.createOmaLink(omaLinkOut);
        assertEquals (omaLinkIn, omaLink);

        omaLink = OmaLinkUtil.createOmaLink("site=mutationassessor.org/&" + queryStringParams1);
        assertEquals (omaLinkIn, omaLink);
    }

    public void testOmaLinkUtil2() throws MalformedURLException {
        String omaLinkIn = "http://mutationassessor.org/pdb.php?" + queryStringParams2;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        assertEquals("omaRedirect.do?site=mutationassessor.org/pdb.php&"
                + queryStringParams2, omaLinkOut);
        String omaLink = OmaLinkUtil.createOmaLink(omaLinkOut);
        assertEquals (omaLinkIn, omaLink);
    }

    public void testOmaLinkUtil3() throws MalformedURLException {
        String omaLinkIn = "http://xvar.org/pdb.php?" + queryStringParams2;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        String omaLink = OmaLinkUtil.createOmaLink(omaLinkOut);
        assertEquals (omaLinkIn, omaLink);
    }

    public void testOmaLinkUtil4() throws MalformedURLException {
        String omaLinkIn = "mutationassessor.org/?" + queryStringParams1;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        assertEquals("omaRedirect.do?site=mutationassessor.org/&" + queryStringParams1, omaLinkOut);

        String omaLink = OmaLinkUtil.createOmaLink("site=mutationassessor.org/&"
                + queryStringParams1);
        assertEquals ("http://" + omaLinkIn, omaLink);
    }
}