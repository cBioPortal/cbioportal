# Issue #11761: Molecular Data Meta Endpoint for Multiple Profiles

## Problem Analysis
Currently, when the frontend needs to get counts for multiple molecular profiles, it must:
1. Call `/api/molecular-profiles/{profileId}/molecular-data/fetch?projection=META` for EACH profile
2. This causes N database hits with joins, which is slow on ClickHouse

The existing `/api/molecular-data/fetch?projection=META` endpoint returns ONE total count across all profiles, but doesn't provide per-profile breakdown.

## Proposed Solution
Create a new endpoint that:
- Accepts multiple molecular profile IDs
- Returns per-profile counts in a single database query
- Returns JSON response (not just HTTP headers) with profile-specific counts

## Implementation Plan

### 1. Backend Changes

#### New Model Class: `MolecularDataCountItem.java`
```java
package org.cbioportal.legacy.model;

public class MolecularDataCountItem {
    private String molecularProfileId;
    private Integer count;
    
    // getters/setters
}
```

#### Update Service Interface: `MolecularDataService.java`
Add method:
```java
List<MolecularDataCountItem> fetchMolecularDataCountsInMultipleMolecularProfiles(
    List<String> molecularProfileIds,
    List<String> sampleIds,
    List<Integer> entrezGeneIds);
```

#### Update Service Implementation: `MolecularDataServiceImpl.java`
Implement the new method by:
1. Querying all profiles at once
2. Grouping results by molecularProfileId
3. Counting entries per profile

#### Update Controller: `MolecularDataController.java`
Add new endpoint:
```java
@PostMapping("/molecular-data/counts")
public ResponseEntity<List<MolecularDataCountItem>> fetchMolecularDataCountsInMultipleMolecularProfiles(
    @RequestBody MolecularDataMultipleStudyFilter filter);
```

### 2. Frontend Changes (if needed)
Update `ResultsViewPageStore.ts` to use the new endpoint instead of making N calls.

## Database Query Optimization
The service will use existing `getMolecularDataInMultipleMolecularProfiles` but with projection="ID" to minimize data transfer, then group and count in Java.

For ClickHouse optimization, we can leverage the already-optimized query from PR #11840.

## Testing Plan
1. Unit tests for service method
2. Integration test for controller endpoint
3. Verify single database query is made (not N queries)
4. Compare performance with old approach

## Files to Modify
- `src/main/java/org/cbioportal/legacy/model/MolecularDataCountItem.java` (NEW)
- `src/main/java/org/cbioportal/legacy/service/MolecularDataService.java`
- `src/main/java/org/cbioportal/legacy/service/impl/MolecularDataServiceImpl.java`
- `src/main/java/org/cbioportal/legacy/web/MolecularDataController.java`
- `src/test/java/org/cbioportal/legacy/service/impl/MolecularDataServiceImplTest.java`
- `src/test/java/org/cbioportal/legacy/web/MolecularDataControllerTest.java`

## Questions/Decisions
1. Should we return counts per profile, or counts per profile+gene combination?
   - **Answer**: Per profile only (simpler, matches the issue description)

2. Should this endpoint support both sampleIds and sampleListId like other endpoints?
   - **Answer**: Yes, for consistency

3. Should we add this to the existing `/molecular-data/fetch` endpoint or create new endpoint?
   - **Answer**: New endpoint `/molecular-data/counts` for clarity

---

**Please review this plan and confirm before I proceed with implementation!**
