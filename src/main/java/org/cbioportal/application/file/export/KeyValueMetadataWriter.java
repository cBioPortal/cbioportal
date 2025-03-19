package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.cbioportal.application.file.model.GenericDatatypeMetadata;
import org.cbioportal.application.file.model.GenericProfileDatatypeMetadata;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * A serializer for file model records that serializes them the cBioPortal key-value metadata format.
 * e.g. meta_study.txt
 */
public class KeyValueMetadataWriter {
    private final Writer writer;

    /**
     * @param writer - the writer to write the key-value metadata to
     *               e.g. StringWriter, FileWriter
     */
    public KeyValueMetadataWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Write a cancer study metadata to the writer
     */
    public void write(CancerStudyMetadata cancerStudyMetadata) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("type_of_cancer", cancerStudyMetadata.getTypeOfCancer());
        metadata.put("cancer_study_identifier", cancerStudyMetadata.getCancerStudyIdentifier());
        metadata.put("name", cancerStudyMetadata.getName());
        metadata.put("description", cancerStudyMetadata.getDescription());
        metadata.put("citation", cancerStudyMetadata.getCitation());
        metadata.put("pmid", cancerStudyMetadata.getPmid());
        metadata.put("groups", cancerStudyMetadata.getGroups());
        //metadata.put("add_global_case_list", cancerStudyMetadata.addGlobalCaseList() ? cancerStudyMetadata.addGlobalCaseList().toString() : null);
        //metadata.put("tags_file", cancerStudyMetadata.tagsFile());
        metadata.put("reference_genome", cancerStudyMetadata.getReferenceGenome());
        write(metadata);
    }

    /**
     * Write a generic datatype metadata to the writer 
     */
    public void write(GenericDatatypeMetadata genericDatatypeMetadata) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("cancer_study_identifier", genericDatatypeMetadata.getCancerStudyIdentifier());
        metadata.put("genetic_alteration_type", genericDatatypeMetadata.getGeneticAlterationType());
        metadata.put("datatype", genericDatatypeMetadata.getDatatype());
        metadata.put("data_filename", genericDatatypeMetadata.getDataFilename());
        write(metadata);
    }

    /**
     * Write a generic profile datatype metadata to the writer 
     */
    public void write(GenericProfileDatatypeMetadata genericProfileDatatypeMetadata) {
        write((GenericDatatypeMetadata) genericProfileDatatypeMetadata);
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("stable_id", genericProfileDatatypeMetadata.getStableId());
        metadata.put("show_profile_in_analysis_tab", genericProfileDatatypeMetadata.getShowProfileInAnalysisTab().toString().toLowerCase());
        metadata.put("profile_name", genericProfileDatatypeMetadata.getProfileName());
        metadata.put("profile_description", genericProfileDatatypeMetadata.getProfileDescription());
        metadata.put("gene_panel", genericProfileDatatypeMetadata.getGenePanel());
        write(metadata);
    }

    public void write(CaseListMetadata caseListMetadata) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("cancer_study_identifier", caseListMetadata.getCancerStudyIdentifier());
        metadata.put("stable_id", caseListMetadata.getStableId());
        metadata.put("case_list_name", caseListMetadata.getName());
        metadata.put("case_list_description", caseListMetadata.getDescription());
        metadata.put("case_list_ids", String.join("\t", caseListMetadata.getSampleIds()));
        write(metadata);
    }
    private void write(LinkedHashMap<String, String> metadata) {
        metadata.forEach((key, value) -> {
            try {
                writer.write(composeKeyValueLine(key, value));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String composeKeyValueLine(String key, String value) {
        return key + ": " + (value == null ? "" : value.replace("\n", "\\n")) + "\n";
    }
}
