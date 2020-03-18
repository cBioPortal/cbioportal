## Introduction
For some data types, when uploading to cBioPortal, it is currently necessary to also provide a z-score transformed version of your input file. The z-score data is essential for the oncoprint functionality. The oncoprint shows high or low mRNA expression of the genes, based on the threshold the user sets when selecting the genomic profile. 

:warning: 
Please keep in mind that the z-scores are calculated using only patient data. Hence, 'mRNA High' in this case implies higher expression than the average patient. Also, the source data on which the z-score data is based does not necessarily follow the normal distribution. If your data does not follow the normal distribution, the z-score threshold is less reliable and will result in more false positives or false negatives. You can consider log transforming your value before calculating z-scores to improve this. However, even the logged values may not follow the normal distribution, especially if the data is bimodal.

## The cBioPortal Z-Score calculation method

cBioPortal currently generates two z-score profiles using two different base populations:
- Distribution based on **diploid** samples only: The expression distribution for unaltered copies of the gene is estimated by calculating the mean and variance of the expression values for samples in which the gene is diploid (i.e. value is "0" as reported by [discrete CNA data](File-Formats.md#discrete-copy-number-data)). We call this the unaltered distribution. If the gene has no diploid samples, then its normalized expression is reported as NA.

- Distribution based on **all** samples: The expression distribution of the gene is estimated by calculating the mean and variance of all samples with expression values (excludes zero's and non-numeric values like NA, Null or NaN). If the gene has samples whose expression values are all zeros or non-numeric, then its normalized expression is reported as NA.

Otherwise for every sample, the gene's normalized expression for both the profiles is reported as

```
(r - mu)/sigma
```
where `r` is the raw expression value, and `mu` and `sigma` are the mean and standard deviation of the base population, respectively.

### How to proceed
cBioPortal expects z-score normalization to take place per gene. You can calculate z-scores with your own preferred method, or use one of the cBioPortal provided approaches:
- [convertExpressionZscores.pl](https://github.com/cBioPortal/cbioportal/blob/master/core/src/main/scripts/convertExpressionZscores.pl) applies Method 1 (diploid samples as base population)
- [NormalizeExpressionLevels_allsampleref.py](https://github.com/cBioPortal/datahub-study-curation-tools/tree/master/generate_Zscores) applies Method 2 (all samples as base population)

Examples of the calculation and running the programs are below.

## convertExpressionZscores method
Given expression and Copy Number Variation data for a set of samples (patients), generate normalized expression values.

### Parameters

`<copy_number_file> <expression_file> <output_file> <normal_sample_suffix> <[min_number_of_diploids]>`

- `<copy_number_file>` : the [discrete copy number (CNA) file](File-Formats.md#discrete-copy-number-data) 
- `<expression_file>` : the [expression (exp) data file](File-Formats.md#expression-data). 
- `<output_file>` : the output file to be generated
- `<normal_sample_suffix>` : use this to identify which of your samples are "normal" samples (if any). E.g. normal TCGA samples have a suffix "-11". Set it to some dummy value, e.g. "NONE", if you have no normal samples in your data.

### Algorithm
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
### Example Calculation
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

### Running the script


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
## NormalizeExpressionLevels_allsampleref method
Given the expression data for a set of samples, generate normalized expression values with the reference population of all samples independent of sample diploid status.

### Parameters
`-i <expression_file> -o <output_file> [-l]`

- `<expression_file>` : the [expression (exp) data file](File-Formats.md#expression-data).
- `<output_file>` : the output file to be generated
- Use the `-l` option to log transform the data before calculating z-scores.

### Algorithm
Input [expression data file](File-Formats.md#expression-data)
```
for each gene:
  log-transform the raw data, if -l is passed
  compute mean and standard deviation for samples ( n = # of samples where expression value is not Zero or non-numeric)
  for each sample:
    compute Zscore when standard deviation != 0
    output NA for genes with standard deviation = 0
```
### Log-transforming the data
Using the `-l` option above calculates log base 2 of the expression values.

Here's how we handle the Negative values when log transforming:
```
Replace the negative values to 0 and add a constant value(+1) to data pior to applying log transform.
example, if raw value is -1, the log transform would be log(0+1)
         if the value is 0, the log transform would be log(0+1)
         if the value is 1, the log transform would be log(1+1)
```

### Example Calculation:

Log transform and calculate the z-scores:
<table>
<tr><td>Hugo_Symbol</td><td>Entrez_Gene_Id</td><td>A1-A0SD-01</td><td>A1-A0SE-01</td><td>A1-A0SH-01</td><td>A1-A0SJ-01</td><td>A1-A0SK-01</td><td>A1-A0SM-01</td><td>A1-A0SO-01</td><td>A1-A0SP-01</td><td>A2-A04N-01</td><td>A2-A04P-01</td><td>A2-A04Q-01</td><td>A2-A04R-01</td><td>A2-A04T-01</td><td>A2-A04U-01</td><td>A2-A04V-01</td><td>A2-A04W-01</td><td>A2-A04X-01</td><td>A2-A04Y-01</td></tr>
<tr><td>RPS11 Expr</td><td>6205</td><td>0.765</td><td>0.716</td><td>0.417125</td><td>0.115</td><td>0.492875</td><td>-0.525</td><td>-0.169</td><td>0.396</td><td>0.50475</td><td>0.400875</td><td>0.393125</td><td>0.9165</td><td>0.627125</td><td>0.337125</td><td>0.705</td><td>0.16425</td><td>0.325</td><td>0.11175</td></tr>
<tr><td>Output</td><td>6205</td><td>1.2803</td><td>1.1007</td><td>-0.1195</td><td>-1.6485</td><td>0.2125</td><td>-2.3426</td><td>-2.3426</td><td>-0.2153</td><td>0.263</td><td>-0.1931</td><td>-0.2285</td><td>1.8054</td><td>0.7616</td><td>-0.4901</td><td>1.0597</td><td>-1.3729</td><td>-0.5482</td><td>-1.6671</td></tr>
  </table>
  
### Running the script
To run the script clone the datahub-study-curation-tools from [here](https://github.com/cBioPortal/datahub-study-curation-tools) and type the commands when in the folder `generate_Zscores`:

```
python NormalizeExpressionLevels_allsampleref.py -i <expression_file> -o <output_file> [-l]
```

#### Example:

```
python NormalizeExpressionLevels_allsampleref.py -i path/to/your/study/data_expression_median.txt -o path/to/your/study/data_mRNA_median_Zscores.txt -l
```
