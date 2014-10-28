package org.mskcc.cbio.importer.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

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
public class EntrezIDSupplier implements Supplier<Map<String, String>> {

    private static final String HUGO_GENE_FILE = "/HUGO_Entrez.tsv";
    private static InputStreamReader reader;
    private static final Logger logger = Logger.getLogger(EntrezIDSupplier.class);

    public EntrezIDSupplier() {
        reader = new InputStreamReader((this.getClass().getResourceAsStream(HUGO_GENE_FILE)));
    }
    /*
     public method to create and supply a Map of HUGO Symbols keys
     and  Entrez id as values
     n.b. the Entrez ID is treated as a numeric String
     */
    @Override
    public Map<String, String> get() {

        Map<String, String> hugoMap = Maps.newHashMap();
        try {
            final CSVParser parser = new CSVParser(this.reader, CSVFormat.TDF.withHeader());
            for (CSVRecord record : parser) {
                
                hugoMap.put(record.get(0), record.get(1));
            }
            logger.info("HGNC - Entrez map completed.");
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return hugoMap;

    }

    /*
     main method to facilitate standalone testing
     */
    public static void main(String... args) {
        Map<String, String> gnMap = Suppliers.memoize(new EntrezIDSupplier()).get();
        // get the map size
        System.out.println("map size " + gnMap.size());

        // lookup some HUGO Symbols
        logger.info("HUGO Symbol BRAC1 Entrez ID " + gnMap.get("BRCA1"));

    }
}
