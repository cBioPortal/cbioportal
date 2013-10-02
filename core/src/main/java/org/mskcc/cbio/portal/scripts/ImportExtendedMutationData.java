/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.ExtendedMutation.MutationEvent;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.util.CaseIdUtil;
import org.mskcc.cbio.portal.util.ExtendedMutationUtil;

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

	private ProgressMonitor pMonitor;
	private File mutationFile;
	private int geneticProfileId;
	private MutationFilter myMutationFilter;
	private static final List<String> validChrValues;
	static {
		validChrValues = new ArrayList<String>();
		for (int lc = 1; lc<24; lc++) {
			validChrValues.add(Integer.toString(lc));
			validChrValues.add("CHR" + Integer.toString(lc));
		}
		validChrValues.add("X");
		validChrValues.add("CHRX");
		validChrValues.add("Y");
		validChrValues.add("CHRY");
		validChrValues.add("NA");
		validChrValues.add("MT"); // mitochondria
	}

	/**
	 * construct an ImportExtendedMutationData with no white lists.
	 * Filter mutations according to the no argument MutationFilter().
	 */
	public ImportExtendedMutationData(File mutationFile, int geneticProfileId,
			ProgressMonitor pMonitor) {
		this.mutationFile = mutationFile;
		this.geneticProfileId = geneticProfileId;
		this.pMonitor = pMonitor;

		// create default MutationFilter
		myMutationFilter = new MutationFilter( );
	}

	/**
	 * Construct an ImportExtendedMutationData with germline and somatic whitelists.
	 * Filter mutations according to the 2 argument MutationFilter().
	 */
	public ImportExtendedMutationData( File mutationFile,
			int geneticProfileId,
			ProgressMonitor pMonitor,
			String germline_white_list_file) throws IllegalArgumentException {
		this.mutationFile = mutationFile;
		this.geneticProfileId = geneticProfileId;
		this.pMonitor = pMonitor;

		// create MutationFilter
		myMutationFilter = new MutationFilter(germline_white_list_file);
	}

	public void importData() throws IOException, DaoException {
		HashSet <String> sequencedCaseSet = new HashSet<String>();
                
                Map<MutationEvent,MutationEvent> existingEvents = new HashMap<MutationEvent,MutationEvent>();
                for (MutationEvent event : DaoMutation.getAllMutationEvents()) {
                    existingEvents.put(event, event);
                }
                Set<MutationEvent> newEvents = new HashSet<MutationEvent>();
                
                Map<ExtendedMutation,ExtendedMutation> mutations = new HashMap<ExtendedMutation,ExtendedMutation>();
                
                long mutationEventId = DaoMutation.getLargestMutationEventId();

		FileReader reader = new FileReader(mutationFile);
		BufferedReader buf = new BufferedReader(reader);

		DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

		//  The MAF File Changes fairly frequently, and we cannot use column index constants.
		String line = buf.readLine();
		line = line.trim();

		MafUtil mafUtil = new MafUtil(line);

		boolean fileHasOMAData = false;

		try {

			// fail gracefully if a non-essential column is missing
			// e.g. if there is no MA_link.var column, we assume that the value is NA and insert it as such
			fileHasOMAData = true;
			pMonitor.setCurrentMessage("Extracting OMA Scores from Column Number:  "
			                           + mafUtil.getMaFImpactIndex());
		} catch( IllegalArgumentException e) {
			fileHasOMAData = false;
		}

		line = buf.readLine();

		while( line != null)
		{
			if( pMonitor != null) {
				pMonitor.incrementCurValue();
				ConsoleUtil.showProgress(pMonitor);
			}
                        
			if( !line.startsWith("#") && line.trim().length() > 0)
			{
				String[] parts = line.split("\t", -1 ); // the -1 keeps trailing empty strings; see JavaDoc for String
				MafRecord record = mafUtil.parseRecord(line);

				// process case id
				String barCode = record.getTumorSampleID();
				String caseId = CaseIdUtil.getCaseId(barCode);

				if( !DaoCaseProfile.caseExistsInGeneticProfile(caseId, geneticProfileId)) {
					DaoCaseProfile.addCaseProfile(caseId, geneticProfileId);
				}

				String validationStatus = record.getValidationStatus();

				if (validationStatus == null ||
				    validationStatus.equalsIgnoreCase("Wildtype"))
				{
					pMonitor.logWarning("Skipping entry with Validation_Status: Wildtype");
					line = buf.readLine();
					continue;
				}

				if (!validChrValues.contains(record.getChr().toUpperCase())) {
					pMonitor.logWarning("Skipping entry with chromosome value: " + record.getChr());
					line = buf.readLine();
					continue;
				}

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
//					functionalImpactScore = getField(parts, "MA:FImpact" );
//					fisValue = getField(parts, "MA:FIS");
//					linkXVar = getField(parts, "MA:link.var" );
//					linkMsa = getField(parts, "MA:link.MSA" );
//					linkPdb = getField(parts, "MA:link.PDB" );

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
					codonChange,
					refseqMrnaId,
					uniprotName,
					uniprotAccession;

				int proteinPosStart,
					proteinPosEnd;

				boolean bestEffectTranscript;

				// determine whether to use canonical or best effect transcript

				// try canonical first
				if (ExtendedMutationUtil.isAcceptableMutation(record.getOncotatorVariantClassification()))
				{
					mutationType = record.getOncotatorVariantClassification();
					bestEffectTranscript = false;
				}
				// if canonical is not acceptable (silent, etc.), try best effect
				else if (ExtendedMutationUtil.isAcceptableMutation(record.getOncotatorVariantClassificationBestEffect()))
				{
					mutationType = record.getOncotatorVariantClassificationBestEffect();
					bestEffectTranscript = true;
				}
				// if best effect is not acceptable either, use the default value
				else
				{
					mutationType = ExtendedMutationUtil.getMutationType(record);
					bestEffectTranscript = false;
				}

				// skip RNA mutations
				if (mutationType != null && mutationType.equalsIgnoreCase("rna"))
				{
					pMonitor.logWarning("Skipping entry with mutation type: RNA");
					line = buf.readLine();
					continue;
				}

				// set values according to the selected transcript
				if (bestEffectTranscript)
				{

					if (!ExtendedMutationUtil.isValidProteinChange(record.getOncotatorProteinChangeBestEffect()))
					{
						proteinChange = "MUTATED";
					}
					else
					{
						// remove starting "p." if any
						proteinChange = ExtendedMutationUtil.normalizeProteinChange(
							record.getOncotatorProteinChangeBestEffect());
					}

					codonChange = record.getOncotatorCodonChangeBestEffect();
					refseqMrnaId = record.getOncotatorRefseqMrnaIdBestEffect();
					uniprotName = record.getOncotatorUniprotNameBestEffect();
					uniprotAccession = record.getOncotatorUniprotAccessionBestEffect();
					proteinPosStart = record.getOncotatorProteinPosStartBestEffect();
					proteinPosEnd = record.getOncotatorProteinPosEndBestEffect();
				}
				else
				{
					proteinChange = ExtendedMutationUtil.getProteinChange(parts, record);
					codonChange = record.getOncotatorCodonChange();
					refseqMrnaId = record.getOncotatorRefseqMrnaId();
					uniprotName = record.getOncotatorUniprotName();
					uniprotAccession = record.getOncotatorUniprotAccession();
					proteinPosStart = record.getOncotatorProteinPosStart();
					proteinPosEnd = record.getOncotatorProteinPosEnd();
				}

				//  Assume we are dealing with Entrez Gene Ids (this is the best / most stable option)
				String geneSymbol = record.getHugoGeneSymbol();
				long entrezGeneId = record.getEntrezGeneId();
				CanonicalGene gene = null;
                                if (entrezGeneId != TabDelimitedFileUtil.NA_LONG) {
                                    gene = daoGene.getGene(entrezGeneId);
                                }

				if(gene == null) {
					// If Entrez Gene ID Fails, try Symbol.
					gene = daoGene.getNonAmbiguousGene(geneSymbol);
				}

				if(gene == null) {
					pMonitor.logWarning("Gene not found:  " + geneSymbol + " ["
					                    + entrezGeneId + "]. Ignoring it "
					                    + "and all mutation data associated with it!");
				} else {
					ExtendedMutation mutation = new ExtendedMutation();

					mutation.setGeneticProfileId(geneticProfileId);
					mutation.setCaseId(caseId);
					mutation.setGene(gene);
					mutation.setSequencingCenter(record.getCenter());
					mutation.setSequencer(record.getSequencer());
					mutation.setProteinChange(proteinChange);
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
					mutation.setOncotatorDbSnpRs(record.getOncotatorDbSnpRs());
					mutation.setOncotatorCodonChange(codonChange);
					mutation.setOncotatorRefseqMrnaId(refseqMrnaId);
					mutation.setOncotatorUniprotName(uniprotName);
					mutation.setOncotatorUniprotAccession(uniprotAccession);
					mutation.setOncotatorProteinPosStart(proteinPosStart);
					mutation.setOncotatorProteinPosEnd(proteinPosEnd);
					mutation.setCanonicalTranscript(!bestEffectTranscript);

					sequencedCaseSet.add(caseId);

					//  Filter out Mutations
					if( myMutationFilter.acceptMutation( mutation )) {
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
					}
				}
			}
			line = buf.readLine();
		}
                
                for (MutationEvent event : newEvents) {
                    try {
                        DaoMutation.addMutationEvent(event);
                    } catch (DaoException ex) {
                        ex.printStackTrace();
                    }
                }
                
                for (ExtendedMutation mutation : mutations.values()) {
                    try {
                        DaoMutation.addMutation(mutation,false);
                    } catch (DaoException ex) {
                        ex.printStackTrace();
                    }
                }
                
		if( MySQLbulkLoader.isBulkLoad()) {
			MySQLbulkLoader.flushAll();
		}
		pMonitor.setCurrentMessage(myMutationFilter.getStatistics() );

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
			return ExtendedMutationUtil.NOT_AVAILABLE; // TODO temp workaround for invalid sent values
		} else {
			return omaScore;
		}
	}

	@Override
	public String toString(){
		return "geneticProfileId: " + this.geneticProfileId + "\n" +
		       "mutationFile: " + this.mutationFile + "\n" +
		       this.myMutationFilter.toString();
	}
}
