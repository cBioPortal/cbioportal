/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.service.ApiService;
import org.mskcc.cbio.portal.model.DBCancerType;
import org.mskcc.cbio.portal.model.DBPatientList;
import org.mskcc.cbio.portal.model.DBClinicalField;
import org.mskcc.cbio.portal.model.DBClinicalPatientData;
import org.mskcc.cbio.portal.model.DBClinicalSampleData;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.model.DBPatient;
import org.mskcc.cbio.portal.model.DBProfileData;
import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.model.DBStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abeshoua
 */
@Controller
public class ApiController {
    @Autowired
    private ApiService service;

    /* DISPATCHERS */
    @Transactional
    @RequestMapping(value = "/cancertypes", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBCancerType> getCancerTypes(@RequestParam(required = false) List<String> cancer_type_ids) {
        if (cancer_type_ids == null) {
		return service.getCancerTypes();
        } else {
		return service.getCancerTypes(cancer_type_ids);
        }
    }

    @Transactional
    @RequestMapping(value = "/clinicaldata/samples", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalSampleData> getSampleClinicalData(@RequestParam(required = true) String study_id, @RequestParam(required = true) List<String> attribute_ids, @RequestParam(required = false) List<String> sample_ids) {
	    if (sample_ids == null) {
		    return service.getSampleClinicalData(study_id, attribute_ids);
	    } else {
		    return service.getSampleClinicalData(study_id, attribute_ids, sample_ids);
	    }
    }
    @Transactional
    @RequestMapping(value = "/clinicaldata/patients", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalPatientData> getPatientClinicalData(@RequestParam(required = true) String study_id, @RequestParam(required = true) List<String> attribute_ids, @RequestParam(required = false) List<String> patient_ids) {
	    if (patient_ids == null) {
		    return service.getPatientClinicalData(study_id, attribute_ids);
	    } else {
		    return service.getPatientClinicalData(study_id, attribute_ids, patient_ids);
	    }
    }
    
    @Transactional
    @RequestMapping(value = "/clinicalattributes/samples", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalField> getSampleClinicalAttributes(@RequestParam(required = false) String study_id, @RequestParam(required = false) List<String> sample_ids) {
	    if (sample_ids == null && study_id == null) {
		    return service.getSampleClinicalAttributes();
	    } else if (study_id != null && sample_ids != null) {
		    return service.getSampleClinicalAttributes(study_id, sample_ids);
	    } else if (sample_ids == null) {
		    return service.getSampleClinicalAttributes(study_id);
	    } else {
		    return new ArrayList<>();
	    }
    }
    @Transactional
    @RequestMapping(value = "/clinicalattributes/patients", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalField> getPatientClinicalAttributes(@RequestParam(required = false) String study_id, @RequestParam(required = false) List<String> patient_ids) {
	    if (patient_ids == null && study_id == null) {
		    return service.getPatientClinicalAttributes();
	    } else if (study_id != null && patient_ids != null) {
		    return service.getPatientClinicalAttributes(study_id, patient_ids);
	    } else if (patient_ids == null) {
		    return service.getPatientClinicalAttributes(study_id);
	    } else {
		    return new ArrayList<>();
	    }
    }
    
    @Transactional
    @RequestMapping(value = "/genes", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBGene> getGenes(@RequestParam(required = false) List<String> hugo_gene_symbols) {
	    if (hugo_gene_symbols == null) {
		    return service.getGenes();
	    } else {
		    return service.getGenes(hugo_gene_symbols);
	    }
    }
    
    @Transactional
    @RequestMapping(value = "/geneticprofiles", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBGeneticProfile> getGeneticProfiles(@RequestParam(required = false) String study_id, @RequestParam(required = false) List<String> genetic_profile_ids) {
	    if (study_id != null) {
		    return service.getGeneticProfiles(study_id);
	    } else if (genetic_profile_ids != null) {
		    return service.getGeneticProfiles(genetic_profile_ids);
	    } else {
		    return service.getGeneticProfiles();
	    }
    }
    
    @Transactional
    @RequestMapping(value = "/patientlists", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBPatientList> getPatientLists(@RequestParam(required = false) String study_id, @RequestParam(required = false) List<String> patient_list_ids) {
		if (study_id != null) {
		    return service.getPatientLists(study_id);
	    } else if (patient_list_ids != null) {
		    return service.getPatientLists(patient_list_ids);
	    } else {
		    return service.getPatientLists();
	    }
    }
    
    @Transactional
    @RequestMapping(value = "/patients", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBPatient> getPatients(@RequestParam(required = true) String study_id, @RequestParam(required = false) List<String> patient_ids,
							@RequestParam(required = false) List<String> sample_ids) {
	    if (patient_ids != null) {
		    return service.getPatientsByPatient(study_id, patient_ids);
	    } else if (sample_ids != null) {
		    return service.getPatientsBySample(study_id, sample_ids);
	    } else {
		    return service.getPatients(study_id);
	    }
    }
    
    @Transactional
    @RequestMapping(value = "/geneticprofiledata", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBProfileData> getGeneticProfileData(@RequestParam(required = true) List<String> genetic_profile_ids, @RequestParam(required = true) List<String> genes, @RequestParam(required = false) List<String> sample_ids) {
	    if (sample_ids == null) {
		    return service.getGeneticProfileData(genetic_profile_ids, genes);
	    } else {
		    return service.getGeneticProfileData(genetic_profile_ids, genes, sample_ids);
	    }
    }
    
    @Transactional
    @RequestMapping(value = "/samples", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBSample> getSamples(@RequestParam(required = true) String study_id, @RequestParam(required = false) List<String> sample_ids, @RequestParam(required = false) List<String> patient_ids) {
	    if (sample_ids != null) {
		    return service.getSamplesBySample(study_id, sample_ids);
	    } else if (patient_ids != null) {
                    return service.getSamplesByPatient(study_id, patient_ids);
	    } else {
                return service.getSamples(study_id);
            }
            
    }
     
    @Transactional
    @RequestMapping(value = "/studies", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBStudy> getStudies(@RequestParam(required = false) List<String> study_ids) {
	    if (study_ids == null) {
		    return service.getStudies();
	    } else {
		    return service.getStudies(study_ids);
	    }
    }
}