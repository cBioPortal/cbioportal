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
	
	private static final List<String> NUCLEOTIDES = Arrays.asList(new String[]{"A", "C", "T", "G"});
	
	private static final String[] CANONICAL_SNP_TYPES = new String[]{"C>A", "C>G", "C>T", "T>A", "T>C", "T>G"};
	
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
	
	private static String getNoContextMutationTypeString(Mutation mutation) {
		String ref = mutation.getMutationEvent().getReferenceAllele();
		if (ref == null || ref.length() != 1) {
			// Not a SNP
			return null;
		}
		String tum = mutation.getMutationEvent().getTumorSeqAllele();
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
		String snp = ref + ">" + tum;
		return snp;
	}
	
	private static Map<String, Integer> makeNoContextSignature(List<Mutation> mutations) {
		Map<String, Integer> noContextSignature = new HashMap<>();
		
		for (Mutation mutation: mutations) {
			String mutationType = getNoContextMutationTypeString(mutation);
			if (mutationType != null) {
				Integer currentCount = noContextSignature.get(mutationType);
				if (currentCount == null) {
					currentCount = 0;
				}
				currentCount += 1;
				noContextSignature.put(mutationType, currentCount);
			}
		}
		for (String mutationType: NO_CONTEXT_MUTATION_TYPES) {
			if (!noContextSignature.containsKey(mutationType)) {
				noContextSignature.put(mutationType, 0);
			}
		}
		return noContextSignature;
	}
	public static MutationSignature NoContextMutationSignature(String id, List<Mutation> mutations) {
		return new MutationSignature(id, makeNoContextSignature(mutations));
	}
}
