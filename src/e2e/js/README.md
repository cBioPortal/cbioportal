# cBioPortal E2E Tests (Mocha/TypeScript)

This directory contains E2E tests for the cBioPortal API using Mocha and TypeScript, converted from the original Java tests.

## Setup

Install dependencies:

```bash
npm install
```

## Running Tests

### Using npm scripts

```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch
```

### Using the test runner script

```bash
# Run tests against default server (http://localhost:8080)
./run-tests.sh

# Run tests against a specific server
CBIOPORTAL_URL=http://your-server:8080 ./run-tests.sh
```

## Configuration

The server URL can be configured via environment variable:

```bash
export CBIOPORTAL_URL=http://localhost:8080
```

Default: `http://localhost:8080`

## Project Structure

```
src/e2e/js/
├── data/                           # Test data JSON files
│   ├── all_alterations.json
│   ├── multi_panel.json
│   ├── patient.json
│   └── sample.json
├── src/                            # Source files
│   ├── config.ts                   # Configuration management
│   ├── types.ts                    # TypeScript type definitions
│   └── utils.ts                    # Test utility functions
├── test/                           # Test specifications
│   └── AlterationEnrichmentController.spec.ts
├── package.json                    # NPM dependencies
├── tsconfig.json                   # TypeScript configuration
├── .mocharc.json                   # Mocha test runner configuration
└── run-tests.sh                    # Test runner script
```

## Test Coverage

The test suite includes the following test cases:

1. **testFetchAlterationEnrichmentsWithDataJson** - Tests combination comparison session with two studies (WES and IMPACT)
2. **testFetchAlterationEnrichmentsWithDataJsonCNAOnly** - Tests filtering of mutation and structural variant profiles
3. **testFetchAlterationEnrichmentsWithDataJsonNoMissense** - Tests exclusion of missense mutations
4. **testFetchAlterationEnrichmentsWithMultiPanel** - Tests samples covered by multiple panels
5. **testFetchAlterationFilteringByAlterationType** - Tests alteration type filtering
6. **testFetchAlterationEnrichmentsExcludingMissenseMutations** - Tests comprehensive missense mutation exclusion
7. **testFetchAlterationEnrichmentsPatientVSample** - Tests PATIENT vs SAMPLE enrichment types

## Dependencies

- **mocha** - Test framework
- **chai** - Assertion library
- **typescript** - TypeScript compiler
- **ts-node** - TypeScript execution for Node.js
- **axios** - HTTP client for API requests

## Notes

- Tests require a running cBioPortal instance
- Default timeout is set to 30 seconds per test
- Tests use the `/api/column-store/alteration-enrichments/fetch` endpoint