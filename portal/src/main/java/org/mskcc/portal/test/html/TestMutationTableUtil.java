package org.mskcc.portal.test.html;

import junit.framework.TestCase;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.portal.html.MutationAssessorHtmlUtil;
import org.mskcc.portal.html.MutationTableUtil;

import java.util.ArrayList;

/**
 * Tests the MutationTableUtil Class.
 *
 * @author Ethan Cerami.
 */
public class TestMutationTableUtil extends TestCase {

    public void test1() {
        MutationTableUtil mutationHtml = new MutationTableUtil("TP53");
        ArrayList<String> headerList = mutationHtml.getTableHeaders();
        assertEquals(9, headerList.size());
        assertEquals ("", mutationHtml.getTableFooterMessage());
    }

    public void test2() {
        MutationTableUtil mutationHtml = new MutationTableUtil("BRCA1");
        ArrayList<String> headerList = mutationHtml.getTableHeaders();
        String footerMessage = mutationHtml.getTableFooterMessage();
        assertEquals(11, headerList.size());
        assertTrue ("Checking for valid BRCA1-specific footer",
                footerMessage.startsWith("* Known BRCA1 185/187DelAG"));
    }

    public void test3() {
        MutationTableUtil mutationHtml = new MutationTableUtil("BRCA2");
        ArrayList<String> headerList = mutationHtml.getTableHeaders();
        String footerMessage = mutationHtml.getTableFooterMessage();
        assertEquals(11, headerList.size());
        assertTrue ("Checking for valid BRCA2-specific footer",
                footerMessage.startsWith("* Known BRCA2 6174delT founder"));
    }

    public void test4() {
        MutationTableUtil mutationHtml = new MutationTableUtil("BRCA1");
        ExtendedMutation mutation = new ExtendedMutation();
        mutation.setGene(new CanonicalGene(1, "BRCA1"));

        ArrayList<String> dataFieldList = mutationHtml.getDataFields(mutation);
        assertEquals(11, dataFieldList.size());
        for (int i=0; i<=7; i++) {
            assertEquals("&nbsp;", dataFieldList.get(i));
        }
    }

    public void test5() {
        MutationTableUtil mutationHtml = new MutationTableUtil("BRCA1");
        ExtendedMutation mutation = new ExtendedMutation();
        mutation.setAminoAcidChange("A22C");
        mutation.setEndPosition(38462606);
        mutation.setSequencingCenter("broad.mit.edu");
        mutation.setMutationType("Frameshift");
        mutation.setMutationStatus("Somatic");
        mutation.setCaseId("TCGA-1234");
        mutation.setValidationStatus("valid");
        mutation.setFunctionalImpactScore("H");
        mutation.setGene(new CanonicalGene(1, "BRCA1"));

        ArrayList<String> dataFieldList = mutationHtml.getDataFields(mutation);
        ArrayList<String> headerList = mutationHtml.getTableHeaders();
        assertEquals(11, dataFieldList.size());
        validateHeaderPlusValue(0, "Case ID", "TCGA-1234", headerList, dataFieldList);
        validateHeaderPlusValue(1, "Mutation Status", "<span class='somatic'>Somatic</span>",
                headerList, dataFieldList);
        validateHeaderPlusValue(2, "Mutation Type", "Frameshift", headerList, dataFieldList);
        validateHeaderPlusValue(3, "Validation Status", "<span class='valid'>valid</span>",
                headerList, dataFieldList);
        validateHeaderPlusValue(4, "Sequencing Center", "Broad", headerList, dataFieldList);
        validateHeaderPlusValue(5, "AA Change", "A22C", headerList, dataFieldList);
        validateHeaderPlusValue(6, "Predicted Impact**",
                "<span class='oma_high'>High</span>", headerList, dataFieldList);
        validateHeaderPlusValue(7, "Alignment", "&nbsp;", headerList, dataFieldList);
        validateHeaderPlusValue(8, "Structure", "&nbsp;", headerList, dataFieldList);
        validateHeaderPlusValue(9, "NT Position*", "5385", headerList, dataFieldList);
        validateHeaderPlusValue(10, "Notes", "5382/5385 insC Founder Mutation", 
                headerList, dataFieldList);

    }

    private void validateHeaderPlusValue(int index, String expectedHeader, String expectedValue,
        ArrayList<String> headerList, ArrayList<String> dataFieldList) {
        String header = headerList.get(index);
        String value = dataFieldList.get(index);
        assertEquals (expectedHeader, header);
        assertEquals (expectedValue, value);
    }


}
