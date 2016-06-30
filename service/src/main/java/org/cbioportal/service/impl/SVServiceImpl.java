/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.service.impl;

import org.cbioportal.model.SV;
import org.cbioportal.persistence.SVRepository;
import org.cbioportal.service.SVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SVServiceImpl implements SVService{
    
    @Autowired
    SVRepository svRepository;
    
    @Override
    public List<SV> getSVs(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols, List<String> sampleStableIds){
        return svRepository.getSVs(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
    }
}
