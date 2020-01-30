/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.util;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import org.junit.*;

/**
 * Tests the OmaLink Utility Class.
 *
 * @author Ethan Cerami.
 */
public class TestOmaLinkUtil {
    private String queryStringParams1 = "cm=var&fts=all&var=17,7517830,G,C";
    private String queryStringParams2 =
        "from=601&prot=EGFR_HUMAN&to=800&var=C620Y";

    @Test
    public void testOmaLinkUtil1() throws MalformedURLException {
        String omaLinkIn = "http://mutationassessor.org/?" + queryStringParams1;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        assertEquals(
            "omaRedirect.do?site=mutationassessor.org/&" + queryStringParams1,
            omaLinkOut
        );
        String omaLink = OmaLinkUtil.createOmaLink(omaLinkOut);
        assertEquals(omaLinkIn, omaLink);
        omaLink =
            OmaLinkUtil.createOmaLink(
                "site=mutationassessor.org/&" + queryStringParams1
            );
        assertEquals(omaLinkIn, omaLink);
    }

    @Test
    public void testOmaLinkUtil2() throws MalformedURLException {
        String omaLinkIn =
            "http://mutationassessor.org/pdb.php?" + queryStringParams2;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        assertEquals(
            "omaRedirect.do?site=mutationassessor.org/pdb.php&" +
            queryStringParams2,
            omaLinkOut
        );
        String omaLink = OmaLinkUtil.createOmaLink(omaLinkOut);
        assertEquals(omaLinkIn, omaLink);
    }

    @Test
    public void testOmaLinkUtil3() throws MalformedURLException {
        String omaLinkIn = "http://xvar.org/pdb.php?" + queryStringParams2;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        String omaLink = OmaLinkUtil.createOmaLink(omaLinkOut);
        assertEquals(omaLinkIn, omaLink);
    }

    @Test
    public void testOmaLinkUtil4() throws MalformedURLException {
        String omaLinkIn = "mutationassessor.org/?" + queryStringParams1;
        String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
        assertEquals(
            "omaRedirect.do?site=mutationassessor.org/&" + queryStringParams1,
            omaLinkOut
        );
        String omaLink = OmaLinkUtil.createOmaLink(
            "site=mutationassessor.org/&" + queryStringParams1
        );
        assertEquals("http://" + omaLinkIn, omaLink);
    }

    private void testExpectedMalformedLink(String omaLinkIn) {
        try {
            String omaLinkOut = OmaLinkUtil.createOmaRedirectLink(omaLinkIn);
            fail(
                "call to OmaLinkUtil.createOmaRedirectLink(\"" +
                omaLinkIn +
                "\") was expected to generate an exception but returned: " +
                omaLinkOut
            );
        } catch (MalformedURLException e) {
            // expected .. no failure
        }
    }

    @Test
    public void testOmaLinkUtil5() {
        testExpectedMalformedLink("NA");
        testExpectedMalformedLink("[Not Available]");
        testExpectedMalformedLink("");
    }
}
