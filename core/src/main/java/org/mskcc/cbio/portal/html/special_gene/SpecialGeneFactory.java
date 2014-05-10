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

package org.mskcc.cbio.portal.html.special_gene;

import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.mapback.MapBack;
import org.mskcc.cbio.portal.mapback.Brca2;
import org.mskcc.cbio.portal.mapback.Brca1;
import org.mskcc.cbio.portal.html.HtmlUtil;

import java.util.ArrayList;

/**
 * Factory for Special Gene Objects.
 *
 * @author Ethan Cerami.
 */
public class SpecialGeneFactory {

    /**
     * Gets instance of a Special Gene.
     * @param geneSymbol Gene Symbol.
     * @return Special Gene Object.
     */
    public static SpecialGene getInstance(String geneSymbol) {
        if (geneSymbol.equalsIgnoreCase(SpecialGeneBrca1.BRCA1)) {
            return new SpecialGeneBrca1();
        } else if (geneSymbol.equalsIgnoreCase(SpecialGeneBrca2.BRCA2)) {
            return new SpecialGeneBrca2();
        } else {
            return null;
        }
    }
}