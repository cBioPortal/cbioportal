package org.cbioportal.application.file.export.writers;

import com.google.common.collect.Iterables;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.io.Writer;
import java.util.SequencedMap;

import static org.cbioportal.application.file.utils.TSVUtil.composeRow;

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
    public void write(CloseableIterator<SequencedMap<ClinicalAttribute, String>> clinicalAttributeData) {
        if (!clinicalAttributeData.hasNext()) {
            return;
        }
        SequencedMap<ClinicalAttribute, String> row = clinicalAttributeData.next();
        Iterable<ClinicalAttribute> attributes = row.keySet();
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getDisplayName));
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getDescription));
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getDatatype));
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getPriority));
        writeRow(Iterables.transform(attributes, ClinicalAttribute::getAttributeId));

        writeRow(row.values());
        while (clinicalAttributeData.hasNext()) {
            row = clinicalAttributeData.next();
            writeRow(row.values());
        }
    }

    private void writeRow(Iterable<String> row) {
        try {
            writer.write(composeRow(row));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeCommentsRow(Iterable<String> row) {
        try {
            writer.write("#" + composeRow(row));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
