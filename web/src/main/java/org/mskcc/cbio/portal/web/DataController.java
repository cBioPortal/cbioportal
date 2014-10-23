/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.portal.model.DBCaseList;
import org.mskcc.cbio.portal.model.DBClinicalData;
import org.mskcc.cbio.portal.model.DBProfileData;
import org.mskcc.cbio.portal.service.CaseListService;
import org.mskcc.cbio.portal.service.ClinicalDataService;
import org.mskcc.cbio.portal.service.ProfileDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author abeshoua
 */
@Controller
@RequestMapping("/data")
public class DataController {
    /* SERVICES */
    @Autowired
    private CaseListService caseListService;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private ProfileDataService profileDataService;
    
    @Transactional
    @RequestMapping("/clinical")
    public @ResponseBody List<DBClinicalData> dispatchClinical(@RequestParam(required = false) List<Integer> study_ids,
                                                  @RequestParam(required = false) List<Integer> case_list_ids,
                                                  @RequestParam(required = false) List<Integer> case_ids) 
                                                  throws Exception {
        if (case_list_ids == null && case_ids == null) {
            if (study_ids == null) {
                throw new Exception("Can't have case_list_ids, case_ids, and study_ids all unspecified");
            }
            // get all corresponding to study
            return clinicalDataService.byInternalStudyId(study_ids);
        } else {
            Set<Integer> caseSet = new HashSet<>();
            if(case_ids != null) {
                caseSet.addAll(case_ids);
            } else {
                List<DBCaseList> caselists = caseListService.byInternalId(case_list_ids);
                for (DBCaseList cl: caselists) {
                    caseSet.addAll(cl.internal_case_ids);
                }
            }
            List<Integer> caseList = new ArrayList<>();
            caseList.addAll(caseSet);
            return clinicalDataService.byInternalCaseId(caseList);
        }
    }
    
    @RequestMapping("/profiles")
    public @ResponseBody List<DBProfileData> dispatchProfiles(@RequestParam(required = false) List<Integer> case_ids,
                                                                     @RequestParam(required = false) List<Integer> case_list_ids,
                                                                     @RequestParam(required = false) List<Integer> genes,
                                                                     @RequestParam(required = false) List<Integer> profile_ids) 
                                                                     throws Exception {
        if (genes == null || profile_ids == null) {
            throw new Exception("Must specify genes and profile_ids");
        }
        if (case_ids == null && case_list_ids == null) {
            return profileDataService.byInternalId(profile_ids, genes);
        } else {
            Set<Integer> caseSet = new HashSet<>();
            if(case_ids != null) {
                caseSet.addAll(case_ids);
            } else {
                List<DBCaseList> caselists = caseListService.byInternalId(case_list_ids);
                for (DBCaseList cl: caselists) {
                    caseSet.addAll(cl.internal_case_ids);
                }
            }
            List<Integer> caseList = new ArrayList<>();
            caseList.addAll(caseSet);
            return profileDataService.byInternalId(profile_ids, genes, caseList);
        }
    }
}
