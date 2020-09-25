/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.model.ExtendedMutation.MutationEvent;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.maf.*;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

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
public class ImportExtendedMutationData{

    private File mutationFile;
    private int geneticProfileId;
    private boolean swissprotIsAccession;
    private MutationFilter myMutationFilter;
    private int entriesSkipped = 0;
    private int samplesSkipped = 0;
    private Set<String> sampleSet = new HashSet<String>();
    private Set<String> geneSet = new HashSet<String>();
    private String genePanel;
    private Set<String> filteredMutations = new HashSet<String>();
    private Set<String> namespaces = new HashSet<String>();
    private Pattern SEQUENCE_SAMPLES_REGEX = Pattern.compile("^.*sequenced_samples:(.*)$");
    private final String ASCN_NAMESPACE = "ascn";

    /**
     * construct an ImportExtendedMutationData.
     * Filter mutations according to the no argument MutationFilter().
     */
    public ImportExtendedMutationData(File mutationFile, int geneticProfileId, String genePanel, Set<String> filteredMutations, Set<String> namespaces) {
        this.mutationFile = mutationFile;
        this.geneticProfileId = geneticProfileId;
        this.swissprotIsAccession = false;
        this.genePanel = genePanel;
        this.filteredMutations = filteredMutations;

        // create default MutationFilter
        myMutationFilter = new MutationFilter( );
        this.namespaces = namespaces;
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
        MySQLbulkLoader.bulkLoadOn();

        HashSet <String> sequencedCaseSet = new HashSet<String>();

        Map<MutationEvent,MutationEvent> existingEvents = new HashMap<MutationEvent,MutationEvent>();
        Set<MutationEvent> newEvents = new HashSet<MutationEvent>();

        Map<ExtendedMutation,ExtendedMutation> mutations = new HashMap<ExtendedMutation,ExtendedMutation>();
        long mutationEventId = DaoMutation.getLargestMutationEventId();

        List<AlleleSpecificCopyNumber> ascnRecords = new ArrayList<AlleleSpecificCopyNumber>();

        FileReader reader = new FileReader(mutationFile);
        BufferedReader buf = new BufferedReader(reader);

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

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
        String genomeBuildName;
        String referenceGenome = cancerStudy.getReferenceGenome();
        if (referenceGenome == null) {
            genomeBuildName = GlobalProperties.getReferenceGenomeName();
        } else {
            genomeBuildName = DaoReferenceGenome.getReferenceGenomeByGenomeName(referenceGenome).getBuildName();
        }

        while((line=buf.readLine()) != null)
        {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();

            if( !line.startsWith("#") && line.trim().length() > 0)
            {
                String[] parts = line.split("\t", -1 ); // the -1 keeps trailing empty strings; see JavaDoc for String
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

                String functionalImpactScore = "";
                // using -1 is not safe, FIS can be a negative value
                Float fisValue = Float.MIN_VALUE;
                String linkXVar = "";
                String linkMsa = "";
                String linkPdb = "";

                if (fileHasOMAData)
                {
//                    functionalImpactScore = getField(parts, "MA:FImpact" );
//                    fisValue = getField(parts, "MA:FIS");
//                    linkXVar = getField(parts, "MA:link.var" );
//                    linkMsa = getField(parts, "MA:link.MSA" );
//                    linkPdb = getField(parts, "MA:link.PDB" );

                    functionalImpactScore = record.getMaFuncImpact();
                    fisValue = record.getMaFIS();
                    linkXVar = record.getMaLinkVar();
                    linkMsa = record.getMaLinkMsa();
                    linkPdb = record.getMaLinkPdb();

                    functionalImpactScore = transformOMAScore(functionalImpactScore);
                    linkXVar = linkXVar.replace("\"", "");
                }

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
                if (this.swissprotIsAccession) {
                    uniprotAccession = record.getSwissprot();
                } else {
                    String uniprotName = record.getSwissprot();
                    uniprotAccession = DaoUniProtIdMapping.mapFromUniprotIdToAccession(uniprotName);
                }
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
                            (mafUtil.getEntrezGeneIdIndex() == -1 ? "" : "and Entrez gene ID 0") + " as intergenic ('IGR') " +
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
                    mutation.setFunctionalImpactScore(functionalImpactScore);
                    mutation.setFisValue(fisValue);
                    mutation.setLinkXVar(linkXVar);
                    mutation.setLinkPdb(linkPdb);
                    mutation.setLinkMsa(linkMsa);
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

                    // TODO rename the oncotator column names (remove "oncotator")
                    mutation.setOncotatorCodonChange(codonChange);
                    mutation.setOncotatorRefseqMrnaId(refseqMrnaId);
                    mutation.setOncotatorUniprotAccession(uniprotAccession);
                    mutation.setOncotatorProteinPosStart(proteinPosStart);
                    mutation.setOncotatorProteinPosEnd(proteinPosEnd);

                    mutation.setDriverFilter(record.getDriverFilter());
                    mutation.setDriverFilterAnn(record.getDriverFilterAnn());
                    mutation.setDriverTiersFilter(record.getDriverTiersFilter());
                    mutation.setDriverTiersFilterAnn(record.getDriverTiersFilterAnn());

                    // TODO we don't use this info right now...
                    mutation.setCanonicalTranscript(true);

                    AlleleSpecificCopyNumber ascn = null;
                    if (namespaces != null && namespaces.contains(ASCN_NAMESPACE)) {
                        Map<String, String> ascnData = record.getNamespacesMap().remove(ASCN_NAMESPACE);
                        // The AlleleSpecificCopyNumber constructor will construct the record from
                        // the ascnData hashmap and the ascnData will simultaneously be removed from
                        // the record's namespaces map since it is going into its own table
                        ascn = new AlleleSpecificCopyNumber(ascnData);
                    }
                    if (record.getNamespacesMap() != null && !record.getNamespacesMap().isEmpty()) {
                        mutation.setAnnotationJson(convertMapToJsonString(record.getNamespacesMap()));
                    }

                    sequencedCaseSet.add(sample.getStableId());

                    //  Filter out Mutations
                    if( myMutationFilter.acceptMutation( mutation, this.filteredMutations )) {
                        MutationEvent event =
                            existingEvents.containsKey(mutation.getEvent()) ?
                            existingEvents.get(mutation.getEvent()) :
                            DaoMutation.getMutationEvent(mutation.getEvent());
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
                        if(!sampleSet.contains(sample.getStableId())) {
                            addSampleProfileRecord(sample);
                        }
                        // update ascn object with mutation unique key details
                        if (ascn != null){
                            ascn.updateAscnUniqueKeyDetails(mutation);
                            ascnRecords.add(ascn);
                        }

                        //keep track:
                        sampleSet.add(sample.getStableId());
                        geneSet.add(mutation.getEntrezGeneId()+"");
                    }
                    else {
                        entriesSkipped++;
                    }
                }
            }
        }

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

        if( MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
        // run sanity check on `mutation_event` to determine whether duplicate
        // events were introduced during current import
        if (DaoMutation.hasDuplicateMutationEvents()) {
            throw new DaoException("Duplicate mutation events were detected during this import. Aborting...");
        }

        /*
         * At MSKCC there are some MUTATION_UNCALLED and FUSION
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

        if( MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
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
        while (line.startsWith("#")) {
            Matcher seqSamplesMatcher = SEQUENCE_SAMPLES_REGEX.matcher(line);
            // line is of format #sequenced_samples: STABLE_ID STABLE_ID STABLE_ID STABLE_ID
            if (seqSamplesMatcher.find()) {
                addSampleProfileRecords(getSequencedSamples(seqSamplesMatcher.group(1), geneticProfile));
            }
            line = buffer.readLine().trim();
        }
        return line;
    }

    private List<Sample> getSequencedSamples(String sequencedSamplesIDList, GeneticProfile geneticProfile) {
        ArrayList<Sample> toReturn = new ArrayList<Sample>();
        for (String stableSampleID : sequencedSamplesIDList.trim().split("\\s")) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(geneticProfile.getCancerStudyId(),
                                                                        StableIdUtil.getSampleId(stableSampleID));
            // if data files are run through validator, this condition should be minimal
            if (sample == null) {
                missingSample(stableSampleID);
            }
            toReturn.add(sample);
        }
        return toReturn;
    }

    private void addSampleProfileRecords(List<Sample> sequencedSamples) throws DaoException {
        for (Sample sample : sequencedSamples) {
            addSampleProfileRecord(sample);
        }
        if( MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }

    private void addSampleProfileRecord(Sample sample) throws DaoException {
        if (!DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId)) {
            Integer genePanelID = (genePanel == null) ? null : GeneticProfileUtil.getGenePanelId(genePanel);
            DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, genePanelID);
        }
    }

    private void missingSample(String stableSampleID) {
        throw new NullPointerException("Sample is not found in database (is it missing from clinical data file?): " + stableSampleID);
    }

    private String convertMapToJsonString(Map<String, Map<String, String>> map) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }
}
