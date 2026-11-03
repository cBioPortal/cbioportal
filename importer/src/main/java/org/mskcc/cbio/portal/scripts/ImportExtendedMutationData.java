/*
 * Copyright (c) 2015 - 2022 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.lang3.StringUtils;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoAlleleSpecificCopyNumber;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.DaoReferenceGenome;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.AlleleSpecificCopyNumber;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.ExtendedMutation.MutationEvent;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ExtendedMutationUtil;
import org.mskcc.cbio.portal.util.GeneticProfileUtil;
import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StableIdUtil;
import org.mskcc.cbio.portal.util.TsvUtil;

/**
 * Import an extended mutation file.
 * Columns may be in any order.
 * <p>
 * @author Ethan Cerami
 * <br>
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * <br>
 * @author Selcuk Onur Sumer
 */
public class ImportExtendedMutationData {

    private File mutationFile;
    private int geneticProfileId;
    private boolean swissprotIsAccession;
    private MutationFilter myMutationFilter;
    private int entriesSkipped = 0;
    private int samplesSkipped = 0;
    private Set<String> sampleSet = new HashSet<String>();
    private Set<Integer> internalSampleIds = new HashSet<Integer>();

    private Set<String> geneSet = new HashSet<String>();
    private Set<String> filteredMutations = new HashSet<String>();
    private Set<String> namespaces = new HashSet<String>();
    private Pattern SEQUENCE_SAMPLES_REGEX = Pattern.compile("^.*sequenced_samples:(.*)$");
    private final String ASCN_NAMESPACE = "ASCN";

    private final Integer genePanelId;

    private final boolean overwriteExisting;

    /**
     * construct an ImportExtendedMutationData.
     * Filter mutations according to the no argument MutationFilter().
     */
    public ImportExtendedMutationData(File mutationFile, int geneticProfileId, String genePanel, Set<String> filteredMutations, Set<String> namespaces) {
        this(mutationFile, geneticProfileId, genePanel, filteredMutations, namespaces, false);
    }

    public ImportExtendedMutationData(File mutationFile, int geneticProfileId, String genePanel, Set<String> filteredMutations, Set<String> namespaces, boolean overwriteExisting) {
        this.mutationFile = mutationFile;
        this.geneticProfileId = geneticProfileId;
        this.swissprotIsAccession = false;
        this.genePanelId = (genePanel == null) ? null : GeneticProfileUtil.getGenePanelId(genePanel);
        this.filteredMutations = filteredMutations;

        // create default MutationFilter
        myMutationFilter = new MutationFilter( );
        this.namespaces = namespaces;
        this.overwriteExisting = overwriteExisting;
    }

    public ImportExtendedMutationData(File mutationFile, int geneticProfileId, String genePanel) {
        this(mutationFile, geneticProfileId, genePanel, null, null);
    }

    /**
     * Turns parsing the SWISSPROT column as an accession on or off again.
     *
     * If off, the column will be parsed as the name (formerly ID).
     *
     * @param swissprotIsAccession  whether to parse the column as an accession
     */
    public void setSwissprotIsAccession(boolean swissprotIsAccession) {
        this.swissprotIsAccession = swissprotIsAccession;
    }

    public void importData() throws IOException, DaoException {
        ClickHouseBulkLoader.bulkLoadOn();

        HashSet <String> sequencedCaseSet = new HashSet<String>();

        Map<MutationEvent,MutationEvent> existingEvents = new HashMap<MutationEvent,MutationEvent>();
        ProgressMonitor.setCurrentMessage("Starting to load existing mutation events...");
        for(MutationEvent mutationEvent: DaoMutation.getAllMutationEvents()) {
            existingEvents.put(mutationEvent, mutationEvent);
        }
        ProgressMonitor.setCurrentMessage("Loaded " + existingEvents.size() + " existing mutation events.");
        Set<MutationEvent> newEvents = new HashSet<MutationEvent>();

        Map<ExtendedMutation,ExtendedMutation> mutations = new HashMap<ExtendedMutation,ExtendedMutation>();
        long mutationEventId = DaoMutation.getLargestMutationEventId();

        List<AlleleSpecificCopyNumber> ascnRecords = new ArrayList<AlleleSpecificCopyNumber>();

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        try (FileReader reader = new FileReader(mutationFile);
             BufferedReader buf = new BufferedReader(reader)) {

        // process MAF header and return line immediately following it
        String line = processMAFHeader(buf);

        MafUtil mafUtil = new MafUtil(line, namespaces);

        boolean fileHasOMAData = false;

        if (mafUtil.getMaFImpactIndex() >= 0) {
            // fail gracefully if a non-essential column is missing
            // e.g. if there is no MA_link.var column, we assume that the value is NA and insert it as such
            fileHasOMAData = true;
            ProgressMonitor.setCurrentMessage(" --> OMA Scores Column Number:  "
                                       + mafUtil.getMaFImpactIndex());
        }
        else {
            fileHasOMAData = false;
        }

        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(geneticProfile.getCancerStudyId());
        String referenceGenome = cancerStudy.getReferenceGenome();
        if (referenceGenome == null) {
            referenceGenome = GlobalProperties.getReferenceGenomeName();
        }
        String genomeBuildName = DaoReferenceGenome.getReferenceGenomeByGenomeName(referenceGenome).getBuildName();
        Set<Integer> processedSamples = new HashSet<>();
        while((line=buf.readLine()) != null)
        {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();

            if(TsvUtil.isDataLine(line))
            {
                String[] parts = TsvUtil.splitTsvLine(line);
                MafRecord record = mafUtil.parseRecord(line);

                if (!record.getNcbiBuild().equalsIgnoreCase(genomeBuildName)) {
                    ProgressMonitor.logWarning("Genome Build Name does not match, expecting " + genomeBuildName);
                }
                // process case id
                String barCode = record.getTumorSampleID();
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(geneticProfile.getCancerStudyId(),
                        StableIdUtil.getSampleId(barCode));
                // can be null in case of 'normal' sample:
                // (if data files are run through validator, this condition should be minimal)
                if (sample == null) {
                    if (StableIdUtil.isNormal(barCode)) {
                        //if new sample:
                        if (sampleSet.add(barCode))
                            samplesSkipped++;
                        continue;
                    }
                    else {
                        throw new RuntimeException("Unknown sample id '" + StableIdUtil.getSampleId(barCode) + "' found in MAF file: " + this.mutationFile.getCanonicalPath());
                    }
                } else if (overwriteExisting && !processedSamples.contains(sample.getInternalId())) {
                    DaoMutation.deleteAllRecordsInGeneticProfileForSample(geneticProfileId, sample.getInternalId());
                    processedSamples.add(sample.getInternalId());
                }

                String validationStatus = record.getValidationStatus();

                if (validationStatus == null ||
                    validationStatus.equalsIgnoreCase("Wildtype"))
                {
                    ProgressMonitor.logWarning("Skipping entry with Validation_Status: Wildtype");
                    entriesSkipped++;
                    continue;
                }

                String chr = DaoGeneOptimized.normalizeChr(record.getChr().toUpperCase());
                if (chr==null) {
                    ProgressMonitor.logWarning("Skipping entry with chromosome value: " + record.getChr());
                    entriesSkipped++;
                    continue;
                }
                record.setChr(chr);

                if (record.getStartPosition() < 0)
                    record.setStartPosition(0);

                if (record.getEndPosition() < 0)
                    record.setEndPosition(0);

                String mutationType,
                    proteinChange,
                    aaChange,
                    codonChange,
                    refseqMrnaId,
                    uniprotAccession;

                int proteinPosStart,
                    proteinPosEnd;

                // determine whether to use canonical or best effect transcript

                // try canonical first
                if (ExtendedMutationUtil.isAcceptableMutation(record.getVariantClassification()))
                {
                    mutationType = record.getVariantClassification();
                }
                // if not acceptable either, use the default value
                else
                {
                    mutationType = ExtendedMutationUtil.getMutationType(record);
                }

                // skip RNA mutations
                if (mutationType != null && mutationType.equalsIgnoreCase("rna"))
                {
                    ProgressMonitor.logWarning("Skipping entry with mutation type: RNA");
                    entriesSkipped++;
                    continue;
                }

                proteinChange = ExtendedMutationUtil.getProteinChange(parts, record);
                //proteinChange = record.getProteinChange();
                aaChange = record.getAminoAcidChange();
                codonChange = record.getCodons();
                refseqMrnaId = record.getRefSeq();
                //always uniprot accession
                uniprotAccession = record.getSwissprot();
                
                proteinPosStart = ExtendedMutationUtil.getProteinPosStart(
                        record.getProteinPosition(), proteinChange);
                proteinPosEnd = ExtendedMutationUtil.getProteinPosEnd(
                        record.getProteinPosition(), proteinChange);

                //  Assume we are dealing with Entrez Gene Ids (this is the best / most stable option)
                String geneSymbol = record.getHugoGeneSymbol();
                String entrezIdString = record.getGivenEntrezGeneId();

                CanonicalGene gene = null;
                // try to parse entrez if it is not empty nor 0:
                if (!(entrezIdString.isEmpty() ||
                      entrezIdString.equals("0"))) {
                    Long entrezGeneId;
                    try {
                        entrezGeneId = Long.parseLong(entrezIdString);
                    } catch (NumberFormatException e) {
                        entrezGeneId = null;
                    }
                    //non numeric values or negative values should not be allowed:
                    if (entrezGeneId == null || entrezGeneId < 0) {
                        ProgressMonitor.logWarning(
                                "Ignoring line with invalid Entrez_Id " +
                                entrezIdString);
                        entriesSkipped++;
                        continue;
                    } else {
                        gene = daoGene.getGene(entrezGeneId);
                        if (gene == null) {
                            //skip if not in DB:
                            ProgressMonitor.logWarning(
                                    "Entrez gene ID " + entrezGeneId +
                                    " not found. Record will be skipped.");
                            entriesSkipped++;
                            continue;
                        }
                    }
                }

                // If Entrez Gene ID Fails, try Symbol.
                if (gene == null &&
                        !(geneSymbol.equals("") ||
                          geneSymbol.equals("Unknown"))) {
                    gene = daoGene.getNonAmbiguousGene(geneSymbol, true);
                }

                // assume symbol=Unknown and entrez=0 (or missing Entrez column) to imply an
                // intergenic, irrespective of what the column Variant_Classification says
                if (geneSymbol.equals("Unknown") &&
                        (entrezIdString.equals("0") || mafUtil.getEntrezGeneIdIndex() == -1)) {
                    // give extra warning if mutationType is something different from IGR:
                    if (mutationType != null &&
                            !mutationType.equalsIgnoreCase("IGR")) {
                        ProgressMonitor.logWarning(
                            "Treating mutation with gene symbol 'Unknown' " +
                            (mafUtil.getEntrezGeneIdIndex() == -1 ? "" : "and Entrez gene id 0") + " as intergenic ('IGR') " +
                            "instead of '" + mutationType + "'. Entry filtered/skipped.");
                    }
                    // treat as IGR:
                    myMutationFilter.decisions++;
                    myMutationFilter.addRejectedVariant(myMutationFilter.rejectionMap, "IGR");
                    // skip entry:
                    entriesSkipped++;
                    continue;
                }

                // skip the record if a gene was expected but not identified
                if (gene == null) {
                    ProgressMonitor.logWarning(
                            "Ambiguous or missing gene: " + geneSymbol +
                            " ["+ record.getGivenEntrezGeneId() +
                            "] or ambiguous alias. Ignoring it " +
                            "and all mutation data associated with it!");
                    entriesSkipped++;
                    continue;
                } else {
                    ExtendedMutation mutation = new ExtendedMutation();

                    mutation.setGeneticProfileId(geneticProfileId);
                    mutation.setSampleId(sample.getInternalId());
                    mutation.setGene(gene);
                    mutation.setSequencingCenter(record.getCenter());
                    mutation.setSequencer(record.getSequencer());
                    mutation.setProteinChange(proteinChange);
                    mutation.setAminoAcidChange(aaChange);
                    mutation.setMutationType(mutationType);
                    mutation.setChr(record.getChr());
                    mutation.setStartPosition(record.getStartPosition());
                    mutation.setEndPosition(record.getEndPosition());
                    mutation.setValidationStatus(record.getValidationStatus());
                    mutation.setMutationStatus(record.getMutationStatus());
                    mutation.setNcbiBuild(record.getNcbiBuild());
                    mutation.setStrand(record.getStrand());
                    mutation.setVariantType(record.getVariantType());
                    mutation.setAllele(record.getTumorSeqAllele1(), record.getTumorSeqAllele2(), record.getReferenceAllele());
                    // log whether tumor seq allele is empty (failed to resolve tumor seq allele because of invalid data values)
                    if (mutation.getTumorSeqAllele().isEmpty()) {
                        ProgressMonitor.logWarning("Tumor allele could not be resolved for sample '" + sample.getStableId() +
                            "' (chr,start,end,ref,tum1,tum2) = (" + record.getChr() + "," + record.getStartPosition() + "," +
                            record.getEndPosition() + "," + record.getReferenceAllele() + "," + record.getTumorSeqAllele1() +
                            "," + record.getTumorSeqAllele2() + ")");
                    }
                    mutation.setDbSnpRs(record.getDbSNP_RS());
                    mutation.setDbSnpValStatus(record.getDbSnpValStatus());
                    mutation.setMatchedNormSampleBarcode(record.getMatchedNormSampleBarcode());
                    mutation.setMatchNormSeqAllele1(record.getMatchNormSeqAllele1());
                    mutation.setMatchNormSeqAllele2(record.getMatchNormSeqAllele2());
                    mutation.setTumorValidationAllele1(record.getTumorValidationAllele1());
                    mutation.setTumorValidationAllele2(record.getTumorValidationAllele2());
                    mutation.setMatchNormValidationAllele1(record.getMatchNormValidationAllele1());
                    mutation.setMatchNormValidationAllele2(record.getMatchNormValidationAllele2());
                    mutation.setVerificationStatus(record.getVerificationStatus());
                    mutation.setSequencingPhase(record.getSequencingPhase());
                    mutation.setSequenceSource(record.getSequenceSource());
                    mutation.setValidationMethod(record.getValidationMethod());
                    mutation.setScore(record.getScore());
                    mutation.setBamFile(record.getBamFile());
                    mutation.setTumorAltCount(ExtendedMutationUtil.getTumorAltCount(record));
                    mutation.setTumorRefCount(ExtendedMutationUtil.getTumorRefCount(record));
                    mutation.setNormalAltCount(ExtendedMutationUtil.getNormalAltCount(record));
                    mutation.setNormalRefCount(ExtendedMutationUtil.getNormalRefCount(record));

                    //  renamed the oncotator column names to mutation
                    mutation.setCodonChange(codonChange);
                    mutation.setRefseqMrnaId(refseqMrnaId);
                    mutation.setUniprotAccession(uniprotAccession);
                    mutation.setProteinPosStart(proteinPosStart);
                    mutation.setProteinPosEnd(proteinPosEnd);

                    mutation.setDriverFilter(record.getDriverFilter());
                    mutation.setDriverFilterAnn(record.getDriverFilterAnn());
                    mutation.setDriverTiersFilter(record.getDriverTiersFilter());
                    mutation.setDriverTiersFilterAnn(record.getDriverTiersFilterAnn());

                    // TODO we don't use this info right now...
                    mutation.setCanonicalTranscript(true);

                    AlleleSpecificCopyNumber ascn = null;
                    if (namespaces != null && namespaces.contains(ASCN_NAMESPACE)) {
                        Map<String, Object> ascnData = record.getNamespacesMap().remove(ASCN_NAMESPACE);
                        // The AlleleSpecificCopyNumber constructor will construct the record from
                        // the ascnData hashmap and the ascnData will simultaneously be removed from
                        // the record's namespaces map since it is going into its own table
                        ascn = new AlleleSpecificCopyNumber(ascnData);
                    }
                    mutation.setAnnotationJson(
                        mafUtil.getNamespaceColumnParser().writeValueAsString(record.getNamespacesMap())
                    );

                    sequencedCaseSet.add(sample.getStableId());

                    //  Filter out Mutations
                    if( myMutationFilter.acceptMutation( mutation, this.filteredMutations )) {
                        MutationEvent event = existingEvents.get(mutation.getEvent());
                        if (event!=null) {
                            mutation.setEvent(event);
                        } else {
                            mutation.setMutationEventId(++mutationEventId);
                            existingEvents.put(mutation.getEvent(), mutation.getEvent());
                            newEvents.add(mutation.getEvent());
                        }

                        ExtendedMutation exist = mutations.get(mutation);
                        if (exist!=null) {
                            ExtendedMutation merged = mergeMutationData(exist, mutation);
                            mutations.put(merged, merged);
                        } else {
                            mutations.put(mutation,mutation);
                        }
                        // update ascn object with mutation unique key details
                        if (ascn != null){
                            ascn.updateAscnUniqueKeyDetails(mutation);
                            ascnRecords.add(ascn);
                        }

                        //keep track:
                        sampleSet.add(sample.getStableId());
                        internalSampleIds.add(sample.getInternalId());
                        geneSet.add(mutation.getEntrezGeneId()+"");
                    }
                    else {
                        entriesSkipped++;
                    }
                }
            }
        }
        DaoSampleProfile.upsertSampleToProfileMapping(internalSampleIds, geneticProfileId, genePanelId);

        for (MutationEvent event : newEvents) {
            try {
                DaoMutation.addMutationEvent(event);
            } catch (DaoException ex) {
                throw ex;
            }
        }

        for (ExtendedMutation mutation : mutations.values()) {
            try {
                DaoMutation.addMutation(mutation,false);
            } catch (DaoException ex) {
                throw ex;
            }
        }

        for (AlleleSpecificCopyNumber ascn : ascnRecords) {
            try {
                DaoAlleleSpecificCopyNumber.addAlleleSpecificCopyNumber(ascn);
            } catch (DaoException ex) {
                throw ex;
            }
        }

        if( ClickHouseBulkLoader.isBulkLoad()) {
            ClickHouseBulkLoader.flushAll();
        }
        // run sanity check on `mutation_event` to determine whether duplicate
        // events were introduced during current import
        if (DaoMutation.hasDuplicateMutationEvents()) {
            throw new DaoException("Duplicate mutation events were detected during this import. Aborting...");
        }

        /*
         * At MSKCC there are some MUTATION_UNCALLED
         * profiles that shouldn't be included when determining the number of
         * mutations for a sample
         */
        if (geneticProfile.getGeneticAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
            DaoMutation.createMutationCountClinicalData(geneticProfile);
        }
        // the mutation count by keyword is on a per genetic profile basis so
        // fine to calculate for any genetic profile
        ProgressMonitor.setCurrentMessage("Calculating mutation counts by keyword...");
        DaoMutation.calculateMutationCountByKeyword(geneticProfileId);

        if( ClickHouseBulkLoader.isBulkLoad()) {
            ClickHouseBulkLoader.flushAll();
        }

        if (entriesSkipped > 0) {
            ProgressMonitor.setCurrentMessage(" --> total number of data entries skipped (see table below):  " + entriesSkipped);
        }
        ProgressMonitor.setCurrentMessage(" --> total number of samples: " + sampleSet.size());
        if (samplesSkipped > 0) {
            ProgressMonitor.setCurrentMessage(" --> total number of samples skipped (normal samples): " + samplesSkipped);
        }
        ProgressMonitor.setCurrentMessage(" --> total number of genes for which one or more mutation events were stored:  " + geneSet.size());

        ProgressMonitor.setCurrentMessage("Filtering table:\n-----------------");
        ProgressMonitor.setCurrentMessage(myMutationFilter.getStatistics() );
        } // end try-with-resources
    }

    /**
         * merge the current mutation
         * @return
         */
        private ExtendedMutation mergeMutationData(ExtendedMutation mut1, ExtendedMutation mut2) {
            ExtendedMutation ret = mut1;
            if (!mut1.getMatchedNormSampleBarcode().equalsIgnoreCase(mut2.getMatchedNormSampleBarcode())) {
                if (mut2.getMatchedNormSampleBarcode().matches("TCGA-..-....-10.*")) {
                    // select blood normal if available
                    ret = mut2;
                }
            } else if (!mut1.getValidationStatus().equalsIgnoreCase(mut2.getValidationStatus())) {
                if (mut2.getValidationStatus().equalsIgnoreCase("Valid") ||
                        mut2.getValidationStatus().equalsIgnoreCase("VALIDATED")) {
                    // select validated mutations
                    ret = mut2;
                }
            } else if (!mut1.getMutationStatus().equalsIgnoreCase(mut2.getMutationStatus())) {
                if (mut2.getMutationStatus().equalsIgnoreCase("Germline")) {
                    // select germline over somatic
                    ret = mut2;
                } else if (mut2.getMutationStatus().equalsIgnoreCase("SOMATIC")) {
                    if (!mut1.getMutationStatus().equalsIgnoreCase("Germline")) {
                        // select somatic over others
                        ret = mut2;
                    }
                }
            }

            // merge centers
            Set<String> centers = new TreeSet<String>(Arrays.asList(mut1.getSequencingCenter().split(";")));
            if (centers.addAll(Arrays.asList(mut2.getSequencingCenter().split(";")))) {
                if (centers.size()>1) {
                    centers.remove("NA");
                }
                ret.setSequencingCenter(StringUtils.join(centers, ";"));
            }

            return ret;
        }

    private String transformOMAScore( String omaScore) {
        if( omaScore == null || omaScore.length() ==0) {
            return omaScore;
        }
        if( omaScore.equalsIgnoreCase("H") || omaScore.equalsIgnoreCase("high")) {
            return "H";
        } else if( omaScore.equalsIgnoreCase("M") || omaScore.equalsIgnoreCase("medium")) {
            return "M";
        } else if( omaScore.equalsIgnoreCase("L") || omaScore.equalsIgnoreCase("low")) {
            return "L";
        } else if( omaScore.equalsIgnoreCase("N") || omaScore.equalsIgnoreCase("neutral")) {
            return "N";
        } else if( omaScore.equalsIgnoreCase("[sent]")) {
            return "NA";
        } else {
            return omaScore;
        }
    }

    private String processMAFHeader(BufferedReader buffer) throws IOException, DaoException {
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
        String line = buffer.readLine().trim();
        Set<Integer> internalSampleIds = new HashSet<>();
        while (line.startsWith("#")) {
            Matcher seqSamplesMatcher = SEQUENCE_SAMPLES_REGEX.matcher(line);
            // line is of format #sequenced_samples: STABLE_ID STABLE_ID STABLE_ID STABLE_ID
            if (seqSamplesMatcher.find()) {
                internalSampleIds.addAll(getSequencedInternalSampleId(seqSamplesMatcher.group(1), geneticProfile));
            }
            line = buffer.readLine().trim();
        }
        DaoSampleProfile.upsertSampleToProfileMapping(internalSampleIds, geneticProfileId, genePanelId);
        return line;
    }

    private Set<Integer> getSequencedInternalSampleId(String sequencedSamplesIDList, GeneticProfile geneticProfile) {
        Set<Integer> toReturn = new HashSet<>();
        for (String stableSampleID : sequencedSamplesIDList.trim().split("\\s")) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(geneticProfile.getCancerStudyId(),
                                                                        StableIdUtil.getSampleId(stableSampleID));
            // if data files are run through validator, this condition should be minimal
            if (sample == null) {
                missingSample(stableSampleID);
            }
            toReturn.add(sample.getInternalId());
        }
        return toReturn;
    }

    private void missingSample(String stableSampleID) {
        throw new NullPointerException("Sample is not found in database (is it missing from clinical data file?): " + stableSampleID);
    }

}
