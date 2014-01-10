/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
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

    /**
     * Generates an array (JSON array) of mutations for the given case
     * and gene lists.
     *
     * @param geneticProfileId  genetic profile id
     * @param targetGeneList    list of target genes
     * @param targetCaseList    list of target cases
     * @return                  JSONArray of mutations
     */
    public JSONArray getMutationData(String geneticProfileId,
                                        ArrayList<String> targetGeneList,
                                        ArrayList<String> targetCaseList) throws DaoException
    {
        // final object to be send as JSON
        JSONArray mutationArray = new JSONArray();

        //  Get the Genetic Profile
        GeneticProfile geneticProfile =
                DaoGeneticProfile.getGeneticProfileByStableId(geneticProfileId);

        ArrayList<ExtendedMutation> mutationList;

        //convert case list into a set (to be able to use with get mutation data)
        HashSet<String> setOfCaseIds = new HashSet<String>(targetCaseList);

        if (geneticProfile != null)
        {
            GetMutationData remoteCallMutation = new GetMutationData();

            mutationList = remoteCallMutation.getMutationData(geneticProfile,
                    targetGeneList,
                    setOfCaseIds,
                    null);
        }
        else
        {
            // profile id does not exist, just return an empty array
            return mutationArray;
        }

        Map<Long, Set<CosmicMutationFrequency>> cosmic = DaoCosmicData.getCosmicForMutationEvents(mutationList);

        Map<String, Integer> countMap = this.getMutationCountMap(mutationList);
        
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        
        GeneticProfile cnaProfile = DaoCancerStudy.getCancerStudyByInternalId(geneticProfile.getCancerStudyId()).getCopyNumberAlterationProfile(true);
        Map<String,Map<String,String>> cnaDataMap = new HashMap<String,Map<String,String>>();
        if (cnaProfile!=null) {
            for (String geneSymbol : targetGeneList) {
                cnaDataMap.put(geneSymbol,
                        DaoGeneticAlteration.getInstance().getGeneticAlterationMap(cnaProfile.getGeneticProfileId(), daoGeneOptimized.getGene(geneSymbol).getEntrezGeneId()));
            }
        }

        for (ExtendedMutation mutation : mutationList)
        {
            String caseId = mutation.getCaseId();

            if (targetCaseList.contains(caseId))
            {
                mutationArray.add(getMutationDataMap(mutation, geneticProfile, countMap,cnaDataMap, cosmic));
            }
        }

        return mutationArray;
    }

    protected HashMap<String, Object> getMutationDataMap(
            ExtendedMutation mutation,
            GeneticProfile geneticProfile,
            Map<String, Integer> countMap,
            Map<String,Map<String,String>> cnaDataMap,
            Map<Long, Set<CosmicMutationFrequency>> cosmic) throws DaoException
    {
        HashMap<String, Object> mutationData = new HashMap<String, Object>();

        int cancerStudyId = geneticProfile.getCancerStudyId();
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
        String typeOfCancer = DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId()).getName();
        String cancerStudyStableId = cancerStudy.getCancerStudyStableId();
        String linkToPatientView = GlobalProperties.getLinkToPatientView(mutation.getCaseId(), cancerStudyStableId);

        // mutationId is not a unique id wrt the whole DB,
        // but it is unique wrt the returned data set
        mutationData.put("mutationId", this.generateMutationId(mutation));
        mutationData.put("mutationSid", this.generateMutationSid(mutation));
        mutationData.put("keyword", mutation.getKeyword());
        mutationData.put("geneticProfileId", geneticProfile.getStableId());
        mutationData.put("mutationEventId", mutation.getMutationEventId());
        mutationData.put("geneSymbol", mutation.getGeneSymbol());
        mutationData.put("caseId", mutation.getCaseId());
        mutationData.put("linkToPatientView", linkToPatientView);
        mutationData.put("cancerType", typeOfCancer);
        mutationData.put("cancerStudy", cancerStudy.getName());
        mutationData.put("cancerStudyShort", getShortName(cancerStudy));
        mutationData.put("cancerStudyLink", GlobalProperties.getLinkToCancerStudyView(cancerStudyStableId));
        mutationData.put("proteinChange", mutation.getProteinChange());
        mutationData.put("mutationType", mutation.getMutationType());
        mutationData.put("cosmic", convertCosmicDataToMatrix(cosmic.get(mutation.getMutationEventId())));
        mutationData.put("functionalImpactScore", mutation.getFunctionalImpactScore());
        mutationData.put("fisValue", this.getFisValue(mutation));
        mutationData.put("msaLink", this.getMsaLink(mutation));
        mutationData.put("xVarLink", this.getXVarLink(mutation));
        mutationData.put("pdbLink", this.getPdbLink(mutation));
        mutationData.put("igvLink", this.getIGVForBAMViewingLink(cancerStudyStableId, mutation));
        mutationData.put("mutationStatus", mutation.getMutationStatus());
        mutationData.put("validationStatus", mutation.getValidationStatus());
        mutationData.put("sequencingCenter", this.getSequencingCenter(mutation));
        mutationData.put("ncbiBuildNo", this.getNcbiBuild(mutation));
        mutationData.put("chr", this.getChromosome(mutation));
        mutationData.put("startPos", mutation.getStartPosition());
        mutationData.put("endPos", mutation.getEndPosition());
        mutationData.put("referenceAllele", mutation.getReferenceAllele());
        mutationData.put("variantAllele", this.getVariantAllele(mutation));
        mutationData.put("tumorFreq", this.getTumorFreq(mutation));
        mutationData.put("normalFreq", this.getNormalFreq(mutation));
        mutationData.put("tumorRefCount", this.getTumorRefCount(mutation));
        mutationData.put("tumorAltCount", this.getTumorAltCount(mutation));
        mutationData.put("normalRefCount", this.getNormalRefCount(mutation));
        mutationData.put("normalAltCount", this.getNormalAltCount(mutation));
        mutationData.put("canonicalTranscript", mutation.isCanonicalTranscript());
        mutationData.put("refseqMrnaId", mutation.getOncotatorRefseqMrnaId());
        mutationData.put("codonChange", mutation.getOncotatorCodonChange());
        mutationData.put("uniprotId", this.getUniprotId(mutation));
        mutationData.put("proteinPosStart", mutation.getOncotatorProteinPosStart());
        mutationData.put("proteinPosEnd", mutation.getOncotatorProteinPosEnd());
        mutationData.put("mutationCount", countMap.get(mutation.getCaseId()));
        mutationData.put("specialGeneData", this.getSpecialGeneData(mutation));
        mutationData.put("cna", getCnaData(cnaDataMap, mutation));

        return mutationData;
    }
    
    private String getCnaData(Map<String,Map<String,String>> cnaDataMap, ExtendedMutation mutation) {
        return cnaDataMap.get(mutation.getGeneSymbol()).get(mutation.getCaseId());
    }

    public String generateMutationId(ExtendedMutation mutation) {
        return "m" + Integer.toString(mutation.hashCode());
    }

    public String generateMutationSid(ExtendedMutation mutation) {
        return mutation.getGene()
                + mutation.getCaseId()
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
    public Map<String, Integer> getMutationCountMap(List<ExtendedMutation> mutations)
    {
        // build mutation count map
        List<String> caseIds = new LinkedList<String>();

        Integer geneticProfileId = -1;

        // assuming same genetic profile id for all mutations in the list
        if (mutations.size() > 0)
        {
            geneticProfileId = mutations.iterator().next().getGeneticProfileId();
        }

        // collect case ids
        for (ExtendedMutation mutation : mutations)
        {
            caseIds.add(mutation.getCaseId());
        }

        Map<String, Integer> counts;

        // retrieve count map
        try
        {
            counts = DaoMutation.countMutationEvents(geneticProfileId, caseIds);
        }
        catch (DaoException e)
        {
            counts = null;
        }

        return counts;
    }

    private String getShortName(CancerStudy cancerStudy) throws DaoException {
        String sName = cancerStudy.getCancerStudyStableId();
        String tumorType = cancerStudy.getTypeOfCancerId();
        sName = sName.replace(tumorType + "_", "").replaceAll("_", " ").toUpperCase();
        TypeOfCancer typeOfCancerById = DaoTypeOfCancer.getTypeOfCancerById(tumorType);
        sName = typeOfCancerById.getShortName() + " (" + sName + ")";

        return sName;
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
        // TODO uniprot name or uniprot accession
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


    protected String getIGVForBAMViewingLink(String cancerStudyStableId, ExtendedMutation mutation)
    {
        String link = null;

        if (GlobalProperties.wantIGVBAMLinking()) {
            String locus = (this.getChromosome(mutation) + ":" +
                    String.valueOf(mutation.getStartPosition()) + "-" +
                    String.valueOf(mutation.getEndPosition()));
            if (IGVLinking.validBAMViewingArgs(cancerStudyStableId, mutation.getCaseId(), locus)) {
                try {
                    link = GlobalProperties.getLinkToIGVForBAM(cancerStudyStableId,
                            mutation.getCaseId(),
                            URLEncoder.encode(locus, "US-ASCII"));
                }
                catch (java.io.UnsupportedEncodingException e) {
                    logger.error("Could not encode IGVForBAMViewing link:  " + e.getMessage());
                }
            }
        }

        return link;
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
