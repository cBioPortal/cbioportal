# cBioPortal Study View Data Binning Algorithm Documentation

## 1. Overview

Data binning groups continuous data into discrete intervals ("bins") for visualization in histograms. In cBioPortal's Study View:

- **Purpose**: Facilitates cohort analysis of clinical and genomic attributes, such as age or mutation frequency.
- **Functionality**: Supports interactive patient subset selection via histogram interactions.
- **Binning Methods**:
  - **Quartile-Based**: Divides data into four equal parts.
  - **Median-Split**: Splits data at the median value.
  - **Custom Bin Size**: Allows user-defined bin sizes.

## 2. Implementation Details

**Key Components**:

- **Java Classes**:
  - `StudyViewController.java`: Handles REST API endpoints.
  - `StudyViewServiceImpl.java`: Implements service logic.
  - `BinningUtils.java`: Contains utility methods for binning operations.

**Logical Workflow**:

1. **Data Retrieval**:
   - Frontend requests binning via REST API.
   - Backend fetches raw data from the MySQL database.

2. **In-Memory Processing**:
   - Data loaded into Java collections for processing.

3. **Binning Calculation**:
   - Based on selected method (quartile, median, custom).

4. **Response Generation**:
   - Bin counts returned to frontend in JSON format.

## 3. Binning Algorithm

**Step-by-Step Process**:

1. **Data Retrieval**:
   - Query clinical data using JDBI mapper.
   - Store results in a `List` collection.

2. **Statistical Calculation**:
   - For quartile-based binning:
     ```java
     double[] quartiles = BinningUtils.calculateQuartiles(dataValues);
     ```

3. **Bin Generation**:
   - Depending on the binning method:
     ```java
     switch(binningMethod) {
         case QUARTILE:
             bins = Arrays.asList(min, quartiles[0], quartiles[1], quartiles[2], max);
             break;
         case MEDIAN:
             bins = Arrays.asList(min, median, max);
             break;
         case CUSTOM:
             bins = generateCustomBins(minValue, binSize);
     }
     ```

4. **Data Assignment**:
   - Iterate through data points.
   - Use binary search for bin placement.

5. **Count Aggregation**:
   - Create a frequency map: `Map<Bin, Integer>`.

## 4. Configuration & Parameters

**Tunable Parameters**:

- `binning.strategy`: Options are `QUARTILE`, `MEDIAN`, or `CUSTOM`.
- `custom.bin.size`: Numeric value defining bin size (e.g., 10-year age groups).
- `minimum.value`: Starting point for bin generation.
- `max.bins`: Safety limit for bin count (default: 50).

## 5. Code References

**Critical Methods**:

- **Entry Point**:
  ```java
  StudyViewServiceImpl.getBinnedClinicalData()
  ```

- **Core Logic**:
  ```java
  BinningUtils.calculateDistribution()
  BinningUtils.createBins()
  BinningUtils.calculateBinCounts()
  ```

- **Data Access**:
  ```java
  ClinicalDataMapper.getClinicalDataByStudy()
  ```

## 6. Potential Improvements

**Current Limitations**:

- **Memory Intensive**: Loads full dataset into JVM heap.
- **Scalability Issues**: O(n) complexity with dataset size.
- **Static Processing**: No incremental binning support.
- **Database Dependency**: Requires clinical data table joins.

**Optimization Opportunities**:

1. **Database-Level Binning**:
   - Utilize ClickHouse's `histogram` function to perform binning at the database level, reducing memory usage.

2. **Streaming Data Processing**:
   - Implement streaming to handle large datasets more efficiently.

3. **Caching Layer**:
   - Add caching for frequent queries to improve performance.

4. **Parallel Processing**:
   - Introduce parallel processing techniques for large datasets.

---

*This documentation aligns with cBioPortal's [architecture standards](https://docs.cbioportal.org/architecture-overview/) and prepares for the SQL migration outlined in [GSoC Issue #117](https://github.com/cBioPortal/gsoc/issues/117).*

---

**References**:

1. [cBioPortal for Cancer Genomics](https://www.cbioportal.org/)
2. [Improved Histogram Binning in the cBioPortal Study View](https://www.thehyve.nl/articles/improved-histogram-binning-cbioportal)
3. [Architecture Overview - cBioPortal Docs](https://docs.cbioportal.org/architecture-overview/)
4. [Integrative Analysis of Complex Cancer Genomics and Clinical Profiles Using the cBioPortal](https://pmc.ncbi.nlm.nih.gov/articles/PMC4160307/)
```
