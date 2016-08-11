/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.persistence.GeneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author jiaojiao
 */
@Repository
public class GeneMyBatisRepository implements GeneRepository{
    @Autowired
    GeneHugoMapper geneHugoMapper;
    @Override
    public Gene getGeneByHugoSymbol(String hugoSymbol){
        return geneHugoMapper.getGeneByHugoSymbol(hugoSymbol);
    }
    
    @Override
    public List<Gene> getGeneListByHugoSymbols(List<String> hugoSymbols){
        return geneHugoMapper.getGeneListByHugoSymbols(hugoSymbols);
    }
   
}
