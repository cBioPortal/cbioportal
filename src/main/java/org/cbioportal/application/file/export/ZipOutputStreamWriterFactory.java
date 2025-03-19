package org.cbioportal.application.file.export;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamWriterFactory implements FileWriterFactory, Closeable {

    private final ZipOutputStream zipOutputStream;

    public ZipOutputStreamWriterFactory(OutputStream outputStream) {
        this.zipOutputStream = new ZipOutputStream(outputStream);
    }

    @Override
    public Writer newWriter(String name) throws IOException {
        return new ZipEntryOutputStreamWriter(name, zipOutputStream);
    }

    @Override
    public void close() throws IOException {
        zipOutputStream.close();
    }

    static class ZipEntryOutputStreamWriter extends OutputStreamWriter {
        private final ZipOutputStream zipOutputStream;

        public ZipEntryOutputStreamWriter(String name, ZipOutputStream zipOutputStream) throws IOException {
            super(zipOutputStream);
            zipOutputStream.putNextEntry(new ZipEntry(name));
            this.zipOutputStream = zipOutputStream;
        }

        @Override
        public void close() throws IOException {
            flush();
            zipOutputStream.closeEntry();
        }
    }
}
