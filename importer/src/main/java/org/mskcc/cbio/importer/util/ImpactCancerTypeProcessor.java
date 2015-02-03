package org.mskcc.cbio.importer.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import edu.stanford.nlp.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import scala.Tuple2;
import scala.Tuple3;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Created by criscuof on 2/2/15.
 */

/*
represents a Java application to compare legacy Impact cancer type data
with current oncotree cancer types
both cancer_type and cancer_type_detail attributes from the data_clinical.txt files
are evaluated

 */
public class ImpactCancerTypeProcessor {
    private final static Logger logger = Logger.getLogger(ImpactCancerTypeProcessor.class);
    private  Set<String> oncotreeTypeSet = Sets.newHashSet();
    private Map<String, Tuple2<String,Integer>> cancerTypeMap = Maps.newTreeMap();
    private Map<String, Tuple2<String,Integer>> cancerTypDetailedMap = Maps.newTreeMap();
    private Map<String,String> oncoTreeTypeToCodeMap = Maps.newHashMap();
    private Multimap<String,String> dmpSampleMap = ArrayListMultimap.create();
    private static final Splitter slashSplitter = Splitter.on('/');
    private Map<String,String> detailTypeToTypeMap = Maps.newHashMap();

    public ImpactCancerTypeProcessor (File inFile, File outFile) {
        Preconditions.checkArgument(null != inFile,
                "A data_clinical.txt file is required");
        Preconditions.checkArgument(null != outFile,
                "An output file is required");
        this.completeOncotreeTypeSet();
        this.processClincalFile(inFile);
        this.generateReport(outFile.toPath());
    }

    private void processClincalFile(final File clinicalFile){
        try (FileReader reader = new FileReader(clinicalFile)) {
            final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
            Observable<Tuple3<String,String,String>> source = Observable.from(parser)
                    .filter(new Func1<CSVRecord, Boolean>() {
                        @Override
                        public Boolean call(CSVRecord record) {
                           return !record.get("CANCER_TYPE_DETAILED").equals("NA");
                        }
                    })
                   .map(new Func1<CSVRecord, Tuple3<String, String, String>>() {
                            @Override
                            public Tuple3<String, String, String> call(CSVRecord record) {
                                return new Tuple3<String, String, String>(record.get("SAMPLE_ID"),
                                        record.get("CANCER_TYPE"),
                                        record.get("CANCER_TYPE_DETAILED"));
                            }
                        }
                   );
            source.subscribe(new Subscriber<Tuple3<String, String, String>>() {
                @Override
                public void onCompleted() {
                    logger.info("Clinical file processing completed");
                    logger.info("There are "+ cancerTypeMap.size() +" unique cancer types");
                    logger.info("There are " +cancerTypDetailedMap.size() +" unique cancer type details");
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.error(throwable.getMessage());
                    throwable.printStackTrace();
                }

                @Override
                public void onNext(Tuple3<String, String, String> clinicalTuple) {
                    String sampleId = clinicalTuple._1();
                    String cancerType = clinicalTuple._2();
                    String cancerTypeDetail = clinicalTuple._3();
                    Tuple2<String,Integer> matchCancerTuple = findClosestTypeMatch(cancerType);
                    Tuple2<String,Integer> matchDetailTuple = findClosestTypeMatch(cancerTypeDetail);
                    if (!cancerTypDetailedMap.containsKey(cancerTypeDetail)){
                        cancerTypDetailedMap.put(cancerTypeDetail, matchDetailTuple);
                        cancerTypeMap.put(cancerType, matchCancerTuple);
                        detailTypeToTypeMap.put(cancerTypeDetail, cancerType);
                    }
                    dmpSampleMap.put(cancerTypeDetail, sampleId);
                }
            });
        } catch ( Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateReport(final Path outputPath) {
        final List<String> lineList = Lists.newArrayList();
        lineList.add(StagingCommonNames.tabJoiner.join("DMP Cancer Type Detail",
                "Closest OncoTree Match"," Levenshstein Distance",
                "DMP Cancer Type" ,"Closest OncoTree Match", " Levenshstein Distance",
                "DMP Sample IDs"   ));
        Observable<String> lineSource = Observable.from(this.cancerTypDetailedMap.entrySet())
                .map(new Func1<Map.Entry<String, Tuple2<String, Integer>>, String>() {
                    @Override
                    public String call(Map.Entry<String, Tuple2<String, Integer>> entry) {
                        String dmpDetailedType = entry.getKey();
                        String oncoDetailMatch = oncoTreeTypeToCodeMap.get(entry.getValue()._1());
                        Integer detailDistance = entry.getValue()._2();
                        String dmpCancerType = detailTypeToTypeMap.get(dmpDetailedType);
                        Tuple2<String, Integer> typeTuple = cancerTypeMap.get(dmpCancerType);
                        String oncoTypeMatch = oncoTreeTypeToCodeMap.get(typeTuple._1());
                        Integer typeDistance = typeTuple._2();
                        String samples = StagingCommonNames.commaJoiner.join(dmpSampleMap.get(dmpDetailedType));
                        return StagingCommonNames.tabJoiner.join(dmpDetailedType,
                                oncoDetailMatch,detailDistance.toString(),
                                dmpCancerType,
                                oncoTypeMatch, typeDistance.toString(),
                                samples);
                    }
                });lineSource.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                logger.info("Completed report available at " +outputPath.getFileName());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
                throwable.printStackTrace();}

            @Override
            public void onNext(String s) {
                lineList.add(s);
            }
        });
        try {
            Files.write(outputPath, lineList, Charset.defaultCharset());
            logger.info(lineList.size()-1 +" cancer detail types reported"); // subtract header
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Tuple2<String,Integer> findClosestTypeMatch(String type){
        Tuple2<String,Integer> matchTuple = new Tuple2<String,Integer>("XXXXXXX",Integer.MAX_VALUE);
        for(String oncoTreeType : this.oncotreeTypeSet) {
            Integer distance = StringUtils.editDistance(type.toLowerCase(), oncoTreeType.toLowerCase());
            if (distance < matchTuple._2()){
                matchTuple = new Tuple2<>(oncoTreeType,distance);
            }
        }
        return matchTuple;
    }

    private void completeOncotreeTypeSet() {
        Table<Integer, String, String> oncoTable = ImporterSpreadsheetService.
                INSTANCE.getWorksheetTableByName("oncotree_src");
        Set<Integer> rowSet = oncoTable.rowKeySet();
        for (Integer rowKey : rowSet){
            for(Map.Entry<String,String> entry :oncoTable.row(rowKey).entrySet()){
                String s1 = entry.getValue();
                String s2 = (s1.indexOf("(")>0 )? s1.substring(0,s1.indexOf("(") ):
                        s1;
                // save mapping for display
                this.oncotreeTypeSet.add(s2.trim());
                this.oncoTreeTypeToCodeMap.put(s2.trim(), s1);
            }
        }
    }

    public static void main(String...args){
        ImpactCancerTypeProcessor processor = new ImpactCancerTypeProcessor(new File("/tmp/data_clinical.txt"),
                new File("/tmp/cancert_type.tsv"));
        for (String s : Lists.newArrayList(processor.oncotreeTypeSet)){
           // logger.info(s);
        }
    }
}
