package org.mskcc.cbio.importer.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;

import java.lang.reflect.InvocationTargetException;
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
 * Created by criscuof on 2/1/15.
 */
public class OncotreePropertiesMetadata {
    /*
    basic pojo to represent data in oncotree_properties worksheet
    field names selected to facilitate loading directly from worksheet
     */
    private static final String worksheetName = MetadataCommonNames.Workseet_OncotreeProperties;
    private static final Logger logger = Logger.getLogger(OncotreePropertiesMetadata.class);

    private final  String node;
    private final String attribute;
    private final  String value;

     /*
    constructor based on row from google worksheet
     */
    public OncotreePropertiesMetadata(Map<String,String> worksheetRowMap){
        Preconditions.checkArgument(null != worksheetRowMap, "A google worksheet map is required");
        this.node = worksheetRowMap.get("node");
        this.attribute = worksheetRowMap.get("attribute");
        this.value = worksheetRowMap.get("value");

    }

    public String getNode() {
        return node;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    public static List<OncotreePropertiesMetadata> getOncotreePropertiesMetadataByAttributeValue(String attributeValue){
        List<OncotreePropertiesMetadata> metadataList = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(attributeValue)){
            Map<Integer,Map<String,String>>metadataRowMap =
                    ImporterSpreadsheetService.INSTANCE.getWorksheetTableByName(worksheetName).rowMap();
            for(Integer key : metadataRowMap.keySet()){
                Map<String,String> rowMap = metadataRowMap.get(key);
                if( rowMap.get("attribute").equals(attributeValue)){
                    metadataList.add(new OncotreePropertiesMetadata(rowMap));}

            }
        }
        return metadataList;
    }

    public static void main(String...args){
        List<OncotreePropertiesMetadata> metadataList = OncotreePropertiesMetadata.
                getOncotreePropertiesMetadataByAttributeValue("HTML_COLOR_NAME");
        if(!metadataList.isEmpty()){
            logger.info("Found " + metadataList.size() + " colors");
            for (OncotreePropertiesMetadata metadata : metadataList){
                logger.info(metadata.getNode());
            }

        }
    }
}
