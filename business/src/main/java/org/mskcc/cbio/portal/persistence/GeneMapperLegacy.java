/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBGene;

/**
 *
 * @author abeshoua
 */
public interface GeneMapperLegacy {
    List<DBGene> getGenesByEntrez(
        @Param("entrez_gene_ids") List<Long> entrez_gene_ids
    );
    List<DBGene> getGenesByHugo(
        @Param("hugo_gene_symbols") List<String> hugo_gene_symbols
    );
    List<DBGene> getAllGenes();
}
