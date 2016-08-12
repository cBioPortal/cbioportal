/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Gene;

/**
 *
 * @author jiaojiao
 */
public interface GeneHugoMapper {
    Gene getGeneByHugoSymbol(@Param("hugoSymbol") String hugoSymbol);
    List<Gene> getGeneListByHugoSymbols(@Param("hugoSymbols") List<String> hugoSymbols);
}
