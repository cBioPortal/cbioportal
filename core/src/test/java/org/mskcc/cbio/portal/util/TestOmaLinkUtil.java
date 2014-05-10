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

package org.mskcc.cbio.portal.util;

import junit.framework.TestCase;

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