package org.mskcc.portal.test.mapback;

import junit.framework.TestCase;
import org.mskcc.portal.mapback.MapBack;
import org.mskcc.portal.mapback.Brca1;
import org.mskcc.portal.mapback.Brca2;

public class TestMapBack extends TestCase {

    public void testMapBack1 () {
        Brca1 brca1 = new Brca1();

        //  From:  http://www.pharmgkb.org/search/annotatedGene/brca1/variant.jsp
        //  we know the BRCA1 187 DEL AG Maps to: Chr17: 38529572 (hg18)
        validate(brca1, 38529572, 187, 'A');

        //  From:  http://www.pharmgkb.org/search/annotatedGene/brca1/variant.jsp
        //  we know the BRCA1 5385insC Maps to: Chr17: 38462606 (hg 18)
        validate(brca1, 38462606, 5385, 'C');
        
        validate(brca1, 38512070, 260, 'C');
        validate(brca1, 38499531, 1662, 'G');
        validate(brca1, 38498908, 2285, 'C');
        validate(brca1, 38451310, 5622, 'C');
    }

    public void testMapBack2 () {
        Brca2 brca2 = new Brca2();
        MapBack mapBack = new MapBack (brca2, 31812438);
        long ntPosition = mapBack.getNtPositionWhereMutationOccurs();

        assertEquals ("Nucleotide Position does not match!", 6174, ntPosition);
    }


    private void validate (Brca1 brca1, long mutationLocation, long expected, char expectedBp) {
        MapBack mapBack = new MapBack (brca1, mutationLocation);
        long ntPosition = mapBack.getNtPositionWhereMutationOccurs();

        assertEquals ("Nucleotide Position does not match!", expected, ntPosition);
        assertEquals ("Base Pair does not match!", expectedBp, mapBack.getBpWhereMutationOccurs());
    }
}