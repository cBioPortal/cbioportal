package org.cbioportal.domain.clinical_data_enrichment;

import java.math.BigDecimal;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;

/**
 * Record representing the result of a clinical data enrichment analysis.
 *
 * <p>Contains the statistical test results for comparing clinical attributes across sample groups,
 * including the clinical attribute metadata, test score, p-value, and method used.
 *
 * @param clinicalAttribute the clinical attribute that was analyzed
 * @param score the statistical test score (e.g., Kruskal-Wallis statistic or Chi-squared statistic)
 * @param method the statistical method used (e.g., "Kruskal Wallis Test", "Chi-squared Test")
 * @param pValue the p-value from the statistical test
 */
public record ClinicalDataEnrichment(
    ClinicalAttribute clinicalAttribute,
    BigDecimal score,
    EnrichmentTestMethod method,
    BigDecimal pValue) {}
