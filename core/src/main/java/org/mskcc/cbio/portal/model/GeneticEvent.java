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

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.model.GeneticEventImpl.*;

/**
 * Interface for Encapsulating Information Regarding One Gene in One Sample.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public interface GeneticEvent {

    /**
     * Is the Gene Amplified at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaAmplified();

    /**
     * Is the Gene Homozygously Deleted at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaHomozygouslyDeleted();

    /**
     * Is the Gene Heterozgously Deleted at the Copy Number Level?
     *
     * @return true or false.
     */
    public boolean isCnaHeterozygousDeleted();

    /**
     * Is the Gene mRNA upregulated?
     *
     * @return true or false.
     */
    public boolean isMRNAUpRegulated();

    /**
     * Is the Gene mRNA down-regulated?
     *
     * @return true or false.
     */
    public boolean isMRNADownRegulated();

    /**
     * Is the Gene RPPA upregulated?
     *
     * @return true or false.
     */
    public boolean isRPPAUpRegulated();

    /**
     * Is the Gene RPPA down-regulated?
     *
     * @return true or false.
     */
    public boolean isRPPADownRegulated();

    /**
     * Is gene mutated.
     *
     * @return true or false.
     */
    public boolean isMutated();

    /**
     * Gets the Gene.
     * @return Gene Symbol.
     */
    public String getGene();

    /**
     * Gets the Case ID.
     * @return Case ID.
     */
    public String caseCaseId();

    /**
     * Gets the CNA Value.
     *
     * @return cna Value.
     */
    public CNA getCnaValue();

    /**
     * Gets the MRNA Value.
     *
     * @return mRNA Value.
     */
    public MRNA getMrnaValue();

    /**
     * Gets the RPPA Value.
     *
     * @return RPPA Value.
     */
    public RPPA getRPPAValue();
    
    /**
     * Gets the mutations Value.
     *
     * @return mutations Value.
     */
    public mutations getMutationValue();

    /**
     * Gets the mutation type (amino acid change).
     *
     * @return mutation Type.
     */
    public String getMutationType();
    
    /**
     * Set the event's comparator.
     */
    public void setGeneticEventComparator( GeneticEventComparator geneticEventComparator );
}