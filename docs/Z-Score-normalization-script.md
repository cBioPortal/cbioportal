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
<table>
<tr><td>Hugo_Symbol</td><td>Entrez_Gene_Id</td><td>A1-A0SD-01</td><td>A1-A0SE-01</td><td>A1-A0SH-01</td><td>A1-A0SJ-01</td><td>A1-A0SK-01</td><td>A1-A0SM-01</td><td>A1-A0SO-01</td><td>A1-A0SP-01</td><td>A2-A04N-01</td><td>A2-A04P-01</td><td>A2-A04Q-01</td><td>A2-A04R-01</td><td>A2-A04T-01</td><td>A2-A04U-01</td><td>A2-A04V-01</td><td>A2-A04W-01</td><td>A2-A04X-01</td><td>A2-A04Y-01</td></tr>
<tr><td>RPS11 Expr</td><td>6205</td><td>0.765</td><td>0.716</td><td>0.417125</td><td>0.115</td><td>0.492875</td><td>-0.525</td><td>-0.169</td><td>0.396</td><td>0.50475</td><td>0.400875</td><td>0.393125</td><td>0.9165</td><td>0.627125</td><td>0.337125</td><td>0.705</td><td>0.16425</td><td>0.325</td><td>0.11175</td></tr>
<tr><td>RPS11 CNA</td><td>6205</td><td>0</td><td>0</td><td>0</td><td>1</td><td>1</td><td>0</td><td>-1</td><td>0</td><td>0</td><td>2</td><td>0</td><td>0</td><td>1</td><td>-1</td><td>0</td><td>0</td><td>-1</td><td>0</td></tr>
</table>

Calculate mean and stdev where CNA is 0 (=diploid):

<table>
<tr><td>Diploid avg</td><td>Diploid std</td></tr>
<tr><td>---</td><td>---</td></tr>
<tr><td>0.414954545454545</td><td>0.399504498851105</td></tr>
</table>

Calculate the z-scores:
<table>
<tr><td>Hugo_Symbol</td><td>Entrez_Gene_Id</td><td>A1-A0SD-01</td><td>A1-A0SE-01</td><td>A1-A0SH-01</td><td>A1-A0SJ-01</td><td>A1-A0SK-01</td><td>A1-A0SM-01</td><td>A1-A0SO-01</td><td>A1-A0SP-01</td><td>A2-A04N-01</td><td>A2-A04P-01</td><td>A2-A04Q-01</td><td>A2-A04R-01</td><td>A2-A04T-01</td><td>A2-A04U-01</td><td>A2-A04V-01</td><td>A2-A04W-01</td><td>A2-A04X-01</td><td>A2-A04Y-01</td></tr>
<tr><td>RPS11 Expr Output</td><td>6205</td><td>0.8762</td><td>0.7535</td><td>0.0054</td><td>-0.7508</td><td>0.1950</td><td>-2.3528</td><td>-1.4617</td><td>-0.0474</td><td>0.2248</td><td>-0.0352</td><td>-0.0546</td><td>1.2554</td><td>0.5311</td><td>-0.1948</td><td>0.7260</td><td>-0.6275</td><td>-0.2252</td><td>-0.7590</td></tr>
</table>

Note: this implies that your full dataset does not have average=0, std=1

## Running the script


To run the script type the following commands when in the folder `<cbioportal_source_folder>/core/src/main/scripts`:

```
export PORTAL_HOME=<cbioportal_configuration_folder>
```

and then 

```
./convertExpressionZscores.pl <copy_number_file> <expression_file> <output_file> <normal_sample_suffix> <[min_number_of_diploids]>
```

#### Example:

```
./convertExpressionZscores.pl  ../../test/scripts/test_data/study_es_0/data_CNA.txt ../../test/scripts/test_data/study_es_0/data_expression_median.txt data_expression_ZSCORES.txt NONE
```

