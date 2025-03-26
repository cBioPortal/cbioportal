package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.CaseListMetadata;
import org.cbioportal.application.file.model.GenericDatatypeMetadata;
import org.cbioportal.application.file.model.GenericProfileDatatypeMetadata;

import java.io.IOException;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

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

    private static final LinkedHashMap<String, Function<CancerStudyMetadata, String>> CANCER_STUDY_METADATA_MAPPING = new LinkedHashMap<>();

    static {
        CANCER_STUDY_METADATA_MAPPING.put("type_of_cancer", CancerStudyMetadata::getTypeOfCancer);
        CANCER_STUDY_METADATA_MAPPING.put("cancer_study_identifier", CancerStudyMetadata::getCancerStudyIdentifier);
        CANCER_STUDY_METADATA_MAPPING.put("name", CancerStudyMetadata::getName);
        CANCER_STUDY_METADATA_MAPPING.put("description", CancerStudyMetadata::getDescription);
        CANCER_STUDY_METADATA_MAPPING.put("citation", CancerStudyMetadata::getCitation);
        CANCER_STUDY_METADATA_MAPPING.put("pmid", CancerStudyMetadata::getPmid);
        CANCER_STUDY_METADATA_MAPPING.put("groups", CancerStudyMetadata::getGroups);
        CANCER_STUDY_METADATA_MAPPING.put("add_global_case_list", cancerStudyMetadata -> cancerStudyMetadata.getAddGlobalCaseList() == null ? null : cancerStudyMetadata.getAddGlobalCaseList().toString());
        //TODO implement tags_file
        //CANCER_STUDY_METADATA_MAPPING.put("tags_file", CancerStudyMetadata::getTagsFile);
        CANCER_STUDY_METADATA_MAPPING.put("reference_genome", CancerStudyMetadata::getReferenceGenome);
    }

    /**
     * Write a cancer study metadata to the writer
     */
    public void write(CancerStudyMetadata cancerStudyMetadata) {
        var keyValueStream = CANCER_STUDY_METADATA_MAPPING.entrySet().stream()
                .map(entry -> entry(entry.getKey(), entry.getValue().apply(cancerStudyMetadata)));
        write(keyValueStream);
    }

    private static final LinkedHashMap<String, Function<GenericDatatypeMetadata, String>> GENETIC_PROFILE_METADATA_MAPPING = new LinkedHashMap<>();
    static {
        GENETIC_PROFILE_METADATA_MAPPING.put("cancer_study_identifier", GenericDatatypeMetadata::getCancerStudyIdentifier);
        GENETIC_PROFILE_METADATA_MAPPING.put("genetic_alteration_type", GenericDatatypeMetadata::getGeneticAlterationType);
        GENETIC_PROFILE_METADATA_MAPPING.put("datatype", GenericDatatypeMetadata::getDatatype);
        GENETIC_PROFILE_METADATA_MAPPING.put("data_filename", GenericDatatypeMetadata::getDataFilename);
    }
    /**
     * Write a generic datatype metadata to the writer
     */
    public void write(GenericDatatypeMetadata genericDatatypeMetadata) {
        var keyValueStream = GENETIC_PROFILE_METADATA_MAPPING.entrySet().stream()
                .map(entry -> entry(entry.getKey(), entry.getValue().apply(genericDatatypeMetadata)));
        write(keyValueStream);
    }

    private static final LinkedHashMap<String, Function<GenericProfileDatatypeMetadata, String>> GENERIC_PROFILE_METADATA_MAPPING = new LinkedHashMap<>();
    static {
        GENERIC_PROFILE_METADATA_MAPPING.put("stable_id", GenericProfileDatatypeMetadata::getStableId);
        GENERIC_PROFILE_METADATA_MAPPING.put("show_profile_in_analysis_tab", genericProfileDatatypeMetadata -> genericProfileDatatypeMetadata.getShowProfileInAnalysisTab().toString().toLowerCase());
        GENERIC_PROFILE_METADATA_MAPPING.put("profile_name", GenericProfileDatatypeMetadata::getProfileName);
        GENERIC_PROFILE_METADATA_MAPPING.put("profile_description", GenericProfileDatatypeMetadata::getProfileDescription);
        GENERIC_PROFILE_METADATA_MAPPING.put("gene_panel", GenericProfileDatatypeMetadata::getGenePanel);
    }
    /**
     * Write a generic profile datatype metadata to the writer
     */
    public void write(GenericProfileDatatypeMetadata genericProfileDatatypeMetadata) {
        write((GenericDatatypeMetadata) genericProfileDatatypeMetadata);
        var keyValueStream = GENERIC_PROFILE_METADATA_MAPPING.entrySet().stream()
                .map(entry -> entry(entry.getKey(), entry.getValue().apply(genericProfileDatatypeMetadata)));
        write(keyValueStream);
    }

    private static final LinkedHashMap<String, Function<CaseListMetadata, String>> CASE_LIST_METADATA_MAPPING = new LinkedHashMap<>();
    static {
        CASE_LIST_METADATA_MAPPING.put("cancer_study_identifier", CaseListMetadata::getCancerStudyIdentifier);
        CASE_LIST_METADATA_MAPPING.put("stable_id", CaseListMetadata::getStableId);
        CASE_LIST_METADATA_MAPPING.put("case_list_name", CaseListMetadata::getName);
        CASE_LIST_METADATA_MAPPING.put("case_list_description", CaseListMetadata::getDescription);
        CASE_LIST_METADATA_MAPPING.put("case_list_ids", caseListMetadata -> String.join("\t", caseListMetadata.getSampleIds()));
    }
    /**
     * Write a case list metadata to the writer
     * @param caseListMetadata
     */
    public void write(CaseListMetadata caseListMetadata) {
        var keyValueStream = CASE_LIST_METADATA_MAPPING.entrySet().stream()
                .map(entry -> entry(entry.getKey(), entry.getValue().apply(caseListMetadata)));
        write(keyValueStream);
    }

    private void write(Stream<Map.Entry<String, String>> metadata) {
        metadata
                //skip values that are null
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> {
            try {
                writer.write(composeKeyValueLine(entry.getKey(), entry.getValue()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String composeKeyValueLine(String key, String value) {
        return key + ": " + (value == null ? "" : value.replace("\n", "\\n")) + "\n";
    }

    /**
     *  Create a key-value entry that allows null values
     *  Map.entry does not allow null values
     */
    private static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }
}
