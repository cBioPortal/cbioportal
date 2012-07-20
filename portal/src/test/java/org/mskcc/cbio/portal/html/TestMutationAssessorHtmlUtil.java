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
        assertEquals("High", omaUtil.getOmaImpactWord());
        assertEquals("oma_high", omaUtil.getOmaImpactCssStyle());
        String impactLink = omaUtil.getFunctionalImpactLink();

        validateLinks(omaUtil);
        assertEquals("<span class='oma_link oma_high'>" +
                "<a href='omaRedirect.do?site=mutationassessor.org" +
                "&cm=var&var=1,103968324,G,T&fts=all'>High</a></span>", impactLink);
    }

    public void test2() {
        ExtendedMutation mutation = createMutation2();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        validateLinks(omaUtil);
        assertEquals("Medium", omaUtil.getOmaImpactWord());
        assertEquals("oma_medium", omaUtil.getOmaImpactCssStyle());
    }

    public void test3() {
        ExtendedMutation mutation = createMutation3();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        validateLinks(omaUtil);
        assertEquals("Low", omaUtil.getOmaImpactWord());
        assertEquals("oma_low", omaUtil.getOmaImpactCssStyle());
    }

    public void test4() {
        ExtendedMutation mutation = createMutation4();
        MutationAssessorHtmlUtil omaUtil = new MutationAssessorHtmlUtil(mutation);
        validateLinks(omaUtil);
        assertEquals("Neutral", omaUtil.getOmaImpactWord());
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
                "&prot=AMYP_HUMAN&from=421&to=510&var=G435V'>Alignment</a>", msaLink);
        assertEquals("<a href='omaRedirect.do?site=mutationassessor.org/" +
                "&cm=msa&ty=f&p=AMYP_HUMAN&rb=421&re=510&var=G435V'>Structure</a>",
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
