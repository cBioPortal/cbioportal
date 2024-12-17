package org.cbioportal.file.export;

import org.cbioportal.file.model.CancerStudyMetadata;
import org.cbioportal.file.model.ClinicalSampleAttributesMetadata;
import org.cbioportal.file.model.GenericProfileDatatypeMetadata;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class MetadataWriterTest {

    StringWriter output = new StringWriter();
    KeyValueMetadataWriter writer = new KeyValueMetadataWriter(output);

    @Test
    public void testCancerStudyMetadataWriter() {
        writer.write(new CancerStudyMetadata(
            "toc",
            "study_id1",
            "study name",
            "study description",
            Optional.of("Citation"),
            Optional.of("1234"),
            Optional.of("GROUP1;GROUP2"),
            Optional.of(true),
            Optional.of("tags_file.txt"),
            Optional.of("hg38")
        ));

        assertEquals("""
            type_of_cancer: toc
            cancer_study_identifier: study_id1
            name: study name
            description: study description
            citation: Citation
            pmid: 1234
            groups: GROUP1;GROUP2
            add_global_case_list: true
            tags_file: tags_file.txt
            reference_genome: hg38
            """, output.toString());
    }

    @Test
    public void testClinicalSampleAttributesMetadataWriter() {
        writer.write(new ClinicalSampleAttributesMetadata(
            "study_id1",
            "data_file.txt"
        ));

        assertEquals("""
            cancer_study_identifier: study_id1
            generic_alteration_type: CLINICAL
            datatype: SAMPLE_ATTRIBUTES
            data_filename: data_file.txt
            """, output.toString());
    }

    @Test
    public void testMutationMetadataWriter() {
        writer.write(new GenericProfileDatatypeMetadata(
            "mutations",
            "MUTATION_EXTENDED",
            "MAF",
            "study_id1",
            "data_file.txt",
            "profile name",
            "profile description",
            Optional.of("gene_panel"),
            true
        ));

        assertEquals("""
           cancer_study_identifier: study_id1
           generic_alteration_type: MUTATION_EXTENDED
           datatype: MAF
           data_filename: data_file.txt
           stable_id: mutations
           show_profile_in_analysis_tab: true
           profile_name: profile name
           profile_description: profile description
           gene_panel: gene_panel
           """, output.toString());
    }
}
