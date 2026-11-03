/*
 * Copyright (c) 2015 - 2022 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model.shared;

// Copied to org.cbioportal.legacy.model.MolecularProfile.GeneticAlterationType, if you alter this,
// don't forget to change the other one too
public enum GeneticAlterationType {
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
};
