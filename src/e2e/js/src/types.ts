/**
 * Types derived from cBioPortal Swagger API documentation
 * Source: https://www.cbioportal.org/api/v3/api-docs/internal
 */

/**
 * CountSummary - Statistics for a group in enrichment analysis
 * Required fields: name, alteredCount, profiledCount
 */
export interface CountSummary {
  /** Group name identifier */
  name: string;
  /** Number of cases with alterations in this group */
  alteredCount: number;
  /** Total number of profiled cases in this group */
  profiledCount: number;
}

/**
 * AlterationEnrichment - Enrichment analysis results for genetic alterations
 * Required fields: entrezGeneId, hugoGeneSymbol, counts
 */
export interface AlterationEnrichment {
  /** Entrez Gene ID */
  entrezGeneId: number;
  /** Hugo Gene Symbol */
  hugoGeneSymbol: string;
  /** Chromosomal cytoband location (optional) */
  cytoband?: string;
  /** Array of count summaries for each group */
  counts: CountSummary[];
  /** Statistical significance p-value (optional) */
  pValue?: number;
}

/**
 * Enrichment type for API requests
 */
export enum EnrichmentType {
  SAMPLE = 'SAMPLE',
  PATIENT = 'PATIENT'
}

/**
 * CancerType - Cancer type information
 * Source: Derived from API response at /api/column-store/studies
 */
export interface CancerType {
  /** Cancer type ID */
  id: string;
  /** Cancer type name */
  name: string;
  /** Dedicated color for visualization */
  dedicatedColor: string;
  /** Short name abbreviation */
  shortName: string;
  /** Parent cancer type */
  parent: string;
}

/**
 * CancerStudyMetadataDTO - Cancer study metadata with detailed statistics
 * Source: Derived from API response at /api/column-store/studies?projection=DETAILED
 * All fields are required unless marked optional
 */
export interface CancerStudyMetadataDTO {
  /** Study identifier */
  studyId: string;
  /** Cancer type identifier */
  cancerTypeId: string;
  /** Study name */
  name: string;
  /** Study description */
  description: string;
  /** Whether study is public */
  publicStudy: boolean;
  /** PubMed ID (optional) */
  pmid?: string;
  /** Citation text (optional) */
  citation?: string;
  /** User groups with access */
  groups: string;
  /** Study status code */
  status: number;
  /** Date study was imported */
  importDate: string;
  /** Total count of all samples */
  allSampleCount: number;
  /** Count of sequenced samples */
  sequencedSampleCount: number;
  /** Count of CNA samples */
  cnaSampleCount: number;
  /** Count of mRNA RNA-Seq samples */
  mrnaRnaSeqSampleCount: number;
  /** Count of mRNA RNA-Seq V2 samples */
  mrnaRnaSeqV2SampleCount: number;
  /** Count of mRNA microarray samples */
  mrnaMicroarraySampleCount: number;
  /** Count of miRNA samples */
  miRnaSampleCount: number;
  /** Count of methylation HM27 samples */
  methylationHm27SampleCount: number;
  /** Count of RPPA samples */
  rppaSampleCount: number;
  /** Count of mass spectrometry samples */
  massSpectrometrySampleCount: number;
  /** Count of complete samples */
  completeSampleCount: number;
  /** Reference genome version */
  referenceGenome: string;
  /** Count of treatments */
  treatmentCount: number;
  /** Count of structural variants */
  structuralVariantCount: number;
  /** Cancer type details */
  cancerType: CancerType;
  /** Whether user has read permission */
  readPermission: boolean;
}