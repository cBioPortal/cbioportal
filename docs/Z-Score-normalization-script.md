## Introduction
For some data types, when uploading to cBioPortal, it is currently necessary to also provide a z-score transformed version of your input file. The z-score data is essential for the oncoprint functionality. The oncoprint shows over- and underexpression of the data, based on the threshold the user sets when selecting the genomic profile.

:warning:
Please keep in mind that the z-scores are calculated using only patient data. Hence, overexpressed in this case implies higher expression than the average patient. Also, the source data on which the z-score data is based does not necessarily follow the normal distribution. If your data does not follow the normal distribution, the z-score threshold is less reliable and will result in more false positives or false negatives. You can consider log transforming your value before calculating z-scores to improve this. However, even the logged values may not follow the normal distribution, especially if the data is bimodal.

Currently, the cBioPortal team is woking on a solution that will allow the user to decide whether they want to use zscores, or log values or zscore of log values in the frontend. Furthermore, the team is also working on functionality to be able to use healthy samples in the calculations.

### How to proceed
cBioPortal expects z-score normalization to take place per gene. You can produce this extra z-score file yourself, or let cBioPortal do it for your input files by using [NormalizeExpressionLevels.py](https://github.com/cBioPortal/datahub-study-curation-tools/blob/master/generate_Zscores/NormalizeExpressionLevels.py). Most information that now follows was taken from the comments in `NormalizeExpressionLevels.py`. We've also added an example of the calculation and example of running the program below.

## The cBioPortal NormalizeExpressionLevels method
Given expression data for a set of samples (patients), generate normalized expression values.

Each **gene** is normalized separately. First, the expression distribution of the gene is estimated by calculating the mean and variance across the reference population of all samples. If the gene has samples with unknown expression values, then the normalized expression of the sample is reported as NA. Otherwise, for every sample, the gene's normalized expression is reported as

```
(r - mu)/sigma
```

where `r` is the raw expression value, and `mu` and `sigma` are the mean and standard deviation of the reference population, respectively.

## Parameters

`<expression_file> <output_file>`

- `<expression_file>` : the [expression (exp) data file](File-Formats.md#expression-data).
- `<output_file>` : the output file to be generated

## NormalizeExpressionLevels Algorithm 
Input: [expression (exp)](File-Formats.md#expression-data) file

```
    for each gene{
       obtain mean and standard deviation across all cases
       for each case{
          zScore <- (value - mean)/sd
       }
    }
```

## Example Calculation
<table>
<tr><td>Hugo_Symbol</td><td>Entrez_Gene_Id</td><td>A1-A0SD-01</td><td>A1-A0SE-01</td><td>A1-A0SH-01</td><td>A1-A0SJ-01</td><td>A1-A0SK-01</td><td>A1-A0SM-01</td><td>A1-A0SO-01</td><td>A1-A0SP-01</td><td>A2-A04N-01</td><td>A2-A04P-01</td><td>A2-A04Q-01</td><td>A2-A04R-01</td><td>A2-A04T-01</td><td>A2-A04U-01</td><td>A2-A04V-01</td><td>A2-A04W-01</td><td>A2-A04X-01</td><td>A2-A04Y-01</td></tr>
<tr><td>RPS11 Expr</td><td>6205</td><td>0.765</td><td>0.716</td><td>0.417125</td><td>0.115</td><td>0.492875</td><td>-0.525</td><td>-0.169</td><td>0.396</td><td>0.50475</td><td>0.400875</td><td>0.393125</td><td>0.9165</td><td>0.627125</td><td>0.337125</td><td>0.705</td><td>0.16425</td><td>0.325</td><td>0.11175</td></tr>
</table>

Calculate mean and stdev:

<table>
<tr><td>avg</td><td>std</td></tr>
<tr><td>---</td><td>---</td></tr>
<tr><td>0.3718611111111112</td><td>0.3382161278127557</td></tr>
</table>

Calculate the z-scores:
<table>
<tr><td>Hugo_Symbol</td><td>Entrez_Gene_Id</td><td>A1-A0SD-01</td><td>A1-A0SE-01</td><td>A1-A0SH-01</td><td>A1-A0SJ-01</td><td>A1-A0SK-01</td><td>A1-A0SM-01</td><td>A1-A0SO-01</td><td>A1-A0SP-01</td><td>A2-A04N-01</td><td>A2-A04P-01</td><td>A2-A04Q-01</td><td>A2-A04R-01</td><td>A2-A04T-01</td><td>A2-A04U-01</td><td>A2-A04V-01</td><td>A2-A04W-01</td><td>A2-A04X-01</td><td>A2-A04Y-01</td></tr>
<tr><td>RPS11 Expr Output</td><td>6205</td><td>1.1624</td><td>1.0175</td><td>0.1338</td><td>-0.7595</td><td>0.3578</td><td>-2.6517</td><td>-1.5992</td><td>0.0714</td><td>0.3929</td><td>0.0858</td><td>0.0629</td><td>1.6103</td><td>0.7547</td><td>-0.1027</td><td>0.985</td><td>-0.6138</td><td>-0.1386</td><td>-0.7691</td></tr>
</table>

Note: this implies that your full dataset does not have average=0, std=1

## Running the script


To run the script clone the datahub-study-curation-tools from [here](https://github.com/cBioPortal/datahub-study-curation-tools) and type the commands when in the folder `generate_Zscores`:

```
python NormalizeExpressionLevels.py -i <expression_file> -o <output_file>
```

## Example

```
python NormalizeExpressionLevels.py -i path/to/your/study/data_expression_median.txt -o path/to/your/study/data_mRNA_median_Zscores.txt
```
