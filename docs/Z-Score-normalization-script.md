## Introduction
For some data types, when uploading to cBioPortal, it is currently necessary to also provide a z-score transformed version of your input file. The z-score data is essential for the oncoprint functionality. The oncoprint shows over- and underexpression of the data, based on the threshold the user sets when selecting the genomic profile. 

:warning: 
Please keep in mind that the z-scores are calculated using only patient data. Hence, overexpressed in this case implies higher expression than the average patient. Also, the source data on which the z-score data is based does not necessarily follow the normal distribution. If your data does not follow the normal distribution, the z-score threshold is less reliable and will result in more false positives or false negatives. You can consider log transforming your value before calculating z-scores to improve this. However, even the logged values may not follow the normal distribution, especially if the data is bimodal.

Currently, the cBioPortal team is woking on a solution that will allow the user to decide whether they want to use zscores, or log values or zscore of log values in the frontend. Furthermore, the team is also working on functionality to be able to use healthy samples in the calculations. 
 
### How to proceed
cBioPortal expects z-score normalization to take place per gene. You can produce this extra z-score file yourself, or let cBioPortal do it for your input files by using `convertExpressionZscores.pl`. Most information that now follows was taken from the comments in `convertExpressionZscores.pl`. We've also added an example of the calculation and example of running the program below.

## The cBioPortal convertExpressionZscores method
Given expression and Copy Number Variation data for a set of samples (patients), generate normalized expression values.

Each **gene** is normalized separately. First, the expression distribution for unaltered copies of the gene is estimated by calculating the mean and variance of the expression values for samples in which the gene is diploid (i.e. value is "0" as reported by [discrete CNA data](File-Formats.md#discrete-copy-number-data)). We call this the unaltered distribution. If the gene has no diploid samples, then its normalized expression is reported as NA. Otherwise, for every sample, the gene's normalized expression is reported as

```
(r - mu)/sigma
```

where `r` is the raw expression value, and `mu` and `sigma` are the mean and standard deviation of the unaltered distribution, respectively.

## Parameters

`<copy_number_file> <expression_file> <output_file> <normal_sample_suffix> <[min_number_of_diploids]>`

- `<copy_number_file>` : the [discrete copy number (CNA) file](File-Formats.md#discrete-copy-number-data) 
- `<expression_file>` : the [expression (exp) data file](File-Formats.md#expression-data). 
- `<output_file>` : the output file to be generated
- `<normal_sample_suffix>` : use this to identify which of your samples are "normal" samples (if any). E.g. normal TCGA samples have a suffix "-11". Set it to some dummy value, e.g. "NONE", if you have no normal samples in your data.

## convertExpressionZscores Transformation Algorithm 
Input: [discrete copy number (CNA)](File-Formats.md#discrete-copy-number-data) and [expression (exp)](File-Formats.md#expression-data) files

```
    for each gene{
       identify diploid cases in CNA
       obtain mean and sd of exp of diploid cases
       for each case{
          zScore <- (value - mean)/sd
       }
    }
```

## Implementation
```
    read CNA: build hash geneCopyNumberStatus: gene -> Array of (caseID, value ) pairs
    read exppression: skip normal cases;
    for each gene{
      get mean and s.d. of elements of diploids
      get zScore for each case
    }
```
## Example Calculation
Hugo_Symbol | Entrez_Gene_Id | A1-A0SD-01 | A1-A0SE-01 | A1-A0SH-01 | A1-A0SJ-01 | A1-A0SK-01 | A1-A0SM-01 | A1-A0SO-01 | A1-A0SP-01 | A2-A04N-01 | A2-A04P-01 | A2-A04Q-01 | A2-A04R-01 | A2-A04T-01 | A2-A04U-01 | A2-A04V-01 | A2-A04W-01 | A2-A04X-01 | A2-A04Y-01
--- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |--- 
RPS11 Expr | 6205 | 0.765 | 0.716 | 0.417125 | 0.115 | 0.492875 | -0.525 | -0.169 | 0.396 | 0.50475 | 0.400875 | 0.393125 | 0.9165 | 0.627125 | 0.337125 | 0.705 | 0.16425 | 0.325 | 0.11175
RPS11 CNA | 6205 | 0 | 0 | 0 | 1 | 1 | 0 | -1 | 0 | 0 | 2 | 0 | 0 | 1 | -1 | 0 | 0 | -1 | 0

Calculate mean and stdev where CNA is 0 (=diploid):

Diploid avg | Diploid std
--- | ---
0.414954545454545 | 0.399504498851105

Calculate the z-scores:

Hugo_Symbol | Entrez_Gene_Id | A1-A0SD-01 | A1-A0SE-01 | A1-A0SH-01 | A1-A0SJ-01 | A1-A0SK-01 | A1-A0SM-01 | A1-A0SO-01 | A1-A0SP-01 | A2-A04N-01 | A2-A04P-01 | A2-A04Q-01 | A2-A04R-01 | A2-A04T-01 | A2-A04U-01 | A2-A04V-01 | A2-A04W-01 | A2-A04X-01 | A2-A04Y-01
--- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |--- 
RPS11 Expr Output | 6205 | 0.8762 | 0.7535 | 0.0054 | -0.7508 | 0.1950 | -2.3528 | -1.4617 | -0.0474 | 0.2248 | -0.0352 | -0.0546 | 1.2554 | 0.5311 | -0.1948 | 0.7260 | -0.6275 | -0.2252 | -0.7590

Note: this implies that your full dataset does not have average=0, std=1

## Running the script


To run the script type the following commands when in the folder `<your_cbioportal_dir>/core/src/main/scripts`: 

```
 export PORTAL_HOME=<your_cbioportal_dir>
```

and then 

```
./convertExpressionZscores.pl <copy_number_file> <expression_file> <output_file> <normal_sample_suffix> <[min_number_of_diploids]>
```

#### Example:

```
./convertExpressionZscores.pl  ../../test/scripts/test_data/study_es_0/data_CNA.txt ../../test/scripts/test_data/study_es_0/data_expression_median.txt data_expression_ZSCORES.txt NONE
```

