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
import org.mskcc.cbio.portal.service.CaseListService;
import org.mskcc.cbio.portal.service.ClinicalDataService;
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
    
    @Transactional
    @RequestMapping("/clinical")
    public @ResponseBody List<DBClinicalData> dispatchClinical(@RequestParam(required = false) List<Integer> study_ids,
                                                  @RequestParam(required = false) List<Integer> case_list_ids,
                                                  @RequestParam(required = false) List<Integer> case_ids) 
                                                  throws Exception {
        if (case_list_ids == null && case_ids == null) {
            if (study_ids == null) {
                throw new Exception();
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
    
   /* @RequestMapping("/profiles")
    public @ResponseBody ArrayList<ProfileDataJSON> dispatchProfiles(@RequestParam(required = false) List<String> ids,
                                                                     @RequestParam(required = false) List<Integer> internal_ids,
                                                                     @RequestParam(required = false) Integer internal_case_list_id,
                                                                     @RequestParam(required = false) List<Integer> internal_case_ids,
                                                                     @RequestParam(required = false) List<String> genes) {
        try {
            if (genes == null || (ids == null && internal_ids == null) || (internal_case_ids == null && internal_case_list_id == null)) {
                return new ArrayList<>(); // TODO: error report: one of each conjunctive group must be non-null
            }
            
            if (ids != null) {
                if (internal_case_ids != null) {
                    return ProfilesController.getProfileDataByIds(ids, internal_case_ids, genes);
                } else {
                    // internal_case_list_id != null
                    return ProfilesController.getProfileDataByIds(ids, internal_case_list_id, genes);
                }
            } else {
                // internal_ids != null
                if (internal_case_ids != null) {
                    return ProfilesController.getProfileDataByInternalIds(internal_ids, internal_case_ids, genes);
                } else {
                    // internal_case_list_id != null
                    return ProfilesController.getProfileDataByInternalIds(internal_ids, internal_case_list_id, genes);
                }
            }
        } catch (DaoException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }*/
    
}
