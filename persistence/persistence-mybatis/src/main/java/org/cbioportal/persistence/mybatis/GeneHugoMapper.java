/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.Gene;

/**
 *
 * @author jiaojiao
 */
public interface GeneHugoMapper {
    Gene getGeneByHugoSymbol(String hugoSymbol);
    List<Gene> getGeneListByHugoSymbols(List<String> hugoSymbols);
}
