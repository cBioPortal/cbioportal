/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.web;

import org.cbioportal.model.SV;
import org.cbioportal.service.SVService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 *
 * @author jake
 */
@RestController
public class SVController {
    
    @Autowired
    private SVService svService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/sv")
    public List<SV> getSV(@RequestParam List<Integer> geneticProfileStableIds,
                                      @RequestParam List<String> hugoGeneSymbols,
                                      @RequestParam(required = false) List<String> sampleStableIds){
        
        return svService.getSVs(geneticProfileIds, hugoGeneSymbols, sampleStableIds);
    
    }
    
    
}
