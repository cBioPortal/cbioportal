package org.mskcc.cgds.scripts.drug.internal;

import org.mskcc.cgds.scripts.drug.DrugDataResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class DrugBankResource extends DrugDataResource {
    @Override
    public InputStream getResourceAsStream() throws IOException {
        if(getResourceURL().toLowerCase().endsWith("gz"))
            return new GZIPInputStream(super.getResourceAsStream());
        else
            return super.getResourceAsStream();
    }

}
