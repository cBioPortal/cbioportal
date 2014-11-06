package org.mskcc.cbio.importer.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.IDMapper;
import scala.Tuple2;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/5/14.
 */
public class GeneSymbolIDMapper implements IDMapper {

    /*
    represents an implementation of the IDMapper interface that supports
    HUGO Symbol <-> Entrez ID conversions
    also supports a method to provide the HUGO Synbol & Entrez ID for a specified Ensembl ID
    supports approx 40K mappings
     */

    private static final Logger logger = Logger.getLogger(GeneSymbolIDMapper.class);
    private Map<String,String> entrezMap = Suppliers.memoize(new EntrezIDSupplier()).get();
    private Map<String,Tuple2<String,String>>
            gnMap = Suppliers.memoize(new EnsemblNameMapSupplier()).get();

    @Override
    public String symbolToEntrezID(String geneSymbol) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(geneSymbol));
        return (this.entrezMap.containsKey(geneSymbol)) ? this.entrezMap.get(geneSymbol): "";
   }

    @Override
    public String entrezIDToSymbol(String entrezID) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(entrezID));
        for(Map.Entry<String,String> entry : this.entrezMap.entrySet()){
            if (entry.getValue().equals(entrezID)) {
                return entry.getKey();
            }
        }
        return "";
    }

    /*
    method to return the HUGO Symbol and EntrezID for a specified Ensembl ID
    return object is a Tuple2 containing the HUGO Gene Symbol & the EntrezID
    an empty tuple is returned if a mapping cannot be completed
     */

    public Tuple2<String,String> ensemblToHugoSymbolAndEntrezID(String ensemblID) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(ensemblID));
        return (this.gnMap.containsKey(ensemblID))? this.gnMap.get(ensemblID) :
                new Tuple2<String,String>("","");
    }

    public class EntrezIDSupplier implements Supplier<Map<String, String>> {

        private InputStreamReader reader;
        private final String HUGO_GENE_FILE = "/HUGO_Entrez.tsv";
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

            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
            return hugoMap;
        }
    }

    public class EnsemblNameMapSupplier implements Supplier<Map<String, Tuple2<String, String>>> {

        private static final String ENSEMBL_GENE_FILE = "/HGNC_Ensembl.tsv";
        private InputStreamReader reader;


        public EnsemblNameMapSupplier() {
            reader = new InputStreamReader((this.getClass().getResourceAsStream(ENSEMBL_GENE_FILE)));
        }

        /*
        public method to create and supply a Map of Ensemble gene ids as keys
        and Tuple2s containing Hugo symbol & Entrez id as values
        */
        @Override
        public Map<String, Tuple2<String, String>> get() {

            Map<String, Tuple2<String, String>> ensemblMap = Maps.newHashMap();
            try {
                final CSVParser parser = new CSVParser(this.reader, CSVFormat.TDF.withHeader());
                for (CSVRecord record : parser) {
                    ensemblMap.put(record.get("Ensembl"), new Tuple2(record.get("Symbol"), record.get("Entrez")));
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

            return ensemblMap;

        }
    }

    }
