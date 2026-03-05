# Resource Data Model Redesign Plan

## Database Changes

### `resource_definition` — unchanged

Its `DISPLAY_NAME` serves as the top-level category label in the UI tree.

### Drop old tables

- `resource_sample`
- `resource_patient`
- `resource_study`

### Add `resource_node`

```sql
CREATE TABLE `resource_node` (
  `ID`                  bigint        NOT NULL AUTO_INCREMENT,
  `RESOURCE_ID`         varchar(255)  NOT NULL,
  `CANCER_STUDY_ID`     int(11)       NOT NULL,
  `ENTITY_TYPE`         ENUM('STUDY','PATIENT','SAMPLE') NOT NULL,
  `ENTITY_INTERNAL_ID`  int(11)       NOT NULL,
  `PARENT_ID`           bigint        DEFAULT NULL,
  `NODE_TYPE`           ENUM('GROUP','ITEM') NOT NULL,
  `DISPLAY_NAME`        varchar(255)  NOT NULL,
  `URL`                 varchar(512)  DEFAULT NULL,   -- ITEM nodes only
  `TYPE`                varchar(64)   DEFAULT NULL,   -- ITEM nodes only; free-text
  `METADATA`            JSON          DEFAULT NULL,
  `PRIORITY`            int(11)       DEFAULT 0,
  PRIMARY KEY (`ID`),
  INDEX `idx_node_entity` (`RESOURCE_ID`, `CANCER_STUDY_ID`, `ENTITY_TYPE`, `ENTITY_INTERNAL_ID`),
  INDEX `idx_node_parent` (`PARENT_ID`),
  FOREIGN KEY (`RESOURCE_ID`, `CANCER_STUDY_ID`)
      REFERENCES `resource_definition` (`RESOURCE_ID`, `CANCER_STUDY_ID`) ON DELETE CASCADE,
  FOREIGN KEY (`PARENT_ID`)
      REFERENCES `resource_node` (`ID`) ON DELETE CASCADE
);
```

### Node semantics

| Field | Description |
|---|---|
| `NODE_TYPE=GROUP` | Folder node; no URL; can have children |
| `NODE_TYPE=ITEM` | Leaf node; has URL; no children |
| `PARENT_ID=NULL` | Root node directly under a `resource_definition` |
| `TYPE` | Free-text domain label (e.g. `H_AND_E`, `IHC`, `CT`, `BAM`, `PDF`); no schema change needed for new domains |

### Migration

Existing `resource_sample`, `resource_patient`, `resource_study` rows each become a root-level `ITEM` node with `PARENT_ID=NULL`.

```sql
-- Example: resource_patient → resource_node
INSERT INTO resource_node
  (RESOURCE_ID, CANCER_STUDY_ID, ENTITY_TYPE, ENTITY_INTERNAL_ID,
   PARENT_ID, NODE_TYPE, DISPLAY_NAME, URL)
SELECT
  rd.RESOURCE_ID, rd.CANCER_STUDY_ID, 'PATIENT', rp.INTERNAL_ID,
  NULL, 'ITEM', rd.DISPLAY_NAME, rp.URL
FROM resource_patient rp
JOIN resource_definition rd
  ON rp.RESOURCE_ID = rd.RESOURCE_ID;

-- Same pattern for resource_sample (ENTITY_TYPE='SAMPLE')
-- and resource_study (ENTITY_TYPE='STUDY')
```

---

## Import File Format

Three optional columns added to existing data files
(`data_resource_patient.txt`, `data_resource_sample.txt`, `data_resource_study.txt`):

| Column | Description |
|---|---|
| `DISPLAY_NAME` | Human-readable label for the item |
| `TYPE` | Free-text sub-classification (e.g. `H_AND_E`, `CT`, `BAM`) |
| `GROUP_PATH` | `/`-separated path of GROUP ancestors (e.g. `Block A/H&E Panel`) |

### Importer logic for `GROUP_PATH`

1. Split value on `/` to get ordered path segments
2. For each segment, upsert a GROUP node at the correct depth within the same `(RESOURCE_ID, ENTITY_ID)` scope
3. Insert the ITEM node under the deepest GROUP
4. Empty `GROUP_PATH` → root-level ITEM (backward compatible with old format)

### Example

```
PATIENT_ID  RESOURCE_ID   URL                    DISPLAY_NAME     TYPE           GROUP_PATH
P001        pathology     https://viewer/1       H&E              H_AND_E        Block A – Primary Tumor
P001        pathology     https://viewer/2       IHC CD3          IHC            Block A – Primary Tumor
P001        pathology     https://viewer/3       IHC PD-L1        IHC            Block A – Primary Tumor
P001        pathology     https://viewer/4       H&E              H_AND_E        Block B – Metastasis
P001        ct_scans      https://ohif/1         Instance         CT             CT 2023-01-15/Series 1: Axial T2
P001        ct_scans      https://ohif/2         Instance         CT             CT 2023-01-15/Series 2: Coronal T1
P001        reports       https://reports/1      Biopsy Report    PATH_REPORT    2023
P001        raw_data      https://storage/1      Tumor BAM        BAM            WGS
P001        publications  https://pubmed/1       TCGA Paper 2021  JOURNAL
```

---

## UI Tree Structure

```
▼ Pathology                      ← resource_definition.DISPLAY_NAME
  ▼ Block A – Primary Tumor      ← GROUP node
      H&E            [H_AND_E]   → iframe
      IHC CD3        [IHC]       → iframe
      IHC PD-L1      [IHC]       → iframe
  ▼ Block B – Metastasis         ← GROUP node
      H&E            [H_AND_E]   → iframe

▼ Radiology
  ▼ CT 2023-01-15                ← GROUP node (depth 1)
    ▼ Series 1: Axial T2         ← GROUP node (depth 2)
        Instance     [CT]        → iframe
    ▼ Series 2: Coronal T1
        Instance     [CT]        → iframe

▼ Clinical Reports
  ▼ 2023                         ← GROUP node
      Biopsy Report  [PATH_REPORT] → link

▼ Raw Data Files
  ▼ WGS                          ← GROUP node
      Tumor BAM      [BAM]       → link
      Normal BAM     [BAM]       → link

▼ Publications                   ← flat, no GROUP
    TCGA Paper 2021  [JOURNAL]   → link
```

---

## Implementation Steps

1. Write migration SQL (drop old tables → create `resource_node` → migrate existing rows)
2. Bump DB schema version in `pom.xml` and `cgds.sql`
3. Update data importer to handle `GROUP_PATH` resolution
4. Update persistence layer (JPA/MyBatis) for `resource_node`
5. Update REST API to return tree-structured resource responses
6. Update frontend to render the accordion/tree UI
