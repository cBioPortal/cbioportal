package org.cbioportal.application.file.imp;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipInputStreamReaderIterator implements Closeable, Iterator<ZipInputStreamReaderIterator.ZipEntryInputStreamReader> {

    private final ZipInputStream zipInputStream;
    private ZipEntry zipEntry;

    public ZipInputStreamReaderIterator(InputStream zipInputStream) {
        this.zipInputStream = new ZipInputStream(zipInputStream);
        try {
            this.zipEntry = this.zipInputStream.getNextEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() {
        return this.zipEntry != null;
    }

    @Override
    public ZipEntryInputStreamReader next() {
        if (this.zipEntry == null) {
           throw new NoSuchElementException(); 
        }
        ZipEntryInputStreamReader reader = new ZipEntryInputStreamReader(this.zipInputStream, this.zipEntry);
        try {
            this.zipEntry = this.zipInputStream.getNextEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return reader;
    }

    @Override
    public void close() throws IOException {
        zipInputStream.close();
    }

    public static class ZipEntryInputStreamReader extends InputStreamReader {

        private final ZipInputStream zipInputStream;
        private final ZipEntry zipEntry;

        public ZipEntryInputStreamReader(ZipInputStream zipInputStream, ZipEntry zipEntry) {
            super(zipInputStream);
            this.zipInputStream = zipInputStream;
            this.zipEntry = zipEntry;
        }
        
        public String getFilename() {
            return zipEntry.getName();
        }

        @Override
        public void close() throws IOException {
            this.zipInputStream.closeEntry();
        }
    }
}
