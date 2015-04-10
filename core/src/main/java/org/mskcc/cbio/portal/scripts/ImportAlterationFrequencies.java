/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.scripts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.mskcc.cbio.portal.dao.DaoAlterationFrequency;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;

/**
 *
 * @author abeshoua
 */
public class ImportAlterationFrequencies {
	private static class SampleAlterationProfile {
		public int MUT; // 0 is false, 1 is true, -1 is no data
		public int AMP;
		public int DEL;
		public SampleAlterationProfile() {
			this.MUT = 0; this.AMP = 0; this.DEL = 0; // TODO: change default to -1 and manage this correctly
		}
	}
	private static class AlterationFrequencyProfile {
		public float mutCt;
		public float ampCt;
		public float delCt;
		public float totalCt;
		public float mutAmpCt;
		public float mutDelCt;
		public AlterationFrequencyProfile() {
			mutCt = 0; ampCt = 0; delCt = 0; totalCt = 0; 
			mutAmpCt = 0; mutDelCt = 0;
		}
			
	}
	private static String makeHashKey(long entrezGeneId, int sampleId) {
		return Long.toString(entrezGeneId,10)+"&"+Integer.toString(sampleId,10);
	}
	private static long[] decodeHashKey(String key) {
		long[] ret = new long[2];
		String[] splitKey = key.split("&");
		ret[0] = Long.parseLong(splitKey[0], 10);
		ret[1] = Long.parseLong(splitKey[1], 10);
		return ret;
	}
	private static int addMutToAlterationProfile(int old) {
		return old | 1;
	}
	private static int addAmpToAlterationProfile(int old) {
		return old | 2;
	}
	private static int addDelToAlterationProfile(int old) {
		return old | 4;
	}
	private static DaoAlterationFrequency.AlterationFrequencyType decodeAlterationProfile(int prof) {
		boolean MUT = ((prof & 1)!=0);
		boolean AMP = ((prof & 2)!=0);
		boolean DEL = ((prof & 4)!=0);
		if (MUT) {
			if (AMP) {
				return DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_AMP;
			} else if (DEL) {
				return DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_DEL;
			} else {
				return DaoAlterationFrequency.AlterationFrequencyType.MUT;
			}
		} else {
			if (AMP) {
				return DaoAlterationFrequency.AlterationFrequencyType.CNA_AMP;
			} else if (DEL) {
				return DaoAlterationFrequency.AlterationFrequencyType.CNA_DEL;
			} else {
				return DaoAlterationFrequency.AlterationFrequencyType.WILD_TYPE;
			}
		}
	}
	
	public static void main(String[] args) {
		for(CancerStudy study: DaoCancerStudy.getAllCancerStudies()) {
			try {
				importAlterationFrequencies(study.getCancerStudyStableId());
			} catch (Exception e) {
				System.out.println("Error importing alteration frequencies for "+study.getCancerStudyStableId());
				e.printStackTrace();
			}
		}
	}
	public static void importAlterationFrequencies(String studyStableId) throws Exception {
		// TODO: collect by patient not by sample
		// get list of profiles
		System.out.println("Importing alteration frequencies for "+studyStableId);
		System.out.println("Organizing profiles");
		CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyStableId);
		List<GeneticProfile> geneticProfiles = study.getGeneticProfiles();
		List<GeneticProfile> mutationProfiles = new LinkedList<>();
		List<GeneticProfile> cnaProfiles = new LinkedList<>();
		for (GeneticProfile g: geneticProfiles) {
			if(g.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED) {
				mutationProfiles.add(g);
			} else if (g.getGeneticAlterationType() == GeneticAlterationType.COPY_NUMBER_ALTERATION) {
				cnaProfiles.add(g);
			}
		}
		// collect sample data
		System.out.println("Loading raw data from profiles");
		HashMap<String, Integer> geneData = new HashMap<>();
		// key is entrezgeneid&internalsampleid
		for (GeneticProfile prof : mutationProfiles) {
			List<ExtendedMutation> mutations = DaoMutation.getMutations(prof.getGeneticProfileId());
			for (ExtendedMutation mut : mutations) {
				int sampleId = mut.getSampleId();
				long geneId = mut.getEntrezGeneId();
				String key = makeHashKey(geneId, sampleId);
				if (!geneData.containsKey(key)) {
					geneData.put(key, 0);
				}
				geneData.put(key, addMutToAlterationProfile(geneData.get(key)));
			}
		}
		for (GeneticProfile prof: cnaProfiles) {
			DaoGeneticAlteration geneticAlteration = DaoGeneticAlteration.getInstance();
			Set<CanonicalGene> genes = geneticAlteration.getGenesInProfile(prof.getGeneticProfileId());
			LinkedList<Long> geneIds = new LinkedList<>();
			for (CanonicalGene gene: genes) {
				geneIds.add(gene.getEntrezGeneId());
			}
			System.out.println(geneIds.size());
			int geneIdChunkSize = 1000;
			for (int ind=0; ind<geneIds.size(); ind+= geneIdChunkSize) {
				List<Long> geneIdsChunk = geneIds.subList(ind, Math.min(ind+geneIdChunkSize, geneIds.size()));
				HashMap<Long, HashMap<Integer, String>> GAMap = geneticAlteration.getGeneticAlterationMap(prof.getGeneticProfileId(), geneIdsChunk);
				for (Long geneId: GAMap.keySet()) {
					for (Integer sampleId: GAMap.get(geneId).keySet()) {
						String key = makeHashKey(geneId, sampleId);
						Integer oldData = geneData.get(key);
						if (oldData == null) {
							oldData = 0;
						}
						String newData = GAMap.get(geneId).get(sampleId);
						if (newData.equals("2")) {
							geneData.put(key, addAmpToAlterationProfile(oldData));
						} else if (newData.equals("-2")) {
							geneData.put(key, addDelToAlterationProfile(oldData));
						}
					}
				}
			}
		}
		System.out.println("Aggregate frequency data");
		// convert sample data into frequency data
		HashMap<Long, AlterationFrequencyProfile> alterationFrequency = new HashMap<>();
		for (String key: geneData.keySet()) {
			long entrezGeneId = decodeHashKey(key)[0];
			AlterationFrequencyProfile altFreqProf = alterationFrequency.get(entrezGeneId);
			if (altFreqProf == null) {
				altFreqProf = new AlterationFrequencyProfile();
				alterationFrequency.put(entrezGeneId, altFreqProf);
			}
			int sampleData = geneData.get(key);
			altFreqProf.totalCt += 1;
			DaoAlterationFrequency.AlterationFrequencyType sampleProfile = decodeAlterationProfile(sampleData);
			if (sampleProfile == DaoAlterationFrequency.AlterationFrequencyType.MUT) {
				altFreqProf.mutCt += 1;
			} else if (sampleProfile == DaoAlterationFrequency.AlterationFrequencyType.CNA_AMP) {
				altFreqProf.ampCt += 1;
			} else if (sampleProfile == DaoAlterationFrequency.AlterationFrequencyType.CNA_DEL) {
				altFreqProf.delCt += 1;
			} else if (sampleProfile == DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_AMP) {
				altFreqProf.mutAmpCt += 1;
				altFreqProf.mutCt += 1;
				altFreqProf.ampCt += 1;
			} else if (sampleProfile == DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_DEL) {
				altFreqProf.mutDelCt += 1;
				altFreqProf.mutCt += 1;
				altFreqProf.delCt += 1;
			}
		}
		System.out.println("Put frequency data into database");
		// put frequency data into database
		// TODO: handle denominator correctly
		for (Long entrezGeneId: alterationFrequency.keySet()) {
			AlterationFrequencyProfile altFreqProf = alterationFrequency.get(entrezGeneId);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), entrezGeneId, DaoAlterationFrequency.AlterationFrequencyType.MUT, altFreqProf.mutCt/altFreqProf.totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), entrezGeneId, DaoAlterationFrequency.AlterationFrequencyType.CNA_AMP, altFreqProf.ampCt/altFreqProf.totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), entrezGeneId, DaoAlterationFrequency.AlterationFrequencyType.CNA_DEL, altFreqProf.delCt/altFreqProf.totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), entrezGeneId, DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_AMP, altFreqProf.mutAmpCt/altFreqProf.totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), entrezGeneId, DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_DEL, altFreqProf.mutDelCt/altFreqProf.totalCt);
		}
		System.out.println("clearing table");
		geneData.clear();
	}
}
