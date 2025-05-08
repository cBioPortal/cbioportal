package org.cbioportal.application.file.utils;

import java.io.IOException;
import java.io.Writer;

public interface FileWriterFactory {
    void setBasePath(String basePath);

    String getBasePath();

    Writer newWriter(String name) throws IOException;

    void fail(Exception e);
}
