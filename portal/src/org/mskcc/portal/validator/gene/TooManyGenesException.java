package org.mskcc.portal.validator.gene;

/**
 * Indicates that the User has Specified Too Many Genes.
 *
 * @author Ethan Cerami.
 */
public class TooManyGenesException extends GeneValidationException {

    public TooManyGenesException(int numGenesSpecified, int maxNumGenesAllowed) {
        super("Too many genes specified:  "
                + numGenesSpecified + ".  Please restrict your query to a maximum of "
                + maxNumGenesAllowed + " genes.");
    }

}