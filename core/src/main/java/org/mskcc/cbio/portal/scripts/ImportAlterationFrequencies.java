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
		HashMap<String, SampleAlterationProfile> geneData = new HashMap<>();
		// key is entrezgeneid&internalsampleid
		for (GeneticProfile prof : mutationProfiles) {
			List<ExtendedMutation> mutations = DaoMutation.getMutations(prof.getGeneticProfileId());
			for (ExtendedMutation mut : mutations) {
				int sampleId = mut.getSampleId();
				long geneId = mut.getEntrezGeneId();
				String key = makeHashKey(geneId, sampleId);
				if (!geneData.containsKey(key)) {
					geneData.put(key, new SampleAlterationProfile());
				}
				geneData.get(key).MUT = 1;
			}
		}
		for (GeneticProfile prof: cnaProfiles) {
			DaoGeneticAlteration geneticAlteration = DaoGeneticAlteration.getInstance();
			Set<CanonicalGene> genes = geneticAlteration.getGenesInProfile(prof.getGeneticProfileId());
			LinkedList<Long> geneIds = new LinkedList<>();
			for (CanonicalGene gene: genes) {
				geneIds.add(gene.getEntrezGeneId());
			}
			HashMap<Long, HashMap<Integer, String>> GAMap = geneticAlteration.getGeneticAlterationMap(prof.getGeneticProfileId(), geneIds);
			for (Long geneId: GAMap.keySet()) {
				for (Integer sampleId: GAMap.get(geneId).keySet()) {
					String key = makeHashKey(geneId, sampleId);
					if (!geneData.containsKey(key)) {
						geneData.put(key, new SampleAlterationProfile());
					}
					if (GAMap.get(geneId).get(sampleId).equals("2")) {
						geneData.get(key).AMP = 1;
					} else if (GAMap.get(geneId).get(sampleId).equals("-2")) {
						geneData.get(key).DEL = 1;
					}
				}
			}
			GAMap.clear();
		
		}
		System.out.println("Aggregate frequency data");
		// convert sample data into frequency data
		HashMap<Long, AlterationFrequencyProfile> alterationFrequency = new HashMap<>();
		for (String key: geneData.keySet()) {
			long entrezGeneId = decodeHashKey(key)[0];
			if (!alterationFrequency.containsKey(entrezGeneId)) {
				alterationFrequency.put(entrezGeneId, new AlterationFrequencyProfile());
			}
			AlterationFrequencyProfile altFreqProf = alterationFrequency.get(entrezGeneId);
			SampleAlterationProfile sampleData = geneData.get(key);
			altFreqProf.totalCt ++;
			if (sampleData.MUT == 1) {
				if (sampleData.AMP == 1) {
					altFreqProf.mutAmpCt++;
				}
				if (sampleData.DEL == 1) {
					altFreqProf.mutDelCt++;
				}
				altFreqProf.mutCt++;
			}
			if (sampleData.AMP == 1) {
				altFreqProf.ampCt++;
			}
			if (sampleData.DEL == 1) {
				altFreqProf.delCt++;
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
