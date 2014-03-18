/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.mut_diagram.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoUniProtIdMapping;
import org.mskcc.cbio.portal.model.CanonicalGene;
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
    public List<String> mapFromHugoToUniprotAccessions(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol, "hugoGeneSymbol must not be null");
        try {
            CanonicalGene gene = geneDao.getGene(hugoGeneSymbol);
            if (gene == null) {
                logger.warn("could not find gene with hugoGeneSymbol " + hugoGeneSymbol + " for uniprot id mapping");
            }
            else {
                return DaoUniProtIdMapping.mapFromEntrezGeneIdToUniprotAccession((int) gene.getEntrezGeneId()); // is entrez gene id really a long?
            }
        }
        catch (DaoException e) {
            logger.error("could not find uniprot id mapping for hugoGeneSymbol " + hugoGeneSymbol, e);
        }
        return Collections.emptyList();
    }
    
    public String  mapFromUniprotIdToUniprotAccession(String uniprotId) {
        checkNotNull(uniprotId, "uniprotId must not be null");
        try {
            return DaoUniProtIdMapping.mapFromUniprotAccessionToUniprotId(uniprotId);
        }
        catch (DaoException e) {
            logger.error("could not find uniprot id mapping for " + uniprotId, e);
        }
        return null;
    }
}