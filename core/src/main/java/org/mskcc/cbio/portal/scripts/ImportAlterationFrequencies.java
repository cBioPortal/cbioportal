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
	public static void main(String[] args) {
		for(CancerStudy study: DaoCancerStudy.getAllCancerStudies()) {
			try {
				importAlterationFrequencies(study.getCancerStudyStableId());
			} catch (Exception e) {
				System.out.println("Error importing alteration frequencies for "+study.getCancerStudyStableId());
			}
		}
	}
	public static void importAlterationFrequencies(String studyStableId) throws Exception {
		// get list of profiles
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
		HashMap<Long, HashMap<Integer, SampleAlterationProfile>> geneData = new HashMap<>();
		for (GeneticProfile prof : mutationProfiles) {
			List<ExtendedMutation> mutations = DaoMutation.getMutations(prof.getGeneticProfileId());
			for (ExtendedMutation mut : mutations) {
				int sampleId = mut.getSampleId();
				long geneId = mut.getEntrezGeneId();
				if (!geneData.containsKey(geneId)) {
					geneData.put(geneId, new HashMap<Integer, SampleAlterationProfile>());
				}
				if (!geneData.get(geneId).containsKey(sampleId)) {
					geneData.get(geneId).put(sampleId, new SampleAlterationProfile());
				}
				geneData.get(geneId).get(sampleId).MUT = 1;
			}
		}
		for (GeneticProfile prof: cnaProfiles) {
			DaoGeneticAlteration geneticAlteration = DaoGeneticAlteration.getInstance();
			Set<CanonicalGene> genes = geneticAlteration.getGenesInProfile(prof.getGeneticProfileId());
			for (CanonicalGene gene: genes) {
				long geneId = gene.getEntrezGeneId();
				HashMap<Integer, String> GAMap = geneticAlteration.getGeneticAlterationMap(prof.getGeneticProfileId(), geneId);
				for (Integer sampleId: GAMap.keySet()) {
					if (!geneData.containsKey(geneId)) {
						geneData.put(geneId, new HashMap<Integer, SampleAlterationProfile>());
					}
					if (!geneData.get(geneId).containsKey(sampleId)) {
						geneData.get(geneId).put(sampleId, new SampleAlterationProfile());
					}
					if (GAMap.get(sampleId).equals("2")) {
						geneData.get(geneId).get(sampleId).AMP = 1;
					} else if (GAMap.get(sampleId).equals("-2")) {
						geneData.get(geneId).get(sampleId).DEL = 1;
					}
				}
			}
		
		}
		// convert sample data into frequency data
		for (Long gene: geneData.keySet()) {
			float mutCt = 0, ampCt = 0, delCt = 0, totalCt = 0, mutAmpCt = 0, mutDelCt = 0;
			for (Integer sampleId: geneData.get(gene).keySet()) {
				totalCt++;
				SampleAlterationProfile sampleData = geneData.get(gene).get(sampleId);
				if (sampleData.MUT == 1) {
					if (sampleData.AMP == 1) {
						mutAmpCt++;
					}
					if (sampleData.DEL == 1) {
						mutDelCt++;
					}
					mutCt++;
				}
				if (sampleData.AMP == 1) {
					ampCt++;
				}
				if (sampleData.DEL == 1) {
					delCt++;
				}
			}
			// put frequency data into database
			// TODO: handle denominator correctly
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), gene, DaoAlterationFrequency.AlterationFrequencyType.MUT, mutCt/totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), gene, DaoAlterationFrequency.AlterationFrequencyType.CNA_AMP, ampCt/totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), gene, DaoAlterationFrequency.AlterationFrequencyType.CNA_DEL, delCt/totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), gene, DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_AMP, mutAmpCt/totalCt);
			DaoAlterationFrequency.addAlterationFrequency(study.getInternalId(), gene, DaoAlterationFrequency.AlterationFrequencyType.MUT_AND_CNA_DEL, mutDelCt/totalCt);
		}
	}
}
