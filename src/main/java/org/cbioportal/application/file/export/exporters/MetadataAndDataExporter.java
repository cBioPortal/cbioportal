package org.cbioportal.application.file.export.exporters;

import org.cbioportal.application.file.model.StudyRelatedMetadata;
import org.cbioportal.application.file.utils.FileWriterFactory;

import java.io.Closeable;
import java.io.Writer;
import java.util.Iterator;

/**
 * Export metadata and data for a specific genetic alteration type and datatype.
 *
 * @param <M>  - a metadata type
 * @param <DS> - a data type
 */
public abstract class MetadataAndDataExporter<M extends StudyRelatedMetadata, DS extends Iterator<?> & Closeable> extends MetadataExporter<M> {

    /**
     * @return the genetic alteration type of the datatype. e.g. "MUTATION_EXTENDED"
     */
    public abstract String getGeneticAlterationType();

    public abstract String getDatatype();

    @Override
    public boolean exportData(FileWriterFactory fileWriterFactory, String studyId) {
        try (DS data = getData(studyId)) {
            if (data.hasNext()) {
                if (!super.exportData(fileWriterFactory, studyId)) {
                    throw new IllegalStateException("Cannot get metadata for study " + studyId + " for genetic alteration type" + getGeneticAlterationType() + " and datatype " + getDatatype());
                }
                try (Writer dataFileWriter = fileWriterFactory.newWriter(getDataFilename())) {
                    writeData(dataFileWriter, data);
                }
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public String getDataFilename() {
        return "data_" + getGeneticAlterationType().toLowerCase() + "_" + getDatatype().toLowerCase() + ".txt";
    }

    protected abstract DS getData(String studyId);

    protected abstract void writeData(Writer writer, DS data);

    public String getMetaFilename() {
        return "meta_" + getGeneticAlterationType().toLowerCase() + "_" + getDatatype().toLowerCase() + ".txt";
    }
}
