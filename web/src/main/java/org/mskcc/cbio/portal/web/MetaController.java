/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.DBCancerType;
import org.mskcc.cbio.portal.model.DBCaseList;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.model.DBStudy;
import org.mskcc.cbio.portal.persistence.CancerTypeMapper;
import org.mskcc.cbio.portal.persistence.CaseListMapper;
import org.mskcc.cbio.portal.persistence.StudyMapper;
import org.mskcc.cbio.portal.service.CancerTypeService;
import org.mskcc.cbio.portal.service.CaseListService;
import org.mskcc.cbio.portal.service.GeneService;
import org.mskcc.cbio.portal.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author abeshoua
 */
@Controller
@RequestMapping("/meta")
public class MetaController {
    @Autowired
    private StudyMapper studyMapper;
    @Autowired
    private CaseListMapper caseListMapper;
    @Autowired
    private CancerTypeMapper cancerTypeMapper;
    
    @Autowired
    private GeneService geneService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private CaseListService caseListService;
    @Autowired
    private CancerTypeService cancerTypeService;
    
    
    private List<Long> parseLongs(List<String> list) throws NumberFormatException {
        // Util
        List<Long> ret = new ArrayList<>();
        for(String s: list) {
            ret.add(Long.parseLong(s, 10));
        }
        return ret;
    }
    private List<Integer> parseInts(List<String> list) throws NumberFormatException {
        // Util
        List<Integer> ret = new ArrayList<>();
        for(String s: list) {
            ret.add(Integer.parseInt(s, 10));
        }
        return ret;
    }
    
    
    @Transactional
    @RequestMapping("/cancertypes")
    public @ResponseBody List<DBCancerType> dispatchCancerTypes(@RequestParam(required = false) List<String> ids) {
        if (ids == null) {
            return cancerTypeService.getAll();
        } else {
            return cancerTypeService.byId(ids);
        }
    }
    @Transactional
    @RequestMapping("/genes")
    public @ResponseBody List<DBGene> dispatchGenes(@RequestParam(required = true) List<String> ids) {
        try {
            List<Long> entrez = parseLongs(ids);
            return geneService.byEntrezGeneId(entrez);
        } catch (NumberFormatException e) {
            return geneService.byHugoGeneSymbol(ids);
        }
    }
    
    @Transactional
    @RequestMapping("/studies")
    public @ResponseBody List<DBStudy> dispatchStudies(@RequestParam(required = false) List<String> ids) throws Exception {
        if (ids == null) {
            return studyService.getAll();
        }
        try {
            List<Integer> internals = parseInts(ids);
            return studyService.byInternalId(internals);
        } catch (NumberFormatException e) {
            return studyService.byStableId(ids);
        }
    }
    @Transactional
    @RequestMapping("/caselists")
    public @ResponseBody List<DBCaseList> dispatchCaseLists(@RequestParam(required = false) List<String> case_list_ids,
                                                                   @RequestParam(required = false) List<Integer> study_ids) 
                                                                   throws Exception {
        if (case_list_ids == null && study_ids == null) {
            return caseListService.getAll();
        } else if (case_list_ids != null) {
            try {
                List<Integer> internals = parseInts(case_list_ids);
                return caseListService.byInternalId(internals);
            } catch (NumberFormatException e) {
                return caseListService.byStableId(case_list_ids);
            }
        } else {
            // study_ids != null
            return caseListService.byInternalStudyId(study_ids);
        }
    }
   
    
    @RequestMapping("/profiles")
    public @ResponseBody ArrayList<GeneticProfileJSON> dispatchProfiles(@RequestParam(required = false) String id, 
                                                                        @RequestParam(required = false) Integer internal_id,
                                                                        @RequestParam(required = false) Integer internal_study_id) {
        try {
            if (internal_study_id == null && id == null && internal_id == null) {
                return new ArrayList<>(); //TODO: error report: you have to have one of these be non-null
            } else if (id != null) {
                return ProfilesController.getProfile(id);
            } else if (internal_id != null) {
                return ProfilesController.getProfile(internal_id);
            } else {
                // internal_study_id != null
                return ProfilesController.getProfiles(internal_study_id);
            }
        } catch (DaoException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            // TODO: fail verbosely
            return new ArrayList<>();
        }
    }
    
    
    @RequestMapping("/clinical")
    public @ResponseBody ArrayList<ClinicalFieldJSON> dispatchClinical(@RequestParam(required = false) Integer internal_study_id,
                                                                       @RequestParam(required = false) Integer internal_case_list_id,
                                                                       @RequestParam(required = false) List<Integer> internal_case_ids) {
        try {
            if (internal_study_id == null && internal_case_list_id == null && internal_case_ids == null) {
                return new ArrayList<>(); // TODO: error report: one of these must be non-null
            } else if (internal_case_list_id != null) {
                return ClinicalController.getClinicalFieldsByCaseList(internal_case_list_id);
            } else if (internal_case_ids != null) {
                return ClinicalController.getClinicalFieldsByCaseList(internal_case_ids);
            } else {
                // internal_study_id != null
                return ClinicalController.getClinicalFieldsByStudy(internal_study_id);
            }
        } catch (DaoException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            // TODO: fail verbosely
            return new ArrayList<>();
        }
    }
    
}
