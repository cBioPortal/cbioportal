package org.mskcc.cbio.importer.cvr.darwin.util;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.darwin.util.deid.Deid;
import org.mskcc.cbio.importer.cvr.darwin.util.deid.DeidMapper;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import javax.annotation.Nullable;
import java.util.*;

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
 * Created by criscuof on 11/20/14.
 */
public enum IdMapService {
    INSTANCE;
    static  final Logger logger = Logger.getLogger(IdMapService.class);
    private Multimap<Integer,String> idMap = Suppliers.memoize(new IdMapSupplier()).get();

    public Multimap<Integer,String> getIdMap() {
        return idMap;
    }

    /*
    public method to return a Set of valid Darwin ids
     */
    public final Set<Integer> getDarwinIdSet(){
        return this.idMap.keySet();

    }

    /*
    public method to return a list of valid Darwin ids as a csv list
    enclosed in parenthesis
    for use in myBatis selects for groups
     */
    public List<Integer> getDarwinIdList() {
        return Lists.newArrayList(this.idMap.keySet());
    }

    public final Collection<String> getSampleIdsByDarwinId(Integer darwinId){
        Preconditions.checkArgument(null != darwinId && darwinId > 0,
                "A valid Darwin deidentification id is required");
         return (this.idMap.containsKey(darwinId))?
            idMap.get(darwinId) : new ArrayList<String>();
    }
    /*
    public method to format the collection of DMP sample ids to a
     csv string for display
     */
    public final String displayDmpIdsByDarwinId(Integer darwinId){
        Collection<String> ids = this.getSampleIdsByDarwinId(darwinId);
        if (null != ids && ids.size()>0){
            return StagingCommonNames.commaJoiner.join(ids);
        }
        return "";
    }

    public final Integer resolveDarwinIdBySampleId(String sampleId){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleId),
                "A DMP sample ID is required");
       if ( this.idMap.containsValue(sampleId)){
           for(Map.Entry<Integer,String> entry: idMap.entries()){
               if(entry.getValue().equals(sampleId)){
                   return entry.getKey();
               }
           }
        }
        logger.info("ID Map does not contain DMP Sample ID: " + sampleId);
        return 0;
    }

    // main class for standalone testing
    public static void main (String...args) {
        Multimap<Integer,String> idMap = IdMapService.INSTANCE.getIdMap();
        logger.info("ID map has "+idMap.size() +" entries");
        // get sample ids for a specific darwin id
        for(String sampleId :IdMapService.INSTANCE.getSampleIdsByDarwinId(308814)) {
            logger.info("darwin id 308814 sample id " +sampleId);
            // test reverse
            logger.info("Darwin Id for sample id " + sampleId +" is " + IdMapService.INSTANCE.resolveDarwinIdBySampleId(sampleId));
            logger.info("CSV for sample id " +IdMapService.INSTANCE.displayDmpIdsByDarwinId(308814));
        }
    }

    private class IdMapSupplier implements Supplier<Multimap<Integer,String>> {
        private final Logger logger = Logger.getLogger(IdMapSupplier.class);
        //TODO make map dimensions properties
        private Multimap<Integer, String> idMap = HashMultimap.create(10000, 50);
        public IdMapSupplier() {
        }

        @Override
        public Multimap<Integer, String> get() {
           logger.info("IdMapSupplier get invoked");
            SqlSession session = DarwinSessionManager.INSTANCE.getDarwinSession();
            try {
                DeidMapper mapper = session.getMapper(DeidMapper.class);
                for (Deid deid : mapper.getAllDeids()) {
                    if(null != deid.getDeidentificationid() && deid.getDeidentificationid() >0 ) {
                        idMap.put(deid.getDeidentificationid(), deid.getSampleid());
                    }
                }
            } catch (Exception e) {
               System.out.println(e.getMessage());
                e.printStackTrace();
            }
            return this.idMap;
        }

    }

}
