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
@RequestMapping("/meta")
public class MetaController {
    
    @RequestMapping("/cancertypes")
    public @ResponseBody ArrayList<TypeOfCancerJSON> dispatchCancerTypes(@RequestParam(required = false) String id) {
        try {
            if (id == null) {
                return CancerTypesController.getCancerTypes();
            } else {
                return CancerTypesController.getCancerType(id);
            }
        } catch (DaoException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            // TODO: fail verbosely
            return new ArrayList<>();
        }
    }
    
    @RequestMapping("/studies")
    public @ResponseBody ArrayList<CancerStudyJSON> dispatchStudies(@RequestParam(required = false) String id, @RequestParam(required = false) Integer internalId) {
        try {
            if (id != null && internalId != null) {
                return new ArrayList<>(); // TODO: error report: you can only pass either a stable id or an internal id
            } else if (id != null) {
                return StudiesController.getStudy(id);
            } else if (internalId != null) {
                return StudiesController.getStudy(internalId);
            } else {
                return StudiesController.getStudies();
            }
        } catch (DaoException e) {
            return new ArrayList<>();
        } catch (Exception e) {
            // TODO: fail verbosely
            return new ArrayList<>();
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
    
    @RequestMapping("/caselists")
    public @ResponseBody ArrayList<CaseListJSON> dispatchCaseLists(@RequestParam(required = false) String id,
                                                                   @RequestParam(required = false) Integer internal_id,
                                                                   @RequestParam(required = false) Integer internal_study_id) {
        try {
            if (internal_study_id == null && id == null && internal_id == null) {
                return new ArrayList<>(); //TODO: error report: you have to have one of these be non-null
            } else if (id != null) {
                return CaseListsController.getCaseList(id);
            } else if (internal_id != null) {
                return CaseListsController.getCaseList(internal_id);
            } else {
                // internal_study_id != null
                return CaseListsController.getCaseLists(internal_study_id);
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
