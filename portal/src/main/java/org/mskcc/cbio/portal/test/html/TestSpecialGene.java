package org.mskcc.portal.test.html;

import junit.framework.TestCase;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.portal.html.special_gene.SpecialGeneFactory;
import org.mskcc.portal.html.special_gene.SpecialGene;

import java.util.ArrayList;

/**
 * Tests the Special Mutation Util Class.
 *
 * @author Ethan Cerami.
 */
public class TestSpecialGene extends TestCase {

    public void test1() {
        //  From:  http://www.pharmgkb.org/search/annotatedGene/brca1/variant.jsp
        //  we know the BRCA1 187 DEL AG Maps to: Chr17: 38529572 (hg18)
        CanonicalGene brca1 = createBrca1();
        ExtendedMutation mutation = new ExtendedMutation(brca1, "XXX", "XXX", "XXX");
        mutation.setEndPosition(38529572);
        SpecialGene specialGene = SpecialGeneFactory.getInstance(brca1.getHugoGeneSymbolAllCaps());
        ArrayList<String> headerList = specialGene.getDataFieldHeaders ();
        assertEquals (2, headerList.size());
        ArrayList<String> dataFieldList = specialGene.getDataFields(mutation);
        assertEquals (2, dataFieldList.size());
        assertEquals ("187", dataFieldList.get(0));
        assertEquals ("185/187DelAG Founder Mutation", dataFieldList.get(1));
        assertEquals ("* Known BRCA1 185/187DelAG and 5382/5385 insC " +
                "founder mutations are noted.",
                specialGene.getFooter());
    }

    public void test2() {
        //  From:  http://www.pharmgkb.org/search/annotatedGene/brca1/variant.jsp
        //  we know the BRCA1 187 DEL AG Maps to: Chr17: 38529572 (hg18)
        CanonicalGene brca1 = createBrca1();
        SpecialGene specialGene = SpecialGeneFactory.getInstance(brca1.getHugoGeneSymbolAllCaps());
        ExtendedMutation mutation = new ExtendedMutation(brca1, "XXX", "XXX", "XXX");
        mutation.setEndPosition(38529574);
        ArrayList<String> dataFieldList = specialGene.getDataFields(mutation);
        assertEquals (2, dataFieldList.size());
        assertEquals ("185", dataFieldList.get(0));
        assertEquals ("185/187DelAG Founder Mutation", dataFieldList.get(1));
    }

    public void test3() {
        CanonicalGene brca1 = createBrca1();
        ExtendedMutation mutation = new ExtendedMutation(brca1, "XXX", "XXX", "XXX");
        mutation.setEndPosition(38529578);
        SpecialGene specialGene = SpecialGeneFactory.getInstance(brca1.getHugoGeneSymbolAllCaps());
        ArrayList<String> headerList = specialGene.getDataFieldHeaders();
        assertEquals (2, headerList.size());
        ArrayList<String> dataFieldList = specialGene.getDataFields(mutation);
        assertEquals (2, dataFieldList.size());
        assertEquals ("181", dataFieldList.get(0));
        assertEquals ("&nbsp;", dataFieldList.get(1));
    }

    public void test4() {
        CanonicalGene tp53 = new CanonicalGene(12345, "TP53");
        SpecialGene specialGene = SpecialGeneFactory.getInstance(tp53.getHugoGeneSymbolAllCaps());
        assertEquals (null, specialGene);
    }

    public void test5() {
        //  From:  http://www.pharmgkb.org/search/annotatedGene/brca1/variant.jsp
        //  we know the BRCA1 5385insC Maps to: Chr17: 38462606 (hg 18)
        CanonicalGene brca1 = createBrca1();
        ExtendedMutation mutation = new ExtendedMutation(brca1, "XXX", "XXX", "XXX");
        mutation.setEndPosition(38462606);
        SpecialGene specialGene = SpecialGeneFactory.getInstance(brca1.getHugoGeneSymbolAllCaps());
        ArrayList<String> headerList = specialGene.getDataFieldHeaders();
        assertEquals (2, headerList.size());
        ArrayList<String> dataFieldList = specialGene.getDataFields(mutation);
        assertEquals (2, dataFieldList.size());
        assertEquals ("5385", dataFieldList.get(0));
        assertEquals ("5382/5385 insC Founder Mutation", dataFieldList.get(1));
    }

    public void test6() {
        CanonicalGene brca2 = createBrca2();
        ExtendedMutation mutation = new ExtendedMutation(brca2, "XXX", "XXX", "XXX");
        mutation.setEndPosition(31812438);
        SpecialGene specialGene = SpecialGeneFactory.getInstance(brca2.getHugoGeneSymbolAllCaps());
        ArrayList<String> headerList = specialGene.getDataFieldHeaders();
        assertEquals (2, headerList.size());
        ArrayList<String> dataFieldList = specialGene.getDataFields(mutation);
        assertEquals (2, dataFieldList.size());
        assertEquals ("6174", dataFieldList.get(0));
        assertEquals ("6174delT founder mutation.", dataFieldList.get(1));
    }

    public void test7() {
        CanonicalGene brca1 = createBrca1();
        ExtendedMutation mutation = new ExtendedMutation(brca1, "XXX", "XXX", "XXX");
        mutation.setEndPosition(35);
        SpecialGene specialGene = SpecialGeneFactory.getInstance(brca1.getHugoGeneSymbolAllCaps());
        ArrayList<String> headerList = specialGene.getDataFieldHeaders ();
        assertEquals (2, headerList.size());
        ArrayList<String> dataFieldList = specialGene.getDataFields(mutation);
        assertEquals (2, dataFieldList.size());
        assertEquals ("--", dataFieldList.get(0));
    }

    private CanonicalGene createBrca1() {
        return new CanonicalGene(1, "BRCA1");
    }

    private CanonicalGene createBrca2() {
        return new CanonicalGene(2, "BRCA2");
    }
}
