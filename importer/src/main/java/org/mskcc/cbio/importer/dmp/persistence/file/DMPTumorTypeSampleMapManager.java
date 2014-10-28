/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * 
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center 
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center 
 *  has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.dmp.persistence.file;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;
import com.google.gdata.util.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import joptsimple.internal.Strings;
import org.mskcc.cbio.importer.dmp.model.Result;

/**
 *
 * @author criscuof
 */
public class DMPTumorTypeSampleMapManager {


    private final Multimap<String, String> tumorSampleMap;
    
    private static final Joiner tabJoiner = Joiner.on("\t");

    public DMPTumorTypeSampleMapManager(Object aManager) {

        Preconditions.checkArgument(null != aManager, "A DMPStagingFileManager is required");


        this.tumorSampleMap = Suppliers.memoize(new TumorTypeMapSupplier()).get();
    }

    public void updateTumorTypeSampleMap(List<Result> resultList) {
        Preconditions.checkArgument(null != resultList && !resultList.isEmpty(),
                "A valid List of DMP Result objects is required");
        for (Result result : resultList) {
            // remove any existing entry for the same sample id
            // that means the sample is being updated and may no longer be associated 
            // with the same tumor type
            if (this.tumorSampleMap.containsValue(result.getMetaData().getDmpSampleId())) {
                this.tumorSampleMap.removeAll(result.getMetaData().getDmpSampleId());
            }
            this.tumorSampleMap.put(result.getMetaData().getTumorTypeName(),
                    result.getMetaData().getDmpSampleId());
        }
        this.persistTumorTypeMap();

    }
    /*
    private method to persist the tumor type/sample map to a file
     */
    private void persistTumorTypeMap() {
        List<String> lines = FluentIterable.from(this.tumorSampleMap.keySet()).transform(new Function<String, String>() {
            @Override
            public String apply(String key) {
                String s =FluentIterable.from(tumorSampleMap.get(key))
                        .join(tabJoiner);
                
                return (tabJoiner.join(key,s));
            }
        }).toList();

        //this.fileManager.persistDMPCaseListData(lines);
    }
    /*
     public method to return the DMP sample ids assosiated with a specified 
     tumor type. An Optional object is used to encapsulate the result to
     provide support for an empty response.
     */

    public Optional<Collection<String>> getSampleIDsByTumorType(String tumorType) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(tumorType), "A tumor type is required");
        if (this.tumorSampleMap.containsKey(tumorType)) {
            return Optional.of(this.tumorSampleMap.get(tumorType));
        }
        return Optional.absent();
    }

    public Optional<Collection<Map.Entry<String, String>>> getTumorTypeMap() {
        if (!this.tumorSampleMap.isEmpty()) {
            return Optional.of(this.tumorSampleMap.entries());
        }
        return Optional.absent();
    }

    /**
     * a private class that implements a Supplier to handle the persistence of
     * the tumor type map all DMP data
     */
    private class TumorTypeMapSupplier implements Supplier<Multimap<String, String>> {

       // private DMPStagingFileManager fileManager;
        private Multimap<String, String> tumorSampleMap;
        private final Splitter tabSplitter = Splitter.on("\t");

        TumorTypeMapSupplier() {
           // this.fileManager = aManager;
        }

        /*
         interface get() implementation
         Returns a HashMultimap representing a tumor type (key) and the DMP sample
         ids associated with that tumor type
         the get() method creates the map from data read from the tumor type file
         */
        @Override
        public Multimap<String, String> get() {
            // instantiate a new map
            /*
            this.tumorSampleMap = HashMultimap.create(500, 2000);
            List<String> tumorTypeList = this.fileManager.readDMPCaseListData();
            if (!tumorTypeList.isEmpty()) {

                for (String line : tumorTypeList) {
                    List<String> tokens = Lists.newArrayList(tabSplitter.split(line));
                    String tumorType = tokens.remove(0);
                    for (String token : tokens) {
                        this.tumorSampleMap.put(tumorType, Integer.valueOf(token));
                    }
                }

            }
                    */
            return this.tumorSampleMap;

        }

    } // end of inner class
}  // end of outer class
