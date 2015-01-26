package org.mskcc.cbio.importer.model;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.internal.Maps;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import scala.Tuple2;

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
 * Created by criscuof on 1/23/15.
 */
public enum OncotreeMap {
    INSTANCE;

    private static final Logger logger = Logger.getLogger(OncotreeMap.class);

    private Map<String,String> oncoMap = Suppliers.memoize(new OncotreeMapSupplier()).get();

    public Optional<String> getTumorTypeByOncotreeCode(String code){
        if(Strings.isNullOrEmpty(code) || !oncoMap.containsKey(code)){ return Optional.absent();}
        return Optional.of(oncoMap.get(code));
    }

    private class OncotreeMapSupplier implements Supplier<Map<String,String>> {
        private static final String worksheetName = MetadataCommonNames.Worksheet_OncotreeSrc;

        @Override
        public Map<String, String> get() {
            Map<String,String> oncoMap = Maps.newTreeMap();

            for (String colName : ImporterSpreadsheetService.INSTANCE.getWorksheetColumnNames(worksheetName)){
                for (String cellValue : ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName(worksheetName,colName)){
                    Tuple2<String,String> tuple2 = this.parseTumorTypeFromOncotreeEntry(cellValue);
                    if (null != tuple2) {
                        oncoMap.put(tuple2._1(), tuple2._2());
                    }
                }
            }
            return oncoMap;
        }

        private Tuple2<String,String> parseTumorTypeFromOncotreeEntry(String entry){
            int openParenPos = entry.indexOf('(');
            int closeParenPos = entry.indexOf(')');
            if ( openParenPos > 0 && closeParenPos > 0) {
                return new Tuple2<>(entry.substring(openParenPos, closeParenPos).trim(),
                        entry.substring(openParenPos + 1, closeParenPos).trim());
            }
            return null;
        }
    }

    public static void main (Strings...args){
        // quaternary test
        logger.info("Quaternary type: " +OncotreeMap.INSTANCE.getTumorTypeByOncotreeCode("GSARC"));
        //tertiary test
        logger.info("Tertiary test: " + OncotreeMap.INSTANCE.getTumorTypeByOncotreeCode("CLNC"));
        //secondary test
        logger.info("Secondary test " + OncotreeMap.INSTANCE.getTumorTypeByOncotreeCode("MCCA"));
    }
}
