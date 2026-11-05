package org.cbioportal.shared.model;

/**
 * The kind of data a molecular profile holds. Stored in the {@code genetic_alteration_type} column
 * and returned by the API as {@code molecularAlterationType}.
 */
public enum MolecularAlterationType {
  MUTATION_EXTENDED,
  // uncalled mutations (mskcc internal) for showing read counts even if
  // mutation wasn't called
  MUTATION_UNCALLED,
  STRUCTURAL_VARIANT,
  COPY_NUMBER_ALTERATION,
  MICRO_RNA_EXPRESSION,
  MRNA_EXPRESSION,
  MRNA_EXPRESSION_NORMALS,
  RNA_EXPRESSION,
  METHYLATION,
  METHYLATION_BINARY,
  PHOSPHORYLATION,
  PROTEIN_LEVEL,
  PROTEIN_ARRAY_PROTEIN_LEVEL,
  PROTEIN_ARRAY_PHOSPHORYLATION,
  GENESET_SCORE,
  GENERIC_ASSAY
}
