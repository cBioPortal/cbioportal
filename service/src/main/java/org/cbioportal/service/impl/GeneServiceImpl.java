/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.service.impl;

import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jiaojiao
 */
@Service
public class GeneServiceImpl implements GeneService{
    @Autowired
    private GeneRepository geneRepository;
    
    @Override
    public List<Gene> getGeneListByHugoSymbols(List<String> hugoSymbols){
        return geneRepository.getGeneListByHugoSymbols(hugoSymbols);
    }
}
