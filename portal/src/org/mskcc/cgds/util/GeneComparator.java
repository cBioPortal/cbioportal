package org.mskcc.cgds.util;

// imports
import java.util.Comparator;
import org.mskcc.cgds.model.Gene;

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
