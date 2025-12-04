# Virtual Study JSON Schema

Use this payload when calling `POST /api/public_virtual_studies/{custom_id}` with the publisher API key to create and immediately publish a public virtual study.

## VirtualStudyData object
- `name` (string, required): Display title of the virtual study on the landing page and in the study header.
- `description` (string, optional): Free-text description of virtual study
- `studies` (array of objects, optional but required for static cohorts): Explicit sample membership. Each item has `id` (study stable ID) and `samples` (array of sample stable IDs). This is the set published when `dynamic` is false or absent.
- `studyViewFilter` (object, recommended): Study View filter definition used to describe the cohort. When `dynamic` is `true`, this filter is applied on every fetch to regenerate the `studies` set. The filter must include **either** `sampleIdentifiers` **or** `studyIds` (mutually exclusive).
- `typeOfCancerId` (string, optional): Cancer type code used to categorize the public study; validated against existing cancer types. Can also be passed as a query parameter.
- `pmid` (string, optional): PubMed ID linked to the public study; can also be set via query parameter.
- `dynamic` (boolean, optional): When `true`, `VirtualStudyService` recomputes `studies` from `studyViewFilter` on each retrieval; when `false` or missing, the provided `studies` list is used as-is.

## StudyViewFilter shape
The `studyViewFilter` mirrors the payload accepted by Study View APIs and consumed by `StudyViewFilterApplier`. Key fields:
- `sampleIdentifiers` (array of `{studyId, sampleId}`) **or** `studyIds` (array of study stable IDs): One of these is required; they cannot both be present.
- `clinicalDataFilters` / `customDataFilters` (arrays): Each entry has `attributeId` and `values` (array of `{value}` or `{start,end}`; do not mix value with start/end in the same object).
- `geneFilters`, `structuralVariantFilters`, `mutationDataFilters`: Gene-level selections. Provide `molecularProfileIds` plus `geneQueries`/`values` per the Study View gene filter contract (supports mutation, SV, and CNA filters).
- `genomicDataFilters`, `genericAssayDataFilters`, `genomicProfiles`: Expression/assay profile filters; profile types must match existing molecular profiles in the targeted studies.
- `caseLists`: Array of arrays of case list types used to intersect predefined case lists.
- `namespaceDataFilters`: Namespaced categorical filters applied like clinical equality filters.
- `sampleTreatmentFilters`, `sampleTreatmentGroupFilters`, `sampleTreatmentTargetFilters`, `patientTreatmentFilters`, `patientTreatmentGroupFilters`, `patientTreatmentTargetFilters`: Treatment filter blocks with the same shape as Study View treatment filters.
- `alterationFilter`, `clinicalEventFilters`, `mutationDataFilters`: Additional filter blocks applied by Study View; structure matches the Study View API.

## Examples
Static cohort with explicit samples:
```json
{
  "name": "BRCA ER+ samples",
  "description": "Explicit sample list across two studies",
  "studies": [
    { "id": "brca_tcga", "samples": ["TCGA-AR-A1AI-01", "TCGA-AR-A1AJ-01"] },
    { "id": "brca_igr", "samples": ["P-0000010-T01-IM6"] }
  ],
  "studyViewFilter": {
    "sampleIdentifiers": [
      { "studyId": "brca_tcga", "sampleId": "TCGA-AR-A1AI-01" },
      { "studyId": "brca_tcga", "sampleId": "TCGA-AR-A1AJ-01" },
      { "studyId": "brca_igr", "sampleId": "P-0000010-T01-IM6" }
    ],
    "clinicalDataFilters": [
      { "attributeId": "ER_STATUS_BY_IHC", "values": [ { "value": "Positive" } ] }
    ]
  },
  "typeOfCancerId": "brca",
  "pmid": "12345678"
}
```

Dynamic cohort recalculated from filters:
```json
{
  "name": "MSI-high colorectal (dynamic)",
  "description": "Rebuilt from filters on each load",
  "dynamic": true,
  "studyViewFilter": {
    "studyIds": ["coadread_tcga"],
    "clinicalDataFilters": [
      { "attributeId": "MUTATION_SIGNAT_MSI_STATUS", "values": [ { "value": "MSI-H" } ] }
    ]
  },
  "typeOfCancerId": "coadread"
}
```
