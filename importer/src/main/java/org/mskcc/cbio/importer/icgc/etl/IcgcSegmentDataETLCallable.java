package org.mskcc.cbio.importer.icgc.etl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.util.concurrent.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.model.IcgcSegmentModel;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.model.IcgcMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.segment.SegmentModel;
import org.mskcc.cbio.importer.persistence.staging.segment.SegmentTransformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
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
 * Created by criscuof on 12/30/14.
 */
public class IcgcSegmentDataETLCallable extends SegmentTransformer implements Callable<String> {

    /*
    responsible for importing ICGC copy number data using a provided URL, transforming these data into
    model instances, and persisting the transformed data to a staging file
     */
    private static final Logger logger = Logger.getLogger(IcgcSegmentDataETLCallable.class);

    private final String icgcCopyNumberUrl;

    private Multimap<String, IcgcSegmentModel> copyNumberModelMap = HashMultimap.create(5000, 100);
    private Set<String>sampleSet = Sets.newTreeSet();

    public IcgcSegmentDataETLCallable(IcgcMetadata meta, Path aPath, TsvStagingFileHandler aHandler){
        super(aHandler);
        Preconditions.checkArgument(null != meta && !Strings.isNullOrEmpty(meta.getCopynumberurl()), "The ICGC metadata parameter is null or invalid");
        Preconditions.checkArgument(null != aPath,"A Path to the staging file directory is required" );
        this.icgcCopyNumberUrl = meta.getCopynumberurl();
        //this.registerStagingFileDirectory(aPath.resolve( meta.getDownloaddirectory()),meta.getStudyname());
        this.registerStagingFileDirectory(aPath,meta.getStudyname());
    }

    /*
    Callable interface method that invokes generation of a segment file for an ICGC cancer study
     */
    @Override
    public String call() throws Exception {
        this.resolveCopyNumberMap();
        List<IcgcSegmentModel> modelList = this.transformIcgcSamples();
        this.fileHandler.transformImportDataToTsvStagingFile(modelList, SegmentModel.getTransformationModel());
        return "Completed";
    }

   private  List<IcgcSegmentModel>  transformIcgcSamples() {
       List<IcgcSegmentModel> segModelList = Lists.newArrayList();
       // process each unique sample
       for(String sampleId : this.sampleSet) {
        //   logger.info("Transforming sample id: " + sampleId);
           segModelList.addAll(this.transformIcgcSample(sampleId, this.copyNumberModelMap.get(sampleId)));
       }
      // logger.info("ICGC Copy Model List contains: " +segModelList.size() +" entries");

       return segModelList;
   }

    /*
    private method to process each sample in the copy number file
    the IcgcSegmentModel objects belonging to a single study are organized into a Table where each row represents
    a chromosome and each column represents a start position for a copy number variation
    this allows each chromosome to be processed and orders the copy number variations by start position
    cnvs for each chromosome are processed in order by start position and a List of IcgcSegmentModel objects
    ordered by chromosome and start position is generated.
    this list includes default values that span chromosome regions without variation
     */
    private List<IcgcSegmentModel>  transformIcgcSample(String sampleId, Collection<IcgcSegmentModel> models) {
       
        // sort the models by start position - the source data are unsorted
        Map<String,IcgcSegmentModel> sortedModelMap = Maps.newTreeMap();
        for (IcgcSegmentModel model : models){
            sortedModelMap.put(model.getChromosome_start(),model);
        }

        Table<String,Integer,IcgcSegmentModel> cnvTable = HashBasedTable.create();
        for(IcgcSegmentModel model : models) {
            cnvTable.put(model.getChromosome(), Integer.valueOf(model.getChromosome_start()), model);
        }

        // process each chromosome (1,2,...22,X,Y)
        List<IcgcSegmentModel> modelList = Lists.newArrayList();
        for (String chr : StagingCommonNames.validChromosomeSet){
           if(cnvTable.containsRow(chr)){
                   Map<Integer, IcgcSegmentModel> chrMap =  cnvTable.row(chr);
                  modelList.addAll(this.resolveChromosomeMap(sampleId, chr, cnvTable.row(chr).values()));

           } else {
               modelList.add(this.createDefaultCopyNumberModel(sampleId, chr, "1",
                       StagingCommonNames.chromosomeLengthMap.get(chr.toUpperCase()).toString()));
           }
        }
       // logger.info("The model list for sample: " + sampleId +" contains " +modelList.size() +" entries");
        return modelList;
    }

    /*
    private method to generate an ordered List of IcgcCopyNumber model objects covering the entire span of a
    chromosome for a specified ICGC sample.
    those portions of a chromosome that did not demonstrate copy number variation are represented by
    default values
     */

    private List<IcgcSegmentModel> resolveChromosomeMap( String sampleId, String chr, Collection<IcgcSegmentModel> chrModels){
       // logger.info("Processing sample " +sampleId +" chromosome " +chr);
        // sort the models by their start position
        SortedSet<IcgcSegmentModel> sortedModelSet = FluentIterable.from(chrModels)
                .toSortedSet(new IcgcCopyNumberModelStartPositionComparator());
        Integer currentStop = 1;
        Integer currentStart =1;
        Long maxStop =  StagingCommonNames.chromosomeLengthMap.get(chr);
        int mapSize = chrModels.size();
        List<IcgcSegmentModel>modelList = Lists.newArrayList();
        int entryCount = 1;
        for ( IcgcSegmentModel model : sortedModelSet) {
            Integer start = Integer.valueOf(model.getChromosome_start());
            if( start > (currentStop +1)) {
                // generate default copy number segment to fill in the gap
                modelList.add(this.createDefaultCopyNumberModel(sampleId, chr, currentStop.toString(), start.toString()));
                currentStart = currentStop + 1;
                currentStop = start - 1;
            }
            // process the current model object
            modelList.add(model);
            currentStart = Integer.valueOf(model.getChromosome_start());
            currentStop = Integer.valueOf(model.getChromosome_end());
        }
        // fill in the gap to the end of the chromosome
        if(currentStop < maxStop){
            modelList.add( this.createDefaultCopyNumberModel(sampleId, chr, currentStop.toString(), maxStop.toString()));
        }
        //logger.info("The model list for sample: " + sampleId +" chromosome: " +chr +" contains " +modelList.size() +" entries");
        return modelList;
    }

    /*
    private method to create a sparse instance of an IcgcSegmentModel to represent portions of a chromosome
    for a study that did not demonstrate a copy number variation
    may represent an entire chromosome
     */
    private IcgcSegmentModel createDefaultCopyNumberModel(String sampleId, String chr, String start, String stop){
        IcgcSegmentModel model = new IcgcSegmentModel();
        model.setIcgc_sample_id(sampleId);
        model.setChromosome(chr);
        model.setChromosome_start(start);
        model.setChromosome_end(stop);
        model.setSegment_median("0.0");
        model.setCopy_number("2");
        return model;
    }

    /*
    private method to read in the ICGC copy number file for a specified study directly from the URL
    each file line is used to instantiate a IcgcSegmentModel object
    each object is entered into Multimap using the sample id as a key
    a set of unique sample ids found in the copy number file is also completed
     */
    private void resolveCopyNumberMap() {
        int lineCount = 0;
        try {
            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcCopyNumberUrl)));
            String line = "";
            while ((line = rdr.readLine()) != null) {
                if (lineCount++ > 0){
                    IcgcSegmentModel model = StringUtils.columnStringToObject(IcgcSegmentModel.class, line, StagingCommonNames.tabPattern,
                            IcgcFunctionLibrary.resolveFieldNames(IcgcSegmentModel.class));
                    if (!Strings.isNullOrEmpty(model.getSegment_median()) ){
                        this.copyNumberModelMap.put(model.getIcgc_sample_id(), model);
                        this.sampleSet.add(model.getIcgc_sample_id());
                    }
                }
            }
            logger.info("ICGC Copy Number URL: " +this.icgcCopyNumberUrl +" processed " +lineCount
                    +" records and mapped " +this.copyNumberModelMap.size() +" CNVs");
            logger.info("There are " +this.sampleSet.size() +" samples in this cnv file");
        } catch (IOException | InvocationTargetException |NoSuchMethodException
                |NoSuchFieldException  | InstantiationException | IllegalAccessException e ) {
            logger.error("Error at line count " +lineCount +" url: " +this.icgcCopyNumberUrl);
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // main  method for standalone testing
    public static void main (String...args) {
        final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
        IcgcMetadata meta = IcgcMetadata.getIcgcMetadataById("PACA-CA").get();
        Path testPath = Paths.get("/tmp/icgctest");

        ListenableFuture<String> lf = service.submit(new IcgcSegmentDataETLCallable(meta,testPath, new MutationFileHandlerImpl()));
        Futures.addCallback(lf, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                logger.info("Result: " +result);
                service.shutdown();
                System.exit(0);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error(t.getMessage());
                t.printStackTrace();
                service.shutdown();
                System.exit(-1);
            }
        });

    }
    /*
    Comparator implementation to support sorting data by start position
     */
    private  class IcgcCopyNumberModelStartPositionComparator implements Comparator<IcgcSegmentModel> {
        @Override
        public int compare(IcgcSegmentModel o1, IcgcSegmentModel o2) {
            Integer start1 = Integer.valueOf(o1.getChromosome_start());
            Integer start2 = Integer.valueOf(o2.getChromosome_start());
            return start1.compareTo(start2);
        }
    }


}
