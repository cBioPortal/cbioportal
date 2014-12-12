package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.model.IcgcMetadata;

import javax.annotation.Nullable;
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
 * Created by criscuof on 12/9/14.
 */
public enum IcgcMetadataService {
    INSTANCE;
    private static final String icgcWorksheetName ="icgc";
    private static final String studyIdColumnName = "icgcid";
    private static final Logger logger = Logger.getLogger(IcgcMetadataService.class);

    public IcgcMetadata getIcgcMetadataById(String studyID) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(studyID),
                "An ICGC ID is required");
        Optional<Map<String,String >> rowOptional = ImporterSpreadsheetService.INSTANCE.getWorksheetRowByColumnValue(icgcWorksheetName, studyIdColumnName,
                studyID);
        if (rowOptional.isPresent()){
            return( new IcgcMetadata(rowOptional.get()));
        }
        return null;
    }

    /*
    public method to return a List of non-US ICGC studies registered in the cbio-portal
     */
    public List<String> getRegisteredIcgcStudyList() {
        return ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName("icgc","icgcid");
    }


    /*
    public method to generate a Map of all ICGC metadata entries from the importer spreadsheet
     keyed by the ICGC ID attribute
     */
    public List<IcgcMetadata> getIcgcMetadataList() {

        return FluentIterable.from(ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName("icgc","icgcid"))
                .transform(new Function<String, IcgcMetadata>() {
                    @Nullable
                    @Override
                    public IcgcMetadata apply(String studyId) {
                        return getIcgcMetadataById(studyId);
                    }
                }).toList();
    }

    /*
    main method for testing
     */
    public static void main (String...args){
        String icgcId = "LICA-FR";
        IcgcMetadata meta = IcgcMetadataService.INSTANCE.getIcgcMetadataById(icgcId);
        logger.info("download directory " +meta.getDownloaddirectory());
        for (String studyId : IcgcMetadataService.INSTANCE.getRegisteredIcgcStudyList()){
            logger.info(studyId);
        }
    }

}
