# Virtual Study JSON Schema

Use this payload when calling  
`POST /api/public_virtual_studies/{custom_id}`  
with a publisher API key to create and publish a **virtual study**.

A virtual study is either:
- **Static**: fixed sample set (requires `studies[*].samples`)  
- **Dynamic**: sample set recomputed from filters (`dynamic: true`; `studies[*].samples` may be omitted)

> `studies` (array) is **always required**.  

---

## 1. Virtual Study Data Fields

| Field | Type | Required | Description |
|-------|-------|----------|-------------|
| `name` | string | yes | Title shown on the study page and in search results. |
| `description` | string | no | Free-text description. |
| `studies` | array of objects | yes | Declares which studies participate in the virtual study. Each entry: `{ id: <studyId>, samples: [...] }`. Sample list required for static studies; optional for dynamic ones. |
| `dynamic` | boolean | no | If `true`, sample membership is recomputed from `studyViewFilter` on each load. If `false` or omitted, the study is static and must include explicit sample lists. |
| `studyViewFilter` | object | required for dynamic studies | Study View–style filter block describing cohort selection. Used for dynamic recomputation and for documenting static cohorts. |
| `typeOfCancerId` | string | no | Cancer type classification. Must match an existing cancer type. |
| `pmid` | string | no | PubMed ID linked to the study. |

---

## 2. `studies[]` Structure

| Field | Type | Required | Description |
|-------|-------|----------|-------------|
| `id` | string | yes | Study stable ID included in this virtual study. |
| `samples` | array of strings | static: yes<br>dynamic: no | Sample stable IDs belonging to this study. Required for static virtual studies; optional for dynamic ones (filters determine membership). |

---

## 3. `studyViewFilter` Structure

### Entry-point selectors

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `sampleIdentifiers` | array of `{studyId, sampleId}` | yes | Explicit sample list. |
| `studyIds` | array of strings | yes | Apply filters across entire studies. |

### Common filter blocks (all optional)

| Filter Block | Purpose | Shape |
|--------------|---------|-------|
| `clinicalDataFilters` | Clinical attribute filtering | `{ attributeId, values: [{value}] }` or numeric ranges (`{start,end}`). Do not mix value and range in same object. |
| `customDataFilters` | Custom categorical filters | Same shape as clinical filters. |
| `namespaceDataFilters` | Namespaced categorical filters | Same shape as clinical filters. |
| `geneFilters` / `mutationDataFilters` / `structuralVariantFilters` | Gene-based molecular selection | Must include `molecularProfileIds`. Uses `geneQueries` and `values`. |
| `genomicDataFilters` / `genericAssayDataFilters` / `genomicProfiles` | Expression, assay, or other profile filters | Profiles must exist in the referenced studies. |
| `clinicalEventFilters` | Event timeline filters | Same contract as Study View API. |
| Treatment filters (`sampleTreatmentFilters`, etc.) | Treatment-based sample or patient selection | Same structure as clinical categorical filters. |
| `caseLists` | Intersect predefined case lists | Array of arrays of case list IDs. |

---

## 4. Examples

### Static Virtual Study

```json
{
  "name": "BRCA ER+ samples",
  "description": "Explicit sample list across two studies",
  "studies": [
    { "id": "brca_tcga", "samples": ["TCGA-AR-A1AI-01", "TCGA-AR-A1AJ-01"] },
    { "id": "brca_igr",  "samples": ["P-0000010-T01-IM6"] }
  ],
  "typeOfCancerId": "brca",
  "pmid": "12345678"
}
```

### Dynamic Virtual Study
```json
{
  "name": "MSI-high colorectal (dynamic)",
  "description": "Rebuilt from filters on each load",
  "dynamic": true,
  "studies": [
    { "id": "coadread_tcga" }
  ],
  "studyViewFilter": {
    "studyIds": ["coadread_tcga"],
    "clinicalDataFilters": [
      { "attributeId": "MUTATION_SIGNAT_MSI_STATUS", "values": [{ "value": "MSI-H" }] }
    ]
  },
  "typeOfCancerId": "coadread"
}
```

## Exploring the Full Filter Structure

The `studyViewFilter` object supports a wide range of filter types used throughout the Study View interface—clinical, molecular, genomic, treatment, event-based, and many others. Because these filters mirror the full Study View filtering capabilities, the structure can be extensive and is not exhaustively documented here.

### Easiest Way to Discover Supported Filters

The most reliable and user-friendly way to explore the complete filter schema is:

1. **Create a virtual study using the cBioPortal UI** (Study View → Create Virtual Study).  
2. **Save it**, then retrieve the generated filter definition via the link: `https://<cbioportal-host>/api/session/virtual_study/<virtual-study-id>`
3. Inspect the `data` field of the returned JSON.  
- The `data.studyViewFilter` value shows the exact filter structure produced by the UI.  
- This includes all nested blocks, attribute IDs, molecular profile IDs, treatment filters, genomic filters, etc., exactly as the server expects them.

This UI-to-API round-trip is the most accurate way to discover:
- Which filter blocks are supported on your specific cBioPortal instance  
- The exact shape of each filter object  
- Correct field names and attribute IDs used by your data installation  
- How combined filters are structured (e.g., gene + clinical + case lists)

Because filter capabilities may vary between portal installations or data versions, using the UI-generated filter as a reference ensures your `studyViewFilter` is valid and reproducible.
