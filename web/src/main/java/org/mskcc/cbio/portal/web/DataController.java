/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoException;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
/**
 *
 * @author abeshoua
 */
@Controller
@RequestMapping("/data")
public class DataController {
    
    @RequestMapping("/clinical")
    public @ResponseBody ArrayList<ClinicalDataJSON> dispatchClinical(@RequestParam(required = false) Integer internal_study_id,
                                                                       @RequestParam(required = false) Integer internal_case_list_id,
                                                                       @RequestParam(required = false) List<Integer> internal_case_ids) {
        try {
            if (internal_study_id == null && internal_case_list_id == null && internal_case_ids == null) {
                return new ArrayList<>(); // TODO: error report: one of these must be non-null
            } else if (internal_case_list_id != null) {
                return ClinicalController.getClinicalDataByCaseList(internal_case_list_id);
            } else if (internal_case_ids != null) {
                return ClinicalController.getClinicalDataByCaseList(internal_case_ids);
            } else {
                // internal_study_id != null
                return ClinicalController.getClinicalDataByStudy(internal_study_id);
            }
        } catch (DaoException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            // TODO: fail verbosely
            return new ArrayList<>();
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
