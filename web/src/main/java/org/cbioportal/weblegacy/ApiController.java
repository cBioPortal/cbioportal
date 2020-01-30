package org.cbioportal.weblegacy;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.mskcc.cbio.portal.model.CosmicCount;
import org.mskcc.cbio.portal.model.DBCancerType;
import org.mskcc.cbio.portal.model.DBClinicalField;
import org.mskcc.cbio.portal.model.DBClinicalPatientData;
import org.mskcc.cbio.portal.model.DBClinicalSampleData;
import org.mskcc.cbio.portal.model.DBGene;
import org.mskcc.cbio.portal.model.DBGeneAlias;
import org.mskcc.cbio.portal.model.DBGeneticProfile;
import org.mskcc.cbio.portal.model.DBPatient;
import org.mskcc.cbio.portal.model.DBProfileData;
import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.model.DBSampleList;
import org.mskcc.cbio.portal.model.DBStudy;
import org.mskcc.cbio.portal.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author abeshoua
 */
@Controller
public class ApiController {
    @Autowired
    private ApiService service;

    /* DISPATCHERS */
    @ApiOperation(
        value = "Get cancer types with meta data",
        nickname = "getCancerTypes",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/cancertypes",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBCancerType> getCancerTypes(
        @ApiParam(
            required = false,
            value = "List of cancer type identifiers (example: cll,brca,coad). Unrecognized ids are silently ignored. Empty string returns all."
        ) @RequestParam(required = false) List<String> cancer_type_ids
    ) {
        if (cancer_type_ids == null) {
            return service.getCancerTypes();
        } else {
            return service.getCancerTypes(cancer_type_ids);
        }
    }

    @ApiOperation(
        value = "Get COSMIC counts for given keywords.",
        nickname = "getCOSMICCounts",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/cosmic_count",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<CosmicCount> getCosmicCounts(
        @ApiParam(
            required = true,
            value = "COSMIC event keywords, the keyword in a mutation object"
        ) @RequestParam(required = true) List<String> keywords
    ) {
        return service.getCOSMICCountsByKeywords(keywords);
    }

    @ApiOperation(
        value = "Get mutation count for certain gene. If per_study is true will return count for each study, if false will return the total count. User can specify specifc study set to look for.",
        nickname = "getMutationCount",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/mutation_count",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<Map<String, String>> getMutationsCounts(
        HttpServletRequest request,
        @ApiParam(
            required = true,
            value = "\"count\" or \"frequency\""
        ) @RequestParam(required = true) String type,
        @RequestParam(required = true) Boolean per_study,
        @RequestParam(required = false) List<String> studyId,
        @RequestParam(required = true) List<String> gene,
        @RequestParam(required = false) List<Integer> start,
        @RequestParam(required = false) List<Integer> end,
        @RequestParam(required = false) List<String> echo
    ) {
        Enumeration<String> parameterNames = request.getParameterNames();
        String[] fixedInput = {
            "type",
            "per_study",
            "gene",
            "start",
            "end",
            "echo"
        };
        Map<String, String[]> customizedAttrs = new HashMap<String, String[]>();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            if (!Arrays.asList(fixedInput).contains(paramName)) {
                String[] paramValues = request.getParameterValues(paramName);
                customizedAttrs.put(paramName, paramValues[0].split(","));
            }
        }
        return service.getMutationsCounts(
            customizedAttrs,
            type,
            per_study,
            studyId,
            gene,
            start,
            end,
            echo
        );
    }

    @ApiOperation(
        value = "Get clinical data records, filtered by sample ids",
        nickname = "getSampleClinicalData",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/clinicaldata/samples",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBClinicalSampleData> getSampleClinicalData(
        @ApiParam(
            required = true,
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)"
        ) @RequestParam(required = true) String study_id,
        @ApiParam(
            required = true,
            value = "List of attribute ids, such as those returned by /api/clinicalattributes/samples. (example: SAMPLE_TYPE,IS_FFPE)"
        ) @RequestParam(required = true) List<String> attribute_ids,
        @ApiParam(
            required = false,
            value = "List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all."
        ) @RequestParam(required = false) List<String> sample_ids
    ) {
        if (sample_ids == null) {
            return service.getSampleClinicalData(study_id, attribute_ids);
        } else {
            return service.getSampleClinicalData(
                study_id,
                attribute_ids,
                sample_ids
            );
        }
    }

    @ApiOperation(
        value = "Get clinical data records filtered by patient ids",
        nickname = "getPatientClinicalData",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/clinicaldata/patients",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBClinicalPatientData> getPatientClinicalData(
        @ApiParam(
            required = true,
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)"
        ) @RequestParam(required = true) String study_id,
        @ApiParam(
            required = true,
            value = "List of attribute ids, such as those returned by /api/clinicalattributes/patients. (example: PATIENT_ID,DFS_STATUS)"
        ) @RequestParam(required = true) List<String> attribute_ids,
        @ApiParam(
            required = false,
            value = "List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all."
        ) @RequestParam(required = false) List<String> patient_ids
    ) {
        if (patient_ids == null) {
            return service.getPatientClinicalData(study_id, attribute_ids);
        } else {
            return service.getPatientClinicalData(
                study_id,
                attribute_ids,
                patient_ids
            );
        }
    }

    @ApiOperation(
        value = "Get clinical attribute identifiers, filtered by identifier",
        nickname = "getClinicalAttributes",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/clinicalattributes",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBClinicalField> getClinicalAttributes(
        @ApiParam(
            value = "List of attribute ids. If provided, returned clinical attributes will be the ones with matching attribute ids. Empty string returns all clinical attributes if study_id is not present."
        ) @RequestParam(required = false) List<String> attr_ids,
        @ApiParam(
            value = "Study id. If provided, return clinical attributes will be the ones that this study contains."
        ) @RequestParam(required = false) String study_id
    ) {
        if (study_id != null) {
            return service.getClinicalAttributes(study_id);
        }
        if (attr_ids == null) {
            return service.getClinicalAttributes();
        } else {
            return service.getClinicalAttributes(attr_ids);
        }
    }

    @ApiOperation(
        value = "Get clinical attribute identifiers, filtered by sample",
        nickname = "getSampleClinicalAttributes",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/clinicalattributes/samples",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBClinicalField> getSampleClinicalAttributes(
        @ApiParam(
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies."
        ) @RequestParam(required = false) String study_id,
        @ApiParam(
            required = false,
            value = "List of sample_ids. If provided, returned clinical attributes will be those which appear in any listed sample. Empty string returns clinical attributes across all samples."
        ) @RequestParam(required = false) List<String> sample_ids
    ) {
        if (sample_ids == null && study_id == null) {
            return service.getSampleClinicalAttributes();
        }
        if (study_id != null && sample_ids != null) {
            return service.getSampleClinicalAttributes(study_id, sample_ids);
        }
        if (sample_ids == null) {
            return service.getSampleClinicalAttributes(study_id);
        } else {
            return new ArrayList<>();
        }
    }

    @ApiOperation(
        value = "Get clinical attribute identifiers, filtered by patient",
        nickname = "getPatientClinicalAttributes",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/clinicalattributes/patients",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBClinicalField> getPatientClinicalAttributes(
        @ApiParam(
            required = false,
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Empty string returns clinical attributes across all studies."
        ) @RequestParam(required = false) String study_id,
        @ApiParam(
            required = false,
            value = "List of patient_ids. If provided, returned clinical attributes will be those which appear in any listed patient. Empty string returns clinical attributes across all patients."
        ) @RequestParam(required = false) List<String> patient_ids
    ) {
        if (patient_ids == null && study_id == null) {
            return service.getPatientClinicalAttributes();
        }
        if (study_id != null && patient_ids != null) {
            return service.getPatientClinicalAttributes(study_id, patient_ids);
        }
        if (patient_ids == null) {
            return service.getPatientClinicalAttributes(study_id);
        } else {
            return new ArrayList<>();
        }
    }

    @ApiOperation(
        value = "Get gene meta data by hugo gene symbol lookup",
        nickname = "getGenes",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/genes",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBGene> getGenes(
        @ApiParam(
            required = false,
            value = "List of hugo gene symbols. Unrecognized genes are silently ignored. Empty string returns all genes."
        ) @RequestParam(required = false) List<String> hugo_gene_symbols
    ) {
        if (hugo_gene_symbols == null) {
            return service.getGenes();
        } else {
            return service.getGenes(hugo_gene_symbols);
        }
    }

    @ApiOperation(
        value = "Get noncanonical gene symbols by Entrez id lookup",
        nickname = "getGenesAliases",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/genesaliases",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBGeneAlias> getGenesAliases(
        @ApiParam(
            required = false,
            value = "List of Entrez gene ids. Unrecognized IDs are silently ignored. Empty list returns all genes."
        ) @RequestParam(required = false) List<Long> entrez_gene_ids
    ) {
        if (entrez_gene_ids == null) {
            return service.getGenesAliases();
        } else {
            return service.getGenesAliases(entrez_gene_ids);
        }
    }

    @ApiOperation(
        value = "Get list of genetic profile identifiers by study",
        nickname = "getGeneticProfiles",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/geneticprofiles",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBGeneticProfile> getGeneticProfiles(
        @ApiParam(
            required = false,
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by genetic profile ids (across all studies)."
        ) @RequestParam(required = false) String study_id,
        @ApiParam(
            required = false,
            value = "List of genetic_profile_ids. (example: brca_tcga_pub_mutations). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored."
        ) @RequestParam(required = false) List<String> genetic_profile_ids
    ) {
        if (study_id != null) {
            return service.getGeneticProfiles(study_id);
        }
        if (genetic_profile_ids != null) {
            return service.getGeneticProfiles(genetic_profile_ids);
        } else {
            return service.getGeneticProfiles();
        }
    }

    @ApiOperation(
        value = "Get list of sample lists (list name and sample id list) by study",
        nickname = "getSampleLists",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/samplelists",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBSampleList> getSampleLists(
        @ApiParam(
            required = false,
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga). Must be empty to query by sample list ids (across all studies)."
        ) @RequestParam(required = false) String study_id,
        @ApiParam(
            required = false,
            value = "List of sample list ids. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all genetic profiles. If study_id argument was provided, this argument will be ignored."
        ) @RequestParam(required = false) List<String> sample_list_ids
    ) {
        if (study_id != null) {
            return service.getSampleLists(study_id);
        }
        if (sample_list_ids != null) {
            return service.getSampleLists(sample_list_ids);
        } else {
            return service.getSampleLists();
        }
    }

    @ApiOperation(
        value = "Get patient id list by study or by sample id",
        nickname = "getPatients",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/patients",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBPatient> getPatients(
        @ApiParam(
            required = true,
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)"
        ) @RequestParam(required = true) String study_id,
        @ApiParam(
            required = false,
            value = "List of patient ids such as those returned by /api/patients. Empty string returns all. Must be empty to query by sample ids."
        ) @RequestParam(required = false) List<String> patient_ids,
        @ApiParam(
            required = false,
            value = "List of sample identifiers. Empty string returns all. If patient_ids argument was provided, this argument will be ignored."
        ) @RequestParam(required = false) List<String> sample_ids
    ) {
        if (patient_ids != null) {
            return service.getPatientsByPatient(study_id, patient_ids);
        }
        if (sample_ids != null) {
            return service.getPatientsBySample(study_id, sample_ids);
        } else {
            return service.getPatients(study_id);
        }
    }

    @ApiOperation(
        value = "Get genetic profile data across samples for given genes, and filtered by sample id or sample list id",
        nickname = "getGeneticProfileData",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/geneticprofiledata",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBProfileData> getGeneticProfileData(
        @ApiParam(
            required = true,
            value = "List of genetic_profile_ids such as those returned by /api/geneticprofiles. (example: brca_tcga_pub_mutations). Unrecognized genetic profile ids are silently ignored. Profile data is only returned for matching ids."
        ) @RequestParam(required = true) List<String> genetic_profile_ids,
        @ApiParam(
            required = true,
            value = "List of hugo gene symbols. (example: AKT1,CASP8,TGFBR1) Unrecognized gene ids are silently ignored. Profile data is only returned for matching genes."
        ) @RequestParam(required = true) List<String> genes,
        @ApiParam(
            required = false,
            value = "List of sample identifiers such as those returned by /api/samples. Empty string returns all. Must be empty to query by sample list ids."
        ) @RequestParam(required = false) List<String> sample_ids,
        @ApiParam(
            required = false,
            value = "A single sample list ids such as those returned by /api/samplelists. (example: brca_tcga_idc,brca_tcga_lobular). Empty string returns all. If sample_ids argument was provided, this argument will be ignored."
        ) @RequestParam(required = false) String sample_list_id
    ) {
        if (sample_ids == null && sample_list_id == null) {
            return service.getGeneticProfileData(genetic_profile_ids, genes);
        }
        if (sample_ids != null) {
            return service.getGeneticProfileDataBySample(
                genetic_profile_ids,
                genes,
                sample_ids
            );
        } else {
            return service.getGeneticProfileDataBySampleList(
                genetic_profile_ids,
                genes,
                sample_list_id
            );
        }
    }

    @ApiOperation(
        value = "Get list of samples ids with meta data by study, filtered by sample ids or patient ids",
        nickname = "getSamples",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        value = "/samples",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBSample> getSamples(
        @ApiParam(
            required = true,
            value = "A single study id, such as those returned by /api/studies. (example: brca_tcga)"
        ) @RequestParam(required = true) String study_id,
        @ApiParam(
            required = false,
            value = "List of sample identifiers. Unrecognized ids are silently ignored. Empty string returns all. Must be empty to query by patient_ids."
        ) @RequestParam(required = false) List<String> sample_ids,
        @ApiParam(
            required = false,
            value = "List of patient identifiers such as those returned by /api/patients. Unrecognized ids are silently ignored. Empty string returns all. If sample_ids argument was provided, this arument will be ignored."
        ) @RequestParam(required = false) List<String> patient_ids
    ) {
        if (sample_ids != null) {
            return service.getSamplesBySample(study_id, sample_ids);
        }
        if (patient_ids != null) {
            return service.getSamplesByPatient(study_id, patient_ids);
        } else {
            return service.getSamples(study_id);
        }
    }

    @ApiOperation(value = "Get studies", nickname = "getStudies", notes = "")
    @Transactional
    @RequestMapping(
        value = "/studies",
        method = { RequestMethod.GET, RequestMethod.POST }
    )
    public @ResponseBody List<DBStudy> getStudies(
        @ApiParam(
            required = false,
            value = "List of study_ids. Unrecognized ids are silently ignored. Empty string returns all."
        ) @RequestParam(required = false) List<String> study_ids
    ) {
        if (study_ids == null) {
            return service.getStudies();
        } else {
            return service.getStudies(study_ids);
        }
    }
}
