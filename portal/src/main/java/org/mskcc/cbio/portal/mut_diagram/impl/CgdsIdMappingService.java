package org.mskcc.cbio.portal.mut_diagram.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoUniProtIdMapping;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.portal.mut_diagram.IdMappingService;

/**
 * Implementation of IdMappingService that reads from the CGDS data source.
 */
public final class CgdsIdMappingService implements IdMappingService {
    private static final Logger logger = Logger.getLogger(CgdsIdMappingService.class);
    private final DaoGeneOptimized geneDao;

    public CgdsIdMappingService(final DaoGeneOptimized geneDao) {
        this.geneDao = geneDao;
    }

    /** {@inheritDoc} */
    public List<String> getUniProtIds(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol, "hugoGeneSymbol must not be null");
        try {
            CanonicalGene gene = geneDao.getGene(hugoGeneSymbol);
            if (gene == null) {
                logger.warn("could not find gene with hugoGeneSymbol " + hugoGeneSymbol + " for uniprot id mapping");
            }
            else {
                return DaoUniProtIdMapping.getUniProtIds((int) gene.getEntrezGeneId()); // is entrez gene id really a long?
            }
        }
        catch (DaoException e) {
            logger.error("could not find uniprot id mapping for hugoGeneSymbol " + hugoGeneSymbol, e);
        }
        return Collections.emptyList();
    }
}