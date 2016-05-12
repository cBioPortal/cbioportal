/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web.api;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.service.ApiService;
import org.mskcc.cbio.portal.model.DBCancerType;
import org.mskcc.cbio.portal.model.DBClinicalField;
import org.mskcc.cbio.portal.model.DBClinicalPatientData;
import org.mskcc.cbio.portal.model.DBClinicalSampleData;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.model.DBPatient;
import org.mskcc.cbio.portal.model.DBProfileData;
import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.model.DBSampleList;
import org.mskcc.cbio.portal.model.DBStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;

/**
 *
 * @author abeshoua
 */
@Controller
public class ApiController {
    @Autowired
    private ApiService service;

    /* DISPATCHERS */
    @ApiOperation(value = "Get cancer types with meta data",
            nickname = "getCancerTypes",
            notes = "")
    @Transactional
    @RequestMapping(value = "/cancertypes", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBCancerType> getCancerTypes(
            @ApiParam(required = false, value = "List of cancer type identifiers (example: cll,brca,coad). Unrecognized ids are silently ignored. Empty string returns all.")
            @RequestParam(required = false)
            List<String> cancer_type_ids) {
        if (cancer_type_ids == null) {
            return service.getCancerTypes();
        } else {
            return service.getCancerTypes(cancer_type_ids);
        }
    }

    @ApiOperation(value = "Get clinical data records, filtered by sample ids",
            nickname = "getSampleClinicalData",
            notes = "")
    @Transactional
    @RequestMapping(value = "/clinicaldata/samples", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalSampleData> getSampleClinicalData(
            @ApiParam(required = true, value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)")
            @RequestParam(required = true)
            String study_id,
            @ApiParam(required = true, value = "List of attribute ids, such as those returned by /api/clinicalattributes/samples. (example: SAMPLE_TYPE,IS_FFPE)")
            @RequestParam(required = true)
            List<String> attribute_ids,
            @ApiParam(required = false, value = "List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all.")
            @RequestParam(required = false)
            List<String> sample_ids) {
        if (sample_ids == null) {
            return service.getSampleClinicalData(study_id, attribute_ids);
        } else {
            return service.getSampleClinicalData(study_id, attribute_ids, sample_ids);
        }
    }

    @ApiOperation(value = "Get clinical data records filtered by patient ids",
            nickname = "getPatientClinicalData",
            notes = "")
    @Transactional
    @RequestMapping(value = "/clinicaldata/patients", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalPatientData> getPatientClinicalData(
            @ApiParam(required = true, value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)")
            @RequestParam(required = true)
            String study_id,
            @ApiParam(required = true, value = "List of attribute ids, such as those returned by /api/clinicalattributes/patients. (example: PATIENT_ID,DFS_STATUS)")
            @RequestParam(required = true)
            List<String> attribute_ids,
            @ApiParam(required = false, value = "List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all.")
            @RequestParam(required = false)
            List<String> patient_ids) {
        if (patient_ids == null) {
            return service.getPatientClinicalData(study_id, attribute_ids);
        } else {
            return service.getPatientClinicalData(study_id, attribute_ids, patient_ids);
        }
    }
    
    @ApiOperation(value = "Get clinical attribute identifiers, filtered by sample",
            nickname = "getSampleClinicalAttributes",
            notes = "")
    @Transactional
    @RequestMapping(value = "/clinicalattributes/samples", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalField> getSampleClinicalAttributes(
            @ApiParam(value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.")
            @RequestParam(required = false)
            String study_id,
            @ApiParam(required = false, value = "List of sample_ids. If provided, returned clinical attributes will be those which appear in any listed sample. Empty string returns clinical attributes across all samples.")
            @RequestParam(required = false)
            List<String> sample_ids) {
        if (sample_ids == null && study_id == null) {
            return service.getSampleClinicalAttributes();
        } else if (study_id != null && sample_ids != null) {
            return service.getSampleClinicalAttributes(study_id, sample_ids);
        } else if (sample_ids == null) {
            return service.getSampleClinicalAttributesByInternalIds(study_id, service.getSampleInternalIds(study_id));
        } else {
            return new ArrayList<>();
        }
    }

    @ApiOperation(value = "Get clinical attribute identifiers, filtered by patient",
            nickname = "getPatientClinicalAttributes",
            notes = "")
    @Transactional
    @RequestMapping(value = "/clinicalattributes/patients", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBClinicalField> getPatientClinicalAttributes(
            @ApiParam(required = false, value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies.")
            @RequestParam(required = false)
            String study_id,
            @ApiParam(required = false, value = "List of patient_ids. If provided, returned clinical attributes will be those which appear in any listed patient. Empty string returns clinical attributes across all patients.")
            @RequestParam(required = false)
            List<String> patient_ids) {
        if (patient_ids == null && study_id == null) {
            return service.getPatientClinicalAttributes();
        } else if (study_id != null && patient_ids != null) {
            return service.getPatientClinicalAttributes(study_id, patient_ids);
        } else if (patient_ids == null) {
            return service.getPatientClinicalAttributesByInternalIds(study_id, service.getPatientInternalIdsByStudy(study_id));
        } else {
            return new ArrayList<>();
        }
    }
    
    @ApiOperation(value = "Get gene meta data by hugo gene symbol lookup",
            nickname = "getGenes",
            notes = "")
    @Transactional
    @RequestMapping(value = "/genes", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBGene> getGenes(
            @ApiParam(required = false, value = "List of hugo gene symbols. Unrecognized genes are silently ignored. Empty string returns all genes.")
            @RequestParam(required = false)
            List<String> hugo_gene_symbols) {
        if (hugo_gene_symbols == null) {
            return service.getGenes();
        } else {
            return service.getGenes(hugo_gene_symbols);
        }
    }
    
    @ApiOperation(value = "Get list of genetic profile identifiers by study",
            nickname = "getGeneticProfiles",
            notes = "")
    @Transactional
    @RequestMapping(value = "/geneticprofiles", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBGeneticProfile> getGeneticProfiles(
            @ApiParam(required = false, value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by genetic profile ids (across all studies).")
            @RequestParam(required = false)
            String study_id,
            @ApiParam(required = false, value = "List of genetic_profile_ids. (example: brca_tcga_pub_mutations). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.")
            @RequestParam(required = false)
            List<String> genetic_profile_ids) {
        if (study_id != null) {
            return service.getGeneticProfiles(study_id);
        } else if (genetic_profile_ids != null) {
            return service.getGeneticProfiles(genetic_profile_ids);
        } else {
            return service.getGeneticProfiles();
        }
    }
    
    @ApiOperation(value = "Get list of sample lists (list name and sample id list) by study",
            nickname = "getSampleLists",
            notes = "")
    @Transactional
    @RequestMapping(value = "/samplelists", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBSampleList> getSampleLists(
            @ApiParam(required = false, value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by sample list ids (across all studies).")
            @RequestParam(required = false)
            String study_id,
            @ApiParam(required = false, value = "List of sample list ids. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored.")
            @RequestParam(required = false)
            List<String> sample_list_ids) {
        if (study_id != null) {
            return service.getSampleLists(study_id);
        } else if (sample_list_ids != null) {
            return service.getSampleLists(sample_list_ids);
        } else {
            return service.getSampleLists();
        }
    }
    
    @ApiOperation(value = "Get patient id list by study or by sample id",
            nickname = "getPatients",
            notes = "")
    @Transactional
    @RequestMapping(value = "/patients", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBPatient> getPatients(
            @ApiParam(required = true, value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)")
            @RequestParam(required = true)
            String study_id,
            @ApiParam(required = false, value = "List of patient ids such as those returned by /api/patients. Empty string returns all. Must be empty to query by sample ids.")
            @RequestParam(required = false)
            List<String> patient_ids,
            @ApiParam(required = false, value = "List of sample identifiers. Empty string returns all. If patient_ids argument was provided, this argument will be ignored.")
            @RequestParam(required = false)
            List<String> sample_ids) {
        if (patient_ids != null) {
            return service.getPatientsByPatient(study_id, patient_ids);
        } else if (sample_ids != null) {
            return service.getPatientsBySample(study_id, sample_ids);
        } else {
            return service.getPatients(study_id);
        }
    }
    
    @ApiOperation(value = "Get genetic profile data across samples for given genes, and filtered by sample id or sample list id",
            nickname = "getGeneticProfileData",
            notes = "")
    @Transactional
    @RequestMapping(value = "/geneticprofiledata", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBProfileData> getGeneticProfileData(
            @ApiParam(required = true, value = "List of genetic_profile_ids such as those returned by /api/geneticprofiles. (example: brca_tcga_pub_mutations). Unrecognized genetic profile ids are silently ignored. Profile data is only returned for matching ids.")
            @RequestParam(required = true)
            List<String> genetic_profile_ids,
            @ApiParam(required = true, value = "List of hugo gene symbols. (example: AKT1,CASP8,TGFBR1) Unrecognized gene ids are silently ignored. Profile data is only returned for matching genes.")
            @RequestParam(required = true)
            List<String> genes,
            @ApiParam(required = false, value = "List of sample identifiers such as those returned by /api/samples. Empty string returns all. Must be empty to query by sample list ids.")
            @RequestParam(required = false)
            List<String> sample_ids,
            @ApiParam(required = false, value = "A single sample list ids such as those returned by /api/samplelists. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all. If sample_ids argument was provided, this argument will be ignored.")
            @RequestParam(required = false)
            String sample_list_id) {
        if (sample_ids == null && sample_list_id == null) {
            return service.getGeneticProfileData(genetic_profile_ids, genes);
            } else if (sample_ids != null) {
                    return service.getGeneticProfileDataBySample(genetic_profile_ids, genes, sample_ids);
            } else {
                    return service.getGeneticProfileDataBySampleList(genetic_profile_ids, genes, sample_list_id);
            }
    }
    
    @ApiOperation(value = "Get list of samples ids with meta data by study, filtered by sample ids or patient ids",
            nickname = "getSamples",
            notes = "")
    @Transactional
    @RequestMapping(value = "/samples", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBSample> getSamples(
            @ApiParam(required = true, value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)")
            @RequestParam(required = true)
            String study_id,
            @ApiParam(required = false, value = "List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all. Must be empty to query by patient_ids.")
            @RequestParam(required = false)
            List<String> sample_ids,
            @ApiParam(required = false, value = "List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all. If sample_ids argument was provided, this arument will be ignored.")
            @RequestParam(required = false)
            List<String> patient_ids) {
        if (sample_ids != null) {
            return service.getSamplesBySample(study_id, sample_ids);
        } else if (patient_ids != null) {
                    return service.getSamplesByPatient(study_id, patient_ids);
        } else {
                return service.getSamples(study_id);
            }
            
    }
     
    @ApiOperation(value = "Get studies",
            nickname = "getStudies",
            notes = "")
    @Transactional
    @RequestMapping(value = "/studies", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody List<DBStudy> getStudies(
            @ApiParam(required = false, value = "List of study_ids. Unrecognized ids are silently ignored. Empty string returns all.")
            @RequestParam(required = false)
            List<String> study_ids) {
        if (study_ids == null) {
            return service.getStudies();
        } else {
            return service.getStudies(study_ids);
        }
    }
}
