/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author abeshoua
 */
public class MutationSignatureFactory {
	public static enum MutationSignatureType {
		NO_CONTEXT, ONE_BP_CONTEXT
	}
	private static final List<String> NUCLEOTIDES = Arrays.asList(new String[]{"A", "C", "T", "G"});
	
	private static final String[] CANONICAL_SNP_TYPES = new String[]{"CA", "CG", "CT", "TA", "TC", "TG"};
	
	private static final String[] NO_CONTEXT_MUTATION_TYPES;
	private static final String[] ONE_BP_CONTEXT_MUTATION_TYPES;
	
	static {
		NO_CONTEXT_MUTATION_TYPES = new String[CANONICAL_SNP_TYPES.length];
		System.arraycopy(CANONICAL_SNP_TYPES, 0, NO_CONTEXT_MUTATION_TYPES, 0, CANONICAL_SNP_TYPES.length);

		ONE_BP_CONTEXT_MUTATION_TYPES = new String[NUCLEOTIDES.size() * CANONICAL_SNP_TYPES.length * NUCLEOTIDES.size()];
		int i = 0;
		for (String snp : CANONICAL_SNP_TYPES) {
			for (String before : NUCLEOTIDES) {
				for (String after : NUCLEOTIDES) {
					ONE_BP_CONTEXT_MUTATION_TYPES[i] = before + snp + after;
					i++;
				}
			}
		}
	}
	
	private static String complementaryNucleotide(String nucleotide) {
		String comp = null;
		switch(nucleotide) {
			case "A":
				comp = "T";
				break;
			case "T":
				comp = "A";
				break;
			case "C":
				comp = "G";
				break;
			case "G":
				comp = "C";
				break;
		}
		return comp;
	}
	
	private static String[] getMutationTypes(MutationSignatureType type) {
		String[] mutationTypes;
		switch(type) {
			default:
			case NO_CONTEXT:
				mutationTypes = NO_CONTEXT_MUTATION_TYPES;
				break;
			case ONE_BP_CONTEXT:
				mutationTypes = ONE_BP_CONTEXT_MUTATION_TYPES;
				break;
				
		}
		return mutationTypes;
	}
	
	private static String getMutationTypeFromMutation(MutationSignatureType type, Mutation mutation) {
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
		// Put it into canonical form: starting with C or T
		if (!ref.equals("C") && !ref.equals("T")) {
			ref = complementaryNucleotide(ref);
			tum = complementaryNucleotide(tum);
		}
		String snp = ref + tum;
		String ret;
		switch (type) {
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
	
	private static Integer[] makeSignature(MutationSignatureType type, List<Mutation> mutations) {
		String[] mutationTypes = getMutationTypes(type);
		Integer[] ret = new Integer[mutationTypes.length];
		Arrays.fill(ret, 0);
		// Keep map for quick reference
		Map<String, Integer> mutationTypeToIndex = new HashMap<>();
		for (int i=0; i<mutationTypes.length; i++) {
			mutationTypeToIndex.put(mutationTypes[i], i);
		}
		
		for (Mutation mutation: mutations) {
			String mutationType = getMutationTypeFromMutation(type, mutation);
			if (mutationType != null) {
				ret[mutationTypeToIndex.get(mutationType)] += 1;
			}
		}
		return ret;
	}
	public static MutationSignature MutationSignature(String id, MutationSignatureType type, List<Mutation> mutations) {
		return new MutationSignature(id, getMutationTypes(type), makeSignature(type, mutations));
	}
	
	public static MutationSignature MutationSignature(String id, MutationSignatureType type, Integer[] counts) {
		return new MutationSignature(id, getMutationTypes(type), counts);
	}
}
