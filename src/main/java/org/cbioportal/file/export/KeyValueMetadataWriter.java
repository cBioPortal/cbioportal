package org.cbioportal.file.export;

import org.cbioportal.file.model.CancerStudyMetadata;
import org.cbioportal.file.model.GenericDatatypeMetadata;
import org.cbioportal.file.model.GenericProfileDatatypeMetadata;

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
        LinkedHashMap<String, String> metdata = new LinkedHashMap<>();
        metdata.put("type_of_cancer", cancerStudyMetadata.typeOfCancer());
        metdata.put("cancer_study_identifier", cancerStudyMetadata.cancerStudyIdentifier());
        metdata.put("name", cancerStudyMetadata.name());
        metdata.put("description", cancerStudyMetadata.description());
        cancerStudyMetadata.citation().ifPresent(citation -> metdata.put("citation", citation));
        cancerStudyMetadata.pmid().ifPresent(pmid -> metdata.put("pmid", pmid));
        cancerStudyMetadata.groups().ifPresent(groups -> metdata.put("groups", groups));
        cancerStudyMetadata.addGlobalCaseList().ifPresent(addGlobalCaseList -> metdata.put("add_global_case_list", addGlobalCaseList.toString()));
        cancerStudyMetadata.tagsFile().ifPresent(tagsFile -> metdata.put("tags_file", tagsFile));
        cancerStudyMetadata.referenceGenome().ifPresent(referenceGenome -> metdata.put("reference_genome", referenceGenome));
        write(metdata);
    }

    /**
     * Write a generic datatype metadata to the writer 
     */
    public void write(GenericDatatypeMetadata genericDatatypeMetadata) {
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("cancer_study_identifier", genericDatatypeMetadata.cancerStudyIdentifier());
        metadata.put("genetic_alteration_type", genericDatatypeMetadata.geneticAlterationType());
        metadata.put("datatype", genericDatatypeMetadata.datatype());
        metadata.put("data_filename", genericDatatypeMetadata.dataFilename());
        write(metadata);
    }

    /**
     * Write a generic profile datatype metadata to the writer 
     */
    public void write(GenericProfileDatatypeMetadata genericProfileDatatypeMetadata) {
        write((GenericDatatypeMetadata) genericProfileDatatypeMetadata);
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("stable_id", genericProfileDatatypeMetadata.stableId());
        metadata.put("show_profile_in_analysis_tab", genericProfileDatatypeMetadata.showProfileInAnalysisTab().toString().toLowerCase());
        metadata.put("profile_name", genericProfileDatatypeMetadata.profileName());
        metadata.put("profile_description", genericProfileDatatypeMetadata.profileDescription());
        genericProfileDatatypeMetadata.genePanel().ifPresent(genePanel -> metadata.put("gene_panel", genePanel));
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
