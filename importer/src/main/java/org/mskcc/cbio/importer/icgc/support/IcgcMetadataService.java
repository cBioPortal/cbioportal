package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;

import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.IcgcMetadata;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

    private LoadingCache<String, IcgcMetadata>  icgcMetadataCache;

    //TODO: change return type to Optional
    public IcgcMetadata getIcgcMetadataById(String icgcId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(icgcId),
                "An ICGC ID is required");
        if (null == this.icgcMetadataCache) { this.intitializeIcgcMetadataCache(); }
        try {
            return this.icgcMetadataCache.get(icgcId);
        } catch (ExecutionException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return null;

    }

    private void intitializeIcgcMetadataCache() {

       this.icgcMetadataCache =CacheBuilder.newBuilder()
               .maximumSize(100)
               .expireAfterAccess(10, TimeUnit.MINUTES)
               .build( new CacheLoader<String,IcgcMetadata>(){

                   @Override
                   public IcgcMetadata load(String key) throws Exception {
                       Optional<Map<String,String >> rowOptional = ImporterSpreadsheetService.INSTANCE.getWorksheetRowByColumnValue(icgcWorksheetName,
                               studyIdColumnName,
                               key);
                       if (rowOptional.isPresent()){
                           return( new IcgcMetadata(rowOptional.get()));
                       }
                       return null;
                   }
               });
        logger.info("IcgcMetadataCache has been initalized");


    }

    /*
    utility method to resolve the download directory for a specified ICGC study
     */
    public Optional<String> getCancerStudyPathByStudyId(String studyId ){
        if (Strings.isNullOrEmpty(studyId)) { return Optional.absent(); }
        IcgcMetadata icgcMeta = this.getIcgcMetadataById(studyId);
        if ( null != icgcMeta){
            Optional<CancerStudyMetadata> opt = CancerStudyMetadata.findCancerStudyMetaDataByStableId(icgcMeta.getStudyname());
            if (opt.isPresent()){
                return Optional.of(opt.get().getStudyPath());
            }
        }
        logger.info("Unable to find a study path for ICGC study " + studyId);
        return Optional.absent();
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
            IcgcMetadataService.INSTANCE.getIcgcMetadataById(studyId);

            logger.info(studyId + " cache size = " +IcgcMetadataService.INSTANCE.icgcMetadataCache.size());
        }
        // test for resolving cancer path from icgc study id
        Optional<String> pathOpt = IcgcMetadataService.INSTANCE.getCancerStudyPathByStudyId("BRCA-UK");
        if(pathOpt.isPresent()){
            logger.info("Path = " +pathOpt.get());
        }
    }

}
