package org.mskcc.portal.test.html;

import junit.framework.TestCase;
import org.mskcc.cgds.model.ExtendedMutation;
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
        assertEquals(10, headerList.size());
        assertTrue ("Checking for valid BRCA1-specific footer",
                footerMessage.startsWith("* Known BRCA1 185/187DelAG"));
    }

    public void test3() {
        MutationTableUtil mutationHtml = new MutationTableUtil("BRCA2");
        ArrayList<String> headerList = mutationHtml.getTableHeaders();
        String footerMessage = mutationHtml.getTableFooterMessage();
        assertEquals(10, headerList.size());
        assertTrue ("Checking for valid BRCA2-specific footer",
                footerMessage.startsWith("* Known BRCA2 6174delT founder"));
    }

}
