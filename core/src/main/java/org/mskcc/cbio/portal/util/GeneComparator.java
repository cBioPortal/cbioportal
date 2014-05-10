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

// imports
import java.util.Comparator;
import org.mskcc.cbio.portal.model.Gene;

/**
 * Compares two genes for equivalency (ref and standard symbol).
 */
public class GeneComparator implements Comparator {

    public int compare(Object object0, Object object1) {
        if (object0 != null && object1 != null) {
            Gene gene0 = (Gene) object0;
            Gene gene1 = (Gene) object1;
            String name0 = gene0.getStandardSymbol();
            String name1 = gene1.getStandardSymbol();
            if (name0 != null && name1 != null) {
                return name0.compareTo(name1);
            }
        }
        return -1;
    }
}
