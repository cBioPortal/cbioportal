package org.cbioportal.file.export;

import org.cbioportal.file.model.CancerStudyMetadata;
import org.cbioportal.file.model.GenericDatatypeFileMetadata;
import org.cbioportal.file.model.GenericProfileDatatypeFileMetadata;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * A serializer for file model records that serializes them the cBioPortal key-value configuration format.
 * e.g. meta_study.txt
 */
public class KeyValueConfigurationWriter {
    private final Writer writer;

    /**
     * @param writer - the writer to write the key-value configuration to
     *               e.g. StringWriter, FileWriter
     */
    public KeyValueConfigurationWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Write a cancer study metadata to the output writer
     */
    public void write(CancerStudyMetadata cancerStudyMetadata) {
        LinkedHashMap<String, String> config = new LinkedHashMap<>();
        config.put("type_of_cancer", cancerStudyMetadata.typeOfCancer());
        config.put("cancer_study_identifier", cancerStudyMetadata.cancerStudyIdentifier());
        config.put("name", cancerStudyMetadata.name());
        config.put("description", cancerStudyMetadata.description());
        cancerStudyMetadata.citation().ifPresent(citation -> config.put("citation", citation));
        cancerStudyMetadata.pmid().ifPresent(pmid -> config.put("pmid", pmid));
        cancerStudyMetadata.groups().ifPresent(groups -> config.put("groups", groups));
        cancerStudyMetadata.addGlobalCaseList().ifPresent(addGlobalCaseList -> config.put("add_global_case_list", addGlobalCaseList.toString()));
        cancerStudyMetadata.tagsFile().ifPresent(tagsFile -> config.put("tags_file", tagsFile));
        cancerStudyMetadata.referenceGenome().ifPresent(referenceGenome -> config.put("reference_genome", referenceGenome));
        write(config);
    }

    public void write(GenericDatatypeFileMetadata genericDatatypeFileMetadata) {
        LinkedHashMap<String, String> config = new LinkedHashMap<>();
        config.put("cancer_study_identifier", genericDatatypeFileMetadata.cancerStudyIdentifier());
        config.put("generic_alteration_type", genericDatatypeFileMetadata.geneticAlterationType());
        config.put("datatype", genericDatatypeFileMetadata.datatype());
        config.put("data_filename", genericDatatypeFileMetadata.dataFilename());
        write(config);
    }

    public void write(GenericProfileDatatypeFileMetadata genericProfileDatatypeFileMetadata) {
        write((GenericDatatypeFileMetadata) genericProfileDatatypeFileMetadata);
        LinkedHashMap<String, String> config = new LinkedHashMap<>();
        config.put("stable_id", genericProfileDatatypeFileMetadata.stableId());
        config.put("show_profile_in_analysis_tab", genericProfileDatatypeFileMetadata.showProfileInAnalysisTab().toString().toLowerCase());
        config.put("profile_name", genericProfileDatatypeFileMetadata.profileName());
        config.put("profile_description", genericProfileDatatypeFileMetadata.profileDescription());
        genericProfileDatatypeFileMetadata.genePanel().ifPresent(genePanel -> config.put("gene_panel", genePanel));
        write(config);
    }

    private void write(LinkedHashMap<String, String> config) {
        config.forEach((key, value) -> {
            try {
                writer.write(composeKeyValueLine(key, value));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String composeKeyValueLine(String key, String value) {
        return key + ": " + value + "\n";
    }
}
