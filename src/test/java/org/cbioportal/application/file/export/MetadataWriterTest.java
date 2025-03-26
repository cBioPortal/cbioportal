package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.ClinicalSampleAttributesMetadata;
import org.cbioportal.application.file.model.GenericProfileDatatypeMetadata;
import org.junit.Test;

import java.io.StringWriter;

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
            "Citation",
            "1234",
            "GROUP1;GROUP2",
            true,
            "hg38"));

        assertEquals("""
            type_of_cancer: toc
            cancer_study_identifier: study_id1
            name: study name
            description: study description
            citation: Citation
            pmid: 1234
            groups: GROUP1;GROUP2
            add_global_case_list: true
            reference_genome: hg38
            """, output.toString());
    }

    @Test
    public void testNulls() {
        writer.write(new CancerStudyMetadata(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        ));

        assertEquals("", output.toString());
    }
    @Test
    public void testBlanks() {
        writer.write(new CancerStudyMetadata(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            null, //Boolean
            ""
        ));

        assertEquals("type_of_cancer: \ncancer_study_identifier: \nname: \ndescription: \ncitation: \npmid: \ngroups: \nreference_genome: \n", output.toString());
    }
    @Test
    public void testEscapeNewLines() {
        writer.write(new CancerStudyMetadata(
            "toc1",
            "cancer_study_identifier1",
            "This is a\nmultiline\nname",
            "This is a\nmultiline\ndescription",
            null,
            null,
            null,
            null,
            null
        ));

        assertEquals("""
            type_of_cancer: toc1
            cancer_study_identifier: cancer_study_identifier1
            name: This is a\\nmultiline\\nname
            description: This is a\\nmultiline\\ndescription
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
            genetic_alteration_type: CLINICAL
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
            "gene_panel",
            true
        ));

        assertEquals("""
           cancer_study_identifier: study_id1
           genetic_alteration_type: MUTATION_EXTENDED
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
