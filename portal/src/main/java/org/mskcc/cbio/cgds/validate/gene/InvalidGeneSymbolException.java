package org.mskcc.cbio.cgds.validate.gene;

import org.mskcc.cbio.portal.util.StringListUtil;

import java.util.ArrayList;

/**
 * Encapsulates One or More Invalid Gene Symbol Errors.
 *
 * @author Ethan Cerami.
 */
public class InvalidGeneSymbolException extends GeneValidationException {
    private ArrayList<String> invalidGeneList;

    public InvalidGeneSymbolException(ArrayList<String> invalidGeneList) {
        this.invalidGeneList = invalidGeneList;
        setUserMessage(("Invalid or unrecognized gene(s): "
                + getInvalidGeneSymbolsAsString(invalidGeneList)));
    }

    public ArrayList<String> getInvalidGeneList() {
        return invalidGeneList;
    }

    public String getInvalidGeneSymbolsAsString() {
        if (invalidGeneList.size() > 0) {
            return StringListUtil.covertItemsIntoSentence(invalidGeneList);
        } else {
            return null;
        }
    }

    private String getInvalidGeneSymbolsAsString(ArrayList<String> invalidGeneList) {
        if (invalidGeneList.size() > 0) {
            return StringListUtil.covertItemsIntoSentence(invalidGeneList);
        } else {
            return null;
        }
    }
}