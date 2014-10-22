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
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.model.DBStudy;
import org.mskcc.cbio.portal.service.CancerTypeService;
import org.mskcc.cbio.portal.service.CaseListService;
import org.mskcc.cbio.portal.service.GeneService;
import org.mskcc.cbio.portal.service.GeneticProfileService;
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
    /* SERVICES */
    @Autowired
    private GeneService geneService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private CaseListService caseListService;
    @Autowired
    private CancerTypeService cancerTypeService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    /*---*/

    /* UTILS */
    private List<Long> parseLongs(List<String> list) throws NumberFormatException {
        // Util
        List<Long> ret = new ArrayList<>();
        for (String s : list) {
            ret.add(Long.parseLong(s, 10));
        }
        return ret;
    }

    private List<Integer> parseInts(List<String> list) throws NumberFormatException {
        // Util
        List<Integer> ret = new ArrayList<>();
        for (String s : list) {
            ret.add(Integer.parseInt(s, 10));
        }
        return ret;
    }
    /*---*/

    /* DISPATCHERS */
    @Transactional
    @RequestMapping("/cancertypes")
    public @ResponseBody
    List<DBCancerType> dispatchCancerTypes(@RequestParam(required = false) List<String> ids) {
        if (ids == null) {
            return cancerTypeService.getAll();
        } else {
            return cancerTypeService.byId(ids);
        }
    }

    @Transactional
    @RequestMapping("/genes")
    public @ResponseBody
    List<DBGene> dispatchGenes(@RequestParam(required = false) List<String> ids) {
        if (ids == null) {
            return geneService.getAll();
        }
        try {
            List<Long> entrez = parseLongs(ids);
            return geneService.byEntrezGeneId(entrez);
        } catch (NumberFormatException e) {
            return geneService.byHugoGeneSymbol(ids);
        }
    }

    @Transactional
    @RequestMapping("/studies")
    public @ResponseBody
    List<DBStudy> dispatchStudies(@RequestParam(required = false) List<String> ids) throws Exception {
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
    public @ResponseBody
    List<DBCaseList> dispatchCaseLists(@RequestParam(required = false) List<String> case_list_ids,
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

    @Transactional
    @RequestMapping("/profiles")
    public @ResponseBody List<DBGeneticProfile> dispatchProfiles(@RequestParam(required = false) List<String> profile_ids,
                                                   @RequestParam(required = false) List<Integer> study_ids) {
        if (profile_ids == null && study_ids == null) {
            return geneticProfileService.getAll();
        } else if (profile_ids != null) {
            try {
                List<Integer> internals = parseInts(profile_ids);
                return geneticProfileService.byInternalId(internals);
            } catch (NumberFormatException e) {
                return geneticProfileService.byStableId(profile_ids);
            }
        } else {
            // study_ids != null
            return geneticProfileService.byInternalStudyId(study_ids);
        }
    }

    @RequestMapping("/clinical")
    public @ResponseBody
    ArrayList<ClinicalFieldJSON> dispatchClinical(@RequestParam(required = false) List<Integer> internal_study_ids,
                                                  @RequestParam(required = false) List<Integer> internal_case_list_ids,
                                                  @RequestParam(required = false) List<Integer> internal_case_ids) {
    }
            
            
            @RequestParam(required = false) Integer internal_study_id,
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
