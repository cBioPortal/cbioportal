package org.mskcc.cbio.importer.cvr.darwin.util;

import com.google.common.base.*;
import com.google.common.collect.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mskcc.cbio.importer.cvr.darwin.util.deid.Deid;
import org.mskcc.cbio.importer.cvr.darwin.util.deid.DeidMapper;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import scala.Tuple2;

import java.io.File;
import java.io.FileInputStream;
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
    //mod 27Jan2015 - use Excel-based ID map until database mapping is restored
    //private Multimap<Integer,String> idMap = Suppliers.memoize(new IdMapSupplier()).get();
    private Multimap<Integer,String> idMap = Suppliers.memoize(new ExcelIdMapSupplier()).get();
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

    public boolean isSampleIdInDarwin(String sampleId){
        return (!Strings.isNullOrEmpty(sampleId) && this.idMap.containsValue(sampleId));
    }

    /*
    public method to determine if either the current or legacy ids is in darwin
     */
    public boolean isSampleIdInDarwin(Tuple2<String,String> idTuple){
        if (null != idTuple) {
            return( isSampleIdInDarwin(idTuple._1()) || isSampleIdInDarwin(idTuple._2()));
        }
        return false;
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
        for(String sampleId :IdMapService.INSTANCE.getSampleIdsByDarwinId(1672517)) {
            logger.info("darwin id 1672517 sample id " +sampleId);
            // test reverse
            logger.info("Darwin Id for sample id " + sampleId +" is " + IdMapService.INSTANCE.resolveDarwinIdBySampleId(sampleId));
            logger.info("CSV for sample id " +IdMapService.INSTANCE.displayDmpIdsByDarwinId(1672517));
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
    /*
    an inner class that will generate an darwin id to dmp id map
    using a excel spreadsheet as a source.
     */
    private class ExcelIdMapSupplier implements Supplier<Multimap<Integer,String>> {
        private final Logger logger = Logger.getLogger(ExcelIdMapSupplier.class);
        private static final String DARWIN_ID_EXCEL_FILE = "/tmp/DMP_IDs_to_Darwin_DEID.xlsx";

        private Multimap<Integer, String> idMap = HashMultimap.create(10000, 6);
        @Override
        public Multimap<Integer, String> get() {
            try {

                FileInputStream file = new FileInputStream(new File(DARWIN_ID_EXCEL_FILE));
                XSSFWorkbook workbook = new XSSFWorkbook(file);
                XSSFSheet sheet = workbook.getSheetAt(0);
                //Iterate through each rows one by one
                List<Row> rowList = Lists.newArrayList(sheet.iterator());
                // remove the header
                rowList.remove(0); // skip the column headings
                for (Row row : rowList) {
                    //For each row, iterate through all the columns
                    List<Cell> cellList = Lists.newArrayList(row.cellIterator());
                    // add contents to Map
                    String dmpSampleId = (!Strings.isNullOrEmpty(cellList.get(1).getStringCellValue())) ?
                            cellList.get(1).getStringCellValue() : cellList.get(2).getStringCellValue();
                    String darwinId = cellList.get(0).toString().replace(".0", "");
                    idMap.put(Integer.valueOf(darwinId), dmpSampleId);

                }
            } catch (Exception e){
                logger.error(e.getMessage());
                e.printStackTrace();
            }

            logger.info("Mapped " +idMap.size() +" darwin ids");
            return idMap;
        }
    }

}
