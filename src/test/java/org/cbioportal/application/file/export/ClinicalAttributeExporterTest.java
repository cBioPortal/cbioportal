package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.writers.ClinicalAttributeDataWriter;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.junit.Test;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

import static org.junit.Assert.assertEquals;

public class ClinicalAttributeExporterTest {

    StringWriter output = new StringWriter();
    ClinicalAttributeDataWriter writer = new ClinicalAttributeDataWriter(output);

    @Test
    public void testClinicalAttributeDataWriter() {
        ClinicalAttribute clinicalAttribute = new ClinicalAttribute("attr1 name", "attr1 description", "NUMBER", "3", "ATTR1");
        SequencedMap<ClinicalAttribute, String> row1 = new LinkedHashMap<>();
        row1.put(ClinicalAttribute.PATIENT_ID, "PATIENT1");
        row1.put(ClinicalAttribute.SAMPLE_ID, "SAMPLE1");
        row1.put(clinicalAttribute, "1.1");
        SequencedMap<ClinicalAttribute, String> row2 = new LinkedHashMap<>();
        row2.put(ClinicalAttribute.PATIENT_ID, "PATIENT2");
        row2.put(ClinicalAttribute.SAMPLE_ID, "SAMPLE2");
        row2.put(clinicalAttribute, "2.2");

        writer.write(new SimpleCloseableIterator<>(List.of(row1, row2)));

        assertEquals("""
            #Patient Identifier\tSample Identifier\tattr1 name
            #Patient Identifier\tSample Identifier\tattr1 description
            #STRING\tSTRING\tNUMBER
            #1\t1\t3
            PATIENT_ID\tSAMPLE_ID\tATTR1
            PATIENT1\tSAMPLE1\t1.1
            PATIENT2\tSAMPLE2\t2.2
            """, output.toString());
    }

    @Test
    public void testEscapeTabs() {
        ClinicalAttribute clinicalAttribute = new ClinicalAttribute("attr1\tname", "attr1\tdescription", "STRING", "1", "ATTR1");

        SequencedMap<ClinicalAttribute, String> row1 = new LinkedHashMap<>();
        row1.put(ClinicalAttribute.PATIENT_ID, "PATIENT1");
        row1.put(clinicalAttribute, "A\tB");

        writer.write(new SimpleCloseableIterator<>(List.of(row1)));

        assertEquals("""
            #Patient Identifier\tattr1\\tname
            #Patient Identifier\tattr1\\tdescription
            #STRING\tSTRING
            #1\t1
            PATIENT_ID\tATTR1
            PATIENT1\tA\\tB
            """, output.toString());
    }
}
