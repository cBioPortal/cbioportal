package org.mskcc.cbio.importer.util;

import com.google.common.base.*;
import com.google.common.collect.Maps;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.commons.validator.routines.FloatValidator;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import scala.Tuple2;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
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

    private RTree<String,Rectangle> genePositionTree = Suppliers.memoize(new GenomicNameSupplier()).get();


    public String findGeneNameByGenomicPosition(String chromosome, String position,String strand){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(chromosome),"A chromosome name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(position),"A chromosome start position is required");
        if(Strings.isNullOrEmpty(strand)){
            strand = "1";
        }
        if (chromosome.toUpperCase().equals("X")) {
            chromosome = "23";
        } else if (chromosome.toUpperCase().equals("Y")) {
            chromosome = "24";
        }
        Double x = DoubleValidator.getInstance().validate(chromosome) ;
        // negate the position if it relates to the negative strand
        Double y = (strand.equals("1")) ? DoubleValidator.getInstance().validate(position):
                DoubleValidator.getInstance().validate(position) * -1.0D;
        if( null ==x  || null ==y ){
            logger.error("Invalid chromosome or position was provided");
            return "";
        }

        Entry<String,Rectangle> result = null;
        try {
            result = this.genePositionTree.search(Point.create(x, y))
                    .toBlocking().first();
        } catch (Exception e) {
            return StagingCommonNames.INTERGENIC; // a gene name wasn't found
        }
        return result.value();
    }

    @Override
    public String symbolToEntrezID(String geneSymbol)  {
        if(!Strings.isNullOrEmpty(geneSymbol)) {
            return (this.entrezMap.containsKey(geneSymbol)) ? this.entrezMap.get(geneSymbol) : "";
        }
        return "";
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

    /*
  inner class responsible for creating an RTree based on a file of genomic entities and
  their chromosome name, position, and strand
   */
    public class GenomicNameSupplier implements Supplier<RTree<String,Rectangle>> {
        private InputStreamReader reader;
        private final String ENSEMBL_GENE_FILE = "/ensembl_gene_list.tsv";
        private RTree<String, Rectangle> genePositionTree ;

        public GenomicNameSupplier() {
            reader = new InputStreamReader((this.getClass().getResourceAsStream(ENSEMBL_GENE_FILE)));
            this.genePositionTree = RTree.create();
        }

        @Override
        public RTree<String, Rectangle> get() {
            try {
                final CSVParser parser = new CSVParser(this.reader, CSVFormat.TDF.withHeader());
                Observable<Tuple2<String, Optional<Rectangle>>> source = Observable.from(parser)
                        // filter out coordinates not associated with nuclear chromosomes
                        .filter(new Func1<CSVRecord, Boolean>() {
                            @Override
                            public Boolean call(CSVRecord record) {
                                return (StagingCommonNames.validChromosomeSet.contains(record.get("Chromosome")));
                            }
                        })
                        // transform chromosome coordinates to RTree Rectangles
                        .map(new Func1<CSVRecord, Tuple2<String, Optional<Rectangle>>>() {
                            @Override
                            public Tuple2<String, Optional<Rectangle>> call(CSVRecord record) {
                                Optional<Rectangle> optRect = resolveGenePosition(record.get("Chromosome"),
                                        record.get("Gene Start"), record.get("Gene End"), record.get("Strand"));
                                return new Tuple2<String, Optional<Rectangle>>(record.get("Gene Name"), optRect);
                            }
                            // filter out Rectangles not associated with known genes
                        }).filter(new Func1<Tuple2<String, Optional<Rectangle>>, Boolean>() {
                            @Override
                            public Boolean call(Tuple2<String, Optional<Rectangle>> tuple) {
                                return tuple._2().isPresent();
                            }
                        });

                source.subscribe(new Subscriber<Tuple2<String, Optional<Rectangle>>>() {
                    @Override
                    public void onCompleted() {
                        logger.info("Completed RTree, size = " +  genePositionTree.size());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error(throwable.getMessage());
                    }

                    @Override
                    public void onNext(Tuple2<String, Optional<Rectangle>> nameRectTuple) {
                        genePositionTree = genePositionTree.add(nameRectTuple._1(), nameRectTuple._2().get());
                    }
                });

            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

            return this.genePositionTree;
        }


        /*
        private method to create a rectangle with zero height representing the "area" covered by
        the genomic entity - really just the length
        n.b. starting and end coordinates are reversed for features on the minus strand
         */
        private Optional<Rectangle> resolveGenePosition(String chromosome, String startPos, String endPos, String strand) {
            if(Strings.isNullOrEmpty(strand)){
                strand = "1";
            }
            Float s = FloatValidator.getInstance().validate(strand);
            Float y1 ;
            Float y2;
            if (s > 0.0) {
                y1 = FloatValidator.getInstance().validate(startPos);
                y2 = FloatValidator.getInstance().validate(endPos);
            } else {
                y1 = FloatValidator.getInstance().validate(endPos) * s;
                y2 = FloatValidator.getInstance().validate(startPos) *s;
            }

            // convert  X & Y chromosome to numeric values (X=22, Y= 23)
            if (chromosome.toUpperCase().equals("X")) {
                chromosome = "23";
            } else if (chromosome.toUpperCase().equals("Y")) {
                chromosome = "24";
            }
            Float x1 = FloatValidator.getInstance().validate(chromosome);
            if (null == x1 || null == y1 || null == y2) return Optional.absent();
            Float x2 = x1 ; // pseudo rectangle
            return Optional.of(Rectangle.create(x1, y1, x2, y2));
        }
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
    public static void main (String...args){
        GeneSymbolIDMapper test = new GeneSymbolIDMapper();
        logger.info(test.findGeneNameByGenomicPosition("1", "42817156", "1")); //ERMAP
        logger.info(test.findGeneNameByGenomicPosition("1", "42817156", "-1")); //CCDC23
        logger.info(test.findGeneNameByGenomicPosition("8", "13566900", "1")); //C8orf48
        logger.info(test.findGeneNameByGenomicPosition("8", "1000", "1")); // intergenic
        logger.info(test.findGeneNameByGenomicPosition("X", "91982655", "-1")); //VDAC1P3
        logger.info(test.findGeneNameByGenomicPosition("Y", "6246223","1")); //TSPY2

    }

    }
