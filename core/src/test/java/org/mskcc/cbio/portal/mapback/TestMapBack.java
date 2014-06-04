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

package org.mskcc.cbio.portal.mapback;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.mapback.MapBack;
import org.mskcc.cbio.portal.mapback.Brca1;
import org.mskcc.cbio.portal.mapback.Brca2;

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