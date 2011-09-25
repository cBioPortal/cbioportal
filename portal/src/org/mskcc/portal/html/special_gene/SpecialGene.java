package org.mskcc.portal.html.special_gene;

import org.mskcc.cgds.model.ExtendedMutation;

import java.util.ArrayList;

/**
 * Interface for a Special Gene, Deserving of Extra Annotation.
 *
 * @author Ethan Cerami.
*/
public interface SpecialGene {

    /**
     * Gets the extra annocation headers (if any), associated with the special gene.
     * @return ArrayList of annotation headers.
     */
    public ArrayList<String> getDataFieldHeaders();

    /**
     * Gets Footer Annotation Describing Anything special about these mutations.
     * @return footer message.
     */
    public String getFooter();

    /**
     * Gets the extra annotation data fields (if any), associated with the specified
     * mutation.
     *
     * @param mutation ExtentedMutation Object.
     * @return ArrayList of Data Field String objects.
     */
    public ArrayList<String> getDataFields (ExtendedMutation mutation);
}
