package org.cbioportal.application.file.export;

import com.google.common.collect.Iterables;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.LongTable;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;

import static org.cbioportal.application.file.export.TSVUtil.composeRow;
import static org.cbioportal.application.file.export.TSVUtil.composeRowOfOptionals;

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
    public void write(LongTable<ClinicalAttribute, String> clinicalAttributeData) {
        Iterable<ClinicalAttribute> attributes = clinicalAttributeData.getColumns();
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getDisplayName));
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getDescription));
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getDatatype));
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getPriority));
        writeCommentsRow(Iterables.transform(attributes, ClinicalAttribute::getAttributeId));
        while (clinicalAttributeData.hasNext()) {
            Function<ClinicalAttribute, Optional<String>> row = clinicalAttributeData.next();
            writeRow(Iterables.transform(attributes, row::apply));
        }
    }

    private void writeRow(Iterable<Optional<String>> row) {
        try {
            writer.write(composeRowOfOptionals(row));
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
