# ClickHouse SQL Data Binning with roundDown() Function

## 1. Introduction to Data Binning

Data binning is a preprocessing technique used to group continuous data values into discrete intervals or "bins." This transformation is essential for:

- Simplifying complex distributions for visualization in histograms and charts
- Reducing the effects of minor observation errors
- Creating categorical variables from continuous data
- Improving performance of analytical queries by reducing cardinality

In the context of platforms like cBioPortal, efficient binning is critical for analyzing large-scale genomic and clinical datasets. Traditional approaches often implement binning logic in application code (e.g., Java), but this requires loading large datasets into memory. Moving this computation to the database layer using SQL functions like ClickHouse's `roundDown()` can significantly improve performance for large datasets.

## 2. Understanding the roundDown() Function

The `roundDown()` function in ClickHouse accepts a number and rounds it down to the nearest element in a specified array. If the value is less than the lowest bound in the array, the lowest bound is returned.

### Syntax

```sql
roundDown(num, arr)
```

**Parameters:**
- `num`: A number to round down (numeric type)
- `arr`: Array of elements to round down to (array of UInt/Float type)

**Return value:**
- Number rounded down to an element in `arr`
- If value is less than the lowest bound, the lowest bound is returned

## 3. Basic Numerical Binning Examples

### 3.1 Creating a Sample Dataset

Let's create a sample table with numerical values to demonstrate binning:

```sql
CREATE TABLE patient_data (
    patient_id UInt32,
    age Float64,
    tumor_size_mm Float64,
    gene_expression_level Float64
) ENGINE = MergeTree()
ORDER BY patient_id;

-- Insert sample data
INSERT INTO patient_data VALUES
(1, 25.5, 8.2, 0.35),
(2, 42.1, 12.5, 1.2),
(3, 35.7, 5.1, 0.45),
(4, 67.3, 22.8, 2.1),
(5, 18.2, 3.2, 0.12),
(6, 51.9, 15.6, 1.5),
(7, 29.4, 7.3, 0.65),
(8, 73.8, 28.4, 2.8),
(9, 45.2, 14.1, 1.1),
(10, 61.5, 19.8, 1.9);
```

### 3.2 Age Binning with Equal Intervals

Let's create 10-year age groups:

```sql
SELECT 
    patient_id,
    age,
    roundDown(age, [0, 20, 30, 40, 50, 60, 70, 80]) AS age_bin
FROM patient_data
ORDER BY patient_id;
```

**Expected output:**
```
┌─patient_id─┬─────age─┬─age_bin─┐
│          1 │    25.5 │       20 │
│          2 │    42.1 │       40 │
│          3 │    35.7 │       30 │
│          4 │    67.3 │       60 │
│          5 │    18.2 │        0 │
│          6 │    51.9 │       50 │
│          7 │    29.4 │       20 │
│          8 │    73.8 │       70 │
│          9 │    45.2 │       40 │
│         10 │    61.5 │       60 │
└────────────┴─────────┴─────────┘
```

### 3.3 Tumor Size Binning for Clinical Categorization

Bin tumor size into clinical categories:

```sql
SELECT 
    patient_id,
    tumor_size_mm,
    roundDown(tumor_size_mm, [0, 5, 10, 15, 20, 25, 30]) AS tumor_size_category,
    CASE
        WHEN roundDown(tumor_size_mm, [0, 5, 10, 15, 20, 25, 30])  (SELECT min_age FROM min_age) + x * bin_width, 
            range((SELECT num_bins FROM num_bins))
        )
    ) AS age_bin
FROM patient_data
ORDER BY age;
```

**Expected output:**
```
┌─patient_id─┬─────age─┬─age_bin─┐
│          5 │    18.2 │     18.2 │
│          1 │    25.5 │     18.2 │
│          7 │    29.4 │     28.2 │
│          3 │    35.7 │     28.2 │
│          2 │    42.1 │     38.2 │
│          9 │    45.2 │     38.2 │
│          6 │    51.9 │     48.2 │
│         10 │    61.5 │     58.2 │
│          4 │    67.3 │     58.2 │
│          8 │    73.8 │     68.2 │
└────────────┴─────────┴─────────┘
```

### 5.2 Two-Dimensional Binning for Scatter Plots

Create 2D bins for visualization:

```sql
SELECT 
    roundDown(age, [0, 20, 40, 60, 80]) AS age_bin,
    roundDown(tumor_size_mm, [0, 5, 10, 15, 20, 25, 30]) AS tumor_size_bin,
    count() AS patient_count,
    avg(gene_expression_level) AS avg_expression
FROM patient_data
GROUP BY age_bin, tumor_size_bin
ORDER BY age_bin, tumor_size_bin;
```

**Expected output:**
```
┌─age_bin─┬─tumor_size_bin─┬─patient_count─┬─avg_expression─┐
│       0 │              0 │             1 │           0.12 │
│      20 │              5 │             2 │            0.5 │
│      40 │             10 │             2 │           1.15 │
│      40 │             15 │             1 │            1.5 │
│      60 │             15 │             1 │            1.9 │
│      60 │             20 │             1 │            2.1 │
│      60 │             25 │             1 │            2.8 │
└─────────┴────────────────┴───────────────┴────────────────┘
```

### 5.3 Population-Based Quartile Binning

Implement quartile-based binning (similar to cBioPortal's current approach):

```sql
WITH 
    quantiles AS (
        SELECT quantile(0.25)(age) AS q1,
               quantile(0.5)(age) AS q2,
               quantile(0.75)(age) AS q3
        FROM patient_data
    )
    
SELECT 
    patient_id,
    age,
    roundDown(
        age, 
        [0, (SELECT q1 FROM quantiles), (SELECT q2 FROM quantiles), (SELECT q3 FROM quantiles), 100]
    ) AS age_quartile_bin,
    CASE 
        WHEN age < (SELECT q1 FROM quantiles) THEN '1st Quartile'
        WHEN age < (SELECT q2 FROM quantiles) THEN '2nd Quartile'
        WHEN age < (SELECT q3 FROM quantiles) THEN '3rd Quartile'
        ELSE '4th Quartile'
    END AS quartile_group
FROM patient_data
ORDER BY age;
```

**Expected output:**
```
┌─patient_id─┬─────age─┬─age_quartile_bin─┬─quartile_group─┐
│          5 │    18.2 │                0 │ 1st Quartile   │
│          1 │    25.5 │                0 │ 1st Quartile   │
│          7 │    29.4 │               29 │ 2nd Quartile   │
│          3 │    35.7 │               29 │ 2nd Quartile   │
│          2 │    42.1 │               42 │ 3rd Quartile   │
│          9 │    45.2 │               42 │ 3rd Quartile   │
│          6 │    51.9 │               48 │ 4th Quartile   │
│         10 │    61.5 │               48 │ 4th Quartile   │
│          4 │    67.3 │               48 │ 4th Quartile   │
│          8 │    73.8 │               48 │ 4th Quartile   │
└────────────┴─────────┴──────────────────┴────────────────┘
```

## 6. Performance Considerations

When implementing binning with `roundDown()` in ClickHouse, consider:

1. **Pre-computing bins**: For frequently used bins, consider materializing them in a view
2. **Array size**: Large arrays in `roundDown()` may impact performance
3. **Column compression**: Bin values are often repetitive and benefit from compression
4. **Index usage**: Ensure your primary key includes columns used in filtering after binning

Example of a materialized view with pre-computed bins:

```sql
CREATE MATERIALIZED VIEW patient_data_binned
ENGINE = MergeTree()
ORDER BY (age_bin, tumor_size_bin)
AS SELECT
    patient_id,
    age,
    tumor_size_mm,
    gene_expression_level,
    roundDown(age, [0, 20, 40, 60, 80]) AS age_bin,
    roundDown(tumor_size_mm, [0, 5, 10, 15, 20, 25, 30]) AS tumor_size_bin
FROM patient_data;
```

## Conclusion

The `roundDown()` function in ClickHouse provides a powerful and flexible way to implement data binning directly in SQL. By moving binning logic from application code (like Java) to the database, you can significantly improve query performance, reduce memory usage, and simplify code maintenance.

This approach is particularly valuable for applications like cBioPortal that handle large genomic and clinical datasets, where efficient binning is essential for visualization and analysis. The examples in this document demonstrate how `roundDown()` can be used for various binning scenarios, from simple numerical binning to more complex time-series and multi-dimensional binning.



