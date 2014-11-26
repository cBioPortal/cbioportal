/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
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
*/

package org.mskcc.cbio.portal.util;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.html.special_gene.SpecialGene;
import org.mskcc.cbio.portal.html.special_gene.SpecialGeneFactory;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.web_api.GetMutationData;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;

public class MutationDataUtils {
    private static final Logger logger = Logger.getLogger(MutationDataUtils.class);

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
	public static final String MUTATION_TYPE = "mutationType";
	public static final String COSMIC = "cosmic";
	public static final String FUNCTIONAL_IMPACT_SCORE = "functionalImpactScore";
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
	public static final String SPECIAL_GENE_DATA = "specialGeneData";
	public static final String CNA_CONTEXT = "cna";

    /**
     * Generates an array (JSON array) of mutations for the given sample
     * and gene lists.
     *
     * @param geneticProfileId  genetic profile id
     * @param targetGeneList    list of target genes
     * @param targetSampleList    list of target samples
     * @return                  JSONArray of mutations
     */
    public JSONArray getMutationData(String geneticProfileId,
                                        List<String> targetGeneList,
                                        List<String> targetSampleList) throws DaoException
    {
        // final object to be send as JSON
        JSONArray mutationArray = new JSONArray();

        //  Get the Genetic Profile
        GeneticProfile geneticProfile =
                DaoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);

        List<ExtendedMutation> mutationList;

        //convert sample list into a set (to be able to use with get mutation data)
        HashSet<String> setOfSampleIds = new HashSet<String>(targetSampleList);
        List<Integer> internalSampleIds = InternalIdUtil.getInternalSampleIds(geneticProfile.getCancerStudyId(), targetSampleList);

        if (geneticProfile != null)
        {
            GetMutationData remoteCallMutation = new GetMutationData();

            mutationList = remoteCallMutation.getMutationData(geneticProfile,
                    targetGeneList,
                    setOfSampleIds,
                    null);
        }
        else
        {
            // profile id does not exist, just return an empty array
            return mutationArray;
        }

	    CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(
			    geneticProfile.getCancerStudyId());
        Map<Long, Set<CosmicMutationFrequency>> cosmic =
		        DaoCosmicData.getCosmicForMutationEvents(mutationList);
        Map<Integer, Integer> countMap = this.getMutationCountMap(mutationList);
	    Map<String, ClinicalData> clinicalDataMap = getClinicalDataMap(
                    targetSampleList, cancerStudy, TUMOR_TYPE_CLINICAL_ATTR);
        
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        
        GeneticProfile cnaProfile = DaoCancerStudy.getCancerStudyByInternalId(geneticProfile.getCancerStudyId()).getCopyNumberAlterationProfile(true);
        Map<String,Map<Integer,String>> cnaDataMap = new HashMap<String,Map<Integer,String>>();
        if (cnaProfile!=null) {
            for (String geneSymbol : targetGeneList) {
                cnaDataMap.put(geneSymbol,
                        DaoGeneticAlteration.getInstance().getGeneticAlterationMap(cnaProfile.getGeneticProfileId(), daoGeneOptimized.getGene(geneSymbol).getEntrezGeneId()));
            }
        }

        for (ExtendedMutation mutation : mutationList)
        {
            Integer sampleId = mutation.getSampleId();

            if (internalSampleIds.contains(sampleId))
            {
                mutationArray.add(getMutationDataMap(
		                mutation, geneticProfile, cancerStudy, countMap, cnaDataMap, cosmic, clinicalDataMap));
            }
        }

        return mutationArray;
    }

	protected Map<String, ClinicalData> getClinicalDataMap(List<String> targetSampleList,
			CancerStudy cancerStudy,
			String attrId) throws DaoException
	{
		Map<String, ClinicalData> map = new HashMap<String, ClinicalData>();
		ClinicalAttribute attr = DaoClinicalAttribute.getDatum(attrId);

		// check if attrId is in the DB
		if (attr != null)
		{
			List<ClinicalData> clinicalDataList = DaoClinicalData.getSampleData(
				cancerStudy.getInternalId(),
				targetSampleList,
				attr);

			// create the map using case id as a key
			for (ClinicalData data : clinicalDataList)
			{
				map.put(data.getStableId(), data);
			}
		}

		return map;
    }

    protected HashMap<String, Object> getMutationDataMap(
            ExtendedMutation mutation,
            GeneticProfile geneticProfile,
			CancerStudy cancerStudy,
            Map<Integer, Integer> countMap,
            Map<String,Map<Integer,String>> cnaDataMap,
            Map<Long, Set<CosmicMutationFrequency>> cosmic,
			Map<String, ClinicalData> clinicalDataMap) throws DaoException
    {
        HashMap<String, Object> mutationData = new HashMap<String, Object>();


        String typeOfCancer = DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId()).getName();
        String cancerStudyStableId = cancerStudy.getCancerStudyStableId();
        Sample sample = DaoSample.getSampleById(mutation.getSampleId());
        String linkToPatientView = GlobalProperties.getLinkToPatientView(sample.getStableId(), cancerStudyStableId);

        // mutationId is not a unique id wrt the whole DB,
        // but it is unique wrt the returned data set
        mutationData.put(MUTATION_ID, this.generateMutationId(mutation));
        mutationData.put(MUTATION_SID, this.generateMutationSid(mutation, sample));
        mutationData.put(KEYWORD, mutation.getKeyword());
        mutationData.put(GENETIC_PROFILE_ID, geneticProfile.getStableId());
        mutationData.put(MUTATION_EVENT_ID, mutation.getMutationEventId());
        mutationData.put(GENE_SYMBOL, mutation.getGeneSymbol());
        mutationData.put(CASE_ID, sample.getStableId());
        mutationData.put(LINK_TO_PATIENT_VIEW, linkToPatientView);
        mutationData.put(CANCER_TYPE, typeOfCancer);
        mutationData.put(CANCER_STUDY, cancerStudy.getName());
        mutationData.put(CANCER_STUDY_SHORT, cancerStudy.getShortName());
        mutationData.put(CANCER_STUDY_LINK, GlobalProperties.getLinkToCancerStudyView(cancerStudyStableId));
	    mutationData.put(TUMOR_TYPE, this.getTumorType(mutation, clinicalDataMap));
        mutationData.put(PROTEIN_CHANGE, mutation.getProteinChange());
        mutationData.put(MUTATION_TYPE, mutation.getMutationType());
        mutationData.put(COSMIC, convertCosmicDataToMatrix(cosmic.get(mutation.getMutationEventId())));
        mutationData.put(FUNCTIONAL_IMPACT_SCORE, mutation.getFunctionalImpactScore());
        mutationData.put(FIS_VALUE, this.getFisValue(mutation));
        mutationData.put(MSA_LINK, this.getMsaLink(mutation));
        mutationData.put(X_VAR_LINK, this.getXVarLink(mutation));
        mutationData.put(PDB_LINK, this.getPdbLink(mutation));
        mutationData.put(IGV_LINK, this.getIGVForBAMViewingLink(cancerStudyStableId, mutation, sample));
        mutationData.put(MUTATION_STATUS, mutation.getMutationStatus());
        mutationData.put(VALIDATION_STATUS, mutation.getValidationStatus());
        mutationData.put(SEQUENCING_CENTER, this.getSequencingCenter(mutation));
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
        mutationData.put(CANONICAL_TRANSCRIPT, mutation.isCanonicalTranscript());
        mutationData.put(REFSEQ_MRNA_ID, mutation.getOncotatorRefseqMrnaId());
        mutationData.put(CODON_CHANGE, mutation.getOncotatorCodonChange());
        mutationData.put(UNIPROT_ID, mutation.getOncotatorUniprotName());
	    mutationData.put(UNIPROT_ACC, mutation.getOncotatorUniprotAccession());
        mutationData.put(PROTEIN_POS_START, mutation.getOncotatorProteinPosStart());
        mutationData.put(PROTEIN_POS_END, mutation.getOncotatorProteinPosEnd());
        mutationData.put(MUTATION_COUNT, countMap.get(mutation.getSampleId()));
        mutationData.put(SPECIAL_GENE_DATA, this.getSpecialGeneData(mutation));
        mutationData.put(CNA_CONTEXT, getCnaData(cnaDataMap, mutation));

        return mutationData;
    }
    
    private String getCnaData(Map<String,Map<Integer,String>> cnaDataMap, ExtendedMutation mutation) {
        Map<Integer,String> map = cnaDataMap.get(mutation.getGeneSymbol());
        if (map==null) {
            return null;
        }
        return map.get(mutation.getSampleId());
    }

    public String generateMutationId(ExtendedMutation mutation) {
        return "m" + Integer.toString(mutation.hashCode());
    }

    public String generateMutationSid(ExtendedMutation mutation, Sample sample) {
        return mutation.getGene()
                + sample.getStableId()
                + mutation.getEvent().getProteinChange().replace('*', '-');
    }

    /**
     * Returns special gene data (if exists) for the given mutation. Returns null
     * if no special gene exists for the given mutation.
     *
     * @param mutation  mutation instance
     * @return          Map of (field header, field value) pairs.
     */
    protected HashMap<String, String> getSpecialGeneData(ExtendedMutation mutation)
    {
        HashMap<String, String> specialGeneData = null;

        SpecialGene specialGene = SpecialGeneFactory.getInstance(mutation.getGeneSymbol());

        //  fields & values for "Special" genes
        if (specialGene != null)
        {
            specialGeneData = new HashMap<String, String>();

            ArrayList<String> specialHeaders = specialGene.getDataFieldHeaders();
            ArrayList<String> specialData = specialGene.getDataFields(mutation);

            if (specialHeaders.size() == specialData.size())
            {
                for (int i=0; i < specialData.size(); i++)
                {
                    String header = specialHeaders.get(i);
                    String data = specialData.get(i);

                    specialGeneData.put(header, data);
                }
            }
        }

        return specialGeneData;
    }

    /**
     * Creates a map of mutation counts for the given list of mutations.
     *
     * @param mutations     list of mutations
     * @return              map build on (case id, mutation count) pairs
     */
    public Map<Integer, Integer> getMutationCountMap(List<ExtendedMutation> mutations)
    {
        // build mutation count map
        List<Integer> sampleIds = new ArrayList<Integer>();

        Integer geneticProfileId = -1;

        // assuming same genetic profile id for all mutations in the list
        if (mutations.size() > 0)
        {
            geneticProfileId = mutations.iterator().next().getGeneticProfileId();
        }

        // collect case ids
        for (ExtendedMutation mutation : mutations)
        {
            sampleIds.add(mutation.getSampleId());
        }

        Map<Integer, Integer> counts;

        // retrieve count map
        try
        {
            counts = DaoMutation.countMutationEvents(geneticProfileId, sampleIds);
        }
        catch (DaoException e)
        {
            counts = null;
        }

        return counts;
    }

    /**
     * Returns the MSA (alignment) link for the given mutation.
     *
     * @param mutation  mutation instance
     * @return          corresponding MSA link
     */
    protected String getMsaLink(ExtendedMutation mutation)
    {
        String urlMsa = "";

        if (linkIsValid(mutation.getLinkMsa()))
        {
            try
            {
                if(mutation.getLinkMsa().length() == 0 ||
                        mutation.getLinkMsa().equals("NA"))
                {
                    urlMsa = "NA";
                }
                else
                {
                    urlMsa = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkMsa());
                }
            }
            catch (MalformedURLException e)
            {
                logger.error("Could not parse OMA URL:  " + e.getMessage());
            }
        }

        return urlMsa;
    }


    /**
     * Returns the PDB (structure) link for the given mutation.
     *
     * @param mutation  mutation instance
     * @return          corresponding PDB link
     */
    protected String getPdbLink(ExtendedMutation mutation)
    {
        String urlPdb = "";

        if (linkIsValid(mutation.getLinkPdb()))
        {
            try
            {
                if(mutation.getLinkPdb().length() == 0 ||
                        mutation.getLinkPdb().equals("NA"))
                {
                    urlPdb = "NA";
                }
                else
                {
                    urlPdb = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkPdb());
                }
            }
            catch (MalformedURLException e)
            {
                logger.error("Could not parse OMA URL:  " + e.getMessage());
            }
        }

        return urlPdb;
    }

    /**
     * Returns the xVar (mutation assessor) link for the given mutation.
     *
     * @param mutation  mutation instance
     * @return          corresponding xVar link
     */
    protected String getXVarLink(ExtendedMutation mutation)
    {
        String xVarLink = "";

        if (linkIsValid(mutation.getLinkXVar()))
        {
            try
            {
                xVarLink = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkXVar());
            }
            catch (MalformedURLException e)
            {
                logger.error("Could not parse OMA URL:  " + e.getMessage());
                //return HtmlUtil.createEmptySpacer();
            }
        }

        return xVarLink;
    }

    /**
     * Checks the validity of the given link.
     *
     * @param link  string representation of a URL
     * @return      true if valid, false otherwise
     */
    protected boolean linkIsValid(String link)
    {
        return link != null &&
                link.length() > 0 &&
                !link.equalsIgnoreCase("NA");
    }

    protected String getSequencingCenter(ExtendedMutation mutation)
    {
        return SequenceCenterUtil.getSequencingCenterAbbrev(mutation.getSequencingCenter());
    }

    /**
     * Returns one of the tumor sequence alleles which is different from
     * the reference allele.
     *
     * @param mutation  mutation instance
     * @return          tumor sequence allele different from the reference allele
     */
    protected String getVariantAllele(ExtendedMutation mutation)
    {
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
    protected String getNcbiBuild(ExtendedMutation mutation)
    {
        String build = mutation.getNcbiBuild();

        if (build == null)
        {
            return build;
        }
        if (build.equals("36") ||
                build.equals("36.1"))
        {
            return "hg18";
        }
        else if (build.equals("37") ||
                build.equalsIgnoreCase("GRCh37"))
        {
            return "hg19";
        }
        else
        {
            return build;
        }
    }

    /**
     * Returns the corresponding chromosome.
     *
     * @param mutation  mutation instance
     * @return          chromosome number
     */
    protected String getChromosome(ExtendedMutation mutation)
    {
        if (mutation.getChr() == null)
        {
            return null;
        }
        else if (mutation.getChr().equals("NA"))
        {
            return "NA";
        }
        else
        {
            return "chr" + mutation.getChr();
        }
    }

    protected Integer getNormalAltCount(ExtendedMutation mutation)
    {
        Integer count = mutation.getNormalAltCount();

        if (count == TabDelimitedFileUtil.NA_INT)
        {
            count = null;
        }

        return count;
    }

    protected Integer getNormalRefCount(ExtendedMutation mutation)
    {
        Integer count = mutation.getNormalRefCount();

        if (count == TabDelimitedFileUtil.NA_INT)
        {
            count = null;
        }

        return count;
    }

    protected Integer getTumorAltCount(ExtendedMutation mutation)
    {
        Integer count = mutation.getTumorAltCount();

        if (count == TabDelimitedFileUtil.NA_INT)
        {
            count = null;
        }

        return count;
    }

    protected Integer getTumorRefCount(ExtendedMutation mutation)
    {
        Integer count = mutation.getTumorRefCount();

        if (count == TabDelimitedFileUtil.NA_INT)
        {
            count = null;
        }

        return count;
    }

    protected Double getNormalFreq(ExtendedMutation mutation)
    {
        Integer altCount = this.getNormalAltCount(mutation);
        Integer refCount = this.getNormalRefCount(mutation);

        return this.getFreq(altCount, refCount);
    }

    protected Double getTumorFreq(ExtendedMutation mutation)
    {
        Integer altCount = this.getTumorAltCount(mutation);
        Integer refCount = this.getTumorRefCount(mutation);

        return this.getFreq(altCount, refCount);
    }

    protected Double getFreq(Integer altCount, Integer refCount)
    {
        Double freq;

        if (altCount == null ||
                refCount == null)
        {
            freq = null;
        }
        else
        {
            freq = altCount.doubleValue() /
                    (altCount.doubleValue() + refCount.doubleValue());
        }

        return freq;
    }

    protected String getUniprotId(ExtendedMutation mutation)
    {
        return mutation.getOncotatorUniprotName();
    }

    protected Float getFisValue(ExtendedMutation mutation)
    {
        Float fisValue = mutation.getFisValue();

        if (fisValue.equals(Float.MIN_VALUE))
        {
            fisValue = null;
        }

        return fisValue;
    }


    protected String getIGVForBAMViewingLink(String cancerStudyStableId, ExtendedMutation mutation, Sample sample)
    {
        String link = null;

        if (GlobalProperties.wantIGVBAMLinking()) {
            String locus = (this.getChromosome(mutation) + ":" +
                    String.valueOf(mutation.getStartPosition()) + "-" +
                    String.valueOf(mutation.getEndPosition()));
            if (IGVLinking.validBAMViewingArgs(cancerStudyStableId, sample.getStableId(), locus)) {
                try {
                    link = GlobalProperties.getLinkToIGVForBAM(cancerStudyStableId,
                            sample.getStableId(),
                            URLEncoder.encode(locus, "US-ASCII"));
                }
                catch (java.io.UnsupportedEncodingException e) {
                    logger.error("Could not encode IGVForBAMViewing link:  " + e.getMessage());
                }
            }
        }

        return link;
    }

	protected String getTumorType(ExtendedMutation mutation,
			Map<String,ClinicalData> clinicalDataMap)
	{
		String tumorType = null;

		ClinicalData data = clinicalDataMap.get(mutation.getSampleId());

		if (data != null)
		{
			tumorType = data.getAttrVal();
		}

		return tumorType;
	}

    protected List<List> convertCosmicDataToMatrix(Set<CosmicMutationFrequency> cosmic) {
        if (cosmic==null) {
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
