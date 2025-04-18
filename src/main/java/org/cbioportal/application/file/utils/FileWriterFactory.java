package org.cbioportal.application.file.utils;

import java.io.IOException;
import java.io.Writer;

public interface FileWriterFactory {
    Writer newWriter(String name) throws IOException;
}
