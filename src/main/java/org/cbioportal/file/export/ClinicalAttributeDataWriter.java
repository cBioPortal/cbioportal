package org.cbioportal.file.export;

import org.cbioportal.file.model.ClinicalAttribute;
import org.cbioportal.file.model.ClinicalAttributeData;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static org.cbioportal.file.export.TSVUtil.composeRow;

public class ClinicalAttributeDataWriter {

    private final Writer writer;

    /**
     * @param writer - the writer to write the key-value metadata to
     *               e.g. StringWriter, FileWriter
     */
    public ClinicalAttributeDataWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Writes model pojo to the cBioPortal clinical attribute format
     */
    public void write(ClinicalAttributeData clinicalAttributeData) {
        SequencedSet<ClinicalAttribute> attributes = clinicalAttributeData.getAttributes();
        writeCommentsRow(attributes.stream().map(ClinicalAttribute::displayName).toList());
        writeCommentsRow(attributes.stream().map(ClinicalAttribute::description).toList());
        writeCommentsRow(attributes.stream().map(ClinicalAttribute::datatype).toList());
        writeCommentsRow(attributes.stream().map(ClinicalAttribute::priority).toList());
        writeRow(attributes.stream().map(ClinicalAttribute::attributeId).toList());
        Iterator<SequencedMap<ClinicalAttribute, String>> row = clinicalAttributeData.rows();
        while (row.hasNext()) {
            SequencedMap<ClinicalAttribute, String> map = row.next();
            writeRow(attributes.stream().map(map::get).toList());
        }
    }

    private void writeRow(Iterable<String> row) {
        try {
            writer.write(composeRow(row));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCommentsRow(Iterable<String> row) {
        try {
            writer.write("#" + composeRow(row));
            //TODO improve by using buffered writer instead
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
