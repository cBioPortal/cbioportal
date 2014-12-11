package org.mskcc.cbio.importer.icgc.app;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.internal.Preconditions;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.support.IcgcMetadataService;
import org.mskcc.cbio.importer.model.IcgcMetadata;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

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
 * Created by criscuof on 12/10/14.
 */

/*
represents the core processing engine for fetching files for registered ICGC studies and transforming their
contents to importer staging files. All registered ICGC studies are processed with each invocation and
any existing staging files are replaced.
Staging file locations are determined by the corressponding specification in the icgc worksheet
 */
public class IcgcStudyProcessor {
    private static Logger logger = Logger.getLogger(IcgcStudyProcessor.class);
    private static final Integer defaultThreadCount = 4;
    private static final Integer maxThreadCount = 8;
    private ListeningExecutorService service;
    private final Path baseStagingPath;
    private final List<IcgcMetadata> metadataList;

    /*
    default constructor using default thread count
     */
    public IcgcStudyProcessor(Path aPath) {
        Preconditions.checkArgument(null != aPath,
                "A base path for ICGC staging files is required");
        this.baseStagingPath = aPath;
        this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(defaultThreadCount));
        this.metadataList  = IcgcMetadataService.INSTANCE.getIcgcMetadataList();
    }

    public IcgcStudyProcessor(Path aPath, Integer threadCount){
        this(aPath);
        Integer tc = (threadCount > 0 && threadCount <= maxThreadCount) ? threadCount : defaultThreadCount;
        this.service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(tc));
        logger.info("tread count set to " + tc);
    }

}
