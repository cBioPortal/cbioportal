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

package org.mskcc.cbio.portal.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.model.converter.MutationModelConverter;
import org.mskcc.cbio.portal.repository.MutationRepositoryLegacy;
import org.mskcc.cbio.portal.web_api.GetMutationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MutationDataUtils {
    private static final Logger logger = Logger.getLogger(
        MutationDataUtils.class
    );

    // JSON field names
    public static final String MUTATION_ID = "mutationId";
    public static final String MUTATION_SID = "mutationSid";
    public static final String KEYWORD = "keyword";
    public static final String GENETIC_PROFILE_ID = "geneticProfileId";
    public static final String MUTATION_EVENT_ID = "mutationEventId";
    public static final String GENE_SYMBOL = "geneSymbol";
    public static final String CASE_ID = "caseId";
    public static final String LINK_TO_PATIENT_VIEW = "linkToPatientView";
    public static final String CANCER_TYPE = "cancerType";
    public static final String TUMOR_TYPE = "tumorType";
    public static final String TUMOR_TYPE_CLINICAL_ATTR = "CANCER_TYPE";
    public static final String CANCER_STUDY = "cancerStudy";
    public static final String CANCER_STUDY_SHORT = "cancerStudyShort";
    public static final String CANCER_STUDY_LINK = "cancerStudyLink";
    public static final String PROTEIN_CHANGE = "proteinChange";
    public static final String AMINO_ACID_CHANGE = "aminoAcidChange";
    public static final String MUTATION_TYPE = "mutationType";
    public static final String COSMIC = "cosmic";
    public static final String FUNCTIONAL_IMPACT_SCORE =
        "functionalImpactScore";
    public static final String FIS_VALUE = "fisValue";
    public static final String MSA_LINK = "msaLink";
    public static final String X_VAR_LINK = "xVarLink";
    public static final String PDB_LINK = "pdbLink";
    public static final String IGV_LINK = "igvLink";
    public static final String MUTATION_STATUS = "mutationStatus";
    public static final String VALIDATION_STATUS = "validationStatus";
    public static final String SEQUENCING_CENTER = "sequencingCenter";
    public static final String NCBI_BUILD_NO = "ncbiBuildNo";
    public static final String CHR = "chr";
    public static final String START_POS = "startPos";
    public static final String END_POS = "endPos";
    public static final String REFERENCE_ALLELE = "referenceAllele";
    public static final String VARIANT_ALLELE = "variantAllele";
    public static final String TUMOR_FREQ = "tumorFreq";
    public static final String NORMAL_FREQ = "normalFreq";
    public static final String TUMOR_REF_COUNT = "tumorRefCount";
    public static final String TUMOR_ALT_COUNT = "tumorAltCount";
    public static final String NORMAL_REF_COUNT = "normalRefCount";
    public static final String NORMAL_ALT_COUNT = "normalAltCount";
    public static final String CANONICAL_TRANSCRIPT = "canonicalTranscript";
    public static final String REFSEQ_MRNA_ID = "refseqMrnaId";
    public static final String CODON_CHANGE = "codonChange";
    public static final String UNIPROT_ID = "uniprotId";
    public static final String UNIPROT_ACC = "uniprotAcc";
    public static final String PROTEIN_POS_START = "proteinPosStart";
    public static final String PROTEIN_POS_END = "proteinPosEnd";
    public static final String MUTATION_COUNT = "mutationCount";
    public static final String CNA_CONTEXT = "cna";
    public static final String MY_CANCER_GENOME = "myCancerGenome";
    public static final String IS_HOTSPOT = "isHotspot";
    public static final String OMA_LINK_NOT_AVAILABLE_VALUE = "NA";

    @Autowired
    private MutationRepositoryLegacy mutationRepositoryLegacy;

    @Autowired
    private MutationModelConverter mutationModelConverter;

    /**
     * Generates an array (JSON array) of mutations for the given sample
     * and gene lists.
     *
     * @param geneticProfileId  genetic profile id
     * @param targetGeneList    list of target genes
     * @param targetSampleList    list of target samples
     * @return                  JSONArray of mutations
     */
    public JSONArray getMutationData(
        String geneticProfileId,
        List<String> targetGeneList,
        List<String> targetSampleList
    )
        throws DaoException, IOException {
        // final object to be send as JSON
        JSONArray mutationArray = new JSONArray();

        //  Get the Genetic Profile
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(
            geneticProfileId
        );

        List<ExtendedMutation> mutationList;

        //convert sample list into a set (to be able to use with get mutation data)
        HashSet<String> setOfSampleIds = new HashSet<String>(targetSampleList);
        List<Integer> internalSampleIds;

        if (geneticProfile != null) {
            internalSampleIds =
                InternalIdUtil.getInternalSampleIds(
                    geneticProfile.getCancerStudyId(),
                    targetSampleList
                );
            GetMutationData remoteCallMutation = new GetMutationData(
                mutationRepositoryLegacy,
                mutationModelConverter
            );

            mutationList =
                remoteCallMutation.getMutationData(
                    geneticProfile,
                    targetGeneList,
                    setOfSampleIds,
                    null
                );
        } else {
            // profile id does not exist, just return an empty array
            return mutationArray;
        }

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
            geneticProfile.getCancerStudyId()
        );
        Map<Long, Set<CosmicMutationFrequency>> cosmic = DaoCosmicData.getCosmicForMutationEvents(
            mutationList
        );
        Map<Integer, Integer> countMap = this.getMutationCountMap(mutationList);
        Map<Integer, ClinicalData> clinicalDataMap = getClinicalDataMap(
            targetSampleList,
            cancerStudy,
            TUMOR_TYPE_CLINICAL_ATTR
        );

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        GeneticProfile cnaProfile = DaoCancerStudy
            .getCancerStudyByInternalId(geneticProfile.getCancerStudyId())
            .getCopyNumberAlterationProfile(true);
        Map<String, Map<Integer, String>> cnaDataMap = new HashMap<String, Map<Integer, String>>();
        if (cnaProfile != null) {
            for (String geneSymbol : targetGeneList) {
                cnaDataMap.put(
                    geneSymbol,
                    DaoGeneticAlteration
                        .getInstance()
                        .getGeneticAlterationMap(
                            cnaProfile.getGeneticProfileId(),
                            daoGeneOptimized
                                .getGene(geneSymbol)
                                .getEntrezGeneId()
                        )
                );
            }
        }

        for (ExtendedMutation mutation : mutationList) {
            Integer sampleId = mutation.getSampleId();

            if (
                internalSampleIds != null &&
                internalSampleIds.contains(sampleId)
            ) {
                mutationArray.add(
                    getMutationDataMap(
                        mutation,
                        geneticProfile,
                        cancerStudy,
                        countMap,
                        cnaDataMap,
                        cosmic,
                        clinicalDataMap
                    )
                );
            }
        }

        return mutationArray;
    }

    protected Map<Integer, ClinicalData> getClinicalDataMap(
        List<String> targetSampleList,
        CancerStudy cancerStudy,
        String attrId
    )
        throws DaoException {
        Map<Integer, ClinicalData> map = new HashMap<Integer, ClinicalData>();
        ClinicalAttribute attr = DaoClinicalAttributeMeta.getDatum(
            attrId,
            cancerStudy.getInternalId()
        );

        // check if attrId is in the DB
        if (attr != null) {
            List<ClinicalData> clinicalDataList = DaoClinicalData.getSampleData(
                cancerStudy.getInternalId(),
                targetSampleList,
                attr
            );

            // create the map using case id as a key
            for (ClinicalData data : clinicalDataList) {
                Sample s = DaoSample.getSampleByCancerStudyAndSampleId(
                    cancerStudy.getInternalId(),
                    data.getStableId()
                );
                map.put(s.getInternalId(), data);
            }
        }

        return map;
    }

    protected HashMap<String, Object> getMutationDataMap(
        ExtendedMutation mutation,
        GeneticProfile geneticProfile,
        CancerStudy cancerStudy,
        Map<Integer, Integer> countMap,
        Map<String, Map<Integer, String>> cnaDataMap,
        Map<Long, Set<CosmicMutationFrequency>> cosmic,
        Map<Integer, ClinicalData> clinicalDataMap
    )
        throws DaoException, IOException {
        HashMap<String, Object> mutationData = new HashMap<String, Object>();

        String typeOfCancer = DaoTypeOfCancer
            .getTypeOfCancerById(cancerStudy.getTypeOfCancerId())
            .getName();
        String cancerStudyStableId = cancerStudy.getCancerStudyStableId();
        Sample sample = DaoSample.getSampleById(mutation.getSampleId());
        String linkToPatientView = GlobalProperties.getLinkToSampleView(
            sample.getStableId(),
            cancerStudyStableId
        );
        List<String> mcgLinks;
        Boolean isHotspot;
        if (mutation.getMutationType().equalsIgnoreCase("Fusion")) {
            mcgLinks =
                MyCancerGenomeLinkUtil.getMyCancerGenomeLinks(
                    mutation.getGeneSymbol(),
                    "fusion",
                    false
                );
        } else {
            mcgLinks =
                MyCancerGenomeLinkUtil.getMyCancerGenomeLinks(
                    mutation.getGeneSymbol(),
                    mutation.getProteinChange(),
                    false
                );
        }
        isHotspot =
            OncokbHotspotUtil.getOncokbHotspot(
                mutation.getGeneSymbol(),
                mutation.getProteinChange()
            );

        // mutationId is not a unique id wrt the whole DB,
        // but it is unique wrt the returned data set
        mutationData.put(MUTATION_ID, this.generateMutationId(mutation));
        mutationData.put(
            MUTATION_SID,
            this.generateMutationSid(mutation, sample)
        );
        mutationData.put(KEYWORD, mutation.getKeyword());
        mutationData.put(GENETIC_PROFILE_ID, geneticProfile.getStableId());
        mutationData.put(MUTATION_EVENT_ID, mutation.getMutationEventId());
        mutationData.put(GENE_SYMBOL, mutation.getGeneSymbol());
        mutationData.put(CASE_ID, sample.getStableId());
        mutationData.put(LINK_TO_PATIENT_VIEW, linkToPatientView);
        mutationData.put(CANCER_TYPE, typeOfCancer);
        mutationData.put(CANCER_STUDY, cancerStudy.getName());
        mutationData.put(CANCER_STUDY_SHORT, cancerStudy.getShortName());
        mutationData.put(
            CANCER_STUDY_LINK,
            GlobalProperties.getLinkToCancerStudyView(cancerStudyStableId)
        );
        mutationData.put(
            TUMOR_TYPE,
            this.getTumorType(mutation, clinicalDataMap)
        );
        mutationData.put(PROTEIN_CHANGE, mutation.getProteinChange());
        mutationData.put(AMINO_ACID_CHANGE, mutation.getAminoAcidChange());
        mutationData.put(MUTATION_TYPE, mutation.getMutationType());
        mutationData.put(
            COSMIC,
            convertCosmicDataToMatrix(cosmic.get(mutation.getMutationEventId()))
        );
        mutationData.put(
            FUNCTIONAL_IMPACT_SCORE,
            mutation.getFunctionalImpactScore()
        );
        mutationData.put(FIS_VALUE, this.getFisValue(mutation));
        mutationData.put(MSA_LINK, this.getMsaLink(mutation));
        mutationData.put(X_VAR_LINK, this.getXVarLink(mutation));
        mutationData.put(PDB_LINK, this.getPdbLink(mutation));
        mutationData.put(
            IGV_LINK,
            this.getIGVForBAMViewingLink(cancerStudyStableId, mutation, sample)
        );
        mutationData.put(MUTATION_STATUS, mutation.getMutationStatus());
        mutationData.put(VALIDATION_STATUS, mutation.getValidationStatus());
        mutationData.put(SEQUENCING_CENTER, this.getSequencingCenter(mutation));
        mutationData.put(
            GlobalProperties.getNCBIBuild(),
            this.getNcbiBuild(mutation)
        );
        mutationData.put(NCBI_BUILD_NO, this.getNcbiBuild(mutation));
        mutationData.put(CHR, this.getChromosome(mutation));
        mutationData.put(START_POS, mutation.getStartPosition());
        mutationData.put(END_POS, mutation.getEndPosition());
        mutationData.put(REFERENCE_ALLELE, mutation.getReferenceAllele());
        mutationData.put(VARIANT_ALLELE, this.getVariantAllele(mutation));
        mutationData.put(TUMOR_FREQ, this.getTumorFreq(mutation));
        mutationData.put(NORMAL_FREQ, this.getNormalFreq(mutation));
        mutationData.put(TUMOR_REF_COUNT, this.getTumorRefCount(mutation));
        mutationData.put(TUMOR_ALT_COUNT, this.getTumorAltCount(mutation));
        mutationData.put(NORMAL_REF_COUNT, this.getNormalRefCount(mutation));
        mutationData.put(NORMAL_ALT_COUNT, this.getNormalAltCount(mutation));
        mutationData.put(
            CANONICAL_TRANSCRIPT,
            mutation.isCanonicalTranscript()
        );
        mutationData.put(REFSEQ_MRNA_ID, mutation.getOncotatorRefseqMrnaId());
        mutationData.put(CODON_CHANGE, mutation.getOncotatorCodonChange());
        mutationData.put(UNIPROT_ID, mutation.getOncotatorUniprotName());
        mutationData.put(UNIPROT_ACC, mutation.getOncotatorUniprotAccession());
        mutationData.put(
            PROTEIN_POS_START,
            mutation.getOncotatorProteinPosStart()
        );
        mutationData.put(PROTEIN_POS_END, mutation.getOncotatorProteinPosEnd());
        mutationData.put(MUTATION_COUNT, countMap.get(mutation.getSampleId()));
        mutationData.put(CNA_CONTEXT, getCnaData(cnaDataMap, mutation));
        mutationData.put(MY_CANCER_GENOME, mcgLinks);
        mutationData.put(IS_HOTSPOT, isHotspot);

        return mutationData;
    }

    private String getCnaData(
        Map<String, Map<Integer, String>> cnaDataMap,
        ExtendedMutation mutation
    ) {
        Map<Integer, String> map = cnaDataMap.get(mutation.getGeneSymbol());
        if (map == null) {
            return null;
        }
        return map.get(mutation.getSampleId());
    }

    public String generateMutationId(ExtendedMutation mutation) {
        return (
            "m" +
            Integer.toString(mutation.hashCode()) +
            "-" +
            mutation.getSampleId()
        );
    }

    public String generateMutationSid(
        ExtendedMutation mutation,
        Sample sample
    ) {
        // we need stable patient id, internal patient id does not always work
        Patient patient = DaoPatient.getPatientById(
            sample.getInternalPatientId()
        );

        // generate mutation sid by using gene, patient id, and protein change values
        String sid =
            mutation.getGene() +
            patient.getStableId() +
            mutation.getEvent().getProteinChange();

        // remove problematic characters from the id
        return sid.replaceAll("[^a-zA-Z0-9-]", "-");
    }

    /**
     * Creates a map of mutation counts for the given list of mutations.
     *
     * @param mutations     list of mutations
     * @return              map build on (case id, mutation count) pairs
     */
    public Map<Integer, Integer> getMutationCountMap(
        List<ExtendedMutation> mutations
    ) {
        // build mutation count map
        List<Integer> sampleIds = new ArrayList<Integer>();

        Integer geneticProfileId = -1;

        // assuming same genetic profile id for all mutations in the list
        if (mutations.size() > 0) {
            geneticProfileId =
                mutations.iterator().next().getGeneticProfileId();
        }

        // collect case ids
        for (ExtendedMutation mutation : mutations) {
            sampleIds.add(mutation.getSampleId());
        }

        Map<Integer, Integer> counts;

        // retrieve count map
        counts =
            mutationModelConverter.convertMutationCountToMap(
                mutationRepositoryLegacy.countMutationEvents(
                    geneticProfileId,
                    sampleIds
                )
            );

        return counts;
    }

    /**
     * Returns the MSA (alignment) link for the given mutation.
     *
     * @param mutation  mutation instance
     * @return          corresponding MSA link
     */
    protected String getMsaLink(ExtendedMutation mutation) {
        if (
            mutation != null &&
            OmaLinkUtil.omaLinkIsValid(mutation.getLinkMsa())
        ) {
            try {
                return OmaLinkUtil.createOmaRedirectLink(mutation.getLinkMsa());
            } catch (MalformedURLException e) {
                logger.error(
                    "Could not parse OMA URL " +
                    mutation.getLinkMsa() +
                    " : " +
                    e.getMessage()
                );
            }
        }
        return OMA_LINK_NOT_AVAILABLE_VALUE;
    }

    /**
     * Returns the PDB (structure) link for the given mutation.
     *
     * @param mutation  mutation instance
     * @return          corresponding PDB link
     */
    protected String getPdbLink(ExtendedMutation mutation) {
        if (
            mutation != null &&
            OmaLinkUtil.omaLinkIsValid(mutation.getLinkPdb())
        ) {
            try {
                return OmaLinkUtil.createOmaRedirectLink(mutation.getLinkPdb());
            } catch (MalformedURLException e) {
                logger.error(
                    "Could not parse OMA URL " +
                    mutation.getLinkPdb() +
                    " : " +
                    e.getMessage()
                );
            }
        }
        return OMA_LINK_NOT_AVAILABLE_VALUE;
    }

    /**
     * Returns the xVar (mutation assessor) link for the given mutation.
     *
     * @param mutation  mutation instance
     * @return          corresponding xVar link
     */

    protected String getXVarLink(ExtendedMutation mutation) {
        if (
            mutation != null &&
            OmaLinkUtil.omaLinkIsValid(mutation.getLinkXVar())
        ) {
            try {
                return OmaLinkUtil.createOmaRedirectLink(
                    mutation.getLinkXVar()
                );
            } catch (MalformedURLException e) {
                logger.error(
                    "Could not parse OMA URL " +
                    mutation.getLinkXVar() +
                    " : " +
                    e.getMessage()
                );
                //return HtmlUtil.createEmptySpacer();
            }
        }
        return OMA_LINK_NOT_AVAILABLE_VALUE;
    }

    protected String getSequencingCenter(ExtendedMutation mutation) {
        return SequenceCenterUtil.getSequencingCenterAbbrev(
            mutation.getSequencingCenter()
        );
    }

    /**
     * Returns one of the tumor sequence alleles which is different from
     * the reference allele.
     *
     * @param mutation  mutation instance
     * @return          tumor sequence allele different from the reference allele
     */
    protected String getVariantAllele(ExtendedMutation mutation) {
        //		String varAllele = mutation.getTumorSeqAllele1();
        //
        //		if (mutation.getReferenceAllele() != null &&
        //		    mutation.getReferenceAllele().equals(mutation.getTumorSeqAllele1()))
        //		{
        //			varAllele = mutation.getTumorSeqAllele2();
        //		}

        return mutation.getTumorSeqAllele();
    }

    /**
     * Returns the corresponding NCBI build number (hg18 or hg19).
     *
     * @param mutation  mutation instance
     * @return          corresponding NCBI build number
     */
    protected String getNcbiBuild(ExtendedMutation mutation) {
        String build = mutation.getNcbiBuild();

        if (build == null) {
            return build;
        }
        if (build.equals("36") || build.equals("36.1")) {
            return "hg18";
        } else if (build.equals("37") || build.equalsIgnoreCase("GRCh37")) {
            return "hg19";
        } else {
            return build;
        }
    }

    /**
     * Returns the corresponding chromosome.
     *
     * @param mutation  mutation instance
     * @return          chromosome number
     */
    protected String getChromosome(ExtendedMutation mutation) {
        if (mutation.getChr() == null) {
            return null;
        } else if (mutation.getChr().equals("NA")) {
            return "NA";
        } else {
            return "chr" + mutation.getChr();
        }
    }

    protected Integer getNormalAltCount(ExtendedMutation mutation) {
        Integer count = mutation.getNormalAltCount();

        if (count != null && count == TabDelimitedFileUtil.NA_INT) {
            count = null;
        }

        return count;
    }

    protected Integer getNormalRefCount(ExtendedMutation mutation) {
        Integer count = mutation.getNormalRefCount();

        if (count != null && count == TabDelimitedFileUtil.NA_INT) {
            count = null;
        }

        return count;
    }

    protected Integer getTumorAltCount(ExtendedMutation mutation) {
        Integer count = mutation.getTumorAltCount();

        if (count != null && count == TabDelimitedFileUtil.NA_INT) {
            count = null;
        }

        return count;
    }

    protected Integer getTumorRefCount(ExtendedMutation mutation) {
        Integer count = mutation.getTumorRefCount();

        if (count != null && count == TabDelimitedFileUtil.NA_INT) {
            count = null;
        }

        return count;
    }

    protected Double getNormalFreq(ExtendedMutation mutation) {
        Integer altCount = this.getNormalAltCount(mutation);
        Integer refCount = this.getNormalRefCount(mutation);

        return this.getFreq(altCount, refCount);
    }

    protected Double getTumorFreq(ExtendedMutation mutation) {
        Integer altCount = this.getTumorAltCount(mutation);
        Integer refCount = this.getTumorRefCount(mutation);

        return this.getFreq(altCount, refCount);
    }

    protected Double getFreq(Integer altCount, Integer refCount) {
        Double freq;

        if (altCount == null || refCount == null) {
            freq = null;
        } else {
            freq =
                altCount.doubleValue() /
                (altCount.doubleValue() + refCount.doubleValue());
        }

        return freq;
    }

    protected String getUniprotId(ExtendedMutation mutation) {
        return mutation.getOncotatorUniprotName();
    }

    protected Float getFisValue(ExtendedMutation mutation) {
        Float fisValue = mutation.getFisValue();

        if (fisValue.equals(Float.MIN_VALUE)) {
            fisValue = null;
        }

        return fisValue;
    }

    protected String getIGVForBAMViewingLink(
        String cancerStudyStableId,
        ExtendedMutation mutation,
        Sample sample
    ) {
        String link = null;

        if (GlobalProperties.wantIGVBAMLinking()) {
            String locus =
                (
                    this.getChromosome(mutation) +
                    ":" +
                    String.valueOf(mutation.getStartPosition()) +
                    "-" +
                    String.valueOf(mutation.getEndPosition())
                );
            if (
                IGVLinking.validBAMViewingArgs(
                    cancerStudyStableId,
                    sample.getStableId(),
                    locus
                )
            ) {
                try {
                    link =
                        GlobalProperties.getLinkToIGVForBAM(
                            cancerStudyStableId,
                            sample.getStableId(),
                            URLEncoder.encode(locus, "US-ASCII")
                        );
                } catch (java.io.UnsupportedEncodingException e) {
                    logger.error(
                        "Could not encode IGVForBAMViewing link:  " +
                        e.getMessage()
                    );
                }
            }
        }

        return link;
    }

    protected String getTumorType(
        ExtendedMutation mutation,
        Map<Integer, ClinicalData> clinicalDataMap
    ) {
        String tumorType = null;

        ClinicalData data = clinicalDataMap.get(mutation.getSampleId());

        if (data != null) {
            tumorType = data.getAttrVal();
        }

        return tumorType;
    }

    protected List<List> convertCosmicDataToMatrix(
        Set<CosmicMutationFrequency> cosmic
    ) {
        if (cosmic == null) {
            return null;
        }
        List<List> mat = new ArrayList(cosmic.size());
        for (CosmicMutationFrequency cmf : cosmic) {
            List l = new ArrayList(3);
            l.add(cmf.getId());
            l.add(cmf.getAminoAcidChange());
            l.add(cmf.getFrequency());
            mat.add(l);
        }
        return mat;
    }
}
