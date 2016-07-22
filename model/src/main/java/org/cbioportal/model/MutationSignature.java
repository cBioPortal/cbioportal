package org.cbioportal.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutationSignature implements Serializable {
	public static enum MutationSignatureType {
		NO_CONTEXT, ONE_BP_CONTEXT
	}
	private static final List<String> NUCLEOTIDES = Arrays.asList(new String[]{"A", "C", "T", "G"});
	private static final String[] CANONICAL_SNP_TYPES = new String[]{"CA", "CG", "CT", "TA", "TC", "TG"};
	
	private final MutationSignatureType signatureType;
	private final Integer[] counts;
	private final String[] mutationTypes;
	
	private static String[] getMutationTypes(MutationSignatureType type) {
		String[] mutationTypes;
		switch(type) {
			default:
			case NO_CONTEXT:
				mutationTypes = new String[CANONICAL_SNP_TYPES.length];
				System.arraycopy(CANONICAL_SNP_TYPES, 0, mutationTypes, 0, CANONICAL_SNP_TYPES.length);
				break;
			case ONE_BP_CONTEXT:
				mutationTypes = new String[NUCLEOTIDES.size() * CANONICAL_SNP_TYPES.length * NUCLEOTIDES.size()];
				int i = 0;
				for (String snp: CANONICAL_SNP_TYPES) {
					for (String before: NUCLEOTIDES) {
						for (String after: NUCLEOTIDES) {
							mutationTypes[i] = before + snp + after;
							i++;
						}
					}
				}
				break;
				
		}
		return mutationTypes;
	}
	
	private String getMutationType(Mutation mutation) {
		String ref = mutation.getMutationEvent().getReferenceAllele();
		if (ref == null || ref.length() != 1) {
			// Not a SNP
			return null;
		}
		String tum = mutation.getMutationEvent().getVariantAllele();
		if (tum == null || tum.length() != 1) {
			// Not a SNP
			return null;
		}
		if (NUCLEOTIDES.indexOf(ref) == -1 || NUCLEOTIDES.indexOf(tum) == -1) {
			// Somethings wrong
			return null;
		}
		// If everythings good, then we have the SNP
		String snp = ref + tum;
		String ret;
		switch (signatureType) {
			default:
			case NO_CONTEXT:
				ret = snp;
				break;
			case ONE_BP_CONTEXT:
				// REF TRI NOT YET IMPLEMENTED
				ret = null;
				break;
		}
		return ret;
	}
	
	private Integer[] fromData(List<Mutation> mutations) {
		Integer[] ret = new Integer[counts.length];
		Arrays.fill(ret, 0);
		// Keep map for quick reference
		Map<String, Integer> mutationTypeToIndex = new HashMap<>();
		for (int i=0; i<mutationTypes.length; i++) {
			mutationTypeToIndex.put(mutationTypes[i], i);
		}
		
		for (Mutation mutation: mutations) {
			String mutationType = getMutationType(mutation);
			if (mutationType != null) {
				ret[mutationTypeToIndex.get(mutationType)] += 1;
			}
		}
		return ret;
	}
	public MutationSignature(MutationSignatureType type, List<Mutation> mutations) {
		signatureType = type;
		mutationTypes = getMutationTypes(type);
		counts = fromData(mutations);
	}
	
	public Integer[] getCounts() {
		return counts;
	}
	
	public double[] getProbabilityDistribution() {
		double total = 0;
		for (int c: counts) {
			total += c;
		}
		double[] pmf = new double[counts.length];
		for (int i=0; i<counts.length; i++) {
			pmf[i] = counts[i] / total;
		}
		return pmf;
	}
	
	public String[] getMutationTypes() {
		return mutationTypes;
	}
	
	public MutationSignatureType getSignatureType() {
		return signatureType;
	}
	
}
