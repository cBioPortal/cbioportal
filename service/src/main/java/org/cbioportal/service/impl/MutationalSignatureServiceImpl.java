package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationalSignature;
import org.cbioportal.model.SNPCount;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.cbioportal.persistence.MutationalSignatureRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.MutationalSignatureService;

@Service
public class MutationalSignatureServiceImpl implements MutationalSignatureService {

	public static class MutationalSignatureFactory {
		private static final Map<String, String> COMPLEMENTARY_NUCLEOTIDE = new HashMap<>();
		private static final String[] CANONICAL_SNP_TYPES = new String[]{"C>A", "C>G", "C>T", "T>A", "T>C", "T>G"};
		private static final Map<String, Integer> CANONICAL_SNP_TYPES_INDEX;
		
		static {
			COMPLEMENTARY_NUCLEOTIDE.put("A","T");
			COMPLEMENTARY_NUCLEOTIDE.put("T","A");
			COMPLEMENTARY_NUCLEOTIDE.put("C","G");
			COMPLEMENTARY_NUCLEOTIDE.put("G","C");
			
			CANONICAL_SNP_TYPES_INDEX = new HashMap<>();
			for (int j = 0; j < CANONICAL_SNP_TYPES.length; j++) {
				CANONICAL_SNP_TYPES_INDEX.put(CANONICAL_SNP_TYPES[j], j);
			}
		}
		
		private static Integer getCanonicalSNPIndex(String referenceAllele, String tumorAllele) {
			if (!referenceAllele.equals("C") && !referenceAllele.equals("T")) {
				referenceAllele = COMPLEMENTARY_NUCLEOTIDE.get(referenceAllele);
				tumorAllele = COMPLEMENTARY_NUCLEOTIDE.get(tumorAllele);
				if (referenceAllele == null || tumorAllele == null) {
					// Something's wrong
					return -1;
				}
			}
			Integer index = CANONICAL_SNP_TYPES_INDEX.get(referenceAllele+">"+tumorAllele);
			if (index == null) {
				index = -1;
			}
			return index;
		}
		
		private static Map<String, int[]> getSignatureCountsBySample(List<SNPCount> snpCounts, List<String> sampleIds) {
			Map<String, int[]> signatureCounts = new HashMap<>();
			for (String sampleId: sampleIds) {
				signatureCounts.put(sampleId, new int[CANONICAL_SNP_TYPES.length]);
			}
			
			for (SNPCount snp: snpCounts) {
				String sampleId = snp.getSampleId();
				int[] signature = signatureCounts.get(sampleId);
				if (signature != null) {
					// Increment appropriate index
					int snpIndex = getCanonicalSNPIndex(snp.getReferenceAllele(), snp.getTumorAllele());
					if (snpIndex > -1) {
						signature[snpIndex] += snp.getCount();
					}
				}
			}
			return signatureCounts;
		}
		
		public static List<MutationalSignature> NoContextSignatures(List<SNPCount> snpCounts, List<String> sampleIds) {
			Map<String, int[]> signatureCounts = getSignatureCountsBySample(snpCounts, sampleIds);
			List<MutationalSignature> signatures = new LinkedList<>();
			for (Entry<String, int[]> entry: signatureCounts.entrySet()) {
				signatures.add(new MutationalSignature(CANONICAL_SNP_TYPES, entry.getKey(), entry.getValue()));
			}
			return signatures;
		}
	}
	
	@Autowired
	private MutationalSignatureRepository mutationalSignatureRepository;
	
	@Autowired
	private GeneticProfileRepository geneticProfileRepository;
	
	@Autowired
	private SampleRepository sampleRepository;
	
	private GeneticProfile getMutationProfile(String study_id) {
		List<GeneticProfile> geneticProfiles = geneticProfileRepository.getAllGeneticProfilesInStudy(study_id, "SUMMARY", null, null, null, null);
		GeneticProfile mutationProfile = null;
		for (GeneticProfile gp: geneticProfiles) {
			if (gp.getGeneticAlterationType() == GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED) {
				mutationProfile = gp;
				break;
			}
		}
		return mutationProfile;
	}

  @PreAuthorize("hasPermission(#study_id, 'CancerStudy', 'read')")
	public List<MutationalSignature> getMutationalSignaturesBySampleIds(String study_id, List<String> sample_ids) {
		GeneticProfile mutationProfile = getMutationProfile(study_id);
		if (mutationProfile == null) {
			return new ArrayList<>();
		} else {
			List<SNPCount> snpCounts = mutationalSignatureRepository.getSNPCounts(mutationProfile.getStableId(), sample_ids);
			return MutationalSignatureFactory.NoContextSignatures(snpCounts, sample_ids);
		}
	}

  @PreAuthorize("hasPermission(#study_id, 'CancerStudy', 'read')")
	public List<MutationalSignature> getMutationalSignatures(String study_id) {
		GeneticProfile mutationProfile = getMutationProfile(study_id);
		if (mutationProfile == null) {
			return new ArrayList<>();
		} else {
			List<SNPCount> snpCounts = mutationalSignatureRepository.getSNPCounts(mutationProfile.getStableId());
			List<Sample> allSamples = sampleRepository.getAllSamplesInStudy(study_id, "SUMMARY", null, null, null, null);
			List<String> sampleIds = new LinkedList<>();
			for (Sample sample: allSamples) {
				sampleIds.add(sample.getStableId());
			}
			return MutationalSignatureFactory.NoContextSignatures(snpCounts, sampleIds);
		}
	}
}
