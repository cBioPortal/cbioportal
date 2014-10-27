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
import org.mskcc.cbio.portal.model.DBPatientList;
import org.mskcc.cbio.portal.model.DBClinicalData;
import org.mskcc.cbio.portal.model.DBProfileData;
import org.mskcc.cbio.portal.service.PatientListService;
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
    private PatientListService patientListService;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private ProfileDataService profileDataService;
    
    private List<Integer> parseInts(List<String> list) throws NumberFormatException {
        // Util
        List<Integer> ret = new ArrayList<>();
        for (String s : list) {
            ret.add(Integer.parseInt(s, 10));
        }
        return ret;
    }
    
    private List<DBClinicalData> dispatchClinicalHelper(List<String> study_ids, List<String> ids, boolean isSample) throws Exception {
        if (study_ids == null && ids == null) {
            throw new Exception("Not enough specified");//TODO: better error messages
        } else if (ids == null) {
            // Multiple study ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return clinicalDataService.byInternalStudyId(istudy_ids, isSample);
            } catch (NumberFormatException e) {
                return clinicalDataService.byStableStudyId(study_ids, isSample);
            }
        } else if (study_ids == null) {
            // Multiple internal ids
            try {
                List<Integer> iids = parseInts(ids);
                return (isSample? clinicalDataService.byInternalSampleId(iids) : clinicalDataService.byInternalPatientId(iids));
            } catch (NumberFormatException e) {
                throw new Exception("Must specify study id to use stable ids");
            }
        } else {
            // Single study id and multiple stable ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return (isSample? clinicalDataService.byStableSampleId(istudy_ids.get(0), ids): clinicalDataService.byStablePatientId(istudy_ids.get(0), ids));
            } catch (NumberFormatException e) {
                return (isSample? clinicalDataService.byStableSampleId(study_ids.get(0), ids): clinicalDataService.byStablePatientId(study_ids.get(0), ids));
            }
        }
    }
    
    @RequestMapping("/clinical/sample")
    public @ResponseBody List<DBClinicalData> dispatchClinicalSample(@RequestParam(required = false) List<String> study_ids,
                                                                      @RequestParam(required = false) List<String> sample_ids) throws Exception {
        return dispatchClinicalHelper(study_ids, sample_ids, true);
    }
    @RequestMapping("/clinical/patient")
    public @ResponseBody List<DBClinicalData> dispatchClinicalPatient(@RequestParam(required = false) List<String> study_ids,
                                                                      @RequestParam(required = false) List<String> patient_ids) throws Exception {
        return dispatchClinicalHelper(study_ids, patient_ids, false);
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
                List<DBPatientList> caselists = patientListService.byInternalId(case_list_ids);
                for (DBPatientList cl: caselists) {
                    caseSet.addAll(cl.internal_patient_ids);
                }
            }
            List<Integer> caseList = new ArrayList<>();
            caseList.addAll(caseSet);
            return profileDataService.byInternalId(profile_ids, genes, caseList);
        }
    }
}
