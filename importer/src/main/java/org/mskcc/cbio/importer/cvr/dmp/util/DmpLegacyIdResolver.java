package org.mskcc.cbio.importer.cvr.dmp.util;

import com.google.common.base.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Subscriber;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
 * Created by criscuof on 2/19/15.
 */
/*
Represents a service that will resolve legacy DMP sample and patient IDs to new values and vice versa
Intended to support instances where a resource still retains a legacy id
Requires that both a legacy to new patient id file and a legacy to new sample id be available in
the classpath
 */
public enum DmpLegacyIdResolver {
    INSTANCE;
    private static final Logger logger = Logger.getLogger(DmpLegacyIdResolver.class);
    private Map<String,String> patientIdMap = Suppliers.memoize(new IdMapSupplier(PATIENT_TYPE)).get();
    private Map<String,String> sampleIdMap = Suppliers.memoize(new IdMapSupplier(SAMPLE_TYPE)).get();
    private static final String PATIENT_TYPE = "patient";
    private static final String SAMPLE_TYPE = "sample";


    /*
    Public method to lookup the new patient id for a specified legacy patient id
     */
    public Optional<String> resolveNewPatientIdFromLegacyPatientId(String legacyId){
        if(Strings.isNullOrEmpty(legacyId)) {return Optional.absent();}
        if(patientIdMap.containsKey(legacyId))
        {
            return Optional.of(patientIdMap.get(legacyId));
        }
        //logger.error("Legacy patient id " +legacyId +" was not found in patient id map");
        return Optional.absent();
    }
    /*
    Public method to lookup the new sample id for a specified legacy sample id
     */
    public Optional<String> resolveNewSampleIdFromLegacySampleId(String legacyId){
        if(Strings.isNullOrEmpty(legacyId)) {return Optional.absent();}
        if(sampleIdMap.containsKey(legacyId))
        {
            return Optional.of(sampleIdMap.get(legacyId));
        }
       // logger.error("Legacy sample id " +legacyId +" was not found in sample id map");
        return Optional.absent();
    }

    /*
    private class IdMapSupplier supplies a legacy->new DMP patient id map or
    a legacy->new DMP sample id map depending upon the constructor's type argument
    mod 11Mar2015 change Map to Google Guava BiMap to eliminate duplicate vlaues
     */
    private  class IdMapSupplier implements Supplier<Map<String,String>> {
        private final String mapType;
        private  final Set<String>  TYPE_SET = Sets.newHashSet(PATIENT_TYPE,SAMPLE_TYPE);
        private static final String PATIENT_ID_MAP_FILE ="/patientLookup.txt";
        private static final String SAMPLE_ID_MAP_FILE = "/sampleLookup.txt";
        private final Logger logger = Logger.getLogger(IdMapSupplier.class);


        IdMapSupplier(String aType){
            Preconditions.checkArgument(!Strings.isNullOrEmpty(aType) && TYPE_SET.contains(aType),
                    "A valid map type is required");
            this.mapType = aType;
        }
        @Override
        public Map<String, String> get() {
            String inputFileName = (this.mapType.equals(PATIENT_TYPE))?
                    PATIENT_ID_MAP_FILE : SAMPLE_ID_MAP_FILE;
           return this.initializeIdMap(inputFileName);
        }
        private Map<String,String> initializeIdMap(final String idFile){
            final BiMap<String,String> idMap = HashBiMap.create();
            try (Reader reader = new InputStreamReader((this.getClass().getResourceAsStream(idFile)));){

                final CSVParser parser = new CSVParser(reader, CSVFormat.TDF.withHeader());
                Observable<CSVRecord> recordObservable = Observable.from(parser.getRecords());
                recordObservable.subscribe(new Subscriber<CSVRecord>() {
                    @Override
                    public void onCompleted() {
                       logger.info("Processed " + idMap.size() + " ids in " + idFile);
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        logger.error(throwable.getMessage());
                        throwable.printStackTrace();
                    }
                    @Override
                    public void onNext(CSVRecord record) {
                        String legacyId = record.get("legacy_id");
                        String newId = record.get("new_id");
                        if (!idMap.containsKey(legacyId)) {
                            //idMap.put(record.get("legacy_id"), record.get("new_id"));
                           String oldValue=  idMap.forcePut(record.get("legacy_id"), record.get("new_id"));
                            if(!Strings.isNullOrEmpty(oldValue)) {
                                logger.info(">>>>Duplicate new id " +oldValue +"  replaced in id map");
                            }
                        } else {
                            logger.error("legacy id: " +legacyId +" is repeated in " +idFile +" mapped to "
                                    +idMap.get(legacyId) +" and "  +newId);
                        }
                    }
                });

            } catch (IOException e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
            return idMap;
        }



    }
    // main method for standalone testing
    public static void main (String...args){

        // look up patient DMP0099
        logger.info("The new id for patient DMP0099 is "
                +DmpLegacyIdResolver.INSTANCE.resolveNewPatientIdFromLegacyPatientId("DMP0099").get());
        // look up patient DMP0099
        logger.info("The new id for sample DMP2178 is "
                +DmpLegacyIdResolver.INSTANCE.resolveNewSampleIdFromLegacySampleId("DMP2178").get());
        // lookup a non-existent sample
        String badSampleId = "DMPXXXX";
        if (DmpLegacyIdResolver.INSTANCE.resolveNewSampleIdFromLegacySampleId(badSampleId).isPresent()) {
            logger.info("The new id for non-existent sample DMPXXXX is "
                    +DmpLegacyIdResolver.INSTANCE.resolveNewSampleIdFromLegacySampleId(badSampleId).get());
        } else {
            logger.error(badSampleId +" is not a valid legacy sample id");
        }



    }

}
