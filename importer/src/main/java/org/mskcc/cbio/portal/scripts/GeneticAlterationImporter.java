package org.mskcc.cbio.portal.scripts;

import java.util.*;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileSamples;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import static java.lang.String.format;

public class GeneticAlterationImporter {

    protected int geneticProfileId;
    protected List<Integer> orderedSampleList;
    private final Set<Long> importSetOfGenes = new HashSet<>();
    private final Set<Integer> importSetOfGeneticEntityIds = new HashSet<>();

    private final DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();

    protected GeneticAlterationImporter() {}
    public GeneticAlterationImporter(
        int geneticProfileId,
        List<Integer> orderedSampleList
    ) {
        this.geneticProfileId = geneticProfileId;
        this.orderedSampleList = orderedSampleList;
    }

    protected void storeOrderedSampleList() throws DaoException {
        int rowCount = DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, orderedSampleList);
        if (rowCount < 1) {
            throw new IllegalStateException("Failed to store the ordered sample list.");
        }
    }

    /**
     * Check that we have not already imported information regarding this gene.
     * This is an important check, because a GISTIC or RAE file may contain
     * multiple rows for the same gene, and we only want to import the first row.
     */
    public boolean store(
            String[] values,
            CanonicalGene gene,
            String geneSymbol
    ) throws DaoException {
        ensureNumberOfValuesIsCorrect(values.length);
        if (importSetOfGenes.add(gene.getEntrezGeneId())) {
            daoGeneticAlteration.addGeneticAlterations(geneticProfileId, gene.getEntrezGeneId(), values);
            return true;
        }
        String geneSymbolMessage = "";
        if (geneSymbol != null && !geneSymbol.equalsIgnoreCase(gene.getHugoGeneSymbolAllCaps())) {
            geneSymbolMessage = " (given as alias in your file as: " + geneSymbol + ")";
        }
        ProgressMonitor.logWarning(format(
            "Gene %s (%d)%s found to be duplicated in your file. Duplicated row will be ignored!",
            gene.getHugoGeneSymbolAllCaps(),
            gene.getEntrezGeneId(),
            geneSymbolMessage)
        );
        return false;
    }


    /**
     * Universal method that stores values for different genetic entities
     * @param geneticEntityId
     * @param values
     * @return true if entity has been stored, false - if entity already existed
     * @throws DaoException
     */
    public boolean store(
            int geneticEntityId,
            String[] values
    ) throws DaoException {
        ensureNumberOfValuesIsCorrect(values.length);
        if (importSetOfGeneticEntityIds.add(geneticEntityId)) {
            daoGeneticAlteration.addGeneticAlterationsForGeneticEntity(geneticProfileId, geneticEntityId, values);
            return true;
        }
        ProgressMonitor.logWarning("Data for genetic entity with id " + geneticEntityId + " already imported from file. Record will be skipped.");
        return false;
    }

    private void ensureNumberOfValuesIsCorrect(int valuesNumber) {
        if (valuesNumber != orderedSampleList.size()) {
            throw new IllegalArgumentException("There has to be " + orderedSampleList.size() + " values, but only " + valuesNumber+ " has passed.");
        }
    }


    public void initialize() {
        try {
            storeOrderedSampleList();
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }
    public void complete() throws DaoException { }
}
