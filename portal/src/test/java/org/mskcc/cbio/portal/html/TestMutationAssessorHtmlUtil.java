/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.html;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.portal.html.MutationAssessorHtmlUtil;

/**
 * Tests the MutationAssessorHtmlUtil Class.
 *
 * @author Ethan Cerami.
 */
public class TestMutationAssessorHtmlUtil extends TestCase {

    public void test1() {
        ExtendedMutation mutation = createMutation1();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        assertEquals("H", omaUtil.getOmaImpactWord());
        assertEquals("oma_high", omaUtil.getOmaImpactCssStyle());
        String impactLink = omaUtil.getFunctionalImpactLink();

        validateLinks(omaUtil);
        assertEquals("<span class='oma_link oma_high'>" +
                "<a href='omaRedirect.do?site=mutationassessor.org" +
                "&cm=var&var=1,103968324,G,T&fts=all'>H</a></span>", impactLink);
    }

    public void test2() {
        ExtendedMutation mutation = createMutation2();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        validateLinks(omaUtil);
        assertEquals("M", omaUtil.getOmaImpactWord());
        assertEquals("oma_medium", omaUtil.getOmaImpactCssStyle());
    }

    public void test3() {
        ExtendedMutation mutation = createMutation3();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        validateLinks(omaUtil);
        assertEquals("L", omaUtil.getOmaImpactWord());
        assertEquals("oma_low", omaUtil.getOmaImpactCssStyle());
    }

    public void test4() {
        ExtendedMutation mutation = createMutation4();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        validateLinks(omaUtil);
        assertEquals("N", omaUtil.getOmaImpactWord());
        assertEquals("oma_neutral", omaUtil.getOmaImpactCssStyle());
    }

    public void test5() {
        ExtendedMutation mutation = createMutation();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        assertEquals("&nbsp;", omaUtil.getFunctionalImpactLink());
        assertEquals("&nbsp;", omaUtil.getMultipleSequenceAlignmentLink());
        assertEquals("&nbsp;", omaUtil.getPdbStructureLink());
    }

    private void validateLinks(MutationAssessorHtmlUtil omaUtil) {
        String msaLink = omaUtil.getMultipleSequenceAlignmentLink();
        String pdbLink = omaUtil.getPdbStructureLink();
        assertEquals("<a href='omaRedirect.do?site=mutationassessor.org/pdb.php" +
                "&prot=AMYP_HUMAN&from=421&to=510&var=G435V'>" +
                "<img border='0' src='images/mutation/msa.png'></a>",
                     msaLink);
        assertEquals("<a href='omaRedirect.do?site=mutationassessor.org/" +
                "&cm=msa&ty=f&p=AMYP_HUMAN&rb=421&re=510&var=G435V'>" +
                "<img border='0' src='images/mutation/pdb.png'></a>",
                    pdbLink);
    }

    private ExtendedMutation createMutation1() {
        ExtendedMutation mutation = createMutation();
        mutation.setFunctionalImpactScore("H");
        setLinks(mutation);
        return mutation;
    }

    private ExtendedMutation createMutation2() {
        ExtendedMutation mutation = createMutation();
        mutation.setFunctionalImpactScore("M");
        setLinks(mutation);
        return mutation;
    }

    private ExtendedMutation createMutation3() {
        ExtendedMutation mutation = createMutation();
        mutation.setFunctionalImpactScore("L");
        setLinks(mutation);
        return mutation;
    }

    private ExtendedMutation createMutation4() {
        ExtendedMutation mutation = createMutation();
        mutation.setFunctionalImpactScore("N");
        setLinks(mutation);
        return mutation;
    }

    private ExtendedMutation createMutation() {
        CanonicalGene canonicalGene = new CanonicalGene(279, "AMY2A");
        return new ExtendedMutation (canonicalGene, "Somatic", "Valid", "Missense_Mutation");
    }

    private void setLinks(ExtendedMutation mutation) {
        mutation.setLinkXVar("http://mutationassessor.org?cm=var&var=1,103968324,G,T&fts=all");
        mutation.setLinkMsa("http://mutationassessor.org/pdb.php?prot=AMYP_HUMAN&" +
                "from=421&to=510&var=G435V");
        mutation.setLinkPdb("http://mutationassessor.org/?cm=msa&ty=f&p=AMYP_HUMAN&" +
                "rb=421&re=510&var=G435V");
    }
}
