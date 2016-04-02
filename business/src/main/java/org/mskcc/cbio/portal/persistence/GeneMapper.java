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
public interface GeneMapper {
	List<DBGene> byEntrezGeneId(@Param("entrezIds") List<Long> entrezIds);
	List<DBGene> byHugoGeneSymbol(@Param("hugoGeneSymbols") List<String> hugoGeneSymbols);
	List<DBGene> getAll();
}
