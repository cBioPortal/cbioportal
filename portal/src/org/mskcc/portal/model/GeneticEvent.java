package org.mskcc.portal.model;

import org.mskcc.portal.model.GeneticEventImpl.*;

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
     * Gets the mutations Value.
     *
     * @return mutations Value.
     */
    public mutations getMutationValue();
    
    /**
     * Set the event's comparator.
     */
    public void setGeneticEventComparator( GeneticEventComparator geneticEventComparator );
}