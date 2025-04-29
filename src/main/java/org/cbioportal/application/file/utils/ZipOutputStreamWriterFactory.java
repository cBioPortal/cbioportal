package org.cbioportal.application.file.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipOutputStreamWriterFactory implements FileWriterFactory, Closeable {

    private final OutputStream outputStream;
    private final ZipOutputStream zipOutputStream;

    public ZipOutputStreamWriterFactory(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.zipOutputStream = new ZipOutputStream(outputStream);
    }

    @Override
    public Writer newWriter(String name) throws IOException {
        return new ZipEntryOutputStreamWriter(name, zipOutputStream);
    }

    @Override
    public void fail(Exception e) {
        // corrupt the whole zip file intentionally
        RuntimeException primaryException = new RuntimeException(e);
        try {
            outputStream.write(("ERROR: " + e.getMessage()).getBytes());
            outputStream.flush();
        } catch (IOException ex) {
            primaryException.addSuppressed(ex);
        }
        // and stop the following writes
        throw primaryException;
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
