# Claude Instructions for E2E Test Development

You are helping write end-to-end tests for the cBioPortal API using Mocha, TypeScript, and Lodash.

## Project Structure

```
src/e2e/js/
├── src/
│   ├── config.ts       # Server configuration
│   ├── types.ts        # TypeScript type definitions
│   └── utils.ts        # Test utilities (loadTestData, callEnrichmentEndpoint)
├── test/
│   └── [TestName]/
│       ├── [TestName].spec.ts    # Test spec file
│       ├── data1.json             # Test data
│       └── data2.json             # Test data
└── package.json
```

Each test suite lives in its own directory alongside its test data files.

## Core Principles

### 1. Transparency Over Abstraction
**Never hide logic in helper methods.** All data processing must be visible inline in the test with clear comments.

```typescript
// ❌ WRONG - logic hidden in helper
const total = TestUtils.getTotalProfiledSamples(enrichment);

// ✅ CORRECT - logic visible and explained
// Calculate total profiled samples across all groups by summing profiledCount from counts array
const totalProfiled = _.sumBy(enrichment.counts, 'profiledCount');
```

### 2. Use Lodash for All Array/Object Operations
Replace **all** native JavaScript array methods with Lodash equivalents for consistency.

```typescript
// ❌ WRONG - native JS methods
const gene = enrichments.find(e => e.hugoGeneSymbol === 'TP53');
const total = enrichment.counts.reduce((sum, c) => sum + c.profiledCount, 0);
const clone = JSON.parse(JSON.stringify(data));

// ✅ CORRECT - lodash methods
const gene = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
const total = _.sumBy(enrichment.counts, 'profiledCount');
const clone = _.cloneDeep(data);
```

### 3. Comment Every Non-Trivial Step
Every operation must have a comment explaining **what** it does and **why**.

```typescript
// Load test data containing 104 samples across two studies
const testData = await TestUtils.loadTestData('all_alterations.json');

// Call the enrichment endpoint to get alteration statistics
const enrichments = await TestUtils.callEnrichmentEndpoint(testData);

// Find the TP53 gene enrichment from the results
const tp53Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
```

## Required Lodash Methods

Use these Lodash methods instead of native JavaScript:

- **`_.find(array, matcher)`** - Find object by properties (not `.find()`)
- **`_.sumBy(array, property)`** - Sum property values (not `.reduce()`)
- **`_.filter(array, predicate)`** - Filter array (not `.filter()`)
- **`_.forEach(array, fn)`** - Iterate (not `.forEach()` or `for`)
- **`_.every(array, predicate)`** - Check all (not `.every()`)
- **`_.some(array, predicate)`** - Check any (not `.some()`)
- **`_.cloneDeep(obj)`** - Deep clone (not `JSON.parse(JSON.stringify())`)
- **`_.endsWith(string, target)`** - String check (not `.endsWith()`)

## Test Structure

```typescript
import { expect } from 'chai';
import _ from 'lodash';
import { TestUtils } from '../../src/utils';
import { EnrichmentType } from '../../src/types';

describe('ControllerName E2E Tests', () => {
  describe('testSpecificFeature', () => {
    it('should describe the expected behavior', async () => {
      // Step 1: Load test data with comment
      const testData = await TestUtils.loadTestData('filename.json');

      // Step 2: Call API with comment
      const enrichments = await TestUtils.callEnrichmentEndpoint(testData);

      // Step 3: Extract data with comment
      const geneEnrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
      expect(geneEnrichment, 'TP53 should be present').to.not.be.undefined;

      // Step 4: Calculate metrics with comment
      const totalProfiled = _.sumBy(geneEnrichment!.counts, 'profiledCount');

      // Step 5: Assert with descriptive message
      expect(totalProfiled).to.equal(
        100,
        'TP53 should have 100 total profiled samples because it is in IMPACT'
      );
    });
  });
});
```

## Common Patterns

### Pattern 1: Testing with Modified Data
```typescript
// Load base test data
const testData = await TestUtils.loadTestData('data.json');

// Deep clone to avoid mutating original
const modifiedData = _.cloneDeep(testData);

// Modify the clone
modifiedData.alterationEventTypes.mutationEventTypes.missense_mutation = false;

// Make API call with modified data
const enrichments = await TestUtils.callEnrichmentEndpoint(modifiedData);
```

### Pattern 2: Comparing Baseline vs Filtered Results
```typescript
// Establish baseline
const baselineEnrichments = await TestUtils.callEnrichmentEndpoint(testData);
const baselineGene = _.find(baselineEnrichments, { hugoGeneSymbol: 'TP53' });
const baselineTotal = _.sumBy(baselineGene!.counts, 'alteredCount');

// Apply filter
const modifiedData = _.cloneDeep(testData);
modifiedData.someFilter = false;

// Get filtered results
const filteredEnrichments = await TestUtils.callEnrichmentEndpoint(modifiedData);
const filteredGene = _.find(filteredEnrichments, { hugoGeneSymbol: 'TP53' });
const filteredTotal = _.sumBy(filteredGene!.counts, 'alteredCount');

// Compare
expect(filteredTotal).to.be.lessThan(baselineTotal, 'Filtered should have fewer results');
```

### Pattern 3: Verifying All Elements Meet Criteria
```typescript
// Verify all genes have exactly 4 groups using lodash every
const allHaveFourGroups = _.every(enrichments, (enrichment) => enrichment.counts.length === 4);
expect(allHaveFourGroups, 'All genes should have exactly 4 groups').to.be.true;

// Verify each gene has at least one group with an alteration
const allHaveAlterations = _.every(enrichments, (enrichment) =>
  // Use lodash some to check if any count has alteredCount > 0
  _.some(enrichment.counts, (count) => count.alteredCount > 0)
);
expect(allHaveAlterations, 'Each gene should have at least one alteration').to.be.true;
```

### Pattern 4: Filtering Data Structures
```typescript
// Filter each group's molecular profile identifiers
_.forEach(groupsArray, (group) => {
  // Keep only profiles that meet certain criteria
  group.molecularProfileCaseIdentifiers = _.filter(
    group.molecularProfileCaseIdentifiers,
    (identifier) => {
      const profileId = identifier.molecularProfileId;
      return !_.endsWith(profileId, '_mutations');
    }
  );
});
```

## Best Practices

### 1. Comment Context, Not Just Actions
```typescript
// ❌ WRONG - just describing the code
// Call endpoint
const enrichments = await TestUtils.callEnrichmentEndpoint(testData);

// ✅ CORRECT - explaining the context
// This test verifies that genes not in the IMPACT panel only count WES samples
// Of 33 samples, only 26 are covered by the WES panel for mutation profiling
const enrichments = await TestUtils.callEnrichmentEndpoint(testData);
```

### 2. Use Descriptive Variable Names
```typescript
// ❌ WRONG - unclear names
const e1 = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
const t = _.sumBy(e1!.counts, 'profiledCount');

// ✅ CORRECT - self-documenting
const tp53Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
const tp53TotalProfiled = _.sumBy(tp53Enrichment!.counts, 'profiledCount');
```

### 3. Include Expected Values in Assertion Messages
```typescript
// ❌ WRONG - no context in message
expect(totalProfiled).to.equal(103);

// ✅ CORRECT - explains why this value is expected
expect(totalProfiled).to.equal(
  103,
  'TP53 should have 103 total profiled samples across all groups because it is in IMPACT'
);
```

### 4. Add Comments for Business Logic
```typescript
// This combination comparison session has two studies, one WES and the other IMPACT
// 104 samples total, 92 of which belong to WES study
// 14 samples should be profiled for only IMPACT genes
// NOTE: of the 92 WES samples, only 91 are actually profiled
const testData = await TestUtils.loadTestData('all_alterations.json');
```

### 5. Group Related Operations
Separate logical sections with blank lines:

```typescript
// Load test data and make API call
const testData = await TestUtils.loadTestData('data.json');
const enrichments = await TestUtils.callEnrichmentEndpoint(testData);

// Find the gene of interest
const geneEnrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
expect(geneEnrichment, 'Gene should be present').to.not.be.undefined;

// Calculate metrics
const totalProfiled = _.sumBy(geneEnrichment!.counts, 'profiledCount');
const totalAltered = _.sumBy(geneEnrichment!.counts, 'alteredCount');

// Verify expected values
expect(totalProfiled).to.equal(100, 'Should have 100 profiled samples');
expect(totalAltered).to.equal(25, 'Should have 25 altered samples');
```

## Available Utilities

### TestUtils.loadTestData(filename)
Loads a JSON file from the same directory as the calling test.

```typescript
// Automatically loads from test/AlterationEnrichmentController/all_alterations.json
const testData = await TestUtils.loadTestData('all_alterations.json');
```

### TestUtils.callEnrichmentEndpoint(testData, enrichmentType?)
Calls the alteration enrichments API endpoint.

```typescript
// Default: SAMPLE enrichment type
const enrichments = await TestUtils.callEnrichmentEndpoint(testData);

// Patient-level enrichment
const enrichments = await TestUtils.callEnrichmentEndpoint(
  testData,
  EnrichmentType.PATIENT
);
```

## Type Definitions

**IMPORTANT: All TypeScript types MUST be derived from the official cBioPortal Swagger API documentation.**

### Deriving Types from Swagger

When creating or updating TypeScript types:

1. **Check the Swagger documentation first**:
   - Public API: https://www.cbioportal.org/api/v3/api-docs/public
   - Internal API: https://www.cbioportal.org/api/v3/api-docs/internal

2. **Extract the exact schema** from the `components/schemas` section

3. **Match field names, types, and required status exactly** as documented

4. **Add JSDoc comments** documenting:
   - The source URL
   - Which fields are required
   - Brief description of each field

### Current Type Definitions

```typescript
/**
 * Types derived from cBioPortal Swagger API documentation
 * Source: https://www.cbioportal.org/api/v3/api-docs/internal
 */

/**
 * CountSummary - Statistics for a group in enrichment analysis
 * Required fields: name, alteredCount, profiledCount
 */
interface CountSummary {
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
interface AlterationEnrichment {
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

enum EnrichmentType {
  SAMPLE = 'SAMPLE',
  PATIENT = 'PATIENT'
}
```

## Complete Example

```typescript
describe('testFetchAlterationEnrichments', () => {
  it('should return correct profiled sample counts', async () => {
    // This test verifies sample counting across two studies (WES and IMPACT)
    // 104 samples total, 92 from WES (only 91 profiled), 14 from IMPACT
    const testData = await TestUtils.loadTestData('all_alterations.json');

    // Call the alteration enrichments endpoint to get gene statistics
    const enrichments = await TestUtils.callEnrichmentEndpoint(testData);

    // Find the SPSB1 gene enrichment from the results
    const spsb1Enrichment = _.find(enrichments, { hugoGeneSymbol: 'SPSB1' });
    expect(spsb1Enrichment, 'SPSB1 enrichment should be present').to.not.be.undefined;

    // Calculate total profiled samples across all groups by summing profiledCount
    const spsb1TotalProfiled = _.sumBy(spsb1Enrichment!.counts, 'profiledCount');
    expect(spsb1TotalProfiled).to.equal(
      91,
      'SPSB1 should have 91 total profiled samples (only in WES, not in IMPACT)'
    );

    // Find the TP53 gene enrichment from the results
    const tp53Enrichment = _.find(enrichments, { hugoGeneSymbol: 'TP53' });
    expect(tp53Enrichment, 'TP53 enrichment should be present').to.not.be.undefined;

    // Calculate total profiled samples for TP53
    const tp53TotalProfiled = _.sumBy(tp53Enrichment!.counts, 'profiledCount');
    expect(tp53TotalProfiled).to.equal(
      103,
      'TP53 should have 103 total profiled samples because it is in IMPACT panel'
    );
  });
});
```

## Critical Rules

1. **NEVER** create helper methods for data processing
2. **ALWAYS** use Lodash instead of native JavaScript methods
3. **ALWAYS** comment every non-trivial operation
4. **ALWAYS** use descriptive variable names
5. **ALWAYS** include explanatory messages in assertions
6. **ALWAYS** explain business logic and domain knowledge in comments
7. **ALWAYS** group related operations with blank lines between sections
8. Test files must be in `test/[TestName]/[TestName].spec.ts`
9. Test data JSON files must be in the same directory as the spec

Following these rules ensures tests are transparent, maintainable, and easy to debug.