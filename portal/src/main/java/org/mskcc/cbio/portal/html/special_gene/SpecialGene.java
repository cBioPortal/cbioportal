package org.mskcc.cbio.portal.html.special_gene;

import org.mskcc.cbio.cgds.model.ExtendedMutation;

import java.util.ArrayList;

/**
 * Interface for a Special Gene, Deserving of Extra Annotation.
 *
 * @author Ethan Cerami.
*/
public abstract class SpecialGene {

    /**
     * Gets the extra annocation headers (if any), associated with the special gene.
     * @return ArrayList of annotation headers.
     */
    public abstract ArrayList<String> getDataFieldHeaders();

    /**
     * Gets Footer Annotation Describing Anything special about these mutations.
     * @return footer message.
     */
    public abstract String getFooter();

    /**
     * Gets the extra annotation data fields (if any), associated with the specified
     * mutation.
     *
     * @param mutation ExtentedMutation Object.
     * @return ArrayList of Data Field String objects.
     */
    public abstract ArrayList<String> getDataFields (ExtendedMutation mutation);

    protected void setNtPosition(long ntPosition, ArrayList<String> dataFields) {
        if (ntPosition > 0) {
            dataFields.add(Long.toString(ntPosition));
        } else {
            dataFields.add("--");
        }
    }
}
