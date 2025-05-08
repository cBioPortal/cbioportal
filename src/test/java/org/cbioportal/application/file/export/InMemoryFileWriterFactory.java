package org.cbioportal.application.file.export;

import org.cbioportal.application.file.utils.FileWriterFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;

/**
 * A factory for creating in-memory file writers.
 * This is used for testing purposes to capture the output of file writers.
 */
public class InMemoryFileWriterFactory implements FileWriterFactory {

    private final LinkedHashMap<String, StringWriter> fileContents = new LinkedHashMap<>();
    private String basePath;

    @Override
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public Writer newWriter(String name) throws IOException {
        StringWriter stringWriter = new StringWriter();
        fileContents.put(basePath == null ? name : (basePath + name), stringWriter);
        return stringWriter;
    }

    @Override
    public void fail(Exception e) {
    }

    public LinkedHashMap<String, StringWriter> getFileContents() {
        return fileContents;
    }
}
