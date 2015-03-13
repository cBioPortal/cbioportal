package org.mskcc.cbio.importer.darwin;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;
import java.util.List;
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
 * Created by criscuof on 1/27/15.
 */
public class ReadDarwinExcelTest {
    /*
    POC to read in Darwin id mapping Excel file
     */
    private static final Logger logger = Logger.getLogger(ReadDarwinExcelTest.class);

    public static void main(String... args) {
        try {
            /*
            ClassLoader classLoader = getClass().getClassLoader();
	File file = new File(classLoader.getResource("file/test.xml").getFile());
             */
            ClassLoader classLoader = ReadDarwinExcelTest.class.getClassLoader();
            File file = new File(classLoader.getResource("/DMP IDs to Darwin DEID.xlsx").getFile());
            //FileInputStream stream = new FileInputStream(ReadDarwinExcelTest.class.getResourceAsStream("/DMP IDs to Darwin DEID.xlsx"));
            FileInputStream fs = new FileInputStream(file);
            //Create Workbook instance holding reference to .xlsx file


                    XSSFWorkbook workbook = new XSSFWorkbook(fs);

                    //Get first/desired sheet from the workbook
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    Map<String, String> dmpToDarwinIdMap = Maps.newHashMap();

                    //Iterate through each rows one by one
                    List<Row> rowList = Lists.newArrayList(sheet.iterator());

                    for( Row row :rowList) {
                        //For each row, iterate through all the columns
                        List<Cell> cellList = Lists.newArrayList(row.cellIterator());
                        // add contents to Map
                        String dmpSampleId = (!Strings.isNullOrEmpty(cellList.get(1).getStringCellValue())) ?
                                cellList.get(1).getStringCellValue() : cellList.get(2).getStringCellValue();
                        String darwinId = cellList.get(0).toString().replace(".0", "");
                        dmpToDarwinIdMap.put(dmpSampleId, darwinId);
                        logger.info(dmpSampleId + " " + darwinId);

                    }
                    // execute some tests
                    logger.info("Map size "+dmpToDarwinIdMap.size());
                    logger.info("DMP ID = P-0002573-T01-IM3 darwin id = "+dmpToDarwinIdMap.get("P-0002573-T01-IM3"));

                    fs.close();

                }catch(Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }


    }




}
