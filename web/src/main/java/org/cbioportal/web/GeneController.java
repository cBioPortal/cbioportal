/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.web;

import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author jiaojiao
 */
@RestController
public class GeneController {
    @Autowired
    private GeneService geneService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/geneByHugoSymbol")
    public Gene getGeneByHugoSymbol(@RequestParam(required = true) String hugoSymbol){
        return geneService.getGeneByHugoSymbol(hugoSymbol);
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/geneListByHugoSymbols")
    public List<Gene> getGeneListByHugoSymbols(@RequestParam(required = true) List<String> hugoSymbols){
        return geneService.getGeneListByHugoSymbols(hugoSymbols);
    }
}