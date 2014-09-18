package org.mskcc.cbio.icgc.support;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import scala.Tuple2;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */
public class GeneNameMapSupplier implements Supplier<Map<String, Tuple2<String, String>>> {

    private static final String HUGO_GENE_FILE = "/HGNC_Ensembl.tsv";
    private static  InputStreamReader reader;
     private static final Logger logger = Logger.getLogger(GeneNameMapSupplier.class);

    public GeneNameMapSupplier() {
         reader = new InputStreamReader((this.getClass().getResourceAsStream(HUGO_GENE_FILE)));
            
    }

    @Override
    public Map<String, Tuple2<String, String>> get() {

        Map<String, Tuple2<String, String>> ensemblMap = Maps.newHashMap();
        try {
            final CSVParser parser = new CSVParser(this.reader, CSVFormat.TDF.withHeader());
            for (CSVRecord record : parser) {
                ensemblMap.put(record.get("Ensembl"), new Tuple2(record.get("Symbol"), record.get("Entrez")));
            }
            logger.info("HGNC - Entrez - Ensembl symbol map completed.");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return ensemblMap;

    }

}
