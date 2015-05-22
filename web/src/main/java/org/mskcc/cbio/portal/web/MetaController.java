/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.model.DBCancerType;
import org.mskcc.cbio.portal.model.DBPatientList;
import org.mskcc.cbio.portal.model.DBClinicalField;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.model.DBGeneSet;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.model.DBPatient;
import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.model.DBStudy;
import org.mskcc.cbio.portal.service.CancerTypeService;
import org.mskcc.cbio.portal.service.PatientListService;
import org.mskcc.cbio.portal.service.ClinicalFieldService;
import org.mskcc.cbio.portal.service.GeneService;
import org.mskcc.cbio.portal.service.GeneSetService;
import org.mskcc.cbio.portal.service.GeneticProfileService;
import org.mskcc.cbio.portal.service.PatientService;
import org.mskcc.cbio.portal.service.SampleService;
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
    private PatientListService patientListService;
    @Autowired
    private CancerTypeService cancerTypeService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    @Autowired
    private ClinicalFieldService clinicalFieldService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GeneSetService geneSetService;
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
    @RequestMapping("/patients")
    public @ResponseBody List<DBPatient> dispatchPatients(@RequestParam(required = false) List<String> study_ids,
                                                          @RequestParam(required = false) List<String> patient_ids) throws Exception {
        if (study_ids == null && patient_ids == null) {
            throw new Exception("Too little specified"); // TODO better errors
        } else if (patient_ids == null) {
            // Multiple study ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return patientService.byInternalStudyId(istudy_ids);
            } catch (NumberFormatException e) {
                return patientService.byStableStudyId(study_ids);
            }
        } else if (study_ids == null) {
            // Multiple internal patient ids
            try {
                List<Integer> ipatient_ids = parseInts(patient_ids);
                return patientService.byInternalPatientId(ipatient_ids);
            } catch (NumberFormatException e) {
                throw new Exception("Must specify study id to use stable patient ids");
            }
        } else {
            // Single study and multiple stable patient ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return patientService.byStablePatientId(istudy_ids.get(0), patient_ids);
            } catch (NumberFormatException e) {
                return patientService.byStablePatientId(study_ids.get(0), patient_ids);
            }
        }
    }
    
    @Transactional
    @RequestMapping("/samples")
    public @ResponseBody List<DBSample> dispatchSamples(@RequestParam(required = false) List<String> study_ids,
                                                        @RequestParam(required = false) List<String> sample_ids) throws Exception {
        if (study_ids == null && sample_ids == null) {
            throw new Exception("Too little specified"); // TODO better errors
        } else if (sample_ids == null) {
            // Multiple study ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return sampleService.byInternalStudyId(istudy_ids);
            } catch (NumberFormatException e) {
                return sampleService.byStableStudyId(study_ids);
            }
        } else if (study_ids == null) {
            // Multiple internal sample ids
            try {
                List<Integer> isample_ids = parseInts(sample_ids);
                return sampleService.byInternalSampleId(isample_ids);
            } catch (NumberFormatException e) {
                throw new Exception("Must specify study id to use stable sample ids");
            }
        } else {
            // Single study and multiple stable patient ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return sampleService.byStableSampleId(istudy_ids.get(0), sample_ids);
            } catch (NumberFormatException e) {
                return sampleService.byStableSampleId(study_ids.get(0), sample_ids);
            }
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
    @RequestMapping("/patientlists")
    public @ResponseBody
    List<DBPatientList> dispatchPatientLists(@RequestParam(required = false) List<String> patient_list_ids,
            @RequestParam(required = false) List<Integer> study_ids,
            @RequestParam(required = false) Boolean omit_lists)
            throws Exception {
        if (omit_lists == null) {
            omit_lists = false;
        }
        if (patient_list_ids == null && study_ids == null) {
            return patientListService.getAll(omit_lists);
        } else if (patient_list_ids != null) {
            try {
                List<Integer> internals = parseInts(patient_list_ids);
                return patientListService.byInternalId(internals, omit_lists);
            } catch (NumberFormatException e) {
                return patientListService.byStableId(patient_list_ids, omit_lists);
            }
        } else {
            // study_ids != null
            return patientListService.byInternalStudyId(study_ids, omit_lists);
        }
    }
    
    @RequestMapping("/genesets")
    public @ResponseBody
    List<DBGeneSet> dispatchGeneSets(@RequestParam(required = false) List<Integer> ids,
                                    @RequestParam(required = false) Boolean omit_lists) {
        if (omit_lists == null) {
            omit_lists = false;
        }
        if (ids == null) {
            return geneSetService.getAll(omit_lists);
        } else {
            return geneSetService.byInternalId(ids, omit_lists);
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

    
    private List<DBClinicalField> dispatchClinicalHelper(List<String> study_ids, List<String> ids, boolean isSample) throws Exception {
        if (study_ids == null && ids == null) {
            return clinicalFieldService.getAll(isSample);
        } else if (ids == null) {
            // Multiple study ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return clinicalFieldService.byInternalStudyId(istudy_ids, isSample);
            } catch (NumberFormatException e) {
                return clinicalFieldService.byStableStudyId(study_ids, isSample);
            }
        } else if (study_ids == null) {
            // Multiple internal ids
            try {
                List<Integer> iids = parseInts(ids);
                return (isSample? clinicalFieldService.byInternalSampleId(iids) : clinicalFieldService.byInternalPatientId(iids));
            } catch (NumberFormatException e) {
                throw new Exception("Must specify study id to use stable ids");
            }
        } else {
            // Single study id and multiple stable ids
            try {
                List<Integer> istudy_ids = parseInts(study_ids);
                return (isSample? clinicalFieldService.byStableSampleId(istudy_ids.get(0), ids): clinicalFieldService.byStablePatientId(istudy_ids.get(0), ids));
            } catch (NumberFormatException e) {
                return (isSample? clinicalFieldService.byStableSampleId(study_ids.get(0), ids): clinicalFieldService.byStablePatientId(study_ids.get(0), ids));
            }
        }
    }
    
    @RequestMapping("/clinical/samples")
    public @ResponseBody List<DBClinicalField> dispatchClinicalSamples(@RequestParam(required = false) List<String> study_ids,
                                                                      @RequestParam(required = false) List<String> sample_ids) throws Exception {
        return dispatchClinicalHelper(study_ids, sample_ids, true);
    }
    @RequestMapping("/clinical/patients")
    public @ResponseBody List<DBClinicalField> dispatchClinicalPatients(@RequestParam(required = false) List<String> study_ids,
                                                                      @RequestParam(required = false) List<String> patient_ids) throws Exception {
        return dispatchClinicalHelper(study_ids, patient_ids, false);
    }
}
