package org.cbioportal.file.export;

import java.io.IOException;
import java.io.Writer;

public interface FileWriterFactory {
    Writer newWriter(String name) throws IOException;
}
