package org.mskcc.cbio.importer.util;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.model.MetadataCommonNames;
import org.mskcc.cbio.importer.model.OncotreeSrcMetadata;
import javax.annotation.Nullable;
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
 * Created by criscuof on 2/23/15.
 */
public enum CancerTypeResolver {
    /*
    Singleton that provides a mapping between a detailed cancer type and a
    general cancer type. Used for determining the cancer_type column in data_clinical.txt files
     */
    INSTANCE;
    private final static Logger logger = Logger.getLogger(CancerTypeResolver.class);
    private Map<String,String> cancerTypeMap = Suppliers.memoize(new CancerTypeMapSupplier()).get();

    public Optional<String>  resolveCancerTypeByCancerDetailedType(String detailedType){
        if(Strings.isNullOrEmpty(detailedType)){
            return Optional.absent();
        }
        if(cancerTypeMap.containsKey(detailedType)){
            return Optional.of(cancerTypeMap.get(detailedType));
        }
        logger.info("Detailed cancer type " +detailedType +" is not registered");
        return Optional.absent();
    }

    private class CancerTypeMapSupplier implements Supplier<Map<String,String>>{
        CancerTypeMapSupplier() {
        }

        /*
        Function to return first part of a two part oncotree entry
        e.g input = Uterine Mucinous Carcinoma (UMC) output = Uterine Mucinous Carcinoma
         */
        Function<String,String> parseDetailedType = new Function<String,String>() {
            @Nullable
            @Override
            public String apply(String input) {
                return  (input.indexOf("(")>0 )
                        ? input.substring(0, input.indexOf("(")).trim():
                        input;
            }
        };

        @Override
        public Map<String, String> get() {
            /*
            create a map of unique secondary, tertiary and quaternary oncotree values as keys and
            meta main types as values
            supports resolution of meat main type from a secondary, tertiary or quaternary oncotree value
             */
            Map<String,String> cancerTypeMap = Maps.newHashMap();
            Table<Integer, String, String> oncoTable = ImporterSpreadsheetService.
                    INSTANCE.getWorksheetTableByName(MetadataCommonNames.Worksheet_OncotreeSrc);
            Set<Integer> rowSet = oncoTable.rowKeySet();
            for (Integer rowKey : rowSet) {
               OncotreeSrcMetadata md = new OncotreeSrcMetadata(oncoTable.row(rowKey));
                for (String s : Lists.newArrayList(md.getSecondary(), md.getTertiary(), md.getQuaternary())){
                    if (!Strings.isNullOrEmpty(s)) {
                        cancerTypeMap.put(parseDetailedType.apply(s), md.getMetamaintype());
                    }
                }
            }
            return cancerTypeMap;
        }
    }

    public static void main (String...args){
        try{
        String validSecondaryType = "Uterine Sarcoma / Mesenchymal";
        logger.info("detailed = " + validSecondaryType +" type = " +CancerTypeResolver
                .INSTANCE.resolveCancerTypeByCancerDetailedType(validSecondaryType).get());
        String validQuaternaryType = "High Grade Endometrial Stromal Sarcoma";
        logger.info("detailed = " + validQuaternaryType +" type = " +CancerTypeResolver
                .INSTANCE.resolveCancerTypeByCancerDetailedType(validQuaternaryType).get());
        String invalidType = "XXXXXXXXX";

            logger.info("detailed = " +invalidType +" type = " +CancerTypeResolver
                    .INSTANCE.resolveCancerTypeByCancerDetailedType(invalidType).get());
        } catch(Exception e) {
            logger.error(e.getMessage()); // error getting from absent Optional
        }

    }
}
